// Copyright MyScript

package com.myscript.atk.itc.sample.inkcapture;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.myscript.atk.itc.sample.R;
import com.myscript.atk.itc.sample.Debug;
import com.myscript.atk.itc.sample.stroker.SimpleStroker;
import com.myscript.atk.itc.sample.stroker.Stroker;

public class InkCaptureView extends View
{
  private static final String TAG = "InkCaptureView";
  private static final boolean DBG = Debug.DBG_DRAW;
  private static final float DEFAULT_INKWIDTH = 3;

  public interface OnDrawListener
  {
    public void onDrawBegin(InkCaptureView v);

    public void onDrawEnd(InkCaptureView v);

    public void onDrawCancel(InkCaptureView v);

    public void onDrawing(InkCaptureView v, float x, float y);
  }

  public interface OnDragListener
  {
    public boolean canBeginDrag(InkCaptureView v, float x, float y);

    public void onDragBegin(InkCaptureView v, float x, float y);

    public void onDragEnd(InkCaptureView v);

    public void onDragging(InkCaptureView v, float x, float y);
  }

  private static final int STATE_IDLE = 0;
  private static final int STATE_DRAWING = 1;
  private static final int STATE_DRAGGING = 2;

  private Paint mPaintDebug;

  private Stroker mStroker;
  private Rect mDirtyRect;
  private Rect mClipBounds;

  private int mState;
  private int mActivePointerId;

  private float mX;
  private float mY;

  private OnDrawListener mOnDrawListener;
  private OnDragListener mOnDragListener;

  // --------------------------------------------------------------------------------
  // Constructor

  public InkCaptureView(Context context, AttributeSet attrs)
  {
    super(context, attrs);

    mStroker = new SimpleStroker();
    mStroker.setColor(context.getResources().getColor(R.color.ITC_TO_BE_RECOGNIZED_INK_COLOR));
    mStroker.setWidth(DEFAULT_INKWIDTH);
    mDirtyRect = new Rect();
    mClipBounds = new Rect();

    mX = 0.0f;
    mY = 0.0f;

    if (DBG)
    {
      mPaintDebug = new Paint();
      mPaintDebug.setStyle(Paint.Style.STROKE);
      mPaintDebug.setColor(Color.RED);
    }
  }

  // --------------------------------------------------------------------------------
  // Configuration and getters

  public Stroker getStroker()
  {
    return mStroker;
  }

  public void setStrokeWidth(float width)
  {
    mStroker.setWidth(width);
  }

  public float[] getPointsX()
  {
    return mStroker.getPointsX();
  }

  public float[] getPointsY()
  {
    return mStroker.getPointsY();
  }

  public RectF getStrokeFrame()
  {
    return mStroker.getBoundingRect();
  }

  public Paint getStrokePaint()
  {
    return mStroker.getPaint();
  }

  public Path getStrokePath()
  {
    return mStroker.getPath();
  }

  public void setOnDrawListener(OnDrawListener l)
  {
    mOnDrawListener = l;
  }

  public void setOnDragListener(OnDragListener l)
  {
    mOnDragListener = l;
  }

  public boolean isBusy()
  {
    return mState != STATE_IDLE;
  }

  // --------------------------------------------------------------------------------
  // Cancel operation

  /** Cancel current drawing operation. */
  public void cancel()
  {
    if (mState == STATE_DRAWING)
    {
      mOnDrawListener.onDrawCancel(this);

      clear();

      mActivePointerId = -1;
      mState = STATE_IDLE;
    }
  }

  // --------------------------------------------------------------------------------
  // onDraw view event handler

  @Override
  public void onDraw(Canvas canvas)
  {
    super.onDraw(canvas);
    if (mState == STATE_DRAWING)
    {
      canvas.drawPath(mStroker.getPath(), mStroker.getPaint());
    }
    if (DBG)
    {
      canvas.getClipBounds(mClipBounds);
      mClipBounds.inset(1, 1);
      canvas.drawRect(mClipBounds, mPaintDebug);
    }
  }

  // --------------------------------------------------------------------------------
  // onTouch view event handler

  @Override
  public boolean onTouchEvent(MotionEvent event)
  {
    if (isEnabled())
    {
      mDirtyRect.setEmpty();

      int actionMasked = event.getActionMasked();
      switch (actionMasked)
      {
        case MotionEvent.ACTION_POINTER_DOWN :
        case MotionEvent.ACTION_DOWN :
          processActionDownEvent(event);
          break;

        case MotionEvent.ACTION_MOVE :
          processActionMoveEvent(event);
          break;

        // cancel event is issued when pressing the button on the S pen
        case MotionEvent.ACTION_CANCEL :
          if (isStylusButtonPressed(event))
          {
            processActionUpEvent(event);
          }
          else
          {
            processActionCancelEvent(event);
          }
          break;

        case MotionEvent.ACTION_POINTER_UP :
        case MotionEvent.ACTION_UP :
          processActionUpEvent(event);
          break;

        default :
          if (DBG)
          {
            Log.d(TAG, "Unhandled motion event action");
          }
          break;
      }

      if (!mDirtyRect.isEmpty())
      {
        invalidate(mDirtyRect);
      }
    }

    return true;
  }

  // --------------------------------------------------------------------------------
  // Motion event actions processing

