// Copyright MyScript

package com.myscript.atk.sltw.sample.reco;

import android.util.Log;

import com.myscript.atk.sltw.SingleLineTextWidgetApi;
import com.myscript.atk.sltw.sample.controller.CandidateBarController;
import com.myscript.atk.sltw.sample.view.CustomEditText;

public class IsolatedBehavior extends RecognitionBehavior
{
  private static final String TAG = "IsolatedBehavior";
  
  private final static char         ZERO_WIDTH_SPACE = (char) 0x200B;

  private int mCursorIndex;
  
  public IsolatedBehavior(SingleLineTextWidgetApi widget, CustomEditText editText, CandidateBarController candidatebarController)
  {
    super(widget, editText, candidatebarController);
  }

  // ----------------------------------------------------------------------
  // Custom EditText interface
  
  @Override
  public void onCursorIndexChanged(int index)
  {
    Log.d(TAG, "Cursor index changed to " + index + " from input field");
    
    if (index != mCursorIndex) {
      mCandidateBarController.clearLabels();
    }
    
    mCursorIndex = index;
  }
  
  // ----------------------------------------------------------------------
  // Text management

  @Override
  public void onTextChanged(String text, boolean intermediate)
  {
    Log.d(TAG, "Text changed to \"" + text.replace('\n', '\u00B6') + "\" " + (intermediate ? "(intermediate)" : "(stable)"));

    if(!text.equals("") && !intermediate)
    {
      int start = mEditText.getSelectionStart();
      int end = mEditText.getSelectionEnd();

      String before = mEditText.getText().subSequence(0, start).toString();
      String after = mEditText.getText().subSequence(end, mEditText.length()).toString();

      // It is isolated recognition so if text has more than 1 char it means there is a diacritic involved
      // In that case we add a zero-width space to the text otherwise selection of text field will be wrong
      // (Android selection span doesn't like diacritics...) See for ref. : #12342
      if(text.length() > 1)
        setTextNoSelection(before + text + ZERO_WIDTH_SPACE + after);
      else
        setTextNoSelection(before + text + after);
      
      setCursorIndex(start + text.length());

      // Clearing widget strokes
      mWidget.setText("");
    }
  }
  
  // ----------------------------------------------------------------------
  // Toolbar controller interface

  @Override
  public void onSpaceButtonClicked()
  {
    Log.d(TAG, "Space button clicked");
    
    int start = mEditText.getSelectionStart();
    int end = mEditText.getSelectionEnd();
    
    String before = mEditText.getText().subSequence(0, start).toString();
    String after = mEditText.getText().subSequence(end, mEditText.length()).toString();

    setTextNoSelection(before + " " + after);
    setCursorIndex(start + 1);
    
    mCandidateBarController.clearLabels();
  }

  @Override
  public void onDeleteButtonClicked()
  {
    Log.d(TAG, "Delete button clicked");

    int start = mEditText.getSelectionStart();
    int end = mEditText.getSelectionEnd();
    
    String before = "";
    if (start != end) {
      before = mEditText.getText().subSequence(0, start).toString();
    } else if (start != 0) {
      before = mEditText.getText().subSequence(0, start-1).toString();
    }
    String after = mEditText.getText().subSequence(end, mEditText.length()).toString();
    
    setTextNoSelection(before + after);
    setCursorIndex(start <= 0 ? 0 : start - 1);
    
    mCandidateBarController.clearLabels();
  }
  
  // ----------------------------------------------------------------------
  // Candidate bar controller interface

  @Override
  public void onCandidateButtonClicked(final int start, final int end, final String label)
  {
    Log.d(TAG, "Candidate label \"" + label + "\" clicked");
    
    int selStart = mEditText.getSelectionStart();
    
    String before = mEditText.getText().subSequence(0, selStart - end + start).toString();
    String after = mEditText.getText().subSequence(selStart, mEditText.length()).toString();
    
    setTextNoSelection(before + label + after);
    setCursorIndex(selStart + label.length() - (end - start));

    mCandidateBarController.setSelectedCandidate(label);    
  }

  // ----------------------------------------------------------------------
  // Text selection

  @Override
  public void onSelectionChanged(final int start, final int end, final String[] labels, final int selectedIndex)
  {
    // keep candidates if no candidates are proposed (i.e. text is cleared in the writing area)
    if (labels == null)
      return;
    
    super.onSelectionChanged(start, end, labels, selectedIndex);
  }
  
  private void setTextNoSelection(String text)
  {
    // temporarily disable selection listener because setText()
    // automatically places the cursor at the beginning of the text

    mEditText.setOnCursorIndexChangedListener(null);
    mEditText.setTextKeepState(text);
    mEditText.setOnCursorIndexChangedListener(this);
  }
  
  private void setCursorIndex(int index)
  {
    mCursorIndex = index;
    mEditText.setCursorIndex(index);
  }
}