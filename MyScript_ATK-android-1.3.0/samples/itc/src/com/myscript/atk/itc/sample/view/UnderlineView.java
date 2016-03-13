package com.myscript.atk.itc.sample.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.view.View;

import com.myscript.atk.itc.sample.R;

public class UnderlineView extends View
{
  Paint mPaint = new Paint();
  final float mY, mLeft, mRight;

  public UnderlineView(Context context)
  {
    super(context);
    mY = mLeft = mRight = 0;
  }

  public UnderlineView(Context context, float y, float left, float right)
  {
    super(context);
    mPaint.setColor(getResources().getColor(R.color.ITC_UNDERLINE_COLOR));
    mPaint.setStyle(Style.STROKE);
    mPaint.setStrokeWidth(3);
    mY = y;
    mLeft = left;
    mRight = right;
  }

  @Override
  public void onDraw(Canvas canvas)
  {
    canvas.drawLine(mLeft, mY, mRight, mY, mPaint);
  }
}
