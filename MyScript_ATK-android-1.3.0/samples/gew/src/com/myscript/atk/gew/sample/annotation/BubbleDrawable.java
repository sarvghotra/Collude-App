// Copyright MyScript. All right reserved.

package com.myscript.atk.gew.sample.annotation;

import com.myscript.atk.gew.sample.utils.Utils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;

public class BubbleDrawable extends Drawable
{
  // Settings
  private static final int BACKGROUND_COLOR = Color.WHITE;
  private static final float CORNER_ROUND = 0.15f; 				// Corner (cm)
  public static final float MARGINS 	  = 0.25f;				// Tail space (cm)
    
  // Members
  private Paint mPaint;
  private PointF mPoint;										// Tail anchor point
  private Context mContext;										// Android context
  
  public BubbleDrawable(Context context, final PointF anchor)
  {
    mContext = context;
    mPaint = new Paint();
    mPaint.setColor(BACKGROUND_COLOR);
    mPaint.setAntiAlias(true);
    mPoint = anchor;
  }

  @Override
  public void draw(Canvas canvas)
  {
    // Draw round rect
    int height = getBounds().height();
    int width = getBounds().width();
    RectF rect = new RectF(0.0f, 0.0f, width, height);
    int margins = Math.round(Utils.centimetersToPoints(mContext, MARGINS)); 
    rect.inset(margins, margins);
    int corners = Math.round(Utils.centimetersToPoints(mContext, CORNER_ROUND)); 
    canvas.drawRoundRect(rect, corners, corners, mPaint);
    
    // Draw tail
    float head_length = 0;
    float head_width = Math.min(rect.width() * 2f / 3f, rect.height() * 2f / 3f);
    PointF center = new PointF(rect.centerX(), rect.centerY());
    
    Path tail = new Path();
    tail.moveTo(mPoint.x, mPoint.y);
    
    PointF v = new PointF(mPoint.x - center.x, mPoint.y - center.y);
    PointF tv = new PointF(-v.y, v.x);
    double l = Math.sqrt(Math.pow(v.x, 2.f) + Math.pow(v.y, 2.f));    
    
    PointF xv = new PointF(v.x / (float) l, v.y / (float) l);
    PointF xx = new PointF(center.x, center.y);
    PointF s = new PointF(xx.x - xv.x, xx.y - xv.y);
    
    PointF yv = new PointF((v.x * head_length) / (float) l, (v.y * head_length) / (float) l);
    PointF ys = new PointF(s.x + yv.x, s.y + yv.y);
    PointF ytv = new PointF((tv.x * .5f * head_width / (float) l), (tv.y * .5f * head_width / (float) l));
    s = new PointF(ys.x + ytv.x, ys.y + ytv.y);    
    tail.lineTo(s.x, s.y);
    
    PointF ztv = new PointF((tv.x * head_width / (float) l), (tv.y * head_width / (float) l));
    s = new PointF(s.x - ztv.x, s.y - ztv.y);    
    tail.lineTo(s.x, s.y);
    tail.close();
    canvas.drawPath(tail, mPaint);
  }

  @Override
  public void setAlpha(int alpha)
  {
    mPaint.setAlpha(alpha);
  }

  @Override
  public void setColorFilter(ColorFilter cf)
  {
    mPaint.setColorFilter(cf);
  }

  @Override
  public int getOpacity()
  {
    return PixelFormat.TRANSLUCENT;
  }
} 