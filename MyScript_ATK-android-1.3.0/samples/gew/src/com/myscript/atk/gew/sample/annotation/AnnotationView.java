// Copyright MyScript. All right reserved.

package com.myscript.atk.gew.sample.annotation;

import com.myscript.atk.gew.GeometryWidgetApi;

import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

public class AnnotationView extends TextView
{
	private GeometryWidgetApi 					mGeometryWidget;		// The Geometry Widget holding the item being annotated
	private long 								mItemID;				// The ID of the item being annotated
	
    // Constructors
    public AnnotationView(Context context)
    {
      super(context);
    }
	
    public AnnotationView(Context context, GeometryWidgetApi geometryWidget, final long itemID)
    {
      super(context);

      mGeometryWidget = geometryWidget;
      mItemID = itemID;
      setClickable(true);
      setOnClickListener(new OnClickListener()
      {
		@Override
		public void onClick(View v)
		{
          // Try using SimpleOnGestureListener instead of onClick later
			
          if (mGeometryWidget != null)
            mGeometryWidget.select(mItemID);
		}
      });
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) 
    {
     // Call the superclass implementation
     return super.onTouchEvent(event);
    }
}
