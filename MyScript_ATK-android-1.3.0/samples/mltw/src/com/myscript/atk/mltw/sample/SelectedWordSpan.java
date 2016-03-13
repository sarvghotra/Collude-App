package com.myscript.atk.mltw.sample;

import android.text.TextPaint;
import android.text.style.CharacterStyle;
import android.text.style.UpdateAppearance;

/**
 * Span to highlight a selectd word.
 */
public class SelectedWordSpan extends CharacterStyle implements UpdateAppearance
{
  /** The highlight color. */
  private final int mColor;

  /**
   * Creates the span with the given color.
   * 
   * @param color
   *          The highlight color.
   */
  public SelectedWordSpan(final int color)
  {
    mColor = color;
  }

  @Override
  public void updateDrawState(final TextPaint ds)
  {
    ds.setColor(mColor);
  }
}