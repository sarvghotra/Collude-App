// Copyright MyScript

package com.myscript.atk.scw.sample;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;

public class FieldView extends EditText {
  
  public interface OnSelectionChangedListener {
    public void onSelectionChanged(View v, int selStart, int selEnd);
  }
  
  private OnSelectionChangedListener mListener;

  public FieldView(Context context) {
    super(context);
  }

  public FieldView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }
  
  public void setOnSelectionChangedListener(OnSelectionChangedListener l) {
    mListener = l;
  }
  
  @Override
  protected void onSelectionChanged(int selStart, int selEnd) {
    if (mListener != null) {
      mListener.onSelectionChanged(this, selStart, selEnd);
    }
  }
  
  @Override
  public boolean onTouchEvent(MotionEvent event)  {
    
    // Note:
    // The following piece of code prevents the system input method to appear
    // when the user taps on a field, but computation of the offset fails for
    // right-to-left languages.
    
    if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
      requestFocus();
      int index = getLayout().getOffsetForHorizontal(0, event.getX());
      setSelection(index);
    }
    
    return true;
  }

}
