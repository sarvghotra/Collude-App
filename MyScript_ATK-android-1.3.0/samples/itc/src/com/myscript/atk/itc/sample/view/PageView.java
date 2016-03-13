package com.myscript.atk.itc.sample.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

public class PageView extends FrameLayout
{
  public PageView(Context context, AttributeSet attrs)
  {
    super(context, attrs);
  }

//  public void onMeasure(int widthSpec, int heightSpec)
//  {
//    super.onMeasure(widthSpec, heightSpec);
//    int size = Math.min(getMeasuredWidth(), getMeasuredHeight());
//    setMeasuredDimension(MeasureSpec.makeMeasureSpec(size, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(size, MeasureSpec.EXACTLY));
//  }
  
  protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec)
  {
    final int size = MeasureSpec.makeMeasureSpec(
        Math.min(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.getSize(heightMeasureSpec)), MeasureSpec.EXACTLY);
    super.onMeasure(size, size);
  }
}
