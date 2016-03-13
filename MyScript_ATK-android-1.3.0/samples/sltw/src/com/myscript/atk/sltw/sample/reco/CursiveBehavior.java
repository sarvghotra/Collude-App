// Copyright MyScript

package com.myscript.atk.sltw.sample.reco;

import android.text.Editable;
import android.util.Log;

import com.myscript.atk.sltw.SingleLineTextWidgetApi;
import com.myscript.atk.sltw.sample.controller.CandidateBarController;
import com.myscript.atk.sltw.sample.view.CustomEditText;

public class CursiveBehavior extends RecognitionBehavior
{
  private static final String TAG = "CursiveBehavior";


  public CursiveBehavior(SingleLineTextWidgetApi widget, CustomEditText editText, CandidateBarController candidatebarController)
  {
    super(widget, editText, candidatebarController);
  }

  // ----------------------------------------------------------------------
  // Custom EditText interface

  @Override
  public void onCursorIndexChanged(final int index)
  {
    Log.d(TAG, "Cursor index changed to " + index + " from input field");

    if (!mWidget.isCursorHandleDragging())
    {
      if (mWidget.isInsertionMode())
      {
        // switch to correction mode if the cursor is moved to a
        // position different from the current insert index

        if (index != mWidget.getInsertIndex())
        {
          mWidget.setCorrectionMode();
        }
      } else
      {
        // switch to insertion mode if the cursor is moved to the
        // end of the text
        final Editable editable = mEditText.getText();
        if ((editable != null) && (index == editable.length()))
        {
          mWidget.setInsertionMode(index);
        }
      }
    }

    // update cursor index
    // this may trigger an onSelectionChanged() event
    mWidget.setCursorIndex(index);
    // scroll to cursor if widget is in correction mode
    if (!mWidget.isInsertionMode())
    {
      mWidget.scrollToCursor();
    }
  }

  // ----------------------------------------------------------------------
  // Text management

  @Override
  public void onTextChanged(final String text, final boolean intermediate)
  {
    Log.d(TAG, "Text changed to \"" + text.replace('\n', '\u00B6') + "\" " + (intermediate ? "(intermediate)" : "(stable)"));

    // get the text currently stored in the target field
    String previousText = "";
    final Editable editable = mEditText.getText();
    if (editable != null)
      previousText = editable.toString();

    // temporarily disable selection changed listener
    // because setText() automatically places the cursor at the beginning
    // of the text

    mEditText.setOnCursorIndexChangedListener(null);
    mEditText.setTextKeepState(text);
    mEditText.setOnCursorIndexChangedListener(this);

    if (mWidget.isInsertionMode())
    {

      // widget is in insertion mode
      if (previousText.length() == text.length())
        // If there is no change in cursor position, don't ask the editor to trigger onSelectionChanged by setSelection but let the widget trigger directly
        mWidget.setCursorIndex(mWidget.getInsertIndex());
      else
        // If there is a change in cursor, then put cursor at current widget insert index
        mEditText.setCursorIndex(mWidget.getInsertIndex());

    } else
    {

      // widget is in correction mode
      // auto-switch to insertion mode if user appended text or text is empty
      if (text.length() == 0 || text.length() > previousText.length() && text.startsWith(previousText))
      {
        mWidget.setInsertionMode(text.length());
        mEditText.setCursorIndex(text.length());
      } else if (text.length() != previousText.length())
      {
        int currIndex;
        currIndex = mWidget.getCursorIndex();
        mEditText.setCursorIndex(currIndex);
      }

    }
  }
}
