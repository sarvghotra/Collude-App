// Copyright MyScript. All right reserved.

package com.myscript.atk.gew.sample.preferences;


import com.myscript.atk.gew.GeometryWidgetApi;
import com.myscript.atk.gew.sample.R;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.TypedValue;
import android.view.MenuItem;

/**
 * Preferences activity.
 */
public class PreferencesActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener
{
  /** Default settings */
  public final static int 		DEFAULT_THICKNESS_INDEX 		= 1;
  public static final int 		DEFAULT_INK_THICKNESS 			= 2;
  public static final String 	DEFAULT_INK_COLOR 				= "#FF4B5775";
  public static final boolean 	DEFAULT_INK_DASHED 				= false;

  @SuppressWarnings("deprecation")
  @Override
  public void onCreate(final Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.settings);

    final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    pref.registerOnSharedPreferenceChangeListener(this);

    addPreferencesFromResource(R.xml.prefs);

    // Initialize custom preferences
    initKeyValuePref(pref, R.string.pref_ink_thickness_key, R.array.thicknessNames, String.valueOf(DEFAULT_INK_THICKNESS));
  }
 
  /**
   * Set preference option.
   * 
   * @param sharedPref
   *          The SharedPreferences
   * @param prefKey
   *          The preference key
   * @param namesArrayId
   *          The names array
   * @param defaultValue
   *          The default value in namesArrayId values
   */
  private void initKeyValuePref(final SharedPreferences sharedPref, final int prefKey, final int namesArrayId,
      final String defaultValue)
  {
    final String value = sharedPref.getString(getString(prefKey), defaultValue);

    @SuppressWarnings("deprecation")
	final Preference pref = findPreference(getString(prefKey));
    setSummary(pref, namesArrayId, value);

    pref.setOnPreferenceChangeListener(new OnPreferenceChangeListener()
    {
      @Override
      public boolean onPreferenceChange(final Preference preference, final Object newValue)
      {
        setSummary(preference, namesArrayId, newValue);
        return true;
      }
    });
  }

  private void setSummary(final Preference preference, final int namesArrayId, final Object value)
  {
    if (value instanceof String)
    {
      final String[] names = getResources().getStringArray(namesArrayId);
      final int intValue = Integer.parseInt((String) value);
      final int index = intValue - 1;

      if (index < names.length && index > -1)
        preference.setSummary(names[index]);
    }
  }

  @Override
  protected void onResume()
  {
    super.onResume();
  }

  @Override
  protected void onPause()
  {
    super.onPause();
  }

  @Override
  public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
  {
  }
  
  @Override
  public boolean onOptionsItemSelected(final MenuItem item)
  {
    return super.onOptionsItemSelected(item);
  }
  
  public static void onSharedPreferenceChanged(Context context, GeometryWidgetApi geometryWidget, final SharedPreferences sharedPreferences, final String key)
  {
    if (key.equals(context.getResources().getString(R.string.pref_ink_color_key)))
    {
      // Load ink color settings
      final int inkColor = Color.parseColor(sharedPreferences.getString(
    		  context.getResources().getString(R.string.pref_ink_color_key), DEFAULT_INK_COLOR));
      geometryWidget.setInkColor(inkColor);
    }
    else if (key.equals(context.getResources().getString(R.string.pref_ink_thickness_key)))
    {
      // Change ink thickness
      final int inkThickness = Integer.parseInt(sharedPreferences.getString(
    		  context.getResources().getString(R.string.pref_ink_thickness_key), Integer.toString(DEFAULT_INK_THICKNESS)));
      geometryWidget.setInkThickness(inkThickness);
    }
    else if (key.equals(context.getResources().getString(R.string.pref_ink_dashed_key)))
    {
      // Load ink dashed settings
      final boolean inkDashed = sharedPreferences.getBoolean(context.getResources().getString(R.string.pref_ink_dashed_key), DEFAULT_INK_DASHED);
      geometryWidget.setInkDashed(inkDashed);
    }
  }
  
  public static void loadPreferences(Context context, GeometryWidgetApi geometryWidget, final SharedPreferences sharedPreferences)
  {
	// Apply current settings (default values are component values)
	Editor editor = sharedPreferences.edit();
	  
	// Load ink color settings
	final int inkColor = Color.parseColor(sharedPreferences.getString(
			context.getResources().getString(R.string.pref_ink_color_key), DEFAULT_INK_COLOR));
	geometryWidget.setInkColor(inkColor);

  // Compute dots per millimeter for this screen
  float dotsPerMillimeterForThisDevice = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_MM, 1, context.getResources().getDisplayMetrics());

  // Change ink thickness
  int inkThickness = Integer.parseInt(sharedPreferences.getString(
      context.getResources().getString(R.string.pref_ink_thickness_key), Integer.toString(DEFAULT_INK_THICKNESS)));
    inkThickness = (int)((inkThickness*dotsPerMillimeterForThisDevice/6.f) + 0.5f); 
  geometryWidget.setInkThickness(inkThickness);

	// Load ink dashed settings
	final boolean inkDashed = sharedPreferences.getBoolean(context.getResources().getString(R.string.pref_ink_dashed_key), DEFAULT_INK_DASHED);
	geometryWidget.setInkDashed(inkDashed);
	
	editor.commit();
  }
}
