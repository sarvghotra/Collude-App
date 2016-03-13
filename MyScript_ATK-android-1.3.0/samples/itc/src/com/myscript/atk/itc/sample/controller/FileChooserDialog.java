package com.myscript.atk.itc.sample.controller;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Environment;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.myscript.atk.itc.sample.R;

public class FileChooserDialog 
{
  public static enum DirType
  {
    ITF_DIR
  }
  public static final String SAVE_ITF_FILES_PATH = Environment.getExternalStorageDirectory() + "/itcSampleITF/";
  
  private String mDirectory = "";
  private Context mContext;
  private ArrayAdapter<String> mFileListAdapter;
  private ChosenFileListener mChosenFileListener;
  private Dialog mFileDialog;

  // Listener for selected file
  public interface ChosenFileListener 
  {
    public void onChosenFile(String chosenFile);
  }

  public FileChooserDialog(Context context, DirType dirType, ChosenFileListener chosenFileListener)
  {
    mContext = context;
    mDirectory = (dirType == DirType.ITF_DIR ? SAVE_ITF_FILES_PATH : "");
    mChosenFileListener = chosenFileListener;

    try
    {
      mDirectory = new File((dirType == DirType.ITF_DIR ? SAVE_ITF_FILES_PATH : "")).getCanonicalPath();
    }
    catch (IOException ioe)
    {
      ioe.printStackTrace();
    }
    
    mFileDialog = createChooseFileDialog(getFiles());
  }
  
  public void showFileDialog()
  {
    if (mFileDialog != null)
      mFileDialog.show();
  }
  
  public void dismissDialog()
  {
    if (mFileDialog != null)
      mFileDialog.dismiss();    
  }

  private HashMap<String, String> getFiles()
  {
    final HashMap<String, String> files = new HashMap<String, String>();

    try
    {
      File dirFile = new File(mDirectory);
      if (! dirFile.exists() || ! dirFile.isDirectory())
        return files;

      for (File file : dirFile.listFiles()) 
        files.put(file.getName(), file.getCanonicalPath());
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }

    return files;
  }

  private Dialog createChooseFileDialog(final HashMap<String, String> listItems)
  {
    AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(mContext);
    dialogBuilder.setTitle(R.string.choose_itf);
    
    final List<String> keys = new ArrayList<String>(listItems.keySet());

    mFileListAdapter = createListAdapter(keys);

    dialogBuilder.setSingleChoiceItems(mFileListAdapter, -1, new OnClickListener()
    {
      @Override
      public void onClick(DialogInterface arg0, int arg1)
      {
        mChosenFileListener.onChosenFile(listItems.get(keys.get(arg1)));
      }
    });
    // OK button monitoring
    dialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int buttonIndex){}
    });

    // Return the created Dialog
    return dialogBuilder.create();
  }

  private ArrayAdapter<String> createListAdapter(List<String> items)
  {
    return new ArrayAdapter<String>(mContext, android.R.layout.select_dialog_item, android.R.id.text1, items) {
      @Override
      public View getView(int position, View convertView, ViewGroup parent) 
      {
        View v = super.getView(position, convertView, parent);

        if (v instanceof TextView)
        {
          TextView tv = (TextView) v;
          tv.getLayoutParams().height = LayoutParams.WRAP_CONTENT;
        }
        return v;
      }
    };
  }
} 