// Copyright MyScript

package com.myscript.atk.itc.sample.stroker;

import java.util.ArrayList;

import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.Log;

import com.myscript.atk.itc.sample.Debug;
import com.myscript.atk.itc.sample.model.VOPoint;

public class SimpleStroker implements Stroker {

  private final static String TAG = "SimpleStroker";
  private int                   mColor;
  private float                 mWidth;
  private float                 mLastX;
  private float                 mLastY;
  private float[]           mX;
  private float[]           mY;
  private float                 mLastMidX;
  private float                 mLastMidY;
  private Paint                 mPaintStroke;
  private Paint                 mPaintPoint;
  private Path                  mPath;
  private RectF                 mBoundingRect;
  private RectF                 mBoundingRectRaw;   // bounding rectangle without stroke width
  private Path                  mDirtyPath;
  private RectF                 mDirtyRect;
  private RectF                 mDirtyRectRaw;      // dirty rectangle without stroke width
  private ArrayList<VOPoint>    mPoints;
  private final static int STROKE_MAX_POINTS = 2000;

  public SimpleStroker() {
    mPaintStroke = new Paint();
    mPaintStroke.setStyle(Paint.Style.STROKE);
    mPaintStroke.setStrokeCap(Paint.Cap.ROUND);
    mPaintStroke.setStrokeJoin(Paint.Join.ROUND);
    mPaintStroke.setAntiAlias(true);
    
    mPaintPoint = new Paint();
    mPaintPoint.setStyle(Paint.Style.FILL);
    mPaintPoint.setAntiAlias(true);
    
    mPath = new Path();
    mBoundingRect = new RectF();
    mBoundingRectRaw = new RectF();
    mDirtyPath = new Path();
    mDirtyRect = new RectF();
    mDirtyRectRaw = new RectF();
    mPoints = new ArrayList<VOPoint>();
    mX = null;
    mY = null;
  }
  
  // --------------------------------------------------------------------------------
  // Stroker configuration

  @Override
  public void setPressureType(int pressureType)
  {
    // nothing to do
  }
  
  @Override
  public void setColor(int color) {
    mPaintStroke.setColor(color);
    mPaintPoint.setColor(color);
    mColor = color;
  }

  @Override
  public int getColor() {
    return mColor;
  }
  
  @Override
  public void setWidth(float width) {
    mPaintStroke.setStrokeWidth(width);
    mWidth = width;
  }

  @Override
  public float getWidth() {
    return mWidth;
  }

  @Override
  public Paint getPaint() {
    if (mPoints.size() == 1) {
      return mPaintPoint;
    } else {
      return mPaintStroke;
    }
  }

  @Override
  public Path getPath() {
    return mPath;
  }

  @Override
  public VOPoint[] getPoints() {
    normalizedPoints();
    return mPoints.toArray(new VOPoint[mPoints.size()]);
  }
  
  @Override
  public float[] getPointsX()
  {
    if (mX != null)
      return mX;

    // Extract x and y coordinates in cache list
    cachePointsCoordinate();

    return mX;
  }

  @Override
  public float[] getPointsY()
  {
    if (mY != null)
      return mY;

    // Extract x and y coordinates in cache list
    cachePointsCoordinate();

    return mY;
  }

  // --------------------------------------------------------------------------------
  // Stroker operations
  
  @Override
  public void reset() {
    mPoints.clear();
    mX = null;
    mY = null;
    mPath.reset();
    mBoundingRectRaw.setEmpty();
    
    mDirtyPath.reset();
    mDirtyRectRaw.setEmpty();
  }
  
  @Override
  public void reset(float x, float y, float p, long t) {
  	reset();
  	
    mPoints.clear();
    mX = null;
    mY = null;
    mPoints.add(new VOPoint(x, y, p));
    
    mPath.moveTo(x, y);
    mBoundingRectRaw.set(x, y, x, y);
    
    mDirtyPath.moveTo(x, y);
    mDirtyRectRaw.set(x, y, x, y);
    
    mLastX = x;
    mLastY = y;
    mLastMidX = x;
    mLastMidY = y;
  }

