// Copyright MyScript

package com.myscript.atk.sltw.sample.reco;

import android.util.Log;

import com.myscript.atk.sltw.SingleLineTextWidgetApi;
import com.myscript.atk.sltw.sample.controller.CandidateBarController;
import com.myscript.atk.sltw.sample.controller.ToolbarController;
import com.myscript.atk.sltw.sample.view.CustomEditText;

public abstract class RecognitionBehavior
        implements
        SingleLineTextWidgetApi.OnTextChangedListener,
        SingleLineTextWidgetApi.OnSelectionChangedListener,
        ToolbarController.OnSpaceButtonClickedListener,
        ToolbarController.OnDeleteButtonClickedListener,
        CandidateBarController.OnCandidateButtonClickedListener,
        CustomEditText.OnCursorIndexChangedListener
{
  private String TAG = "RecognitionBehavior";

  protected CustomEditText mEditText;
  protected CandidateBarController mCandidateBarController;
  protected SingleLineTextWidgetApi mWidget;


  public RecognitionBehavior(SingleLineTextWidgetApi widget, CustomEditText editText, CandidateBarController candidatebarController)
  {
    mWidget = widget;
    mEditText = editText;
    mCandidateBarController = candidatebarController;
  }

  // ----------------------------------------------------------------------
  // Toolbar controller interface

  @Override
  public void onSpaceButtonClicked()
  {
    Log.d(TAG, "Space button clicked");

    final int start = mEditText.getSelectionStart();
    final int end = mEditText.getSelectionEnd();

    mWidget.replaceCharacters(start, end, " ");
    mEditText.setCursorIndex(start + 1);
  }

  @Override
  public void onDeleteButtonClicked()
  {
    Log.d(TAG, "Delete button clicked");

    final int start = mEditText.getSelectionStart();
    final int end = mEditText.getSelectionEnd();

    if (start != end)
    {
      mWidget.replaceCharacters(start, end, null);
    } else if (start != 0)
    {
      mWidget.replaceCharacters(start - 1, start, null);
      mEditText.setCursorIndex(start - 1);
    }
  }

  // ----------------------------------------------------------------------
  // Candidate bar controller interface

  @Override
  public void onCandidateButtonClicked(final int start, final int end, final String label)
  {
    Log.d(TAG, "Candidate label \"" + label + "\" clicked");
    mWidget.replaceCharacters(start, end, label);
  }

  // ----------------------------------------------------------------------
  // Text selection

  @Override
  public void onSelectionChanged(final int start, final int end, final String[] labels, final int selectedIndex)
  {
    Log.d(TAG, "Selection changed range " + start + "-" + end);

    if (labels != null)
    {
      for (int i = 0; i < labels.length; i++)
      {
        String flags;
        if (i == selectedIndex)
        {
          flags = " (*)";
        } else
        {
          flags = "";
        }
        Log.d(TAG, "labels[" + i + "] = \"" + labels[i] + "\"" + flags);
      }
    }

    mCandidateBarController.setLabels(start, end, labels, selectedIndex);
  }
}