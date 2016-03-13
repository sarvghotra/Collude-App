// Copyright MyScript. All right reserved.

package com.myscript.atk.gew.sample.activity;

import java.io.File;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.RectF;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.View.MeasureSpec;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.myscript.atk.gew.sample.annotation.AnnotationPopupWindow;
import com.myscript.atk.gew.sample.annotation.AnnotationView;
import com.myscript.atk.gew.sample.annotation.AnnotationPopupWindow.AnnotationPopupWindowListener;
import com.myscript.atk.gew.sample.export.BitmapMakerTask;
import com.myscript.atk.gew.sample.export.ShareUtils;
import com.myscript.atk.gew.sample.export.BitmapMakerTask.BitmapMakerCallback;
import com.myscript.atk.gew.sample.preferences.PreferencesActivity;
import com.myscript.atk.gew.sample.resources.SimpleResourceHelper;
import com.myscript.atk.gew.GeometryWidgetApi;
import com.myscript.atk.gew.GeometryWidgetApi.PrimitiveType;
import com.myscript.atk.gew.GeometryWidgetApiDefines;
import com.myscript.atk.gew.sample.BuildConfig;
import com.myscript.atk.gew.sample.R;
import com.myscript.certificate.MyCertificate;

/** This class demonstrates how to basically integrate the styleable GeometryWidget library in a single application. */
public class SampleActivity extends Activity
    implements
      GeometryWidgetApi.OnConfigureListener,
      GeometryWidgetApi.OnRecognitionListener,
      GeometryWidgetApi.OnGestureListener,
      GeometryWidgetApi.OnSelectionListener,
      GeometryWidgetApi.OnLabelViewCreatedListener,
      GeometryWidgetApi.OnWritingListener,
      GeometryWidgetApi.OnUndoRedoListener,
      OnSharedPreferenceChangeListener,
      AnnotationPopupWindowListener
{
  private static final boolean DBG =  true && BuildConfig.DEBUG;
  private static final String TAG = "SampleActivity";
  private static final String SERIALIZE_KEY = "INSTANCE_STATE";
  private Bundle mRestoreBundleCache = null;
  
  /** Shared preferences. */
  private SharedPreferences mSharedPreferences;
  
  /** Geometry Widget */
  private GeometryWidgetApi mGeometryWidget;
    
  /** Task for bitmap generation. */
  private BitmapMakerTask mBitmapMakerTask;
  
  /**
   * List of error dialog types.
   */
  /** Notify the user that a MSB resource is not found or invalid. */
  public static final int DIALOG_ERROR_RESSOURCE = 0;
  /** Notify the user that a MSB certificate is missing or invalid. */
  public static final int DIALOG_ERROR_CERTIFICATE = 1;
  /** One error dialog at a time. */
  private boolean mErrorDlgDisplayed = false;

  @Override
  protected void onCreate(final Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);

    // Set application title
    setTitle(getResources().getString(R.string.activity_name));

    // Register SharedPreference listener
    mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    mSharedPreferences.registerOnSharedPreferenceChangeListener(this);
    
    // Configure recognition listeners
    mGeometryWidget = (GeometryWidgetApi) findViewById(R.id.vo_geometry_widget);
    mGeometryWidget.setOnConfigureListener(this);
    mGeometryWidget.setOnRecognitionListener(this);
    mGeometryWidget.setOnGestureListener(this);
    mGeometryWidget.setOnWritingListener(this);
    mGeometryWidget.setOnSelectionListener(this);
    mGeometryWidget.setOnUndoRedoListener(this);
    mGeometryWidget.setOnLabelViewCreatedListener(this);
    
    // Configure geometry recognition engine
    configure();
  }

  @Override
  protected void onDestroy()
  {
    mGeometryWidget.release(this);
    super.onDestroy();
  }

  @Override
  protected void onSaveInstanceState(final Bundle outState)
  {
    super.onSaveInstanceState(outState);
    
    if ((mRestoreBundleCache != null) && mRestoreBundleCache.containsKey(SERIALIZE_KEY))
    {
      // Defensive: don't want to serialize empty widget and raise crash (fast rotation).
      if (DBG)
        Log.d(TAG, "onSaveInstanceState: restore bundle cache detected, don't serialize empty widget");

      byte[] data = mRestoreBundleCache.getByteArray(SERIALIZE_KEY);
      if (data != null)
      {
        outState.putByteArray(SERIALIZE_KEY, data);
      }      
      mRestoreBundleCache = null;
    }
    else
    {
      // Serialize geometry component state
      byte[] data = mGeometryWidget.serialize();
      outState.putByteArray(SERIALIZE_KEY, data);
    }
  }

  @Override
  protected void onRestoreInstanceState(final Bundle outState)
  {
    if ((outState != null) && outState.containsKey(SERIALIZE_KEY))
      mRestoreBundleCache = outState;
  }
  
  @Override
  public void onBackPressed()
  {
    moveTaskToBack(true);
  }

  // ----------------------------------------------------------------------
  // Geometry Widget Sample - Application settings

  @Override
  public boolean onCreateOptionsMenu(final Menu menu)
  {
    final MenuInflater menuInflater = getMenuInflater();
    menuInflater.inflate(R.menu.main_actions, menu);

    // Calling super after populating the menu is necessary here to ensure that the
    // action bar helpers have a chance to handle this event.
    return super.onCreateOptionsMenu(menu);
  }
  
  @Override
  public boolean onPrepareOptionsMenu(Menu menu)
  {
    MenuItem undo = menu.findItem(R.id.action_undo);
    undo.setEnabled(mGeometryWidget.canUndo());

    MenuItem redo = menu.findItem(R.id.action_redo);
    redo.setEnabled(mGeometryWidget.canRedo());
    
    // isEmpty does not work MenuItem clear = menu.findItem(R.id.action_clear);
    // isEmpty does not work undo.setEnabled(!mGeometryWidget.isEmpty());
    
    return super.onPrepareOptionsMenu(menu);
  }
  
  @Override
  public boolean onOptionsItemSelected(final MenuItem item)
  {
    switch (item.getItemId())
    {
    case R.id.action_undo :
        mGeometryWidget.undo();
        break;
    case R.id.action_redo :
        mGeometryWidget.redo();
        break;
    case R.id.action_clear :
    	mGeometryWidget.clear(true /* allow undo */);
        break;
      case R.id.menu_share :
        shareContent();
        break;
      case R.id.menu_settings :
        startActivityForResult(new Intent(getApplicationContext(), PreferencesActivity.class), 0);
        break;
      default :
        return super.onOptionsItemSelected(item);
    }

    return true;
  }
   
  // ----------------------------------------------------------------------
  // Geometry Widget Sample - Shared preferences

  private void loadPreferences()
  {
    PreferencesActivity.loadPreferences(this, mGeometryWidget, mSharedPreferences);
  }
  
  @Override
  public void onSharedPreferenceChanged(final SharedPreferences sharedPreferences, final String key)
  {
    // Update component
	PreferencesActivity.onSharedPreferenceChanged(this, mGeometryWidget, sharedPreferences, key);
  }
  
  private void showExternalStorageAlert() {    
    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setMessage(R.string.share_no_external_storage_mounted).setTitle("Alert");
    
    builder.setPositiveButton("Ok",new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog,int id) 
        {
        }
      });
    
    AlertDialog dialog = builder.create();
    dialog.show();
 }

  // ----------------------------------------------------------------------
  // Geometry Widget Sample - Export

  private void shareContent()
  {
    if(!ShareUtils.isExternalStorageMounted())
    {
      showExternalStorageAlert();
      return;
    }

    if (mBitmapMakerTask == null || !mBitmapMakerTask.getStatus().equals(AsyncTask.Status.RUNNING))
    {
      // Get preview 
      final String shareText = getString(R.string.share_text, getString(R.string.app_name), getString(R.string.share_platform));
      final Bitmap bitmap = mGeometryWidget.getResultAsImage(SampleActivity.this, null);
      loadPreferences();
      mBitmapMakerTask = (BitmapMakerTask) new BitmapMakerTask(SampleActivity.this, bitmap,
          new BitmapMakerCallback()
          {
            @Override
            public void onResult(final File result)
            {
              if (result != null)
              {
                if (!ShareUtils.shareBitmap(SampleActivity.this, shareText, result))
                  Toast.makeText(SampleActivity.this, getString(R.string.share_error), Toast.LENGTH_SHORT).show();
              }
            }
          }).execute();
    }
  }
  
  // ----------------------------------------------------------------------
  // Geometry Widget Sample - geometry recognition engine configuration

  private void configure()
  {
    // Geometry resource    
    final String[] resources = new String[]{"shk-standard.res"};

    // Prepare resources
    final String subfolder = "sketch";
    final String resourcePath = new String(getFilesDir().getPath() + java.io.File.separator + subfolder);
    SimpleResourceHelper
        .copyResourcesFromAssets(getAssets(), subfolder /* from */, resourcePath /* to */, resources /* resource names */);

    // Configure geometry widget
    mGeometryWidget.setResourcesPath(resourcePath);
    mGeometryWidget.configure(this, resources, MyCertificate.getBytes());
    
    // Load settings
    loadPreferences();
    
    // Configure geometry widget for this screen resolution
    {
      // Compute dots per millimeter for this screen
      float dotsPerMillimeterForThisDevice = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_MM, 1, getResources().getDisplayMetrics());
       
      // Apply screen resolution settings (help solver with screen resolution)
      WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
      Display display = wm.getDefaultDisplay();
      
      Point size = new Point();
      display.getSize(size);
      float width = size.x;
      int ordinal = GeometryWidgetApiDefines.GWFloatParameter.GWFloatParameterTouchScreenPixelsWidth.ordinal();
      mGeometryWidget.setFloatValueForParameter(GeometryWidgetApiDefines.GWFloatParameter.values()[ordinal], width);

      float height = size.y;
      ordinal = GeometryWidgetApiDefines.GWFloatParameter.GWFloatParameterTouchScreenPixelsHeight.ordinal();
      mGeometryWidget.setFloatValueForParameter(GeometryWidgetApiDefines.GWFloatParameter.values()[ordinal], height);

      // Compute geometry widget's reference size for this screen resolution
      for (int i = GeometryWidgetApiDefines.GWFloatParameter.GWFloatParameterTapDetectionDelay.ordinal(); 
           i <= GeometryWidgetApiDefines.GWFloatParameter.GWFloatParameterEllipseRadiusRatio.ordinal(); 
           i++)
      {
        if (i != GeometryWidgetApiDefines.GWFloatParameter.GWFloatParameterTapDetectionDelay.ordinal() && 
            i != GeometryWidgetApiDefines.GWFloatParameter.GWFloatParameterEllipseRadiusRatio.ordinal() &&
            i != GeometryWidgetApiDefines.GWFloatParameter.GWFloatParameterSameLengthMarkerAngle.ordinal())
        {          
          // load default value for parameter
          float defaultDotsForiPad1 = mGeometryWidget.getFloatValueForParameter(GeometryWidgetApiDefines.GWFloatParameter.values()[i]);
    
          // default sizes are stored in dots/units optimized for iPad1 132 dpi.
          float millimeters = 25.4f * defaultDotsForiPad1 / 132.f; // convert default value in millimeters.
    
          float dotsForThisDevice = millimeters * dotsPerMillimeterForThisDevice;
          
          // set value stored in preference back in widget
          mGeometryWidget.setFloatValueForParameter(GeometryWidgetApiDefines.GWFloatParameter.values()[i], dotsForThisDevice);
        }
      } 
    }
  }

  // ----------------------------------------------------------------------
  // Geometry Widget Sample - geometry recognition engine configuration

  @Override
  public void onConfigurationBegin()
  {
    if (DBG)
      Log.d(TAG, "Geometry configuration begins");
  }

  @Override
  public void onConfigurationEnd(final boolean success)
  {
    if (DBG)
    {
      if (success)
        Log.d(TAG, "Geometry configuration succeeded");
      else
        Log.d(TAG, "Geometry configuration failed (" + mGeometryWidget.getErrorString() + ")");
    }

    if (DBG)
    {
      if (success)
        Log.d(TAG, "Geometry configuration loaded successfully");
      else
        Log.d(
            TAG,
            "Geometry configuration error - did you copy the geometry resources to your SD card? ("
                + mGeometryWidget.getErrorString() + ")");
    }
    
    if (success)
    {
        if ((mRestoreBundleCache != null) && mRestoreBundleCache.containsKey(SERIALIZE_KEY))
        {
          byte[] data = mRestoreBundleCache.getByteArray(SERIALIZE_KEY);
          if (data != null)
          {
            mGeometryWidget.unserialize(data);
            
            // Update action bar state
            invalidateOptionsMenu();
          }
          mRestoreBundleCache = null;
        }
    }

    // Notify user using dialog box
    if (!success)
      showErrorDlg(DIALOG_ERROR_RESSOURCE);
  }

  // ----------------------------------------------------------------------
  // Geometry Widget Sample - geometry recognition process

  @Override
  public void onRecognitionBegin()
  {
    if (DBG)
      Log.d(TAG, "Geometry recognition begins");
  }

  @Override
  public void onRecognitionEnd()
  {
    if (DBG)
      Log.d(TAG, "Geometry recognition end");
    
    // Update clear button state
    // isEmpty does not work setClearEnabled(!mGeometryWidget.isEmpty());
  }

  @Override
  public void onRecognitionItemsUpdated()
  {
    if (DBG)
      Log.d(TAG, "Geometry recognition items updated");
    
  }
  
  // ----------------------------------------------------------------------
  // Geometry Widget Sample - geometry recognition gestures

  @Override
  public void onEraseGesture(final boolean partial)
  {
    if (DBG)
      Log.d(TAG, "Erase gesture handled by current geometry and is partial " + partial);
  }

  // ----------------------------------------------------------------------
  // Geometry Widget Sample - selection

  @Override
  public void onItemSelected(long itemID, PointF point, PrimitiveType type)
  {
      LayoutInflater layoutInflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      
      // inflate the custom popup layout
      final View inflatedView = layoutInflater.inflate(R.layout.popup_layout, new LinearLayout(this), false);

      // set height depends on the device size
      FrameLayout parentLayout = (FrameLayout) findViewById(R.id.vo_geometry_layout);
      AnnotationPopupWindow popupWindow = new AnnotationPopupWindow(inflatedView, parentLayout);
      popupWindow.setListener(this);
      popupWindow.setGeometryWidget(mGeometryWidget);

      // configure Input Box
      popupWindow.setItemType(type);
      popupWindow.setItemID(itemID);
      
      // basic popover positioning 
      RectF itemBoundingBox = mGeometryWidget.boundingBoxForItem(itemID);
      View view = this.findViewById(R.id.vo_geometry_widget);
        
      // Point absolute coordinate
      int[] xyRoot = new int[2];
      view.getLocationOnScreen(xyRoot);
      PointF absolute = new PointF(point.x + xyRoot[0], point.y + xyRoot[1]);
      
      // Present Input Box
	  popupWindow.showPopup(view, itemBoundingBox, absolute);
  }
  
  @Override
  public void onItemDeselected(long itemID, PrimitiveType type)
  {
  	
  }
  
  // ----------------------------------------------------------------------
  // Geometry Widget Sample - ink edition

  @Override
  public void onWritingBegin()
  {
    if (DBG)
      Log.d(TAG, "Start writing");
  }

  @Override
  public void onWritingEnd()
  {
    if (DBG)
      Log.d(TAG, "End writing");
  }

  // ----------------------------------------------------------------------
  // Geometry Widget Sample - Undo / Redo

  @Override
  public void onUndoRedoStateChanged()
  {
    if (DBG)
      Log.d(TAG, "Undo redo state has changed");

    // Update action bar state
    invalidateOptionsMenu();
  }

  // ----------------------------------------------------------------------
  // Geometry Widget Sample - Errors

  // showDialog is deprecated but still used to simplify the example. 
  @SuppressWarnings("deprecation")
  private void showErrorDlg(final int id)
  {
    if (DBG)
      Log.i(TAG, "Show error dialog");
    if (!mErrorDlgDisplayed)
    {
      mErrorDlgDisplayed = true;
      showDialog(id);
    }
  }

  @Override
  public Dialog onCreateDialog(final int id)
  {
    final AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
    alertBuilder.setCancelable(false);
    switch (id)
    {
    // Language pack update missing resource
      case DIALOG_ERROR_RESSOURCE :
        alertBuilder.setTitle(R.string.langpack_parsing_error_title);
        alertBuilder.setMessage(R.string.langpack_parsing_error_msg);
        alertBuilder.setPositiveButton(android.R.string.ok, abortListener);
        break;
      // Certificate error
      case DIALOG_ERROR_CERTIFICATE :
        alertBuilder.setTitle(R.string.certificate_error_title);
        alertBuilder.setMessage(R.string.certificate_error_msg);
        alertBuilder.setPositiveButton(android.R.string.ok, abortListener);
        break;
    }
    final AlertDialog alert = alertBuilder.create();
    return alert;
  }

  private final DialogInterface.OnClickListener abortListener = new DialogInterface.OnClickListener()
  {
    @Override
    public void onClick(final DialogInterface di, final int position)
    {
      mErrorDlgDisplayed = false;
      finish();
    }
  };

  // ----------------------------------------------------------------------
  // Geometry Widget Sample - Annotations
  
  @Override
  public void popupWindowClosed(final long itemID, final PrimitiveType type, final String mathViewText)
  {
    boolean success = false;

    if (mathViewText.equals("?"))
    {
      success = false;
    }
    else if (type == PrimitiveType.Line || type == PrimitiveType.Arc)
    {
      String floatString = mathViewText.replace(",", ".");
      float centimeterLength = (floatString.length() > 0) ? Float.parseFloat(floatString) : 0f;
        
      // Rounding centimeterLength first
      //DecimalFormatSymbols symbols = new DecimalFormatSymbols();
      //symbols.setDecimalSeparator('.');
      //DecimalFormat format = new DecimalFormat("#.##");
      //format.setDecimalFormatSymbols(symbols);
      //String roundedCentimeterLengthString = format.format(centimeterLength);
      //float roundedCentimeterLength = Float.parseFloat(roundedCentimeterLengthString);
      float roundedCentimeterLength = centimeterLength;

      // Convert back to points
  	  float pixelsPerCentimeter = 10.f * TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_MM, 1, getResources().getDisplayMetrics());
      float pointLength = roundedCentimeterLength * pixelsPerCentimeter;
          
      // Apply new length value to item
      success = mGeometryWidget.setValue(pointLength, itemID);      
    }
    else if (type == PrimitiveType.Constraint)
    {
      // Apply new angle value
      String floatString = mathViewText.replace(",", ".");
      float angleValue = (floatString.length() > 0) ? Float.parseFloat(floatString) : 0f;
      success = mGeometryWidget.setValue(angleValue, itemID);
    }
    else if (type == PrimitiveType.Point)
    {
      // Save the letter into the `title` property of the item
      success = mGeometryWidget.setTitleForItem(mathViewText, itemID);
    }
        
    // Update annotation on the canvas
    if (success)
    {
      if (type == PrimitiveType.Line || type == PrimitiveType.Arc || type == PrimitiveType.Point)
        setAnnotationString(mathViewText, itemID);
    }
    else
    	setAnnotationString(null, itemID);    
  }

  @Override
  public View onLabelViewCreated(long itemID) {
    String label = mGeometryWidget.titleForItem(itemID);
    boolean displayValue = mGeometryWidget.displayValueForItem(itemID);
    float pointLength = mGeometryWidget.valueForItem(itemID);      
    if(displayValue && pointLength > 0)
    {
      label = AnnotationPopupWindow.annotationStringForLength(this, pointLength);
      return createOrRecyleAnnotationView(label, itemID);
    }
    else if(label != null && label.length() != 0)
    {   
      return createOrRecyleAnnotationView(label, itemID);        
    }
    return null;
  }

  public View createOrRecyleAnnotationView(String string, long itemID)
  {
    // For nil and empty string, delete current annotation
    if (string == null || (string.length() ==  0))
    {
      return null;
    }

    // Get the annotation currently associated to the item.
    AnnotationView view = (AnnotationView) mGeometryWidget.annotationViewForItem(itemID);

    // If the annotation already exists, update the text.
    final int fontSize = 18;
    final int specWidth = MeasureSpec.makeMeasureSpec(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
    if (view != null)
    {
      if (!view.getText().equals(string))
        view.setText(string);

      // Size to fit
      TextView temp = new TextView(this);
      temp.setTextSize(fontSize);
      temp.setGravity(Gravity.CENTER);
      temp.setText(string);
      temp.measure(specWidth, specWidth);
      view.setWidth(temp.getMeasuredWidth());
      view.setHeight(temp.getMeasuredHeight());
    }
    // If the annotation does not exists yet, create a new label.
    else
    {
      view = new AnnotationView(this, mGeometryWidget, itemID);
      view.setTextSize(fontSize);
      view.setGravity(Gravity.CENTER);
      view.setText(string);
      view.setTextColor(Color.BLACK);
      view.setBackgroundColor(Color.TRANSPARENT);

      // Size to fit
      view.measure(specWidth, specWidth);
      view.setWidth(view.getMeasuredWidth());
      view.setHeight(view.getMeasuredHeight());
    }
    return view;
  }
  
  public void setAnnotationString(String string, long itemID)
  {
    AnnotationView view = (AnnotationView) createOrRecyleAnnotationView(string, itemID);
    
    if (view == null )
    {
      // Delete annotation view
      mGeometryWidget.setAnnotationView(null, itemID);
      return;
    }

    // Get the annotation currently associated to the item.
    AnnotationView recyledView = (AnnotationView) mGeometryWidget.annotationViewForItem(itemID);

    if(view != recyledView)
    {
      // Add new created annotation view
      mGeometryWidget.setAnnotationView(view, itemID);
    }
  }
    
}
