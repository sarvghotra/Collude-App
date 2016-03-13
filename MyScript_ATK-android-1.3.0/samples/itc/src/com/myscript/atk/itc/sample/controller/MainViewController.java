package com.myscript.atk.itc.sample.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.myscript.atk.itc.CharBoxFactory;
import com.myscript.atk.itc.Error;
import com.myscript.atk.itc.IPageInterpreterListener;
import com.myscript.atk.itc.ISmartPageChangeListener;
import com.myscript.atk.itc.ISmartPageRecognitionListener;
import com.myscript.atk.itc.PageInterpreter;
import com.myscript.atk.itc.SmartPage;
import com.myscript.atk.itc.SmartStroke;
import com.myscript.atk.itc.SmartWord;
import com.myscript.atk.itc.StrokeFactory;
import com.myscript.atk.itc.WordFactory;
import com.myscript.atk.itc.WordRange;
import com.myscript.atk.itc.sample.Debug;
import com.myscript.atk.itc.sample.R;
import com.myscript.atk.itc.sample.inkcapture.InkCaptureView;
import com.myscript.atk.itc.sample.model.SmartStrokeUserParams;
import com.myscript.atk.itc.sample.model.SmartWordUserParams;
import com.myscript.atk.itc.sample.model.factory.UserParamsFactory;
import com.myscript.atk.itc.sample.util.FormattedDataManager;
import com.myscript.atk.itc.sample.util.SimpleResourceHelper;
import com.myscript.atk.itc.sample.view.GuidelinesView;
import com.myscript.certificate.MyCertificate;

