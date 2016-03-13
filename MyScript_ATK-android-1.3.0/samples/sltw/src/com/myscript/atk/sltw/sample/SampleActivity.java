// Copyright MyScript

package com.myscript.atk.sltw.sample;

import java.util.Locale;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.myscript.atk.sltw.SingleLineTextWidget;
import com.myscript.atk.sltw.SingleLineTextWidgetApi;
import com.myscript.atk.sltw.SingleLineTextWidgetStyleable;
import com.myscript.atk.sltw.sample.controller.CandidateBarController;
import com.myscript.atk.sltw.sample.controller.ToolbarController;
import com.myscript.atk.sltw.sample.reco.BoxModeBehavior;
import com.myscript.atk.sltw.sample.reco.CursiveBehavior;
import com.myscript.atk.sltw.sample.reco.IsolatedBehavior;
import com.myscript.atk.sltw.sample.reco.RecognitionBehavior;
import com.myscript.atk.sltw.sample.util.SimpleResourceHelper;
import com.myscript.atk.sltw.sample.view.CustomEditText;
import com.myscript.certificate.MyCertificate;

/** This class demonstrates how to integrate all features of the {@link SingleLineTextWidgetStyleable} widget in a single application. */
public class SampleActivity extends Activity implements
SingleLineTextWidgetApi.OnConfigureListener,
SingleLineTextWidgetApi.OnRecognitionListener,
SingleLineTextWidgetApi.OnCursorHandleDragListener,
SingleLineTextWidgetApi.OnInsertHandleDragListener,
SingleLineTextWidgetApi.OnInsertHandleClickedListener,
SingleLineTextWidgetApi.OnGestureListener,
SingleLineTextWidgetApi.OnUserScrollListener,
SingleLineTextWidgetApi.OnInsertionWindowListener,
ToolbarController.OnModeButtonClickedListener
{
  // load the StylusCore library when this class is loaded
  static {

    // you can either load the library by name
    // Android will search the default paths for a dynamic library with file name libStylusCore.so
    System.loadLibrary("StylusCore");
    
    // or you can load the library by specifying a full path
    // this is useful when you want to load a specific version of the StylusCore library from the filesystem
    //System.load("/data/data/com.myscript.atk.sltw.sample/lib/libStylusCore.so");
  }

  private static final String TAG = "SampleActivity";
  
  private CustomEditText            mEditText;
  private ToolbarController         mToolbarController;
  private CandidateBarController    mCandidateBarController;

  private SingleLineTextWidget mWidget;
  
  private CursiveBehavior           mCursiveBehavior;
  private IsolatedBehavior          mIsolatedBehavior;
  private BoxModeBehavior           mBoxModeBehavior;

  @TargetApi(11)
  @Override
  protected void onCreate(final Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.sltw_main);

    mEditText = (CustomEditText) findViewById(R.id.textField);
    
    mToolbarController = new ToolbarController(findViewById(R.id.sltw_toolbar));
    mToolbarController.setOnModeButtonClickedListener(this);

    mCandidateBarController = new CandidateBarController(findViewById(R.id.sltw_candidatebar));
    
    mWidget = (SingleLineTextWidget) findViewById(R.id.sltw_text_widget);
    mWidget.setOnConfigureListener(this);
    mWidget.setOnRecognitionListener(this);
    mWidget.setOnCursorHandleDragListener(this);
    mWidget.setOnInsertHandleDragListener(this);
    mWidget.setOnInsertHandleClickedListener(this);
    mWidget.setOnGestureListener(this);
    mWidget.setOnUserScrollListener(this);
    mWidget.setOnInsertionWindowListener(this);
    
    // hovering functionality is disabled by default
    mWidget.setHoverEnabled(true);

    mCursiveBehavior = new CursiveBehavior(mWidget, mEditText, mCandidateBarController);
    mIsolatedBehavior = new IsolatedBehavior(mWidget, mEditText, mCandidateBarController);
    mBoxModeBehavior = new BoxModeBehavior(mWidget, mEditText, mCandidateBarController, this);

    configure(mToolbarController.getMode());
  }
  
  @Override
  protected void onResume()
  {
    super.onResume();
    
    // pipe current text of field to the widget
    final Editable editable = mEditText.getText();
    if(editable != null)
      mWidget.setText(editable.toString());
    else
      mWidget.setText("");
    // set insertion mode at the end of the text
    mWidget.setInsertionMode(mEditText.getText().length());
    // place the cursor at the end of the text
    mEditText.setCursorIndex(mEditText.getText().length());
  }
  
  // ----------------------------------------------------------------------
  // Handwriting recognition configuration
  
  private void configure(int mode)
  {
    SimpleResourceHelper helper = new SimpleResourceHelper(this);

    String[] resources;
    
    if (mode == ToolbarController.BOX_MODE)
        resources = new String[]{"en_US/en_US-ak-iso.lite.res"};
    else {
        resources = new String[] {
                "en_US/en_US-ak-cur.lite.res",
                "en_US/en_US-lk-text.lite.res"
        };
    }
    String[] paths = helper.getResourcePaths(resources);
      
    String[] lexicon = null;
    //no lexicon in password mode, it affect recognition result. And it may not be wanted to write password.
    if (mode == ToolbarController.CURSIVE_MODE)
    {
        lexicon = new String[] {
        // add your user dictionary here
      };
    }

    boolean wasIsolatedMode = mWidget.isIsolatedMode();
    
    long startTime = System.currentTimeMillis();
    mWidget.configure("en_US", paths, lexicon, MyCertificate.getBytes());
    long endTime = System.currentTimeMillis();

    Log.d(TAG, "configure() API processing time=" + (endTime - startTime) + "ms, mode=" + (mode == ToolbarController.BOX_MODE ? "Box" : "Cursive"));
    
    // clear writing area if switching from or to isolated mode

    if (wasIsolatedMode || mWidget.isIsolatedMode()) {
      mEditText.setOnCursorIndexChangedListener(null);
      mEditText.setText("");
      mCandidateBarController.clearLabels();
    }

    setRecognitionBehavior();
  }

  /** Configure recognition behavior (cursive or isolated). */
  private void setRecognitionBehavior()
  {
    RecognitionBehavior behavior;

    if (mWidget.isIsolatedMode() && mToolbarController.getMode() == ToolbarController.BOX_MODE)
    {
      behavior = mBoxModeBehavior;
      mBoxModeBehavior.computeBoxSize("en_US");
      mWidget.setAutoScrollEnabled(false);
      mWidget.setAutoTypesetEnabled(true);
      mWidget.setScrollbarResource(R.drawable.sltw_scrollbar_xml);
      mWidget.setScrollArrowLeftResource(R.drawable.sltw_arrowleft_xml);
      mWidget.setScrollArrowRightResource(R.drawable.sltw_arrowright_xml);
    }
    else if (mWidget.isIsolatedMode())
    {
      behavior = mIsolatedBehavior;
      mWidget.setAutoScrollEnabled(false);
      mWidget.setAutoTypesetEnabled(false);
      mWidget.setScrollbarResource(0);
      mWidget.setScrollArrowLeftResource(0);
      mWidget.setScrollArrowRightResource(0);
    }
    else
    {
      behavior = mCursiveBehavior;
      mWidget.setAutoScrollEnabled(true);
      mWidget.setAutoTypesetEnabled(true);
      mWidget.setScrollbarResource(R.drawable.sltw_scrollbar_xml);
      mWidget.setScrollArrowLeftResource(R.drawable.sltw_arrowleft_xml);
      mWidget.setScrollArrowRightResource(R.drawable.sltw_arrowright_xml);
    }
    mWidget.setOnTextChangedListener(behavior);
    mWidget.setOnSelectionChangedListener(behavior);
    mCandidateBarController.setOnCandidateButtonClickedListener(behavior);
    mToolbarController.setOnSpaceButtonClickedListener(behavior);
    mToolbarController.setOnDeleteButtonClickedListener(behavior);
    mEditText.setOnCursorIndexChangedListener(behavior);
  }

  // ----------------------------------------------------------------------
  // Handwriting recognition engine configuration
  
  @Override
  public void onConfigureBegin()
  {
    Log.d(TAG, "Handwriting configuration begin");
  }

  @Override
  public void onConfigureEnd(final boolean success)
  {
    if (success) {
      Log.d(TAG, "Handwriting configuration succeeded");
      Toast.makeText(this, R.string.sltw_sample_configSuccess, Toast.LENGTH_SHORT).show();
    } else {
      Log.d(TAG, "Handwriting configuration failed (" + mWidget.getErrorString() + ")");
      Toast.makeText(this, R.string.sltw_sample_configFailed, Toast.LENGTH_SHORT).show();
    }

    // hide soft keyboard if it is currently shown
    InputMethodManager manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
    manager.hideSoftInputFromWindow(mEditText.getWindowToken(), 0);
  }

  // ----------------------------------------------------------------------
  // Handwriting recognition process
  
  @Override
  public void onRecognitionBegin()
  {
    Log.d(TAG, "Handwriting recognition begins");
  }

  @Override
  public void onRecognitionEnd()
  {
    Log.d(TAG, "Handwriting recognition end");
  }
  
  // ----------------------------------------------------------------------
  // Cursor handle listeners
  
  @Override
  public void onCursorHandleDragBegin()
  {
    Log.d(TAG, "Cursor handle drag begins");
  }
  
  @Override
  public void onCursorHandleDragEnd(final boolean scrolledAtEnd)
  {
    Log.d(TAG, "Cursor handle drag ends (at end=" + scrolledAtEnd + ")");
    
    if (scrolledAtEnd) {
      final Editable editable = mEditText.getText();
      if(editable != null)
        mWidget.setInsertionMode(editable.length());
    }
  }
  
  @Override
  public void onCursorHandleDrag(int index)
  {
    Log.d(TAG, "Cursor handle dragged to index " + index);

    mEditText.setCursorIndex(index);
  }
  
  // ----------------------------------------------------------------------
  // Insert handle listeners
  
  @Override
  public void onInsertHandleDragBegin()
  {
    Log.d(TAG, "Insert handle drag begin");
  }
  
  @Override
  public void onInsertHandleDragEnd(final boolean snapped)
  {
    Log.d(TAG, "Insert handle drag ends (snapped=" + snapped + ")");
  }
  
  @Override
  public void onInsertHandleClicked()
  {
    Log.d(TAG, "Insert handle clicked");
    
    // switch to correction mode
    mWidget.setCorrectionMode();
    // make sure cursor is visible
    mWidget.moveCursorToVisibleIndex();
  }
  
  // ----------------------------------------------------------------------
  // Handwriting recognition gestures
  
  @Override
  public void onSingleTapGesture(final int index)
  {
    Log.d(TAG, "Handwriting recognition single tap at index " + index);

    mEditText.setCursorIndex(index);
  }

  @Override
  public void onInsertGesture(final int index)
  {
    Log.d(TAG, "Handwriting recognition insert gesture at index " + index);

    if (mWidget.isIsolatedMode())
    {
      //insert space on insert gesture for box mode.
      insertSpace(index);
    }
    if (!mWidget.setInsertionMode(index)) {
      String text = String.format((Locale) null, getString(R.string.sltw_sample_insertGestureFailure), index);
      Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }
    mEditText.setCursorIndex(index);
  }

  /**
   * Allow to insert a space a the given index.
   * 
   * @param index
   *          int
   */
  private void insertSpace(final int index)
  {
      mWidget.replaceCharacters(index, index, " ");
      //we place the cursor after the inserted space
      mEditText.setCursorIndex(index+1);
  }

  @Override
  public void onJoinGesture(final int index)
  {
    Log.d(TAG, "Handwriting recognition join gesture at index " + index);
    
    final Editable editable = mEditText.getText();
    String text = "";
    if(editable != null)
      text = editable.toString();
    final int length = text.length();
    int start = index;
    int end = index;
    
    // find bounds of space region to remove from text
    if(start >= length)
      start = length - 1;
    while (start > 0 && text.charAt(start - 1) == ' ') {
      start--;
    }

    if(end >= length)
      end = length - 1;
    while (end < length && text.charAt(end) == ' ') {
      end++;
    }

    if((start >=0) && (end >= 0))
      mWidget.replaceCharacters(start, end, null);
  }
  
  @Override
  public void onSelectionGesture(final int start, final int end)
  {
    Log.d(TAG, "Handwriting recognition selection gesture at range " + start + "-" + end);
    
    mEditText.setSelection(start, end);
  }
  
  @Override
  public void onUnderlineGesture(final int start, final int end)
  {
    Log.d(TAG, "Handwriting recognition underline gesture at range " + start + "-" + end);

    mEditText.setSelection(start, end);
  }
  
  @Override
  public void onReturnGesture(final int index)
  {
    Log.d(TAG, "Handwriting recognition return gesture at index " + index);

    final String text = String.format((Locale) null, getString(R.string.sltw_sample_returnGestureToast), index);
    Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    
    mWidget.replaceCharacters(index, index, "\n");
  }
  
  @Override
  public void onPinchGesture()
  {
    Log.d(TAG, "Pinch gesture detected");

    Toast.makeText(this, R.string.sltw_sample_pinchGestureToast, Toast.LENGTH_SHORT).show();
  }
  
  // ----------------------------------------------------------------------
  // User scrolling

  @Override
  public void onUserScrollBegin()
  {
    Log.d(TAG, "User scroll begins");
  }
  
  public void onUserScrollEnd(final boolean scrolledAtEnd)
  {
    Log.d(TAG, "User scroll ends (at end=" + scrolledAtEnd + ") at cursor index="+mWidget.getCursorIndex());
    
    if (scrolledAtEnd) {
      if (!mWidget.isInsertionMode()) {
        final Editable editable = mEditText.getText();
        int length = 0;
        if(editable != null)
          length = editable.length();
        // switch to insertion mode at the end of the text
        mWidget.setInsertionMode(length);
        // place the cursor at the end of the text
        mEditText.setCursorIndex(length);
      }
    } else {
      if (mWidget.isInsertionMode()) {
        // switch to correction mode if user scrolled back into the text
        mWidget.setCorrectionMode();
        // make sure cursor is visible
        mWidget.moveCursorToVisibleIndex();
        // synchronize field cursor
        mEditText.setCursorIndex(mWidget.getCursorIndex());
      }
    }
  }

  @Override
  public void onUserScroll()
  {
    Log.d(TAG, "User scroll, cursor index = "+mWidget.getCursorIndex());
    
    if (!mWidget.isInsertionMode()) {
      if (mWidget.moveCursorToVisibleIndex()) {
        int index = mWidget.getCursorIndex();
        if(mWidget.isIsolatedMode()) //transform the box index to a character index
            index++;

        // cursor has been moved, synchronize field cursor
        mEditText.setCursorIndex(index);
      }
    }
  }

  // ----------------------------------------------------------------------
  // Mode change listener

  @Override
  public void onModeButtonClicked(int mode)
  {
    Log.d(TAG, "Mode set to " + (mode == ToolbarController.BOX_MODE ? "Box" : "Cursive"));

    // the following API call is not necessary, as the widget takes care of
    // reusing handwriting recognizer instances to speed up configuration
    // time
    // this line is only here for demonstration purposes
    //mWidget.releaseEngine();

    configure(mode);
  }

  @Override
  public void onInsertionWindowDidClosed()
  {
    Log.d(TAG, "onInsertionWindowDidClosed");

    // make sure cursor is visible
    mWidget.moveCursorToVisibleIndex();
  }
}
