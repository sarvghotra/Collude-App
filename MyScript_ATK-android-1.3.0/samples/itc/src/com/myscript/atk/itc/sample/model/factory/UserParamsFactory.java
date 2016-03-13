package com.myscript.atk.itc.sample.model.factory;

import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;

import com.myscript.atk.itc.CharBoxFactory;
import com.myscript.atk.itc.ICharBoxFactory;
import com.myscript.atk.itc.IStrokeUserParamsFactory;
import com.myscript.atk.itc.IUserParams;
import com.myscript.atk.itc.IWordUserParamsFactory;
import com.myscript.atk.itc.Rect;
import com.myscript.atk.itc.SmartStroke;
import com.myscript.atk.itc.SmartWord;
import com.myscript.atk.itc.Transform;
import com.myscript.atk.itc.WordRange;
import com.myscript.atk.itc.sample.R;
import com.myscript.atk.itc.sample.inkcapture.InkCaptureView;
import com.myscript.atk.itc.sample.model.SmartStrokeUserParams;
import com.myscript.atk.itc.sample.model.SmartWordUserParams;
import com.myscript.atk.itc.sample.stroker.Stroker;
import com.myscript.atk.itc.sample.util.FormattedDataManager;

public class UserParamsFactory implements IStrokeUserParamsFactory, IWordUserParamsFactory
{
  public enum PenMode
  {
    RECOGNITION_MODE, DRAWING_MODE
  }
  
  // SmartWord user parameters properties
  public enum WordProperty
  {
    UNDERLINED, SELECTED, NOT_SELECTED, FORMATTED
  }

  private final Stroker mStroker;
  private PenMode mPenMode;
  private CharBoxFactory mCharBoxFactory;
  private Context mContext;

  public UserParamsFactory(final Stroker stroker, Context context)
  {
    mStroker = stroker;
    mPenMode = PenMode.RECOGNITION_MODE;
    mContext = context;
  }

  // --------------------------------------------------------------------------------
  // Configuration methods

  public void setInkColor(int color)
  {
    mStroker.setColor(color);
  }

  public void setCharBoxFactory(ICharBoxFactory charBoxFactory)
  {
    mCharBoxFactory = (CharBoxFactory)charBoxFactory;
  }

  // --------------------------------------------------------------------------------
  // Stroke user parameters method from scratch

  public SmartStrokeUserParams createStrokeUserParamsFromInkCaptureView(InkCaptureView v)
  {
    // Build the user parameters
    return new SmartStrokeUserParams(new RectF(v.getStrokeFrame()), new Path(v.getStrokePath()), new Paint(v.getStrokePaint()), mPenMode == PenMode.DRAWING_MODE, mContext.getResources());
  }
  
  private SmartStrokeUserParams createStrokeUserParamsFromSmartStroke(SmartStroke smartStroke)
  {
    // Simulated the stroke drawing with a Stroker to extract drawing properties
    simulateStrokeInStroker(smartStroke, 0, smartStroke.getX().length, 0, 0);

    return new SmartStrokeUserParams(new RectF(mStroker.getBoundingRect()), new Path(mStroker.getPath()), new Paint(mStroker.getPaint()), smartStroke.getStrokeType() == SmartStroke.StrokeType.DrawingStroke, mContext.getResources());
  }
  
  public void updateStrokeUserParameters(SmartStroke smartStroke)
  {
    // Simulated the stroke drawing with a Stroker to extract drawing properties
    simulateStrokeInStroker(smartStroke, 0, smartStroke.getX().length, 0, 0);

    SmartStrokeUserParams sup = (SmartStrokeUserParams)smartStroke.getUserParams();
    sup.updateStrokeUserParameters(new Path(mStroker.getPath()), new RectF(mStroker.getBoundingRect()), new Paint(mStroker.getPaint()), mContext.getResources());
  }

  public void simulateStrokeInStroker(final SmartStroke stroke, final int begin, final int end, final float dx, final float dy)
  {
    final float[] x = stroke.getX();
    final float[] y = stroke.getY();
    for (int i = begin; i < end; ++i)
    {
      if (i == begin)
        mStroker.reset(x[i] + dx, y[i] + dy, 0, 0);
      else
        mStroker.addPoint(x[i] + dx, y[i] + dy, 0, 0);
    }
    mStroker.end();
  }
  
