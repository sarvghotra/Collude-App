package com.myscript.atk.itc.sample.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.view.View;

import com.myscript.atk.itc.sample.R;

public class GuidelinesView extends View
{
  // Guidelines
  private boolean mGuidelinesEnable;
  private Context mContext;
  private float mFirstLinePos;
  private float mGap;
  private int mLineCount;
  private Paint mPaintGuidelines;
  // Boolean value used to trigger clear on right time to avoid InkCaptureView Transparency
  private boolean mToggled;

  public GuidelinesView(Context context, AttributeSet attrs)
  {
    super(context, attrs);

    mContext = context;

    mPaintGuidelines = new Paint();
    mPaintGuidelines.setStyle(Style.STROKE);
    mPaintGuidelines.setColor(getResources().getColor(R.color.ITC_GUIDELINE_COLOR));
  }

  @Override
  public void onMeasure(int widthSpec, int heightSpec)
  {
    super.onMeasure(widthSpec, heightSpec);
    final int size = Math.min(getMeasuredWidth(), getMeasuredHeight());
    final int densityDpi = mContext.getResources().getDisplayMetrics().densityDpi;

    mGap = 1.5f * densityDpi / 2.54f;
    mLineCount = Math.round(size / mGap);
    mFirstLinePos = getTop() + mGap;
  }

  public void toggleGuidelines()
  {
    mGuidelinesEnable = !mGuidelinesEnable;
    mToggled = true;
        
    invalidate();
  }

  public float getFirstLinePos()
  {
    return mFirstLinePos;
  }

  public float getGap()
  {
    return mGap;
  }

  public int getLineCount()
  {
    return mLineCount;
  }
  
  public boolean isGuidelinesEnable()
  {
    return mGuidelinesEnable;
  }

  @Override
  protected void onDraw(Canvas canvas)
  {
    super.onDraw(canvas);

    if (mGuidelinesEnable)
    {
      for (int i = 0; i < mLineCount; ++i)
        canvas.drawLine(0, mFirstLinePos + i * mGap, this.getWidth(), mFirstLinePos + i * mGap, mPaintGuidelines);
    }
    else
    {
      // Check for user toggled guidelines view clear
      if (mToggled)
      {
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        mToggled = false;
      }
    }
  }
}
