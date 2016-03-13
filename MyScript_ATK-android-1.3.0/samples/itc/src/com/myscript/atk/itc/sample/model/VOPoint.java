package com.myscript.atk.itc.sample.model;

public class VOPoint
{
  // x coordinate
  public float x;
  // y coordinate
  public float y;
  // pressure value
  public float p;
  
  public VOPoint()
  {
    this.x = 0;
    this.y = 0;
    this.p = 0;
  }

  public VOPoint(float x, float y, float p)
  {
    this.x = x;
    this.y = y;
    this.p = p;
  }

  public VOPoint(VOPoint point)
  {
    this.x = point.x;
    this.y = point.y;
    this.p = point.p;
  }

  public boolean equals(VOPoint other)
  {
    return other.x == x && other.y == y && other.p == p;
  }

  @Override
  public String toString()
  {
    return new StringBuilder()
    .append("x: ").append(x).append(" ")
    .append("y: ").append(y).append(" ")
    .append("p: ").append(p)
    .toString();
  }
}