  public void showDialogForPen(Context context)
  {
    final AlertDialog.Builder penDialogBuilder = new AlertDialog.Builder(context);
    penDialogBuilder.setTitle(R.string.pen);

    // OK button monitoring
    penDialogBuilder.setSingleChoiceItems(R.array.pen_dlg_items, mPenMode.ordinal(), new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which)
      {
        mPenMode = (which == 0) ? PenMode.RECOGNITION_MODE : PenMode.DRAWING_MODE;
        setInkColor((mPenMode == PenMode.RECOGNITION_MODE) ? mContext.getResources().getColor(R.color.ITC_TO_BE_RECOGNIZED_INK_COLOR) : mContext.getResources().getColor(R.color.ITC_DRAWING_INK_COLOR));
        dialog.dismiss();
      }
    });
    AlertDialog penAlertDialog=penDialogBuilder.create();
    penAlertDialog.show();
  }

  // --------------------------------------------------------------------------------
  // Stroke user parameters factory methods

  @Override
  public IUserParams readUserParamsFromBytes(byte[] userParamsBytes)
  {
    return new SmartStrokeUserParams(userParamsBytes, mContext.getResources());
  }

  @Override
  public IUserParams createUserParamsForStroke(SmartStroke newStroke, SmartStroke oldStroke)
  {
    // Get the old user parameters
    SmartStrokeUserParams sup = (SmartStrokeUserParams) oldStroke.getUserParams();
    
    if (sup != null)
      return new SmartStrokeUserParams((SmartStrokeUserParams) oldStroke.getUserParams(), mContext.getResources());
    else
      return createStrokeUserParamsFromSmartStroke(newStroke);
  }

  @Override
  public IUserParams createUserParamsForSubStroke(SmartStroke newStroke, SmartStroke oldStroke, int begin, int end)
  {
    // Simulated the stroke drawing with a Stroker to extract drawing properties
    simulateStrokeInStroker(oldStroke, begin, end, 0, 0);

    // Create the new user parameters
    SmartStrokeUserParams nsup = new SmartStrokeUserParams(new RectF(mStroker.getBoundingRect()), new Path(mStroker.getPath()), new Paint(mStroker.getPaint()), mPenMode == PenMode.DRAWING_MODE, mContext.getResources());

    // Get the old user parameters
    SmartStrokeUserParams osup = (SmartStrokeUserParams) oldStroke.getUserParams();

    // User parameters properties monitoring
    nsup.setRecognized(osup.isRecognized());
    nsup.setSelectedRect(osup.getSelectedRect());
    nsup.setUnderlined(osup.isUnderlined());

    // Formatted property is lost on sub stroke creation

    return nsup;
  }

  @Override
  public IUserParams createUserParamsForTransformedStroke(SmartStroke newStroke, SmartStroke oldStroke, Transform transform)
  {
    // Simulated the stroke drawing with a Stroker to extract drawing properties
    simulateStrokeInStroker(oldStroke, 0, oldStroke.getX().length, transform.getXTranslation(), transform.getYTranslation());

    // Create the new user parameters
    SmartStrokeUserParams nsup = new SmartStrokeUserParams(new RectF(mStroker.getBoundingRect()), new Path(mStroker.getPath()), new Paint(mStroker.getPaint()), mPenMode == PenMode.DRAWING_MODE, mContext.getResources());

    // Get the old user parameters
    SmartStrokeUserParams osup = (SmartStrokeUserParams) oldStroke.getUserParams();

    // User parameters properties monitoring
    nsup.setRecognized(osup.isRecognized());
    nsup.setSelectedRect(osup.getSelectedRect());
    nsup.setUnderlined(osup.isUnderlined());
    nsup.setFormattedData(osup.getFormattedData());

    return nsup;
  }

  // --------------------------------------------------------------------------------
  // Word user parameters factory methods

  @Override
  public IUserParams readCharacterUserParams(byte[] userParamsBytes)
  {
    return new SmartWordUserParams(userParamsBytes, mContext.getResources());
  }

  @Override
  public IUserParams createCharacterUserParamsForNewTypeSetWord(SmartWord newWord, int charIndex, SmartWord oldWord)
  {
    // Get the old user parameters
    final IUserParams userParams = oldWord.getUserParams(charIndex);

    SmartWordUserParams wordCharacterUserParams;
    if (userParams != null)
      wordCharacterUserParams = new SmartWordUserParams((SmartWordUserParams) userParams, mContext.getResources());
    else
      wordCharacterUserParams = new SmartWordUserParams(new Paint(mCharBoxFactory.getPaint()), mContext.getResources());

    // Get the old SmartWord SmartStrokes
    final List<SmartStroke> oldStrokes = oldWord.getStrokes();
    
    if (!oldStrokes.isEmpty())
    {
      // Get the properties on the stroke user parameters
      SmartStrokeUserParams ssup = (SmartStrokeUserParams)oldStrokes.get(0).getUserParams();
      wordCharacterUserParams.setFormattedData(ssup.getFormattedData());
      wordCharacterUserParams.setUnderlined(ssup.isUnderlined());
      wordCharacterUserParams.setSelected(ssup.getSelectedRect() != null);
    }

    return wordCharacterUserParams;
  }

  @Override
  public IUserParams createCharacterUserParamsForWord(IUserParams oldCharacterUserParams)
  {
    return oldCharacterUserParams;
  }

  @Override
  public IUserParams createCharacterUserParamsForTransformedWord(IUserParams oldCharacterUserParams, Transform transform)
  {
    return oldCharacterUserParams;
  }
  
  ///////

  public void applyWordPropertyOnWordRange(final WordRange wordRange, final WordProperty wordProperty, final FormattedDataManager.FormattedData fd)
  {
    // Apply properties on whole words
    final List<SmartWord> wordRangeWords = wordRange.getWords();
    for (final SmartWord smartWord : wordRangeWords)
    {
      // Get the word type
      final int smartWordType = smartWord.getType(); 
      
      // Update the property in the SmartWord SmartStroke user parameters
      if (smartWordType == SmartWord.Type.RAW || smartWordType == SmartWord.Type.MIX)
        updateWordStrokeUserParams(smartWord, wordProperty, fd);
      
      if (smartWordType == SmartWord.Type.TYPESET || smartWordType == SmartWord.Type.MIX)
        updateWordCharacterUserParams(smartWord, wordProperty, fd);
    }
  }
  
  private void updateWordStrokeUserParams(final SmartWord smartWord, final WordProperty wordProperty, final FormattedDataManager.FormattedData fd)
  {
    final List<SmartStroke> strokes = smartWord.getStrokes();

    // Parse all the strokes to set its new property through the user parameters
    for (SmartStroke stroke : strokes)
    {
      // Get the SmartStroke user parameters
      final SmartStrokeUserParams ssup = (SmartStrokeUserParams) stroke.getUserParams();
      if (ssup == null)
        break;

      // Assign the new property
      if (wordProperty == WordProperty.UNDERLINED)
        ssup.setUnderlined(!ssup.isUnderlined());
      else if (wordProperty == WordProperty.SELECTED)
      {
        // Only monitor selection property on raw SmartWord
        // Indeed selection rect drawing will be done through typeset character user parameters
        if (smartWord.getType() == SmartWord.Type.RAW)
          ssup.setSelectedRect(smartWord.getBoundingRect());
        // In case of selection property only the first word stroke is set
        break;
      }
      else if (wordProperty == WordProperty.NOT_SELECTED)
      {
        // Only monitor selection property on raw SmartWord
        // Indeed selection rect drawing will be done through typeset character user parameters
        if (smartWord.getType() == SmartWord.Type.RAW)
          ssup.setSelectedRect(null);
        // In case of selection property only the first word stroke is set
        break;
      }
      else if (wordProperty == WordProperty.FORMATTED)
        ssup.setFormattedData(fd);
    }
  }
  
  private void updateWordCharacterUserParams(final SmartWord smartWord, final WordProperty wordProperty, final FormattedDataManager.FormattedData fd)
  {
    // Parse all SmartWord user parameters to set its new property through the user parameters
    final List<Rect> charBoxes = smartWord.getCharBoxes();
    final int charBoxesSize = charBoxes.size();
    for (int i = 0; i < charBoxesSize; ++i)
    {
      // Get the SmartWord character user parameter
      final SmartWordUserParams swup = (SmartWordUserParams) smartWord.getUserParams(i);
      if (swup == null)
        break;

      // Assign the new property
      if (wordProperty == WordProperty.UNDERLINED)
        swup.setUnderlined(!swup.isUnderlined());
      else if (wordProperty == WordProperty.SELECTED)
        swup.setSelected(true);
      else if (wordProperty == WordProperty.NOT_SELECTED)
        swup.setSelected(false);
      else if (wordProperty == WordProperty.FORMATTED)
        swup.setFormattedData(fd);
    }
  }
}
