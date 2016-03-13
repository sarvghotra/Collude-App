package com.myscript.atk.sltw.sample.reco;

import android.content.Context;
import android.text.Editable;
import android.util.Log;
import android.util.TypedValue;

import com.myscript.atk.sltw.SingleLineTextWidgetApi;
import com.myscript.atk.sltw.sample.controller.CandidateBarController;
import com.myscript.atk.sltw.sample.view.CustomEditText;

public class BoxModeBehavior extends RecognitionBehavior
{
  private static final String TAG = "BoxModeBehavior";
  private static final double CJK_BOX_W_X_H_RATIO = 4.0 / 3.0;
  private static final double BOX_W_X_H_RATIO = 16.0 / 9.0;
  private static final int BOX_SPACE = 9;   // dp
  public static final int BOX_WIDTH = 85; //dp
  public static final int BOX_TOP_MARGIN = 5;//dp

  private Context mContext;

  public BoxModeBehavior(SingleLineTextWidgetApi widget, CustomEditText editText, CandidateBarController candidatebarController, Context context)
  {
    super(widget, editText, candidatebarController);
    mContext = context;
  }

  // ----------------------------------------------------------------------
  // Custom EditText interface

  @Override
  public void onCursorIndexChanged(final int index)
  {
    Log.d(TAG, "Cursor index changed to " + index + " from input field");

    if (!mWidget.isCursorHandleDragging())
    {
      int textLen = -1;
      final Editable editable = mEditText.getText();
      if (editable != null)
        textLen = editable.toString().length();
      if (mWidget.isInsertionMode() && index != textLen)
      {
        // switch to correction mode if the cursor is moved to a
        // position different from the current insert index
        mWidget.setCorrectionMode();
      } else if (index == textLen)
      {
        // switch to insertion mode if the cursor is moved to the
        // end of the text
        mWidget.setInsertionMode(index);
      }
    }

    // update cursor index
    // this may trigger an onSelectionChanged() event
    mWidget.setCursorIndex(index);
    final int indexMask = index == 0 ? 0 : index - 1;
    mWidget.setBoxMask(indexMask, indexMask, false);
    // scroll to cursor if widget is in correction mode
    if (!mWidget.isInsertionMode())
    {
      mWidget.smoothScrollToCursor();
    }
  }

  // ----------------------------------------------------------------------
  // Text management

  @Override
  public void onTextChanged(final String text, final boolean intermediate)
  {
    Log.d(TAG, "Text changed to \"" + text.replace('\n', '\u00B6') + "\" " + (intermediate ? "(intermediate)" : "(stable)"));

    // get the text currently stored in the target field
    int previousLength = -1;
    final Editable editable = mEditText.getText();
    if (editable != null)
      previousLength = editable.toString().length();

    // temporarily disable selection changed listener
    // because setText() automatically places the cursor at the beginning
    // of the text

    mEditText.setOnCursorIndexChangedListener(null);
    mEditText.setTextKeepState(text);
    mEditText.setOnCursorIndexChangedListener(this);

    if (previousLength != text.length())
      mEditText.setCursorIndex(text.length());
  }

  // ----------------------------------------------------------------------
  // Box size computation

  public void computeBoxSize(final String locale)
  {
    final boolean isCJK = locale.equals("zh_CN") || locale.equals("zh_TW") || locale.equals("zh_HK")
            || locale.equals("zh_TR") || locale.equals("ja_JP") || locale.equals("ko") || locale.equals("ko_KR");

    final double boxRatio = isCJK ? CJK_BOX_W_X_H_RATIO : BOX_W_X_H_RATIO;

    final int boxSpace = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, BOX_SPACE, mContext
            .getResources().getDisplayMetrics()));

    final int boxWidth = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, BOX_WIDTH, mContext
            .getResources().getDisplayMetrics()));

    final int boxTopMargin = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, BOX_TOP_MARGIN, mContext
            .getResources().getDisplayMetrics()));
    //in real world app, you have to take care of this dimension, it can overlap the TextWidget scroll bar.
    // a good way to proceed is to retrieve the widget size and ensure that the computed height is not too high
    final int boxHeight = (int) Math.round(boxWidth * boxRatio);

    mWidget.setBoxesConfiguration(boxSpace, boxTopMargin, boxSpace, boxWidth, boxHeight);
  }
}
