// Copyright MyScript

package com.myscript.atk.sltw.sample.view;

import android.content.Context;
import android.text.Editable;
import android.util.AttributeSet;
import android.widget.EditText;

/** This class extends the standard {@link EditText} component by adding ability to listen to cursor and selection changes. */
public class CustomEditText extends EditText
{
  public interface OnCursorIndexChangedListener {
    public void onCursorIndexChanged(int index);
  }
  
  private OnCursorIndexChangedListener mOnCursorIndexChangedListener;
  
  public CustomEditText(final Context context, final AttributeSet attrs)
  {
    super(context, attrs);
  }
  
  public void setOnCursorIndexChangedListener(final OnCursorIndexChangedListener l)
  {
    mOnCursorIndexChangedListener = l;
  }
  
  @Override
  protected void onSelectionChanged(final int selStart, final int selEnd)
  {
    super.onSelectionChanged(selStart, selEnd);
    
    if (mOnCursorIndexChangedListener != null) {
      mOnCursorIndexChangedListener.onCursorIndexChanged(selEnd);
    }
  }
  
  public void setCursorIndex(int index)
  {
    final Editable editable = getText();
    int length = 0;
    if(editable != null)
      length = editable.length();

    if (index < 0) {
      index = 0;
    }
    if (index > length) {
      index = length;
    }
    
    setSelection(index);
  }
}
