// Copyright MyScript. All right reserved.

package com.myscript.atk.gew.sample.annotation;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;

import com.myscript.atk.gew.sample.resources.SimpleResourceHelper;
import com.myscript.atk.gew.sample.utils.Utils;
import com.myscript.atk.gew.GeometryWidgetApi;
import com.myscript.atk.gew.GeometryWidgetApi.PrimitiveType;
import com.myscript.atk.gew.sample.BuildConfig;
import com.myscript.atk.gew.sample.R;
import com.myscript.atk.maw.MathWidget;
import com.myscript.atk.maw.MathWidgetApi;
import com.myscript.atk.maw.MathWidgetApi.AdditionalGestures;
import com.myscript.atk.maw.MathWidgetApi.Symbol;
import com.myscript.certificate.MyCertificate;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.drawable.TransitionDrawable;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class AnnotationPopupWindow extends PopupWindow
  implements
    MathWidgetApi.OnConfigureListener
{
  private GeometryWidgetApi mGeometryWidget;								// The Geometry Widget holding the item being annotated
  private MathWidget mMathWidget;									    	// The Math Widget displayed in the popup
  private boolean mMathWidgetReady;											// The Math Widget status
  private long mItemID;														// The ID of the item being annotated
  private PrimitiveType mItemType;											// The type of the item being annotated
  private TransitionDrawable mTansition;									// The show and hide display animation
  private String mOriginalValue = new String();
  private String mOriginalValueDisplayed = new String();
  private AnnotationPopupWindowListener mListener;
  private final Handler mHandler = new Handler();
  private TextView mTextView;
	
  // MathWidget resources
  final String[] lengthResources = new String[]{"math-ak.res", "math-grm-length-mathwidget.res"};
  final String[] angleResources = new String[]{"math-ak.res", "math-grm-angle-mathwidget.res"};
  final String[] letterResources = new String[]{"math-ak.res", "math-grm-letter-mathwidget.res"};
  private enum MathWidgetMode { MW_MODE_UNINITIALIZED, MW_MODE_LENGTH, MW_MODE_ANGLE, MW_MODE_LETTER }
  private MathWidgetMode mMathWidgetMode = MathWidgetMode.MW_MODE_UNINITIALIZED;
  
  private static int DIM_DURATION = 100;									// ms
  private static float DEFAULT_WIDTH = 5f + 2f * BubbleDrawable.MARGINS;
  private static float DEFAULT_HEIGHT = 2.5f + 2f * BubbleDrawable.MARGINS;
  
  private static final boolean DBG =  true && BuildConfig.DEBUG;
  private static final String TAG = "MathWidget";
  
  // Listener  
  public interface AnnotationPopupWindowListener
  {
    public void popupWindowClosed(final long itemID, final PrimitiveType type, final String mathViewText);
  }
  
  // Constructors 
  public AnnotationPopupWindow(View contentView, FrameLayout parentLayout)
  {
    super(contentView, 
    		Math.round(Utils.centimetersToPoints(contentView.getContext(), DEFAULT_WIDTH)),		// Width 
    		Math.round(Utils.centimetersToPoints(contentView.getContext(), DEFAULT_HEIGHT)), 	// Height
    		true);

    // Make it focusable to show the keyboard to enter in `EditText`
    setFocusable(true);
    // Make it outside touchable to dismiss the popup window
    setOutsideTouchable(true);
    
    // Dim background window
    mTansition = (TransitionDrawable) parentLayout.getForeground();
    
    // Reverse dim of background window after closing
    setOnDismissListener(new PopupWindow.OnDismissListener() {
        @Override
        public void onDismiss() {
            // Reverse Dim
        	mTansition.reverseTransition(DIM_DURATION);
        	
        	// Notify component
        	String resultAsText = "";
        	if (mMathWidgetReady)
              resultAsText = mMathWidget.getResultAsText();
        	
        	if (mOriginalValueDisplayed.equals(resultAsText))
        		mListener.popupWindowClosed(getItemID(), mItemType, mOriginalValue);
        	else
        		mListener.popupWindowClosed(getItemID(), mItemType, resultAsText);
            
        	if (mMathWidgetReady)
        	  mMathWidget.clear(false);
        	
        	mMathWidget.release(null);
        	
        	// Removed to avoid any mutex problem
        	mGeometryWidget.deselect();
        }
    });

    // Get text view
    mTextView = (TextView) getContentView().findViewById(R.id.writeComment);
    
    // Get MathWidget
    mMathWidget = (MathWidget) getContentView().findViewById(R.id.vo_math_widget);
    mMathWidget.setPaddingRatio(0.5f, 0.5f, 0.5f, 1.0f);
    
    // Configure Length MathWidget engine
    mMathWidget.setOnConfigureListener(this);
    
    // Set layout margins for MathWidget
    RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) mMathWidget.getLayoutParams();
    int margins = Math.round(Utils.centimetersToPoints(contentView.getContext(), BubbleDrawable.MARGINS));
    layoutParams.setMargins(margins, margins, margins, margins);
    mMathWidget.setLayoutParams(layoutParams);

    // Set layout margins for TextView
    RelativeLayout.LayoutParams lpTv = (RelativeLayout.LayoutParams) mTextView.getLayoutParams();
    lpTv.setMargins(margins, 0, margins, margins);
    mTextView.setLayoutParams(lpTv);
    mTextView.setPadding(0, 0, 0, margins / 2);
    
    // Set transparent background
    mMathWidget.setBackgroundDrawable(null);
    mMathWidget.setBackgroundColor(Color.TRANSPARENT);
    int color = Color.rgb(21, 164, 255);
    mMathWidget.setInkColor(color);
    mMathWidget.setInkThickness(7);
    mMathWidget.setTextColor(color);
    mMathWidget.setBaselineColor(Color.TRANSPARENT);
    
    // Prepare resources
    final String subfolder = "math";
    final Context context = getContentView().getContext();
    final String resourcePath = new String(context.getFilesDir().getPath() + java.io.File.separator + subfolder);
    mMathWidget.setResourcesPath(resourcePath);
    
    SimpleResourceHelper
        .copyResourcesFromAssets(context.getAssets(), subfolder /* from */, resourcePath /* to */, lengthResources /* length resource names */);
    SimpleResourceHelper
        .copyResourcesFromAssets(context.getAssets(), subfolder /* from */, resourcePath /* to */, angleResources /* angle resource names */);
    SimpleResourceHelper
        .copyResourcesFromAssets(context.getAssets(), subfolder /* from */, resourcePath /* to */, letterResources /* letter resource names */);
  }
  
  public AnnotationPopupWindow(Context context, AttributeSet attrs)
  {	  
    super(context, attrs);
  }

  public void showPopup(final View contentView, RectF itemBoundingBox, PointF point)
  {   
    // Basic positioning
    float offsetValue = 3f;
    PointF regionOffset = new PointF(0f, 0f);
    PointF viewOffset = new PointF(0f, 0f);

    // Ensure the popover is displayed inside the view
    PointF contentViewCenter = new PointF(contentView.getWidth() / 2, contentView.getHeight() / 2);
    if (itemBoundingBox.height() > itemBoundingBox.width())
    {
      float ox = (point.x < contentViewCenter.x) ? offsetValue : -offsetValue;
      float oy = 0f;
      float dx = (point.x < contentViewCenter.x) ? getWidth() / 2f : -(getWidth() / 2f);
      float dy = 0;
      if ((point.y < getHeight()) || (Math.abs(contentView.getHeight() - point.y) < getHeight()))
      {
        dy = (point.y < contentViewCenter.y) ? getHeight() / 2f : -(getHeight() / 2f);
        oy = (point.y < contentViewCenter.y) ? offsetValue : -offsetValue;
      }
      viewOffset   	= new PointF(dx, dy);
      regionOffset  = new PointF(ox, oy);
	}
	else
	{
      float ox = 0f;
      float oy = (point.y < contentViewCenter.y) ? offsetValue : -offsetValue;
      float dy = (point.y < contentViewCenter.y) ? getHeight() / 2f : -(getHeight() / 2f);
      float dx = 0;
      if ((point.x < getWidth()) || (Math.abs(contentView.getWidth() - point.x) < getWidth()))
      {
        dx = (point.x < contentViewCenter.x) ? getWidth() / 2f : -(getWidth() / 2f);
        ox = (point.x < contentViewCenter.x) ? offsetValue : -offsetValue;
      }
      viewOffset   	= new PointF(dx, dy);
      regionOffset  = new PointF(ox, oy);
	}
    float offsetX = regionOffset.x + viewOffset.x;
    float offsetY = regionOffset.y + viewOffset.y;
    float presentationX = Math.min(Math.max(point.x + offsetX, 0), contentView.getWidth());
    float presentationY = Math.min(Math.max(point.y + offsetY, 0), contentView.getHeight());

    // Set a background drawable with rounders corners
    float anchorOffsetX = (getWidth() / 2f) - viewOffset.x;
    float anchorOffsetY = (getHeight() / 2f) - viewOffset.y;
    PointF anchor = new PointF(anchorOffsetX, anchorOffsetY);
    BubbleDrawable bubble = new BubbleDrawable(getContentView().getContext(), anchor);
    setBackgroundDrawable(bubble);
	  
    // Dim background window
    mTansition.startTransition(DIM_DURATION);
        
    // Animate
    this.setAnimationStyle(R.style.PopupAnimation);
    
    // Show popup
    showAtLocation(contentView, Gravity.NO_GRAVITY, (int) (presentationX - (getWidth() / 2f)), (int) (presentationY - (getHeight() / 2f)));
  }
    
  // ----------------------------------------------------------------------
  // AnnotationPopupWindow - Annotations
    
  public long getItemID()
  {
    return mItemID;
  }

  public void setItemID(long itemID)
  {
    this.mItemID = itemID;
    
    String stringToDisplay = new String();
    if ((mItemType == PrimitiveType.Line) || (mItemType == PrimitiveType.Arc))
    {
      mTextView.setText("Write a new value");
      setModeLength();
            
      // Get the length of the item (in points)
      float pointLength = mGeometryWidget.valueForItem(itemID);
      stringToDisplay = annotationStringForLength(this.getContentView().getContext(), pointLength);
      mOriginalValueDisplayed = stringToDisplay;
      mOriginalValue = FullStringForLength(this.getContentView().getContext(), pointLength);
    }
    else if (mItemType == PrimitiveType.Constraint)
    {
      mTextView.setText("Write a new value");
      setModeAngle();
            
      // Get the angle value (in degrees)
      float angle = mGeometryWidget.valueForItem(itemID);
      stringToDisplay = annotationStringForAngle(this.getContentView().getContext(), angle);
    }
    else if (mItemType == PrimitiveType.Point)
    {
      setModeLetter();
    	
      // Get the title, previously associated with the item
      stringToDisplay = mGeometryWidget.titleForItem(itemID);
            
      boolean emptyString = (stringToDisplay == null) || (stringToDisplay.length() == 0);
      mTextView.setText(emptyString ? "Write a letter" : "Write a new letter");
    }
    
    final String finalString = stringToDisplay;
    mHandler.post(new Runnable()
    {
      @Override
      public void run()
      {
        // Load the string into the Math View
        displayString(finalString);
      }
    });
  }

  private void displayString(final String stringToDisplay)
  {
    if (stringToDisplay.length() > 0)
    {
      List<Symbol> symbols = new ArrayList<Symbol>();
    
      float symbolWidth = getWidth()  / stringToDisplay.length();
      float symbolHeight = getHeight();
    
      for (int i = 0; i < stringToDisplay.length(); i++)
      {
        String character = stringToDisplay.substring(i, i + 1);
        Symbol symbol = new Symbol(character, new RectF(i * symbolWidth, 0,  (i + 1) * symbolWidth, symbolHeight), false);
        symbols.add(symbol);
      }
      
      mMathWidget.addSymbols(symbols, false);
    }
  }
    
  public static String annotationStringForAngle(Context context, float angle)
  {
    // Truncate to 2 decimals
    DecimalFormatSymbols symbols = new DecimalFormatSymbols();
    symbols.setDecimalSeparator('.');
    DecimalFormat format = new DecimalFormat("#.##");
    format.setDecimalFormatSymbols(symbols);
    return format.format(angle);
  }
  
  public static String annotationStringForLength(Context context, float pointLength)
  {
    // Screen resolution	  
	float pixelsPerCentimeter = 10.f * TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_MM, 1, context.getResources().getDisplayMetrics());
    
    // Convert to centimeters
    float centimeterLength = pointLength / pixelsPerCentimeter;
      
    // Truncate to 2 decimals
    DecimalFormatSymbols symbols = new DecimalFormatSymbols();
    symbols.setDecimalSeparator('.');
    DecimalFormat format = new DecimalFormat("#.##");
    format.setDecimalFormatSymbols(symbols);
    return format.format(centimeterLength);
  }
  
  public static String FullStringForLength(Context context, float pointLength)
  {
    // Screen resolution	  
	float pixelsPerCentimeter = 10.f * TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_MM, 1, context.getResources().getDisplayMetrics());
    
    // Convert to centimeters
    float centimeterLength = pointLength / pixelsPerCentimeter;
      
    return Float.toString(centimeterLength);
  }
  
  // ----------------------------------------------------------------------
  // MathWidget - Configuration and listeners
  
  private void setModeLength()
  {
    final Context context = getContentView().getContext();
    if (mMathWidgetMode != MathWidgetMode.MW_MODE_UNINITIALIZED)
      mMathWidget.release(context);
   
    // Load resources
    mMathWidget.configure(context, lengthResources, MyCertificate.getBytes(), AdditionalGestures.DefaultGestures);
    mMathWidgetMode = MathWidgetMode.MW_MODE_LENGTH;
  }

  private void setModeAngle()
  {
    final Context context = getContentView().getContext();
    if (mMathWidgetMode != MathWidgetMode.MW_MODE_UNINITIALIZED)
      mMathWidget.release(context);
   
    // Load resources
    mMathWidget.configure(context, angleResources, MyCertificate.getBytes(), AdditionalGestures.DefaultGestures);
    mMathWidgetMode = MathWidgetMode.MW_MODE_ANGLE;
  }
  
  private void setModeLetter()
  {
    final Context context = getContentView().getContext();
    if (mMathWidgetMode != MathWidgetMode.MW_MODE_UNINITIALIZED)
      mMathWidget.release(context);
   
    // Load resources
    mMathWidget.configure(context, letterResources, MyCertificate.getBytes(), AdditionalGestures.DefaultGestures);
    mMathWidgetMode = MathWidgetMode.MW_MODE_LETTER;
  }
  
  @Override
  public void onConfigurationBegin() 
  {
    if (DBG)
      Log.d(TAG, "Math configuration begins");
    
    mMathWidgetReady = false;
  }

  @Override
  public void onConfigurationEnd(boolean success)
  {
	if (DBG)
      Log.d(TAG, "onConfigurationEnd");
		
	if (success)
      mMathWidgetReady = true;
	else
      mTextView.setText("Configuration error");
  }

  public void setListener(AnnotationPopupWindowListener listener)
  {
    this.mListener = listener;
  }
   
  // ----------------------------------------------------------------------
  // AnnotationPopupWindow - Getters and setters
  
  public GeometryWidgetApi getGeometryWidget()
  {
    return mGeometryWidget;
  }

  public void setGeometryWidget(GeometryWidgetApi geometryWidget)
  {
    this.mGeometryWidget = geometryWidget;
  }
  
  public PrimitiveType getItemType()
  {
    return mItemType;
  }

  public void setItemType(PrimitiveType itemType)
  {
    if (this.mItemType != itemType)
    {
      this.mItemType = itemType;
    }
  }  
}