  @Override
  public void addPoint(float x, float y, float p, long t) {
    if (x != mLastX || y != mLastY) {
      mPoints.add(new VOPoint(x, y, p));
      
      float xi = (mLastX + x) / 2;
      float yi = (mLastY + y) / 2;
      
      mPath.quadTo(mLastX, mLastY, xi, yi);
      mBoundingRectRaw.union(xi, yi);
      mBoundingRectRaw.union(mLastX, mLastY);

      mDirtyPath.quadTo(mLastX, mLastY, xi, yi);
      mDirtyRectRaw.union(xi, yi);
      mDirtyRectRaw.union(mLastX, mLastY);

      mLastX = x;
      mLastY = y;
      mLastMidX = xi;
      mLastMidY = yi;
    }
  }

  @Override
  public void end() {
    if (isPointSize()) {
      setPointPath();
    }
  }
  
  @Override
  public void end(float x, float y, float p, long t) {
    if (x != mLastX || y != mLastY) {
      mPoints.add(new VOPoint(x, y, p));
      
      mPath.lineTo(x, y);
      mBoundingRectRaw.union(x, y);

      mDirtyPath.lineTo(x, y);
      mDirtyRectRaw.union(x, y);
    }
    
    if (isPointSize()) {
      setPointPath();
    }
  }
  
  // --------------------------------------------------------------------------------
  // Point detection and path

  private boolean isPointSize()
  {
    float r = mWidth / 2;
    return (mBoundingRectRaw.width() < r && mBoundingRectRaw.height() < r);
  }
  
  private void setPointPath()
  {
    float r = mWidth / 2;
    
    VOPoint point = mPoints.get(0);
    float xi = point.x;
    float yi = point.y;

    mPath.reset();
    mPath.addCircle(xi, yi, r, Path.Direction.CCW);
    mBoundingRectRaw.set(xi-r, yi-r, xi+r, yi+r);
    
    mDirtyPath.reset();
    mDirtyPath.addCircle(xi, yi, r, Path.Direction.CCW);
    mDirtyRectRaw.set(xi-r, yi-r, xi+r, yi+r);
  }

  // --------------------------------------------------------------------------------
  // Bounding rectangles

  @Override
  public RectF getBoundingRect() {
    float r = mWidth / 2;
    mBoundingRect.set(mBoundingRectRaw);
    mBoundingRect.inset(-r, -r);
    return mBoundingRect;
  }
  
  @Override
  public RectF getDirtyRect() {
    float r = mWidth / 2;
    mDirtyRect.set(mDirtyRectRaw);
    mDirtyRect.inset(-r, -r);
    return mDirtyRect;
  }
  
  @Override
  public void resetDirtyRect() {
    mDirtyPath.reset();
    mDirtyPath.moveTo(mLastMidX, mLastMidY);
    mDirtyRectRaw.set(mLastMidX, mLastMidY, mLastMidX, mLastMidY);
  }

  private void normalizedPoints()
  {
    final int nbPoints = mPoints.size();
    if (nbPoints >= STROKE_MAX_POINTS)
    {
      if (Debug.DBG)
        Log.i(TAG, "Stroke too large (" + nbPoints + " points)");

      final ArrayList<VOPoint> newPoints = new ArrayList<VOPoint>();
      final int modulo = (nbPoints / STROKE_MAX_POINTS) + 1;
      // Adding one point every modulo
      for (int i = 0; i < nbPoints; i++)
      {
        if (i % modulo == 0)
          newPoints.add(mPoints.get(i));
      }
      // Always adding last point
      if ((nbPoints % modulo) != 0)
        newPoints.add(mPoints.get(nbPoints - 1));
      mPoints = newPoints;
      mX = null;
      mY = null;

      if (Debug.DBG)
        Log.i("VOUserParamsFactory", "Resampled stroke is now " + mPoints.size() + " points large");
    }
  }

  private void cachePointsCoordinate()
  {
    // Fill an array with x and y coordinates
    int size = mPoints.size();
    mX = new float[size];
    mY = new float[size];
    for (int i = size; --i >= 0; ) {
      VOPoint point = mPoints.get(i);
      mX[i] = point.x;
      mY[i] = point.y;
    }
  }
}
