// Copyright MyScript

package com.myscript.atk.itc.sample.view;

import java.util.HashMap;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import com.myscript.atk.itc.SmartWord;

public class TypesetSmartWordLayoutView extends View
{
  private HashMap<SmartWord, TypesetSmartWordView> mWordViews;

  private Rect mClipBounds;
  private Context mContext;

  public TypesetSmartWordLayoutView(Context context, AttributeSet attrs)
  {
    super(context, attrs);
    mWordViews = new HashMap<SmartWord, TypesetSmartWordView>();
    mClipBounds = new Rect();
    mContext = context;
  }

  public boolean contains(SmartWord word)
  {
    return mWordViews.containsKey(word);
  }

  public void addWord(SmartWord word)
  {
    TypesetSmartWordView v = new TypesetSmartWordView(word, mContext);
    mWordViews.put(word, v);

    invalidate(v.getFrame());
  }

  public void removeWord(SmartWord word)
  {
    TypesetSmartWordView v = mWordViews.remove(word);

    invalidate(v.getFrame());
  }

  public void invalidate(SmartWord word)
  {
    TypesetSmartWordView v = mWordViews.get(word);
    invalidate(v.getFrame());
  }

  @Override
  protected void onDraw(Canvas canvas)
  {
    super.onDraw(canvas);
    canvas.getClipBounds(mClipBounds);

    for (TypesetSmartWordView v : mWordViews.values())
      if (Rect.intersects(v.getFrame(), mClipBounds))
        v.draw(canvas);
  }
}
