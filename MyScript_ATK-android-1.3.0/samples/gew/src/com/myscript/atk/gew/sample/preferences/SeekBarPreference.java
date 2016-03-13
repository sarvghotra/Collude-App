// Copyright MyScript. All right reserved.

package com.myscript.atk.gew.sample.preferences;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

import android.content.Context;
import android.content.DialogInterface;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout.LayoutParams;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

public class SeekBarPreference extends
		DialogPreference implements SeekBar.OnSeekBarChangeListener
{
	// Views
	private SeekBar mSeekBarView = null;
	private TextView mTextView = null;

	// Scaling
	public static final int SCALING = 10;
	
	// Properties
	private int mValue = 0 * SCALING;
	private int mMinimum = 0 * SCALING;
	private int mMaximum = 10 * SCALING;
	private int mStep = 1 /* 0.1 * SCALING */;
	
	public SeekBarPreference(Context context)
	{
	  this(context, null);
	}
	
	public SeekBarPreference(Context context, AttributeSet attrs)
	{
	  super(context, attrs);
	}
	
	public void configure(final int minimum, final int maximum, final int step)
	{
	  mMinimum = minimum * SCALING;
	  mMaximum = maximum * SCALING;
	  mStep = step * SCALING;
	}
	
	protected View onCreateDialogView()
	{
      // Create dialog content
      LinearLayout hLayout = new LinearLayout(getContext());
      hLayout.setOrientation(LinearLayout.HORIZONTAL);
      LinearLayout.LayoutParams lphLayout = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 1.0f);
      hLayout.setLayoutParams(lphLayout);
      
      mSeekBarView = new SeekBar(getContext());
      LinearLayout.LayoutParams lpSeekBar = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 0.30f);
      lpSeekBar.setMargins(10, 20, lpSeekBar.rightMargin, 20);
      mSeekBarView.setLayoutParams(lpSeekBar);
      
      mTextView = new TextView(getContext());
      mTextView.setGravity(Gravity.CENTER);
      LinearLayout.LayoutParams lpTextView = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 0.70f);
      lpTextView.setMargins(lpTextView.leftMargin, 20, lpTextView.rightMargin, 20);
      mTextView.setLayoutParams(lpTextView);
      mTextView.setTextAppearance(getContext(), android.R.style.TextAppearance_Medium);
      
      hLayout.addView(mSeekBarView);
      hLayout.addView(mTextView);

	  // Initialize values
	  mValue = getPersistedInt(mMinimum);

	  if ((mValue < mMinimum) || (mValue > mMaximum))
        mValue = Math.round(((mMaximum - mMinimum) / 2) / mStep) * mStep;
	  else
		  mValue -= mMinimum;
	  
	  mSeekBarView.setKeyProgressIncrement(mStep);
	  mSeekBarView.setMax(mMaximum - mMinimum);
	  mSeekBarView.setOnSeekBarChangeListener(this);	// Must be done after setMax
	  mSeekBarView.setProgress(mValue);

      return hLayout;
	}

	public void onProgressChanged(SeekBar seek, int newValue, boolean fromTouch)
	{
	  if (mStep >= 1)
	    mValue = Math.round(newValue);
	  else
		mValue = newValue;

	  // Truncate to 2 decimals
	  final float value = (float) (mValue + mMinimum) / (float) SCALING;
	  DecimalFormatSymbols symbols = new DecimalFormatSymbols();
	  symbols.setDecimalSeparator('.');
	  DecimalFormat format = new DecimalFormat("#.##");
	  format.setDecimalFormatSymbols(symbols);
	  final String label = format.format(value);
	  mTextView.setText(label);

	  // Notify preference changed from user
	  callChangeListener(mValue);
	}
		
	public void onClick(DialogInterface dialog, int which)
	{
	  if (which == DialogInterface.BUTTON_POSITIVE)
	  {
		if (shouldPersist())
		  persistInt(mValue + mMinimum);
 	  }

	  super.onClick(dialog, which);
	}
	
	public void onStartTrackingTouch(SeekBar seekBar)
	{
	}
	
	public void onStopTrackingTouch(SeekBar seekBar)
	{
	}

	public static int scaledValue(final float value)
	{
      return Math.round(value * ((float) SCALING));
	}
	
	public static float unscaledValue(final int value)
	{
      return ((float) value) / ((float) SCALING);
	}
}
