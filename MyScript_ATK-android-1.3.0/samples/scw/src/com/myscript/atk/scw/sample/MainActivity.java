// Copyright MyScript

package com.myscript.atk.scw.sample;

import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.myscript.atk.scw.sample.util.SimpleResourceHelper;
import com.myscript.certificate.MyCertificate;
import com.myscript.atk.scw.CandidateInfo;
import com.myscript.atk.scw.SingleCharWidget;
import com.myscript.atk.scw.SingleCharWidgetApi.OnConfigureListener;
import com.myscript.atk.scw.SingleCharWidgetApi.OnGestureListener;
import com.myscript.atk.scw.SingleCharWidgetApi.OnRecognitionListener;
import com.myscript.atk.scw.SingleCharWidgetApi.OnTextChangedListener;
import com.myscript.atk.scw.sample.R;

public class MainActivity extends Activity implements
  OnTextChangedListener,
  OnGestureListener,
  OnConfigureListener,
  OnRecognitionListener,
  View.OnClickListener,
  View.OnFocusChangeListener,
  FieldView.OnSelectionChangedListener
{
  static {
    // Load StylusCore library when class is loaded
    System.loadLibrary("StylusCore");
  }

  private static final String TAG = "MainActivity";

  private static final String KEY_LANGUAGE = "language";

  private Handler               mHandler;
  private SimpleResourceHelper  mResourceHelper;
  private LanguageManager       mLanguageManager;
  
  private SingleCharWidget      mWidget;

  private ArrayList<FieldView>  mFields;
  private FieldView             mActiveField;

  private ArrayList<View>       mClickableViews;
  private int[]                 mLocation;
  private Button                mLangButton;
  private Button                mDeleteButton;
  private Button                mSpaceButton;
  private Button                mReturnButton;
  private LinearLayout          mCandidateLayout;
  
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    
    getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    
    mHandler = new Handler();
    mResourceHelper = new SimpleResourceHelper(this);
    mLanguageManager = new LanguageManager(this);
    
    mLangButton = (Button) findViewById(R.id.btn_lang);
    mDeleteButton = (Button) findViewById(R.id.btn_delete);
    mSpaceButton = (Button) findViewById(R.id.btn_space);
    mReturnButton = (Button) findViewById(R.id.btn_return);
    mCandidateLayout = (LinearLayout) findViewById(R.id.candidate_layout);
    
    updateLangButton(getLanguage());

    mWidget = (SingleCharWidget) findViewById(R.id.widget);
    //mWidget._setEnableInputDump(true);
    mWidget.setOnTextChangedListener(this);
    mWidget.setOnGestureListener(this);
    mWidget.setOnConfigureListener(this);
    mWidget.setOnRecognitionListener(this);

    final int[] fieldIds = {
        R.id.field1,
        R.id.field2,
        R.id.field3,
    };

    mFields = new ArrayList<FieldView>();
    for (int id : fieldIds) {
      FieldView field = (FieldView) findViewById(id);
      if (field != null) {
        mFields.add(field);
      }
    }

    for (FieldView field : mFields) {
      field.setOnFocusChangeListener(this);
      field.setOnSelectionChangedListener(this);
    }
    
    mActiveField = mFields.get(0);
    mActiveField.requestFocus();
    
    if (savedInstanceState != null) {
      ArrayList<String> texts = savedInstanceState.getStringArrayList("texts");
      if (texts != null) {
        for (int i=0; i<mFields.size() && i<texts.size(); i++) {
          mFields.get(i).setText(texts.get(i));
        }
      }
    }

    mWidget.setText(mActiveField.getText().toString());
    
// Uncomment the following lines to configure ink marker effect

//    mWidget.setInkCapResources(R.drawable.ink_cap, R.drawable.ink_cap);
//    mWidget.setInkEffect(SingleCharWidget.INK_EFFECT_MARKER);

    mClickableViews = new ArrayList<View>();
    mClickableViews.addAll(mFields);
    mClickableViews.add(mLangButton);
    mClickableViews.add(mDeleteButton);
    mClickableViews.add(mSpaceButton);
    mClickableViews.add(mReturnButton);

    mLocation = new int[2];

    mLangButton.setOnClickListener(this);
    mDeleteButton.setOnClickListener(this);
    mSpaceButton.setOnClickListener(this);
    mReturnButton.setOnClickListener(this);
    
    configure(true);

    setTitle(getResources().getString(R.string.app_name));
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.main, menu);
    return true;
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    ArrayList<String> texts = new ArrayList<String>();
    for (FieldView field : mFields) {
      texts.add(field.getText().toString());
    }
    outState.putStringArrayList("texts", texts);
  }

  // ----------------------------------------------------------------------
  
  private String getLanguage() {
    return getSharedPreferences(KEY_LANGUAGE, MODE_PRIVATE).getString(KEY_LANGUAGE, mLanguageManager.getDefaultLanguage());
  }
  
  private void setLanguage(String language) {
    SharedPreferences.Editor editor = getSharedPreferences(KEY_LANGUAGE, MODE_PRIVATE).edit();
    editor.putString(KEY_LANGUAGE, language);
    editor.commit();
    updateLangButton(language);
  }
  
  private void configure(boolean now) {
    mHandler.removeCallbacks(mConfigureCallback);

    if (now) {
      mConfigureCallback.run();
    } else {
      mHandler.postDelayed(mConfigureCallback, 300);
    }
  }

  private Runnable mConfigureCallback = new Runnable() {
    @Override
    public void run() {
      String language = getLanguage();
      
      Log.d(TAG, "Configuring language to " + language);
      
      String[] resources = LanguageManager.getResourcesForLanguage(language, mActiveField.getInputType());
      
      for (int i=0; i<resources.length; i++) {
        Log.d(TAG, String.format("resources[%d]=\"%s\"", i, resources[i]));
      }
      
      String[] paths = mResourceHelper.getResourcePaths(resources);

      mWidget.setGesturesEnabled(!LanguageManager.isCJK(language));

      mWidget.configure(language, paths, MyCertificate.getBytes());
    }
  };

  // ----------------------------------------------------------------------

  private void onSpaceButtonClicked(View v) {
    Log.d(TAG, "Space button clicked");

    int index = mWidget.getInsertIndex();
    mWidget.replaceCharacters(index, index, " ");
  }

  private void onDeleteButtonClicked(View v) {
    Log.d(TAG, "Delete button clicked");

    String text = mWidget.getText();
    int index = mWidget.getInsertIndex();
    if (text.isEmpty()) {
      Log.d(TAG, "Widget text is empty, canceling");
      return;
    }
    if (index == 0) {
      Log.d(TAG, "Widget insert index at start of text, canceling");
      return;
    }

    BreakIterator bi = BreakIterator.getCharacterInstance();
    bi.setText(text);

    int start;
    int end;
    if (index == text.length()) {
      end = bi.last();
      start = bi.previous();
    } else if (bi.isBoundary(index)) {
      end = index;
      start = bi.preceding(index);
    } else {
      end = bi.following(index);
      start = bi.preceding(index);
    }

    mWidget.replaceCharacters(start, end, null);
  }

  private void onReturnButtonClicked(View v) {
    Log.d(TAG, "Return button clicked");

    int index = mFields.indexOf(mActiveField);
    if (index != -1) {
      if (index == mFields.size() - 1) {
        mActiveField = mFields.get(0);
      } else {
        mActiveField = mFields.get(index + 1);
      }
    }

    mActiveField.requestFocus();
    mActiveField.setSelection(mActiveField.getText().length());
  }

  private void onLangButtonClicked(View v) {
    Log.d(TAG, "Language button clicked");
    
    // update language setting immediately
    String language = getLanguage();
    setLanguage(mLanguageManager.getNextLanguage(language));
    
    // delay widget configuration in case user is cycling rapidly through languages
    configure(false);
  }
  
  @Override
  public void onClick(View v) {
    if (v == mLangButton) {
      onLangButtonClicked(v);
    } else if (v == mDeleteButton) {
      onDeleteButtonClicked(v);
    } else if (v == mSpaceButton) {
      onSpaceButtonClicked(v);
    } else if (v == mReturnButton) {
      onReturnButtonClicked(v);
    }
  }

  @Override
  public void onFocusChange(View v, boolean hasFocus) {
    if (hasFocus) {
      mActiveField = (FieldView) v;

      mWidget.setText(mActiveField.getText().toString());
      // configure the widget again because data format resources may have changed
      configure(false);
    }
  }

  @Override
  public void onSelectionChanged(View v, int selStart, int selEnd) {
    if (v.hasFocus()) {
      int index = (selStart + selEnd) / 2;
      mWidget.setInsertIndex(index);
      updateCandidates(index - 1);
    }
  }

  // ----------------------------------------------------------------------

  @Override
  public void onRecognitionBegin() {
    Log.d(TAG, "Handwriting recognition begin");    
  }

  @Override
  public void onRecognitionEnd() {
    Log.d(TAG, "Handwriting recognition end");
  }

  @Override
  public void onConfigureBegin() {
    Log.d(TAG, "Handwriting configuration begin");
  }

  @Override
  public void onConfigureEnd(boolean success) {
    Log.d(TAG, "Handwriting configuration end (" + (success ? "success" : "failure") + ")");

    String text;
    if (success) {
      text = String.format("Language set to %s (%s)", getLanguage(), mActiveField.getHint());
    } else {
      text = String.format("Error setting language %s. The languages are installed correctly?", getLanguage());
    }

    Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
  }

  @Override
  public boolean onSingleTapGesture(float x, float y) {
    mWidget.getLocationOnScreen(mLocation);

    x += mLocation[0];
    y += mLocation[1];
    
    List<View> views = new ArrayList<View>(mClickableViews);
    for (int i=0; i<mCandidateLayout.getChildCount(); i++) {
      views.add(mCandidateLayout.getChildAt(i));
    }

    for (View v : views) {
      if (v != null) {
        v.getLocationOnScreen(mLocation);

        if (mLocation[0] < x && x < mLocation[0] + v.getWidth() && mLocation[1] < y && y < mLocation[1] + v.getHeight()) {
          if (v instanceof FieldView) {
            v.requestFocus();
          } else {
            v.performClick();
          }
          return true;
        }
      }
    }

    return false;
  }

  @Override
  public void onBackspaceGesture(int count) {
    Log.d(TAG, "Backspace gesture detected, count=" + count);

    Toast.makeText(this, "Backspace gesture detected (" + count + ")", Toast.LENGTH_SHORT).show();
    
    // simulate one or more taps on the delete button
    for (int i=0; i<count; i++) {
      mDeleteButton.performClick();
    }
  }

  @Override
  public void onReturnGesture() {
    Log.d(TAG, "Return gesture detected");

    Toast.makeText(this, "Return gesture detected", Toast.LENGTH_SHORT).show();
    
    // simulate a tap on the return button
    mReturnButton.performClick();
  }

  @Override
  public void onPinchGesture(float x, float y) {
    Log.d(TAG, "Pinch gesture detected, x=" + x + " y=" + y);

    Toast.makeText(this, "Pinch gesture detected", Toast.LENGTH_SHORT).show();
  }
  
  @Override
  public boolean onLongPressGesture(float x, float y) {
    Log.d(TAG, "Long press gesture detected, x=" + x + " y=" + y);
    
    Toast.makeText(this, "Long press gesture detected, x=" + x + " y=" + y, Toast.LENGTH_SHORT).show();

    return false;
  }

  @Override
  public void onTextChanged(String text, boolean intermediate) {
    Log.d(TAG, "Text changed text=\"" + text + "\", intermediate=" + intermediate);

    if (mActiveField != null) {
      // calling setText() on an EditText makes the cursor jump to the beginning of the text
      // we prevent this unwanted behavior by temporarily unregistering the selection changed listener
      mActiveField.setOnSelectionChangedListener(null);
      mActiveField.setText(text);
      mActiveField.setOnSelectionChangedListener(this);
      mActiveField.setSelection(mWidget.getInsertIndex());
    }

    updateCandidates(mWidget.getInsertIndex() - 1);
  }

  // ----------------------------------------------------------------------
  
  private void updateLangButton(final String language) {
    mLangButton.setText(language.split("_")[0]);
  }

  private void updateCandidates(int index) {
    final CandidateInfo info;

    info = mWidget.getWordCandidates(index);
    
    if (info == null) {
      Log.d(TAG, "clear candidates");
    } else {
      Log.d(TAG, "range start=" + info.start);
      Log.d(TAG, "range end=" + info.end);
      for (int i=0; i<info.labels.length; i++) {
        Log.d(TAG, "candidate[" + i + "]=\"" + info.labels[i] + "|" + info.completions[i] + "\"" + (i == info.selectedIndex ? " (*)" : ""));
      }
    }

    if (mCandidateLayout != null) {
      mCandidateLayout.removeAllViews();

      int minWidth = getResources().getDimensionPixelSize(R.dimen.candidate_width_min);
      int labelColor = getResources().getColor(R.color.candidate_label);
      int completionColor = getResources().getColor(R.color.candidate_completion);

      if (info != null) {
        Paint paint = new Paint();

        for (int i=0; i<info.labels.length; i++) {
          String label = replaceInvisibleCharacters(info.labels[i]);
          String completion = replaceInvisibleCharacters(info.completions[i]);

          SpannableStringBuilder sb = new SpannableStringBuilder();
          sb.append(label);
          sb.append(completion);
          sb.setSpan(new ForegroundColorSpan(labelColor), 0, label.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
          sb.setSpan(new ForegroundColorSpan(completionColor), label.length(), label.length() + completion.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);

          if (paint.measureText(sb.toString()) == 0) {
            Log.d(TAG, "Filtered out undisplayable candidate \"" + label + "|" + completion + "\"");
            continue;
          }
          
          Button btn = new Button(this);
          btn.setMinimumWidth(minWidth);
          btn.setTag(i);
          btn.setText(sb);
          btn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
              int i = (Integer) v.getTag();
              Log.d(TAG, "Candidate clicked label=\"" + info.labels[i] + "\", completion=\"" + info.completions[i] + "\"");
              mWidget.replaceCharacters(info.start, info.end, info.labels[i] + info.completions[i]);
            }
          });
          mCandidateLayout.addView(btn);
        }
      }
    }
  }

  private String replaceInvisibleCharacters(String text) {
    return text
      .replace(" ", "\u2423")   // replace spaces
      .replace("\n", "\u00B6")  // replace carriage returns
      .replace("\uDB80\uDC04", "\u21B2")  // replace down-then-left gestures
      .replace("\uDB80\uDC08", "\u21B3")  // replace down-then-right gestures
      .replace("\uDB80\uDC03", "\u2192")  // replace left-to-right gestures
      .replace("\uDB80\uDC02", "\u2190")  // replace right-to-left gestures
      ;
  }

  @Override
  protected void onResume() {
    super.onResume();
    mActiveField.setSelection(mActiveField.getText().length());
  }
}
