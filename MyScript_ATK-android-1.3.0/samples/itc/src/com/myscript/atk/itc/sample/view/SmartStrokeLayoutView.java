package com.myscript.atk.itc.sample.view;

import java.util.HashMap;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import com.myscript.atk.itc.SmartStroke;

public class SmartStrokeLayoutView extends View
{
  private HashMap<SmartStroke, SmartStrokeView> mStrokeViews;
  
  private Rect mClipBounds;

  private Context mContext;

  public SmartStrokeLayoutView(Context context, AttributeSet attrs)
  {
    super(context, attrs);
    mStrokeViews = new HashMap<SmartStroke, SmartStrokeView>();
    mClipBounds = new Rect();
    mContext = context;
  }
  
  public boolean contains(SmartStroke stroke)
  {
    return mStrokeViews.containsKey(stroke);
  }
  
  public void addStroke(SmartStroke stroke)
  {
    SmartStrokeView v = new SmartStrokeView(stroke, mContext);
    mStrokeViews.put(stroke, v);

    invalidate(v.getFrame());
  }
  
  public void removeStroke(SmartStroke stroke)
  {
    SmartStrokeView v = mStrokeViews.remove(stroke);

    invalidate(v.getFrame());
  }

  public void invalidate(SmartStroke stroke)
  {
    SmartStrokeView v = mStrokeViews.get(stroke);
    invalidate(v.getFrame());
  }
  
  @Override
  protected void onDraw(Canvas canvas)
  {
    super.onDraw(canvas);

    canvas.getClipBounds(mClipBounds);
    for (SmartStrokeView v : mStrokeViews.values())
      if (Rect.intersects(v.getFrame(), mClipBounds))
        v.draw(canvas);
  }
}
