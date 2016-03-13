package com.myscript.atk.itc.sample;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.myscript.atk.itc.PageInterpreter;
import com.myscript.atk.itc.WordRange;
import com.myscript.atk.itc.sample.controller.ChangeCandidateActionProvider;
import com.myscript.atk.itc.sample.controller.MainViewController;
import com.myscript.atk.itc.sample.util.SimpleResourceHelper;
import com.myscript.atk.itc.sample.view.PageView;

public class SampleActivity extends Activity
{
  // ----------------------------------------------------------------------
  // Variables

  private MainViewController mMainViewController;

  // Option menu checked items state
  private boolean isShowGuidelinesChecked = false;
  private boolean isShowTextChecked = true;

  private ActionMode mActionMode;
  
  private final static List<String> LANGUAGE_RESOURCES = new ArrayList<String>(Arrays.asList("en_US/en_US-ak-cur.lite.res",
  																																													 "en_US/en_US-lk-text.lite.res"));

  private ActionMode.Callback mActionModeCallback = new ActionMode.Callback()
  {
    // Called when the action mode is created; startActionMode() was called
    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu)
    {
      // Check for single word selection to display corresponding action menu item
      WordRange selectedWordRange = mMainViewController.getSelectedWordRange();
      if (selectedWordRange != null && selectedWordRange.getWords().size() == 1)
      {
        // Inflate a menu resource providing context menu items
        MenuInflater inflater = mode.getMenuInflater();
        inflater.inflate(R.menu.action_menu, menu);
      }
      return true;
    }

    // Called each time the action mode is shown. Always called after onCreateActionMode, but
    // may be called multiple times if the mode is invalidated.
    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu)
    {
      return false; // Return false if nothing is done
    }

    // Called when the user selects a contextual menu item
    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item)
    {
      final int itemId = item.getItemId();
      if (itemId == R.id.typeset)
      {
        mMainViewController.typesetSelectedWords();
        return true;
      }
      else if (itemId == R.id.change_Candidate)
      {
        ((ChangeCandidateActionProvider) item.getActionProvider()).setSelectedData(mMainViewController.getSelectedWordRange());
        return false;
      }
      return false;
    }

    // Called when the user exits the action mode
    @Override
    public void onDestroyActionMode(ActionMode mode)
    {
      mMainViewController.cancelSelection();
      mActionMode = null;
    }
  };

  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.itc_main);

    PageView pageView = (PageView) findViewById(R.id.itc_pageview);

    // disable hardware acceleration on newer Android versions
    // because this breaks ink rendering
    // see for example http://code.google.com/p/android/issues/detail?id=29944
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    {
      pageView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
    }
    
    // Monitor copying resources for further language change 
  	SimpleResourceHelper helper = new SimpleResourceHelper(this);
  	helper.getResourcePaths(LANGUAGE_RESOURCES);

    mMainViewController = new MainViewController(this, pageView, (TextView) findViewById(R.id.itc_recognitiontextview), savedInstanceState);
  }
  
  @Override
  public void onSaveInstanceState(Bundle savedInstanceState)
  {
  	// Always call the superclass so it can save the view hierarchy state
  	super.onSaveInstanceState(savedInstanceState);
  	
  	mMainViewController.onSaveInstanceState(savedInstanceState);
  }
  
  @Override
  public void onRestoreInstanceState(Bundle savedInstanceState)
  {
  	mMainViewController.onRestoreInstanceState(savedInstanceState);	

  	// Always call the superclass so it can save the view hierarchy state
  	super.onRestoreInstanceState(savedInstanceState);
  }

  @Override
  public void onResume()
  {
    LocalBroadcastManager instance = LocalBroadcastManager.getInstance(this);
    instance.registerReceiver(mMessageLanguageReceiver, new IntentFilter("change-language"));
    instance.registerReceiver(mMessageCandidateReceiver, new IntentFilter("change-candidate"));

    super.onResume();
  }

  @Override
  public void onPause()
  {
    // Unregister since the activity is about to be closed.
    LocalBroadcastManager instance = LocalBroadcastManager.getInstance(this);
    instance.unregisterReceiver(mMessageLanguageReceiver);
    instance.unregisterReceiver(mMessageCandidateReceiver);
    super.onPause();
  }

  private BroadcastReceiver mMessageLanguageReceiver = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {
      mMainViewController.changeLanguage(intent.getStringExtra("lastSelectedLanguage"), intent.getStringArrayExtra("resources"));
    }
  };

  private BroadcastReceiver mMessageCandidateReceiver = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {
      mMainViewController.changeCandidate(intent.getIntExtra("candidateIndex", 0));
    }
  };

  @Override
  public boolean onCreateOptionsMenu(Menu menu)
  {
    getMenuInflater().inflate(R.menu.option_menu, menu);
    return true;
  }
  
  @Override
  public boolean onPrepareOptionsMenu(Menu menu)
  {
    // Guidelines check box monitoring 
    MenuItem showGuidelinesCheckable = menu.findItem(R.id.show_guidelines);
    showGuidelinesCheckable.setChecked(isShowGuidelinesChecked);
        
    // ShowText check box monitoring
    MenuItem showTextCheckable = menu.findItem(R.id.show_text);
    showTextCheckable.setChecked(isShowTextChecked);
    
    
    
        
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item)
  {
    // Handle item selection
    final int itemId = item.getItemId();
    if (itemId == R.id.clean)
    {
      mMainViewController.deletePage();
      return true;
    }
    else if (itemId == R.id.show_text)
    {
      mMainViewController.toggleShowHideText();
      // Check box
      isShowTextChecked = !item.isChecked();
      item.setChecked(!isShowTextChecked);
      return true;
    }
    else if (itemId == R.id.typeset_page)
    {
      mMainViewController.typesetPage();
      return true;
    }
    else if (itemId == R.id.show_guidelines)
    {
      mMainViewController.toggleGuidelines();
      // Check box
      isShowGuidelinesChecked = !item.isChecked();
      item.setChecked(isShowGuidelinesChecked);
      return true;
    }
    else if (itemId == R.id.pen)
    {
      mMainViewController.showDialogForPen(this);
      return true;
    }
    else if (itemId == R.id.load_itf)
    {
      mMainViewController.loadITF();
      return true;
    }
    else if (itemId == R.id.change_page_interpreter)
    {
      mMainViewController.changePageInterpreter();
      return true;
    }
    else if (itemId == R.id.save_page_as_itf)
    {
      // Check box
      mMainViewController.saveITF();
      return true;
    }
    else if (itemId == R.id.gestures)
    {
      mMainViewController.showGesturesSettingsDialog();
      return true;
    }
    else if (itemId == R.id.change_language)
    {
      return true;
    }
    else
    {
      return super.onOptionsItemSelected(item);
    }
  }

  // Page monitoring
  public void previousPage(View view) {
      mMainViewController.previousPage();
  }

  public void nextPage(View view) {
    mMainViewController.nextPage();
}

  // Selection monitoring
  public void onSelectedWordRange()
  {
    if (mActionMode != null)
      return;

    Handler mainHandler = new Handler(getMainLooper());
    mainHandler.post(new Runnable()
    {
      @Override
      public void run()
      {
        // Start the CAB using the ActionMode.Callback defined above
        mActionMode = startActionMode(mActionModeCallback);

        mMainViewController.enableInkCaptureView(false);
      }
    });
  }
  
}