public class MainViewController
implements
InkCaptureView.OnDrawListener,
IPageInterpreterListener,
ISmartPageChangeListener,
ISmartPageRecognitionListener
{
  // Debug
  private static final String TAG = "MainViewController";

  // Interactive Text Component objects declarations //

  // Handwriting recognition main entry point
  private PageInterpreter[] mPageInterpreters = new PageInterpreter[4];

  // User parameters factory (implements both stroke and word character user parameter factory)
  private UserParamsFactory mUserParamsFactory;

  // Monitor ITC component graphical part
  public interface ITCModelItemListener
  {
    public void onStrokeAdded(SmartStroke stroke);

    public void onStrokeRemoved(SmartStroke stroke);

    public void onWordAdded(SmartWord word);

    public void onWordRemoved(SmartWord word);

    public void onToggleShowHidePageText(List<SmartWord> words);

    public void onPageNumberChanged(String pageNumber);
  }
  
  private static final String SAVE_ITF_FILES_PATH = Environment.getExternalStorageDirectory() + "/itcSampleITF/";

  // General purpose 
  private int mSelectedPageInterpreterIndex;
  private Context mContext;
  private InkCaptureView mInkCaptureView;
  private TextView mRecognitionResultTextView;
  private GuidelinesView mGuidelinesView;
  private long mStrokeBeginTime;
  
  // Serialization (orientation)
  private final static String SELECTED_PII = "SELECTED_PII";
  private final static String PIB = "PIB";
  private final static String PICID = "PICID";
  private final static String PI = "PI";
  private final static String PI_BUNDLE_SIZE = "PI_BUNDLE_SIZE";
  private final static String PI_CONF_SIZE = "PI_CONF_SIZE";
  private final static String PI_SIZE = "PI_SIZE";

  // Gesture manager
  private GestureManager mGestureManager;
  
  // WordFactory
  final WordFactory mWordFactory;

  // Interactive Text Component model item listener
  private ITCModelItemListener mITCModelItemListener;

  // Page monitoring
  private HashMap<PageInterpreter, Bundle> mPageInterpreterPageBundles = new HashMap<PageInterpreter, Bundle>();
  private HashMap<PageInterpreter, Integer> mPageInterpreterCurrentPage = new HashMap<PageInterpreter, Integer>();
  private HashMap<PageInterpreter, String> mPageInterpreterCurrentConfId = new HashMap<PageInterpreter, String>();

  private int mPageIndex;

  public MainViewController(Context context, FrameLayout pageView, TextView recognitionResultTextView, Bundle savedInstanceState)
  {
    mContext = context;

    // Initialize the guidelines view
    mGuidelinesView = (GuidelinesView) pageView.findViewById(R.id.itc_guidelinesview);

    // Initialize InkCaptureView
    mInkCaptureView = (InkCaptureView) pageView.findViewById(R.id.itc_inkcaptureview);
    mInkCaptureView.setStrokeWidth(context.getResources().getDimension(R.dimen.itc_inkWidth));
    mInkCaptureView.setOnDrawListener(this);

    // Initialize the user parameters factory responsible for whole user parameters monitoring
    mUserParamsFactory = new UserParamsFactory(mInkCaptureView.getStroker(), mContext);

    // Initialize TextView
    mRecognitionResultTextView = recognitionResultTextView;

    // Create all the PageInterpreter
    for (int i = 0; i < mPageInterpreters.length; ++i)
      mPageInterpreters[i] = new PageInterpreter(mContext);
    
    // Initialize the current Page Interpreter
    mSelectedPageInterpreterIndex = 0;

    // Initialize gesture manager
    ModelItemViewController itcModelItemViewController = new ModelItemViewController(context, pageView);
    mGestureManager = new GestureManager(mContext, mGuidelinesView, itcModelItemViewController, mUserParamsFactory);
    mITCModelItemListener = itcModelItemViewController;
    
    // Initialize model item factories
    mWordFactory = new WordFactory(new StrokeFactory(mUserParamsFactory), mUserParamsFactory);
    
    // Configure a PageInterpreter
    if (savedInstanceState == null)
    	loadPageInterpreterState();	

    // Create an empty SmartPage with its listeners and set it to the current PageInterpreter
    configureEmptySmartPage();
  }
  
  public void onSaveInstanceState(Bundle savedInstanceState)
  {
  	savePageInterpreterState();

  	savedInstanceState.putInt(SELECTED_PII, mSelectedPageInterpreterIndex);

  	int index = 0;
  	savedInstanceState.putInt(PI_BUNDLE_SIZE, mPageInterpreterPageBundles.size());
  	final Iterator<HashMap.Entry<PageInterpreter, Bundle>> bundleIt = mPageInterpreterPageBundles.entrySet().iterator();
  	while (bundleIt.hasNext()) {
  		HashMap.Entry<PageInterpreter, Bundle> pairs = (HashMap.Entry<PageInterpreter, Bundle>)bundleIt.next();
  		savedInstanceState.putBundle(PIB + index, pairs.getValue());
  		index++;
  	}

  	index = 0;
  	savedInstanceState.putInt(PI_CONF_SIZE, mPageInterpreterCurrentConfId.size());
  	final Iterator<HashMap.Entry<PageInterpreter, String>> confIt = mPageInterpreterCurrentConfId.entrySet().iterator();
  	while (confIt.hasNext()) {
  		HashMap.Entry<PageInterpreter, String> pairs = (HashMap.Entry<PageInterpreter, String>)confIt.next();
  		savedInstanceState.putString(PICID + index, pairs.getValue());
  		index++;
  	}

  	index = 0;
  	savedInstanceState.putInt(PI_SIZE, mPageInterpreterCurrentPage.size());
  	final Iterator<HashMap.Entry<PageInterpreter, Integer>> indexIt = mPageInterpreterCurrentPage.entrySet().iterator();
  	while (confIt.hasNext()) {
  		HashMap.Entry<PageInterpreter, Integer> pairs = (HashMap.Entry<PageInterpreter, Integer>)indexIt.next();
  		savedInstanceState.putInt(PI + index, pairs.getValue());
  		index++;
  	}

  	mGestureManager.setPageInterpreter(null);
  }
  
  public void onRestoreInstanceState(Bundle savedInstanceState)
  {  	
  	mSelectedPageInterpreterIndex = savedInstanceState.getInt(SELECTED_PII);

    mPageInterpreterPageBundles = new HashMap<PageInterpreter, Bundle>();
    int bundlesSize = savedInstanceState.getInt(PI_BUNDLE_SIZE);
    for(int i = 0; i < bundlesSize; ++i)
    	mPageInterpreterPageBundles.put(mPageInterpreters[i], savedInstanceState.getBundle(PIB + i));
    
    mPageInterpreterCurrentConfId = new HashMap<PageInterpreter, String>();
    int confSize = savedInstanceState.getInt(PI_CONF_SIZE);
    for(int i = 0; i < confSize; ++i)
    	mPageInterpreterCurrentConfId.put(mPageInterpreters[i], savedInstanceState.getString(PICID + i));
    
    mPageInterpreterCurrentPage = new HashMap<PageInterpreter, Integer>();
    int pageIndexesSize = savedInstanceState.getInt(PI_SIZE);
    for (int i = 0; i < pageIndexesSize; ++i)
    	mPageInterpreterCurrentPage.put(mPageInterpreters[i], savedInstanceState.getInt(PI + i));
    
    loadPageInterpreterState();
  }
  
  private void configurePageInterpreterForRecognition()
  {
  	mGestureManager.setPageInterpreter(mPageInterpreters[mSelectedPageInterpreterIndex]);

  	final List<String> resources = new ArrayList<String>();
  	resources.add("en_US/en_US-ak-cur.lite.res");
  	resources.add("en_US/en_US-lk-text.lite.res");
  	SimpleResourceHelper helper = new SimpleResourceHelper(mContext);
  	final List<String> paths = helper.getResourcePaths(resources);

  	// Configure the PageInterpreters
  	mPageInterpreters[mSelectedPageInterpreterIndex].configure("en_US", paths, new ArrayList<String>(), MyCertificate.getBytes(), mContext.getResources().getDisplayMetrics().densityDpi);
  }
  
  private void configureEmptySmartPage()
  {
    // Initialize a SmartPage
    SmartPage page = new SmartPage(mWordFactory);
    page.setSmartPageChangeListener(this);
    page.setSmartPageRecognitionListener(this);
    page.setSmartPageGestureListener(mGestureManager);
    
    // Set the PageInterpreter current page
    mPageInterpreters[mSelectedPageInterpreterIndex].setPage(page);
    mUserParamsFactory.setCharBoxFactory(page.getWordFactory().getCharBoxFactory());
  }

  // ----------------------------------------------------------------------
  // Menu configuration

  public void toggleGuidelines()
  {
    // Toggle guidelines drawing
    mGuidelinesView.toggleGuidelines();

    // Toggle PageInterpreter guidelines property
    if (mGuidelinesView.isGuidelinesEnable())
    {
      mPageInterpreters[mSelectedPageInterpreterIndex].setGuidelines(mGuidelinesView.getFirstLinePos(),
          mGuidelinesView.getGap(),
          mGuidelinesView.getLineCount());
    }
    else
    {
      mPageInterpreters[mSelectedPageInterpreterIndex].clearGuidelines();
    }
  }

  public void showDialogForPen(Context context)
  {
    mUserParamsFactory.showDialogForPen(context);
  }

  public void showGesturesSettingsDialog()
  {
    mGestureManager.showGesturesSettingsDialog();
  }

  public void toggleShowHideText()
  {
    mITCModelItemListener.onToggleShowHidePageText(mPageInterpreters[mSelectedPageInterpreterIndex].getPage().getWords());
  }

  public void changeCandidate(int selectedIndex)
  {
    // Get the selected word range
    WordRange selectedWordRange = mGestureManager.getSelectedWordRange();
    if (selectedWordRange != null)
    {
      // Check the selected words
      final List<SmartWord> selectedWords = selectedWordRange.getWords();
      if (!selectedWords.isEmpty())
      {
        // Get the current page
        SmartPage currentPage = mPageInterpreters[mSelectedPageInterpreterIndex].getPage();

        SmartWord oldWord = selectedWords.get(0);
        SmartWord newWord = currentPage.getWordFactory().createWordWithSelectedCandidate(oldWord, selectedIndex, oldWord.getSpaceBefore());

        currentPage.replaceWord(oldWord, newWord);

        // Update the selected word range
        int indexOfChangedCandidate = selectedWords.indexOf(oldWord);
        selectedWords.set(indexOfChangedCandidate, newWord);
        mGestureManager.updateSelectedWordRange(selectedWords);
      }
    }
  }
  
  public void changePageInterpreter()
  {
    final AlertDialog.Builder pageInterpreterDialogBuilder = new AlertDialog.Builder(mContext);
    pageInterpreterDialogBuilder.setTitle(R.string.change_page_interpreter);

    // OK button monitoring
    pageInterpreterDialogBuilder.setSingleChoiceItems(R.array.page_interpreter_dlg_items, mSelectedPageInterpreterIndex, new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which)
      {
        if (which != mSelectedPageInterpreterIndex)
        {
          // Save the current PageInterpreters state (current SmartPage)
          savePageInterpreterState();
          
          mSelectedPageInterpreterIndex = which;
          
          loadPageInterpreterState();
        }
      }
    });
    // OK button monitoring
    pageInterpreterDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int buttonIndex){}
    });
    AlertDialog pageInterpreterDialog = pageInterpreterDialogBuilder.create();
    pageInterpreterDialog.show();
  }
  
  private void savePageInterpreterState()
  {
    // Save the current SmartPage
    saveCurrentPage();
    
    // Save the PageInterpreter current index
    mPageInterpreterCurrentPage.put(mPageInterpreters[mSelectedPageInterpreterIndex], mPageIndex);
    
    // Save the configuration if not already done 
    if (mPageInterpreterCurrentConfId.get(mPageInterpreters[mSelectedPageInterpreterIndex]) == null)
    {
    	final String confId = mPageInterpreters[mSelectedPageInterpreterIndex].saveConfiguration();
    	mPageInterpreterCurrentConfId.put(mPageInterpreters[mSelectedPageInterpreterIndex], confId);
    }
  }
  
  private void loadPageInterpreterState()
  {
    // Configure PageInterprter for handwriting recognition
    mPageInterpreters[mSelectedPageInterpreterIndex].setPageInterpreterListener(this);
    configurePageInterpreterForRecognition();
    
    // Restore the current PageInterpreter SmartPage index
    if (mPageInterpreterCurrentPage.containsKey(mPageInterpreters[mSelectedPageInterpreterIndex]))
      mPageIndex = mPageInterpreterCurrentPage.get(mPageInterpreters[mSelectedPageInterpreterIndex]);
    else
    {
      mPageIndex = 0;
      mPageInterpreterCurrentPage.put(mPageInterpreters[mSelectedPageInterpreterIndex], mPageIndex);
    }
    Bundle pageInterpreterBundle = getPageInterpreterBundle();
    byte[] pageByteArray = pageInterpreterBundle.getByteArray("page" + mPageIndex);
    
    // Load the current PageInterpreter SmartPage
    loadPage(pageByteArray);
  }

  public void changeLanguage(String language, String[] resources)
  {
    final List<String> fullResources = new ArrayList<String>(resources.length);
    for (String resource : resources)
      fullResources.add(language + "/" + resource);
    Collections.sort(fullResources);
    
	  SimpleResourceHelper helper = new SimpleResourceHelper(mContext);
	  final List<String> fullResourcesPaths = helper.getResourcePaths(fullResources);

    // Call configuration process through the PageManager with the given resources
    mPageInterpreters[mSelectedPageInterpreterIndex].configure(language, fullResourcesPaths, new ArrayList<String>(), MyCertificate.getBytes(), mContext.getResources()
        .getDisplayMetrics().densityDpi);
  }

  public void loadITF()
  {
    // Create DirectoryChooserDialog and register a callback 
    FileChooserDialog directoryChooserDialog =  new FileChooserDialog(mContext, FileChooserDialog.DirType.ITF_DIR, new FileChooserDialog.ChosenFileListener() 
    {
      @Override
      public void onChosenFile(String chosenFile) 
      {
        mPageInterpreters[mSelectedPageInterpreterIndex].getPage().loadITF(chosenFile, false);
      }
    });
    directoryChooserDialog.showFileDialog();
  }

  public void saveITF()
  {
    final AlertDialog.Builder saveITFDialogBuilder = new AlertDialog.Builder(mContext);
    saveITFDialogBuilder.setTitle(R.string.save_page_as_itf);
    final EditText input = new EditText(mContext);
    saveITFDialogBuilder.setView(input);
    saveITFDialogBuilder.setNeutralButton("Ok", new DialogInterface.OnClickListener() {
      public void onClick(DialogInterface dialog, int whichButton) {
        saveFileOnSdCard(input.getText().toString());
      }
    });
    saveITFDialogBuilder.show();
  }
  
  private void saveFileOnSdCard(String fileName)
  {
    try
    {
      boolean isValidFileName = (fileName != null && !fileName.equals(""));

      // Check for available storage and valid file name
      if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) && isValidFileName)
      {
        File fn = new File(SAVE_ITF_FILES_PATH + fileName + ".itf");
        File parentDirectory = fn.getParentFile();
        if (parentDirectory == null || (!parentDirectory.mkdirs() && !parentDirectory.isDirectory()))
        {
          Toast.makeText(mContext, "Failed creating directory on the sdcard", Toast.LENGTH_SHORT).show();
          return;
        }

        // Write the itf to the specified path
        Error error = mPageInterpreters[mSelectedPageInterpreterIndex].getPage().saveITF(fn.getAbsolutePath());
        if (error.getErrorCode() != Error.ErrorCode.NoError)
          Toast.makeText(mContext, error.getErrorMessage(), Toast.LENGTH_SHORT).show();
        else
          Toast.makeText(mContext, "File saved in "+ fn, Toast.LENGTH_SHORT).show();
      }
      else
      {
        Toast.makeText(mContext, isValidFileName ? "External storage not available" : "Empty file name not allowed", Toast.LENGTH_SHORT).show();
      }
    }
    catch(Exception e){
      e.printStackTrace();        
    }
  }

  // --------------------------------------------------------------------------------
  // SmartPage monitoring methods

  public void deletePage()
  {
    mPageInterpreters[mSelectedPageInterpreterIndex].getPage().clear();
  }

  public void previousPage()
  {
    if (mPageIndex > 0)
    {
      // Save the current SmartPage
      Bundle pageInterpreterBundle = saveCurrentPage();

      // Decrease the page index
      mPageIndex--;

      // Load the requested page
      loadPage(pageInterpreterBundle.getByteArray("page" + mPageIndex));
    }
  }

  public void nextPage()
  {
    // Save the current SmartPage
    Bundle pageInterpreterBundle = saveCurrentPage();

    // Increase the page index
    mPageIndex++;

    loadPage(pageInterpreterBundle.getByteArray("page" + mPageIndex));
  }
  
  private Bundle saveCurrentPage()
  {
    // Get the PageInterpreter SmartPage bundle
    Bundle pageInterpreterBundle = getPageInterpreterBundle();
    
    SmartPage page = mPageInterpreters[mSelectedPageInterpreterIndex].getPage();
    
    // Store the current serialized page in a Bundle
    pageInterpreterBundle.putByteArray("page" + mPageIndex, page.toByteArray());

    // Clear all page elements
    page.clear();
    
    return pageInterpreterBundle;
  }
  
  private Bundle getPageInterpreterBundle()
  {
    Bundle pageInterpreterBundle = mPageInterpreterPageBundles.get(mPageInterpreters[mSelectedPageInterpreterIndex]);
    if (pageInterpreterBundle == null)
    {
      pageInterpreterBundle = new Bundle();
      mPageInterpreterPageBundles.put(mPageInterpreters[mSelectedPageInterpreterIndex], pageInterpreterBundle);
    }
    return pageInterpreterBundle;
  }

  
  // --------------------------------------------------------------------------------
  // Selection monitoring methods

  public void cancelSelection()
  {
    enableInkCaptureView(true);

    cancelWordsSelection();
  }

  private void cancelWordsSelection()
  {
    mGestureManager.cancelWordsSelection();
  }

  public WordRange getSelectedWordRange()
  {
    return mGestureManager.getSelectedWordRange();
  }

  // --------------------------------------------------------------------------------
  // InkCaptureView listeners

  @Override
  public void onDrawBegin(InkCaptureView v)
  {
    if (Debug.DBG)
      Log.d(TAG, "Draw begin");

    mStrokeBeginTime = System.currentTimeMillis();

    // Call penDown API to ensure better gesture monitoring
    mPageInterpreters[mSelectedPageInterpreterIndex].getPage().penDown();
  }

  @Override
  public void onDrawEnd(InkCaptureView v)
  {
    if (Debug.DBG)
      Log.d(TAG, "Draw end");

    // Compute the stroke user parameters to assign to the stroke
    SmartStrokeUserParams strokeUserParams = mUserParamsFactory.createStrokeUserParamsFromInkCaptureView(v);
    
    // Get the SmartStroke StrokeType
    int strokeType = strokeUserParams.isDrawingStroke() ? SmartStroke.StrokeType.DrawingStroke : SmartStroke.StrokeType.RecognitionStroke;

    SmartPage page = mPageInterpreters[mSelectedPageInterpreterIndex].getPage();

    // Create the SmartStroke
    SmartStroke stroke = page.getStrokeFactory().createStroke(v.getPointsX(), v.getPointsY(), mStrokeBeginTime, System.currentTimeMillis(),
        strokeType, strokeUserParams);

    page.addStroke(stroke);
  }

  @Override
  public void onDrawCancel(InkCaptureView v)
  {
    if (Debug.DBG)
      Log.d(TAG, "Draw cancel");

    // Call penAbort API to cancel penDown call for better gesture monitoring
    mPageInterpreters[mSelectedPageInterpreterIndex].getPage().penAbort();
  }

  @Override
  public void onDrawing(InkCaptureView v, float x, float y)
  {
  }
  
  public void enableInkCaptureView(boolean enable)
  {
    mInkCaptureView.setEnabled(enable);
  }

  // --------------------------------------------------------------------------------
  //  PageInterpreter and SmartPage listener methods

  @Override
  public void inkChange(final SmartPage smartPage, final List<SmartStroke> addedStrokes, final List<SmartStroke> removedStrokes)
  {
    Handler mainHandler = new Handler(mContext.getMainLooper());
    mainHandler.post(new Runnable()
    {
      @Override
      public void run()
      {
        // Remove the graphical strokes
        if (!removedStrokes.isEmpty())
          for (SmartStroke removedStroke : removedStrokes)
            mITCModelItemListener.onStrokeRemoved(removedStroke);

        // Add the new graphical strokes
        if (!addedStrokes.isEmpty())
          for (SmartStroke addedStroke : addedStrokes)
            mITCModelItemListener.onStrokeAdded(addedStroke);
      }
    });
  }

  @Override
  public void textChange(final SmartPage smartPage, final List<SmartWord> addedWords, final List<SmartWord> removedWords)
  {
    if (Debug.DBG)
      Log.d(TAG, "Entering onTextChange method with " + addedWords.size() + " added word(s) and " + removedWords.size()
          + " removed word(s)");

    Handler mainHandler = new Handler(mContext.getMainLooper());
    mainHandler.post(new Runnable()
    {
      @Override
      public void run()
      {
        // Remove the graphical words
        if (!removedWords.isEmpty())
          for (SmartWord removedWord : removedWords)
            mITCModelItemListener.onWordRemoved(removedWord);

        // Check for formatted data
        checkFormattedData(smartPage);

        // Add the new graphical words
        if (!addedWords.isEmpty())
        {
          for (SmartWord addedWord : addedWords)
            mITCModelItemListener.onWordAdded(addedWord);
        }
      }
    });
  }
  
  private void updateWordsUserParameters(List<SmartWord> words)
  {
    // This is to tackle itf loading feature only
    // Indeed user parameters would not have been set in this case
    if (words.isEmpty())
      return;

    SmartWord firstWord = words.get(0);
    if (firstWord.getType() == SmartWord.Type.RAW)
    {
      if (firstWord.getStrokes().get(0).getUserParams() == null)
      {
        // Replace the words in the page with updated user parameters
        final List<SmartWord> updatedWords = new ArrayList<SmartWord>(words.size());

        // Get the current page
        SmartPage currentPage = mPageInterpreters[mSelectedPageInterpreterIndex].getPage();

        for (SmartWord sw : words)
          updatedWords.add(currentPage.getWordFactory().createCopyOfWord(sw, sw.getSpaceBefore()));

        // Check for replacing needs
        if (updatedWords.size() > 0)
        {
          currentPage.replaceWords(words, updatedWords);

          for (SmartWord updatedWord : updatedWords)
            mITCModelItemListener.onWordAdded(updatedWord);
        }
      }
    }
  }

  @Override
  public void recognizerBegin(final SmartPage smartPage)
  {
    if (Debug.DBG)
      Log.d(TAG, "ITC recognition begin");
  }

  @Override
  public void recognizerEnd(final SmartPage smartPage)
  {
    // Get the page text
    final String pageText = smartPage.getText();

    if (Debug.DBG)
      Log.d(TAG, "Recognized page text " + pageText);

    Handler mainHandler = new Handler(mContext.getMainLooper());
    mainHandler.post(new Runnable()
    {
      @Override
      public void run()
      {
        // Set the page text
        mRecognitionResultTextView.setText(pageText);

        // Update words with missing user parameters (load itf case)
        // This is done due to the sample configuration which drawing is based on user parameters
        updateWordsUserParameters(smartPage.getWords());
      }
    });
  }

  @Override
  public void recognizerIntermediate(final SmartPage smartPage)
  {
    Log.i(TAG, "Intermediate recognition received");
  }

  @Override
  public void configureBegin(final PageInterpreter pageinterpreter)
  {
    Log.i(TAG, "Smart note taking component configuration began");
  }

  @Override
  public void configureEnd(final PageInterpreter pageinterpreter, boolean success, String locale)
  {
    Log.i(TAG, "Smart note taking component configuration " + (success ? "succeeded" : String.format("failed with error: '%s'", pageinterpreter.getRecognitionErrorString())));
  }

  // --------------------------------------------------------------------------------
  // Private MainViewController methods

  private void loadPage(byte[] pageByteArray)
  {
    if (pageByteArray != null)
    {
      // Create a new SmartPage
      SmartPage page = new SmartPage(mPageInterpreters[mSelectedPageInterpreterIndex].getPage().getWordFactory(), pageByteArray);
      page.setSmartPageRecognitionListener(this);
      page.setSmartPageChangeListener(this);
      page.setSmartPageGestureListener(mGestureManager);
      mPageInterpreters[mSelectedPageInterpreterIndex].setPage(page);
      mUserParamsFactory.setCharBoxFactory(page.getWordFactory().getCharBoxFactory());

      // Monitor loaded word views 
      final List<SmartWord> smartWords = page.getWords();
      for(SmartWord smartWord : smartWords)
      {
        final List<SmartStroke> strokes = smartWord.getStrokes();
        updateStrokesUserParameters(strokes);
        mITCModelItemListener.onWordAdded(smartWord);
      }
      // Monitor loaded pending strokes
      final List<SmartStroke> pendingStrokes = page.getPendingStrokes();
      {
        updateStrokesUserParameters(pendingStrokes );
        for (SmartStroke pendingStroke : pendingStrokes)
          mITCModelItemListener.onStrokeAdded(pendingStroke);
      }      
      // Monitor loaded drawing strokes 
      final List<SmartStroke> drawingStrokes = page.getDrawingStrokes();
      {
        updateStrokesUserParameters(drawingStrokes);
        for (SmartStroke drawingStroke: drawingStrokes)
          mITCModelItemListener.onStrokeAdded(drawingStroke);
      }
    }
    else
    {
      // Create an empty SmartPage with its listeners and set it to the current PageInterpreter 
      configureEmptySmartPage();
    }
    mITCModelItemListener.onPageNumberChanged("" + mPageIndex);
  }

  private void updateStrokesUserParameters(final List<SmartStroke> smartStrokes)
  {
    for (SmartStroke smartStroke : smartStrokes)
      mUserParamsFactory.updateStrokeUserParameters(smartStroke);
  }

  public void typesetPage()
  {
    // Typeset all the page words
    typesetWords(mPageInterpreters[mSelectedPageInterpreterIndex].getPage().getWords());
  }

  private List<SmartWord> typesetWords(List<SmartWord> words)
  {
    // Get the current page
    SmartPage currentPage = mPageInterpreters[mSelectedPageInterpreterIndex].getPage();

    // Get the word factory
    WordFactory wf = currentPage.getWordFactory();

    // Get the CharBoxFactory
    final CharBoxFactory cbf = (CharBoxFactory)wf.getCharBoxFactory();
    
    // Set the text size according to guidelines gap if any
    cbf.setTextSize(mGuidelinesView.getGap()* 3/4);
    
    // Create the list of typeset SmartWords
    final List<SmartWord> typeSetWords = new ArrayList<SmartWord>(words.size());
    for (SmartWord word : words)
    {
      if (word.getType() == SmartWord.Type.TYPESET)
        typeSetWords.add(word);
      else
        typeSetWords.add(wf.createTypeSetWord(word, word.getLeftBound(), word.getBaseLine(), word.getSpaceBefore()));
    }

    currentPage.replaceWords(words, typeSetWords);

    return typeSetWords;
  }

  public void typesetSelectedWords()
  {
    final List<SmartWord> selectedWords = mGestureManager.getSelectedWordRange().getWords();

    if (!selectedWords.isEmpty())
    {
      // Typeset the selected SmartWords
      final List<SmartWord> typeSetWords = typesetWords(selectedWords);

      // Update the selected word range
      mGestureManager.updateSelectedWordRange(typeSetWords);
    }
  }

  private void checkFormattedData(final SmartPage smartPage)
  {
    // Reset all user parameters formatted property
    final List<SmartWord> pageWords = smartPage.getWords();
    for (SmartWord smartWord : pageWords)
    {
      final List<SmartStroke> wordStrokes = smartWord.getStrokes();
      if (!wordStrokes.isEmpty())
      {
        // Update the formatted data property before new analysis at recognition end time
        for (SmartStroke stroke : wordStrokes)
        {
          // Reset the formatted data property
          SmartStrokeUserParams ssup = ((SmartStrokeUserParams) stroke.getUserParams());
          if (ssup != null)
            ssup.setFormattedData(null);
        }
      }
      else
      {
        // Update the formatted data property before new analysis at recognition end time
        int wordSize = smartWord.getLabelCharSize();
        for (int i = 0; i < wordSize; ++i)
        {
          SmartWordUserParams swup = (SmartWordUserParams) smartWord.getUserParams(i);
          if (swup != null)
            swup.setFormattedData(null);
        }
      }
    }

    // Identify formatted data
    final List<FormattedDataManager.FormattedData> formattedDatas = FormattedDataManager.identifyPageFormattedData(smartPage);

    // Parse all formatted data
    for (final FormattedDataManager.FormattedData fd : formattedDatas)
    {
      // Apply the formatted property to the given word range user parameters
      mUserParamsFactory.applyWordPropertyOnWordRange(fd.wordRange, UserParamsFactory.WordProperty.FORMATTED, fd);

      // Invalidate the UI to draw properties
      for (final SmartWord smartWord : fd.wordRange.getWords())
        mITCModelItemListener.onWordAdded(smartWord);
    }
  }
}
