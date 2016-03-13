// Copyright (c) MyScript

package com.myscript.atk.maw.sample;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.myscript.atk.maw.MathWidgetApi;
import com.myscript.atk.maw.sample.resources.SimpleResourceHelper;
import com.myscript.atk.maw.sample.BuildConfig;
import com.myscript.atk.maw.sample.R;

import com.myscript.certificate.MyCertificate;

/** This class demonstrates how to basically integrate the Math Widgetstyleable library in a single application. */
public class SampleActivity extends Activity
    implements
      MathWidgetApi.OnConfigureListener,
      MathWidgetApi.OnRecognitionListener,
      MathWidgetApi.OnGestureListener,
      MathWidgetApi.OnWritingListener,
      MathWidgetApi.OnTimeoutListener,
      MathWidgetApi.OnSolvingListener,
      MathWidgetApi.OnUndoRedoListener
{
  private static final boolean DBG = BuildConfig.DEBUG;
  private static final String TAG = "SampleActivity";

  /** Math Widget */
  private MathWidgetApi mWidget;

  /**
   * List of error dialog types.
   */
  /** Notify the user that a MSB resource is not found or invalid. */
  public static final int DIALOG_ERROR_RESSOURCE = 0;
  /** Notify the user that a MSB certificate is missing or invalid. */
  public static final int DIALOG_ERROR_CERTIFICATE = 1;
  /** Notify the user that maximum number of items has been reached. */
  public static final int DIALOG_ERROR_RECOTIMEOUT = 2;
  /** One error dialog at a time. */
  private boolean mErrorDlgDisplayed = false;

  @Override
  protected void onCreate(final Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);

    // Set application title
    setTitle(getResources().getString(R.string.activity_name));

    // Configure listeners
    mWidget = (MathWidgetApi) findViewById(R.id.myscript_maw);
    mWidget.setOnConfigureListener(this);
    mWidget.setOnRecognitionListener(this);
    mWidget.setOnGestureListener(this);
    mWidget.setOnWritingListener(this);
    mWidget.setOnTimeoutListener(this);

    // Connect clear button
    final View clearButton = findViewById(R.id.myscript_math_clearButton);
    if (clearButton != null)
    {
      clearButton.setOnClickListener(new View.OnClickListener()
      {
        @Override
        public void onClick(final View view)
        {
          mWidget.clear(true /* allow undo */);
        }
      });
    }

    // Configure equation recognition engine
    configure();
  }

  @Override
  protected void onDestroy()
  {
    mWidget.release(this);

    // Call super
    super.onDestroy();
  }

  @Override
  protected void onSaveInstanceState(final Bundle outState)
  {
    super.onSaveInstanceState(outState);
  }

  @Override
  public void onBackPressed()
  {
    moveTaskToBack(true);
  }

  // ----------------------------------------------------------------------
  // Math widget styleable library - equation recognition engine configuration

  private void configure()
  {
    // Equation resource    
    final String[] resources = new String[]{"math-ak.res", "math-grm-maw.res"};

    // Prepare resources
    final String subfolder = "math";
    final String resourcePath = new String(getFilesDir().getPath() + java.io.File.separator + subfolder);
    SimpleResourceHelper
        .copyResourcesFromAssets(getAssets(), subfolder /* from */, resourcePath /* to */, resources /* resource names */);

    // Configure math widget
    mWidget.setResourcesPath(resourcePath);
    mWidget.configure(this, resources, MyCertificate.getBytes(), MathWidgetApi.AdditionalGestures.DefaultGestures);
  }

  // ----------------------------------------------------------------------
  // Math widget styleable library - equation recognition engine configuration

  @Override
  public void onConfigurationBegin()
  {
    if (DBG)
      Log.d(TAG, "Equation configuration begins");
  }

  @Override
  public void onConfigurationEnd(final boolean success)
  {
    if (DBG)
    {
      if (success)
        Log.d(TAG, "Equation configuration succeeded");
      else
        Log.d(TAG, "Equation configuration failed (" + mWidget.getErrorString() + ")");
    }

    if (DBG)
    {
      if (success)
        Log.d(TAG, "Equation configuration loaded successfully");
      else
        Log.d(
            TAG,
            "Equation configuration error - did you copy the equation resources to your SD card? ("
                + mWidget.getErrorString() + ")");
    }

    // Notify user using dialog box
    if (!success)
      showErrorDlg(DIALOG_ERROR_RESSOURCE);
  }

  // ----------------------------------------------------------------------
  // Math Widget styleable library - equation recognition process

  @Override
  public void onRecognitionBegin()
  {
    if (DBG)
      Log.d(TAG, "Equation recognition begins");
  }

  @Override
  public void onRecognitionEnd()
  {
    if (DBG)
      Log.d(TAG, "Equation recognition end");
  }

  @Override
  public void onUsingAngleUnitChanged(final boolean used)
  {
    if (DBG)
      Log.d(TAG, "An angle unit usage has changed in the current computation and is currently " + used);
  }

  // ----------------------------------------------------------------------
  // Math Widget styleable library - equation recognition gestures

  @Override
  public void onEraseGesture(final boolean partial)
  {
    if (DBG)
      Log.d(TAG, "Erase gesture handled by current equation and is partial " + partial);
  }

  // ----------------------------------------------------------------------
  // Math Widget styleable library - ink edition

  @Override
  public void onWritingBegin()
  {
    if (DBG)
      Log.d(TAG, "Start writing");
  }

  @Override
  public void onWritingEnd()
  {
    if (DBG)
      Log.d(TAG, "End writing");
  }

  @Override
  public void onRecognitionTimeout()
  {
    showErrorDlg(DIALOG_ERROR_RECOTIMEOUT);
  }

  // ----------------------------------------------------------------------
  // Math Widget styleable library - Undo / Redo

  @Override
  public void onUndoRedoStateChanged()
  {
    if (DBG)
      Log.d(TAG, "End writing");
  }

  // ----------------------------------------------------------------------
  // Math Widget styleable library - Errors

  // showDialog is deprecated but still used to simplify the example. 
  @SuppressWarnings("deprecation")
  private void showErrorDlg(final int id)
  {
    if (DBG)
      Log.i(TAG, "Show error dialog");
    if (!mErrorDlgDisplayed)
    {
      mErrorDlgDisplayed = true;
      showDialog(id);
    }
  }

  @Override
  public Dialog onCreateDialog(final int id)
  {
    final AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
    alertBuilder.setCancelable(false);
    switch (id)
    {
    // Language pack update missing resource
      case DIALOG_ERROR_RESSOURCE :
        alertBuilder.setTitle(R.string.langpack_parsing_error_title);
        alertBuilder.setMessage(R.string.langpack_parsing_error_msg);
        alertBuilder.setPositiveButton(android.R.string.ok, abortListener);
        break;
      // Certificate error
      case DIALOG_ERROR_CERTIFICATE :
        alertBuilder.setTitle(R.string.certificate_error_title);
        alertBuilder.setMessage(R.string.certificate_error_msg);
        alertBuilder.setPositiveButton(android.R.string.ok, abortListener);
        break;
      // Maximum item count error
      case DIALOG_ERROR_RECOTIMEOUT :
        alertBuilder.setTitle(R.string.recotimeout_error_title);
        alertBuilder.setMessage(R.string.recotimeout_error_msg);
        alertBuilder.setPositiveButton(android.R.string.ok, closeListener);
        break;
    }
    final AlertDialog alert = alertBuilder.create();
    return alert;
  }

  private final DialogInterface.OnClickListener closeListener = new DialogInterface.OnClickListener()
  {
    @Override
    public void onClick(final DialogInterface di, final int position)
    {
      mErrorDlgDisplayed = false;
    }
  };

  private final DialogInterface.OnClickListener abortListener = new DialogInterface.OnClickListener()
  {
    @Override
    public void onClick(final DialogInterface di, final int position)
    {
      mErrorDlgDisplayed = false;
      finish();
    }
  };
}