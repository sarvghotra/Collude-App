// Copyright MyScript. All right reserved.

package com.myscript.atk.gew.sample.utils;

import android.content.Context;
import android.util.TypedValue;

public abstract class Utils
{
  public static float centimetersToPoints(Context context, float centimeters)
  {
    // Convert back to points    
    float pixelsPerCentimeter = 10.f * TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_MM, 1, context.getResources().getDisplayMetrics());
    
    return (centimeters * pixelsPerCentimeter);
  }
  
  public static float pointsToCentimeters(Context context, float points)
  {
    // Screen resolution
    float pixelsPerCentimeter = 10.f * TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_MM, 1, context.getResources().getDisplayMetrics());
    
    // Convert to centimeters
    return (points / pixelsPerCentimeter);
  }
}
