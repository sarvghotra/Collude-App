// Copyright MyScript

package com.myscript.atk.itc.sample.stroker;

import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;

import com.myscript.atk.itc.sample.model.VOPoint;

public interface Stroker
{
  public final static int PRESSURE_RAW = 0;
  public final static int PRESSURE_SIMULATED = 1;
  
  public void setPressureType(int pressureType);
  
  public void setColor(int color);
  public int getColor();
  
  public void setWidth(float width);
  public float getWidth();
  
  public Paint getPaint();
  public Path getPath();
  public VOPoint[] getPoints();
  public float[] getPointsX();
  public float[] getPointsY();
  
  public void reset();
  public void reset(float x, float y, float p, long t);
  public void addPoint(float x, float y, float p, long t);
  public void end(float x, float y, float p, long t);
  public void end();
  
  public RectF getBoundingRect();
  public RectF getDirtyRect();
  
  public void resetDirtyRect();
}
