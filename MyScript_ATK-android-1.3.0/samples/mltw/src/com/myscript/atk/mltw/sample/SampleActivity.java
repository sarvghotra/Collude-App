// Copyright MyScript

package com.myscript.atk.mltw.sample;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.Spanned;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.myscript.atk.mltw.ErrorCode;
import com.myscript.atk.mltw.Gesture;
import com.myscript.atk.mltw.Mode;
import com.myscript.atk.mltw.MultiLineTextWidget;
import com.myscript.atk.mltw.model.Word;
import com.myscript.atk.mltw.model.listeners.OnConfigureListener;
import com.myscript.atk.mltw.model.listeners.OnGestureDetectedListener;
import com.myscript.atk.mltw.model.listeners.OnModeChangedListener;
import com.myscript.atk.mltw.model.listeners.OnRecognitionListener;
import com.myscript.atk.mltw.model.listeners.OnSelectionChangedListener;
import com.myscript.atk.mltw.model.listeners.OnTextChangedListener;
import com.myscript.atk.mltw.sample.CandidatesController.OnCandidateClickListener;
import com.myscript.certificate.MyCertificate;

/**
 * This class demonstrates how to integrate some features of the {@link MultiLineTextWidget} widget in a single
 * application.
 */
public class SampleActivity extends Activity
    implements
      OnConfigureListener,
      OnTextChangedListener,
      OnGestureDetectedListener,
      OnSelectionChangedListener,
      OnModeChangedListener,
      OnRecognitionListener,
      OnCandidateClickListener
{
  /** Debug tag. */
  private final static String TAG = SampleActivity.class.getSimpleName();

  /** Number of pages. */
  private final static int PAGE_COUNT = 10;

  /** The MultiLine Text widget. */
  private MultiLineTextWidget mWidget;

  /** The candidate bar managing candidates. */
  private CandidatesController mCandidatesController;

  /** The text view displaying widget text result. */
  private TextView mTextView;

  /** Current recognition text editable object. */
  private Editable mEditable;

  /** The selected word span. */
  private SelectedWordSpan mSelectionSpan;

  /** The button reflow. */
  private Button mReflowButton;

  /** Pages saved content. */
  private List<byte[]> mPagesContent = null;

  /** The current page index. */
  private int mCurrentPage = 0;

  /** Current activity orientation. */
  private int mOrientation = Configuration.ORIENTATION_UNDEFINED;

  /** Flag to trigger reflow on orientation change. */
  private boolean mShouldReflow;

  /** Are we in page mode ? */
  private boolean mPageModeEnabled;

  @TargetApi(Build.VERSION_CODES.HONEYCOMB)
  @Override
  protected void onCreate(final Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

    setContentView(R.layout.mltw_activity_sample);

    // Candidates
    mCandidatesController = new CandidatesController(this, (LinearLayout) findViewById(R.id.mltw_candidatesBar));
    mCandidatesController.setOnCandidateClickListener(this);

    // Recognized text view
    mTextView = (TextView) findViewById(R.id.mltw_noteText);

    mReflowButton = (Button) findViewById(R.id.mltw_menuReflow);
    mReflowButton.setOnClickListener(new OnClickListener()
    {
      @Override
      public void onClick(final View v)
      {
        onReflowMenuSelected();
      }
    });

    // Widget
    mWidget = (MultiLineTextWidget) findViewById(R.id.mltw_noteWidget);
    mWidget.addOnTextChangedListener(this);
    mWidget.addOnSelectionChangedListener(this);
    mWidget.addOnGestureDetectedListener(this);
    mWidget.addOnConfigureListener(this);
    mWidget.addOnModeChangedListener(this);
    mWidget.addOnRecognitionListener(this);
    configureGestures();

    // Trigger reflow when orientation changes via GlobalLayoutListener
    final OnGlobalLayoutListener l = new OnGlobalLayoutListener()
    {
      @Override
      public void onGlobalLayout()
      {
        if (mShouldReflow)
          mWidget.reflow();
        mShouldReflow = false;
      }
    };
    ((View) mWidget).getViewTreeObserver().addOnGlobalLayoutListener(l);

    // force software layer on newer Android versions
    // default layer type breaks ink and baseline rendering
    // see for example http://code.google.com/p/android/issues/detail?id=29944
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH)
      ((View) mWidget).setLayerType(View.LAYER_TYPE_SOFTWARE, null);

    final int inputHeight = getResources().getDimensionPixelSize(R.dimen.mltw_sample_widget_inputview_height);
    mWidget.setInputViewHeight(inputHeight);

    configureLocale();

    mWidget.setWritingMode();

    // Saved content for 10 pages
    mPagesContent = new ArrayList<byte[]>(PAGE_COUNT);
    for (int i = 0; i < PAGE_COUNT; i++)
      mPagesContent.add(null);

    showPageToast();

    final int color = getResources().getColor(R.color.mltw_ink_color_selected);
    mSelectionSpan = new SelectedWordSpan(color);
  }

  @Override
  public void onConfigurationChanged(final Configuration newConfig)
  {
    super.onConfigurationChanged(newConfig);

    if (mOrientation != newConfig.orientation)
      mShouldReflow = true;

    mOrientation = newConfig.orientation;
  }

  @Override
  protected void onDestroy()
  {
    super.onDestroy();

    if (mWidget != null)
    {
      mWidget.removeOnTextChangedListener(this);
      mWidget.removeOnSelectionChangedListener(this);
      mWidget.removeOnGestureDetectedListener(this);
      mWidget.removeOnConfigureListener(this);
      mWidget.removeOnModeChangedListener(this);
      mWidget.release();
      mWidget = null;
    }
  }

  @Override
  public boolean onCreateOptionsMenu(final Menu menu)
  {
    getMenuInflater().inflate(R.menu.mltw_activity_sample, menu);
    return super.onCreateOptionsMenu(menu);
  }

  @Override
  public boolean onPrepareOptionsMenu(final Menu menu)
  {
    final MenuItem menuNext = menu.findItem(R.id.mltw_menuNext);
    final MenuItem menuPrev = menu.findItem(R.id.mltw_menuPrev);
    menuNext.setVisible(mPageModeEnabled);
    menuPrev.setVisible(mPageModeEnabled);

    menuNext.setEnabled(mCurrentPage < PAGE_COUNT - 1);
    menuPrev.setEnabled(mCurrentPage > 0);

    final MenuItem menuPageMode = menu.findItem(R.id.mltw_menuPageMode);
    menuPageMode.setChecked(mPageModeEnabled);

    return super.onPrepareOptionsMenu(menu);
  }

  @Override
  public boolean onOptionsItemSelected(final MenuItem item)
  {
    if (item.getItemId() == R.id.mltw_menuClear)
      onClearMenuSelected();
    else if (item.getItemId() == R.id.mltw_menuShare)
      onImageMenuSelected();
    else if (item.getItemId() == R.id.mltw_menuNext)
      onNextPageMenuSelected();
    else if (item.getItemId() == R.id.mltw_menuPrev)
      onPrevPageMenuSelected();
    else if (item.getItemId() == R.id.mltw_menuPageMode)
      onPageModeMenuSelected();

    return super.onOptionsItemSelected(item);
  }

  // ----------------------------------------------------------------------
  // Handwriting recognition configuration

  /**
   * Configures the widget with the English as default locale.
   */
  private void configureLocale()
  {
    Log.d(TAG, "Configuring en_US");

    final List<String> resources = new ArrayList<String>();
    resources.add("en_US/en_US-ak-cur.lite.res");
    resources.add("en_US/en_US-lk-text.lite.res");

    final SimpleResourceHelper helper = new SimpleResourceHelper(this);
    final List<String> paths = helper.getResourcePaths(resources);

    final List<String> lexicon = new ArrayList<String>();

    mWidget.configure("en_US", paths, lexicon, MyCertificate.getBytes());
  }

  /**
   * Enables/Disables the wanted gestures in the widget.
   */
  private void configureGestures()
  {
    mWidget.setGestureEnabled(Gesture.ERASE, true);
    mWidget.setGestureEnabled(Gesture.OVERWRITE, true);
    mWidget.setGestureEnabled(Gesture.SINGLE_TAP, true);
    mWidget.setGestureEnabled(Gesture.RETURN, true);
    mWidget.setGestureEnabled(Gesture.INSERT, true);
    mWidget.setGestureEnabled(Gesture.JOIN, true);
    mWidget.setGestureEnabled(Gesture.SELECTION, true);
    mWidget.setGestureEnabled(Gesture.UNDERLINE, true);
  }

  // ----------------------------------------------------------------------
  // Widget Configuration callback

  @Override
  public void onConfigureBegin()
  {
  }

  @Override
  public void onConfigureEnd()
  {
    Toast.makeText(this, R.string.mltw_sample_hwrconfig_success, Toast.LENGTH_SHORT).show();
  }

  @Override
  public void onConfigureFailed(final int errorCode)
  {
    switch (errorCode)
    {
      case ErrorCode.CANNOT_LOAD_ENGINE :
      case ErrorCode.CANNOT_INITIALIZE_ENGINE :
        Log.e(TAG, "Engine error");
        break;
      case ErrorCode.MISSING_CERTIFICATE :
        Log.e(TAG, "Certificate error");
        break;
      case ErrorCode.MISSING_CONFIG :
      case ErrorCode.UNSUITABLE_CONFIG :
        Log.e(TAG, "Config error");
        break;
      case ErrorCode.CANNOT_LOAD_RESOURCE :
      case ErrorCode.MISSING_AK_RESOURCE :
      case ErrorCode.UNSUITABLE_AK_RESOURCE :
        Log.e(TAG, "Resource error");
        break;
      case ErrorCode.MALFORMED_INPUT :
      case ErrorCode.CANNOT_START_THREAD :
        Log.e(TAG, "Widget error");
        break;
      case ErrorCode.NOTHING :
      case ErrorCode.GENERAL :
      default :
        Log.e(TAG, "Unknown error");
        break;
    }

    Toast.makeText(this, R.string.mltw_sample_hwrconfig_failure, Toast.LENGTH_SHORT).show();
  }

  // ----------------------------------------------------------------------
  // Widget Text callback

  @Override
  public void onTextChanged(final Editable text)
  {
    Log.d(TAG, "Text changed to [" + text.toString().replace("\n", "↲") + "]");

    mEditable = text;
    mTextView.setText(mEditable);
  }

  // ----------------------------------------------------------------------
  // Widget Selection callback

  @Override
  public void onSelectionChanged(final Word word)
  {
    if (word == null)
    {
      mCandidatesController.clear();
    }
    else
    {
      mCandidatesController.onWordSelected(word);

      Log.d(TAG, "Selection changed to [" + word.getStart() + " - " + word.getEnd() + "]");
      String candidates = "";
      for (final String cand : word.getCandidates())
      {
        if (cand.equals(word.getText()))
          candidates += " [" + cand + "]";
        else
          candidates += " " + cand;
      }
      Log.d(TAG, candidates);
    }

    highlightWord(word);
  }

  /**
   * Highlights the given word in the text view.
   *
   * @param word
   *          The word to highlight.
   */
  private void highlightWord(final Word word)
  {
    mEditable.removeSpan(mSelectionSpan);

    if (word != null)
      mEditable.setSpan(mSelectionSpan, word.getStart(), Math.min(mEditable.length(), word.getEnd()),
          Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

    mTextView.setText(mEditable);
  }

  // ----------------------------------------------------------------------
  // Widget gesture callbacks

  @Override
  public void onSingleTapGesture(final Word word, final int relativeIndex)
  {
    Toast.makeText(this, "onSingleTapGesture at index " + (word.getStart() + relativeIndex), Toast.LENGTH_SHORT).show();

    mWidget.setCorrectionMode(word);
  }

  @Override
  public void onReturnGesture(final Word word, final int relativeIndex)
  {
    Toast.makeText(this, "onReturnGesture at index " + (word.getStart() + relativeIndex), Toast.LENGTH_SHORT).show();

    mWidget.insertLineBreak(word, relativeIndex);
  }

  @Override
  public void onInsertGesture(final Word word, final int relativeIndex)
  {
    Toast.makeText(this, "onInsertGesture at index " + (word.getStart() + relativeIndex), Toast.LENGTH_SHORT).show();

    if (relativeIndex <= 0 || relativeIndex >= word.getText().length())
      mWidget.setInsertionMode(word, relativeIndex);
    else
      mWidget.insertSpace(word, relativeIndex);
  }

  @Override
  public void onJoinGesture(final Word word, final int relativeIndex)
  {
    Toast.makeText(this, "onJoinGesture at index " + (word.getStart() + relativeIndex), Toast.LENGTH_SHORT).show();

    mWidget.removeSpace(word, relativeIndex);
  }

  @Override
  public void onSelectionGesture(final List<Word> words)
  {
    Toast.makeText(this, "onSelectionGesture for " + words.size() + " words", Toast.LENGTH_SHORT).show();

    mWidget.setCorrectionMode(words.get(0));
  }

  @Override
  public void onUnderlineGesture(final List<Word> words)
  {
    Toast.makeText(this, "onUnderlineGesture for " + words.size() + " words", Toast.LENGTH_SHORT).show();

    mWidget.setCorrectionMode(words.get(0));
  }

  // ----------------------------------------------------------------------
  // Widget modes management

  @Override
  public void onModeChanged(final int oldMode, final int newMode)
  {
    switch (newMode)
    {
      case Mode.WRITING :
        Log.d(TAG, "Mode is now WRITING");
        break;
      case Mode.CORRECTION :
        Log.d(TAG, "Mode is now CORRECTION");
        break;
      case Mode.INSERTION :
        Log.d(TAG, "Mode is now INSERTION");
        break;
      default :
        break;
    }
  }

  // ----------------------------------------------------------------------
  // Recognition events

  @Override
  public void onRecognitionBegin()
  {
    setProgressBarIndeterminateVisibility(true);
  }

  @Override
  public void onRecognitionEnd()
  {
    setProgressBarIndeterminateVisibility(false);
  }

  // ----------------------------------------------------------------------
  // Menu callbacks

  /**
   * Called when clear menu button is clicked.
   */
  public void onClearMenuSelected()
  {
    Log.d(TAG, "Clear button clicked");

    mWidget.clear();
  }

  /**
   * Called when reflow menu button is clicked.
   */
  public void onReflowMenuSelected()
  {
    Log.d(TAG, "Reflow button clicked");

    mWidget.reflow();
  }

  /**
   * Called when image menu button is clicked.
   */
  public void onImageMenuSelected()
  {
    Log.d(TAG, "Image button clicked");

    final Bitmap bmp = mWidget.getImage(true, true);

    try
    {
      final OutputStream stream = new FileOutputStream(Environment.getExternalStorageDirectory().getPath()
          + "/test.png");
      /* Write bitmap to file using JPEG or PNG and 80% quality hint for JPEG. */
      bmp.compress(CompressFormat.PNG, 80, stream);
      stream.close();
    }
    catch (final IOException e)
    {
      Toast.makeText(this, "Error while exporting image content", Toast.LENGTH_SHORT).show();
      e.printStackTrace();
    }
    finally
    {
      Toast.makeText(this, "Image successfully exported to /sdcard/test.png", Toast.LENGTH_SHORT).show();
    }
  }

  /**
   * Called when next page menu button is clicked.
   */
  public void onNextPageMenuSelected()
  {
    Log.d(TAG, "Next button clicked");

    if (mCurrentPage == PAGE_COUNT - 1)
      return;

    // Save widget content in current page
    savePage();

    // Clear the widget
    mWidget.clear();
    mCurrentPage++;

    // Load next page content in widget
    loadPage(mPagesContent.get(mCurrentPage));

    invalidateOptionsMenu();
    showPageToast();
  }

  /**
   * Called when load menu button is clicked.
   */
  public void onPrevPageMenuSelected()
  {
    Log.d(TAG, "Prev button clicked");

    if (mCurrentPage == 0)
      return;

    // Save widget content in current page
    savePage();

    // Clear the widget
    mWidget.clear();
    mCurrentPage--;

    // Load previous page content in widget
    loadPage(mPagesContent.get(mCurrentPage));

    invalidateOptionsMenu();
    showPageToast();
  }

  /**
   * Called when page mode menu is clicked.
   */
  private void onPageModeMenuSelected()
  {
    Log.d(TAG, "Page Mode button clicked");

    mPageModeEnabled = !mPageModeEnabled;

    if (mPageModeEnabled)
    {
      mWidget.setInputViewHeight(mWidget.getMeasuredHeight());
    }
    else
    {
      final int inputHeight = getResources().getDimensionPixelSize(R.dimen.mltw_sample_widget_inputview_height);
      mWidget.setInputViewHeight(inputHeight);
    }

    invalidateOptionsMenu();
    showPageToast();
  }

  /**
   * Saves the widget content.
   */
  private void savePage()
  {
    mPagesContent.set(mCurrentPage, mWidget.save());
    Toast.makeText(this, "Widget content saved for page " + (mCurrentPage + 1), Toast.LENGTH_SHORT).show();
  }

  /**
   * Loads the widget with the given data.
   *
   * @param model
   *          The previously saved content of the widget.
   */
  private void loadPage(final byte[] model)
  {
    mWidget.load(model);
    Toast.makeText(this, "Widget content loaded in page " + (mCurrentPage + 1), Toast.LENGTH_SHORT).show();
  }

  /**
   * Displays a toast indicating the currently displayed page.
   */
  private void showPageToast()
  {
    if (mPageModeEnabled)
      Toast.makeText(this, "Page " + (mCurrentPage + 1) + "/10", Toast.LENGTH_SHORT).show();
  }

  // ----------------------------------------------------------------------
  // Candidate bar callbacks

  @Override
  public void onCandidateClick(final Word word, final String candidate)
  {
    mWidget.replaceWord(word, candidate);
    Toast.makeText(this, "New candidate : " + candidate, Toast.LENGTH_SHORT).show();
  }
}