  private void clear()
  {
    mStroker.getBoundingRect().roundOut(mDirtyRect);
    mStroker.reset();

    if (!mDirtyRect.isEmpty())
    {
      invalidate(mDirtyRect);
    }
  }

  private void processActionDownEvent(MotionEvent event)
  {
    boolean isStylus = isStylusMotionEvent(event);

    if (isStylus || mState == STATE_IDLE)
    {
      int pointerIndex = event.getActionIndex();
      int pointerId = event.getPointerId(pointerIndex);

      mX = event.getX(pointerIndex);
      mY = event.getY(pointerIndex);
      float p = event.getPressure(pointerIndex);
      long t = event.getEventTime();

      if (mState == STATE_DRAGGING)
      {
        mOnDragListener.onDragEnd(this);
      }
      if (mState == STATE_DRAWING)
      {
        mOnDrawListener.onDrawCancel(this);
        clear();
      }

      mActivePointerId = pointerId;

      if (mOnDragListener != null && mOnDragListener.canBeginDrag(this, mX, mY))
      {
        mOnDragListener.onDragBegin(this, mX, mY);
        mState = STATE_DRAGGING;
      }
      else if (mOnDrawListener != null)
      {
        mOnDrawListener.onDrawBegin(this);
        mOnDrawListener.onDrawing(this, mX, mY);
        if (isStylus)
        {
          mStroker.setPressureType(Stroker.PRESSURE_RAW);
        }
        else
        {
          mStroker.setPressureType(Stroker.PRESSURE_SIMULATED);
        }
        mStroker.reset(mX, mY, p, t);
        mState = STATE_DRAWING;
      }
    }
  }

  private void processActionMoveEvent(MotionEvent event)
  {
    int pointerIndex = event.findPointerIndex(mActivePointerId);
    if (pointerIndex != -1)
    {
      if (mState == STATE_DRAGGING)
      {
        float x = event.getX(pointerIndex);
        float y = event.getY(pointerIndex);
        mOnDragListener.onDragging(this, x, y);
      }
      else if (mState == STATE_DRAWING)
      {
        int size = event.getHistorySize();
        for (int i = 0; i < size; i++)
        {
          float hx = event.getHistoricalX(pointerIndex, i);
          float hy = event.getHistoricalY(pointerIndex, i);
          float hp = event.getHistoricalPressure(pointerIndex, i);
          long ht = event.getHistoricalEventTime(i);
          mOnDrawListener.onDrawing(this, hx, hy);
          mStroker.addPoint(hx, hy, hp, ht);
        }
        float x = event.getX(pointerIndex);
        float y = event.getY(pointerIndex);
        float p = event.getPressure(pointerIndex);
        long t = event.getEventTime();
        mOnDrawListener.onDrawing(this, x, y);
        mStroker.addPoint(x, y, p, t);
        mStroker.getDirtyRect().roundOut(mDirtyRect);
        mStroker.resetDirtyRect();
      }
    }
  }

  private void processActionUpEvent(MotionEvent event)
  {
    int pointerIndex = event.getActionIndex();
    int pointerId = event.getPointerId(pointerIndex);

    if (pointerId == mActivePointerId)
    {
      if (mState == STATE_DRAGGING)
      {
        mOnDragListener.onDragEnd(this);
      }
      else if (mState == STATE_DRAWING)
      {
        float x = event.getX(pointerIndex);
        float y = event.getY(pointerIndex);
        float p = event.getPressure(pointerIndex);
        long t = event.getEventTime();
        mOnDrawListener.onDrawing(this, x, y);
        mStroker.end(x, y, p, t);
        mStroker.getDirtyRect().roundOut(mDirtyRect);
        mStroker.resetDirtyRect();
        mOnDrawListener.onDrawEnd(this);
      }

      mActivePointerId = -1;
      mState = STATE_IDLE;
    }
  }

  private void processActionCancelEvent(MotionEvent event)
  {
    int pointerIndex = event.getActionIndex();
    int pointerId = event.getPointerId(pointerIndex);

    if (pointerId == mActivePointerId)
    {
      if (mState == STATE_DRAGGING)
      {
        mOnDragListener.onDragEnd(this);
      }
      else if (mState == STATE_DRAWING)
      {
        mOnDrawListener.onDrawCancel(this);
      }

      mActivePointerId = -1;
      mState = STATE_IDLE;
    }
  }

  // --------------------------------------------------------------------------------
  // Stylus detection

  @TargetApi(14)
  private boolean isStylusMotionEvent(MotionEvent event)
  {
    if (Build.VERSION.SDK_INT >= 14)
    {
      int pointerIndex = event.getActionIndex();
      int toolType = event.getToolType(pointerIndex);
      if (toolType == MotionEvent.TOOL_TYPE_STYLUS)
      {
        return true;
      }
    }
    return false;
  }

  @TargetApi(14)
  private boolean isStylusButtonPressed(MotionEvent event)
  {
    if (Build.VERSION.SDK_INT >= 14)
    {
      int buttonState = event.getButtonState();
      if ((buttonState & (MotionEvent.BUTTON_SECONDARY | MotionEvent.BUTTON_TERTIARY)) != 0)
      {
        return true;
      }
    }
    return false;
  }

}
