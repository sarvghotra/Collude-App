package com.myscript.atk.itc.sample.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;

import com.myscript.atk.itc.SmartStroke;
import com.myscript.atk.itc.sample.R;
import com.myscript.atk.itc.sample.Debug;
import com.myscript.atk.itc.sample.model.SmartStrokeUserParams;

public class SmartStrokeView
{
  private static final boolean DBG = Debug.DBG_DRAW;
  
  private SmartStrokeUserParams mStrokeUserParams;
  private Rect mFrame;
  private RectF mSelectedRect;
  private Paint mPaintSelection;
  private Paint mPaintDebug;
  
  public SmartStrokeView(SmartStroke stroke, Context context)
  {
    mFrame = new Rect();
    mStrokeUserParams = ((SmartStrokeUserParams) stroke.getUserParams());
    mStrokeUserParams.getFrame().roundOut(mFrame);

    // Preallocate RectF to avoid object creation at drawing time
    mSelectedRect = new RectF();

    // Initialize the Paint selection
    mPaintSelection = new Paint();
    mPaintSelection.setColor(context.getResources().getColor(R.color.ITC_SELECTION_BACKGROUND_COLOR));
    mPaintSelection.setAlpha(50);

    if (DBG)
    {
      mPaintDebug = new Paint();
      mPaintDebug.setStyle(Style.STROKE);
      mPaintDebug.setColor(context.getResources().getColor(R.color.ITC_DEBUG_COLOR_YELLOW));
    }
  }

  public Rect getFrame()
  {
    return mFrame;
  }

  public void draw(Canvas canvas)
  {
    // Save the canvas state
    canvas.save();

    if (DBG)
      canvas.drawRect(mFrame, mPaintDebug);

    Paint paint = mStrokeUserParams.getPaint();
    Path path = mStrokeUserParams.getPath();
    canvas.scale(1, 1, mFrame.centerX(), mFrame.centerY());

    // First draw the complete Path
    canvas.drawPath(path, paint);

    // Clip the path according to the stroke attributes
    final com.myscript.atk.itc.Rect selectedRect = mStrokeUserParams.getSelectedRect();
    if (selectedRect != null)
    {
      mSelectedRect.set(selectedRect.getLeft(), selectedRect.getTop(), selectedRect.getRight(), selectedRect.getBottom());
      canvas.drawRect(mSelectedRect, mPaintSelection);
    }

    // Restore the canvas state
    canvas.restore();
  }
}
