// Copyright MyScript

package com.myscript.atk.sltw.sample.controller;

import android.view.View;
import android.widget.ToggleButton;

import com.myscript.atk.sltw.sample.R;

/** This class implements a simple toolbar controller. */
public class ToolbarController
{
  public static int CURSIVE_MODE = 0;
  public static int BOX_MODE = 1;

  public interface OnSpaceButtonClickedListener {
    public void onSpaceButtonClicked();
  }
  public interface OnDeleteButtonClickedListener {
    public void onDeleteButtonClicked();
  }
  public interface OnModeButtonClickedListener {
    public void onModeButtonClicked(int mode);
  }

  private OnSpaceButtonClickedListener      mOnSpaceButtonClickedListener;
  private OnDeleteButtonClickedListener     mOnDeleteButtonClickedListener;
  private OnModeButtonClickedListener       mOnModeButtonClickedListener;
  
  private ToggleButton    mModeButton;
  
  public ToolbarController(View v)
  {
    v.findViewById(R.id.sltw_spaceButton).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        notifySpaceButtonClicked();
      }
    });
    
    v.findViewById(R.id.sltw_deleteButton).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        notifyDeleteButtonClicked();
      }
    });
    
    v.findViewById(R.id.sltw_languageButton).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if (mOnModeButtonClickedListener != null) {
          mOnModeButtonClickedListener.onModeButtonClicked(getMode());
        }
      }
    });
    
    mModeButton = (ToggleButton) v.findViewById(R.id.sltw_languageButton);
  }

  public void setOnSpaceButtonClickedListener(OnSpaceButtonClickedListener l)
  {
    mOnSpaceButtonClickedListener = l;
  }

  public void setOnDeleteButtonClickedListener(OnDeleteButtonClickedListener l)
  {
    mOnDeleteButtonClickedListener = l;
  }
  
  public void setOnModeButtonClickedListener(OnModeButtonClickedListener l)
  {
    mOnModeButtonClickedListener = l;
  }

  public int getMode()
  {
    return mModeButton.isChecked() ?  BOX_MODE : CURSIVE_MODE;
  }
  
  public void notifySpaceButtonClicked()
  {
    if (mOnSpaceButtonClickedListener != null) {
      mOnSpaceButtonClickedListener.onSpaceButtonClicked();
    }
  }
  
  public void notifyDeleteButtonClicked()
  {
    if (mOnDeleteButtonClickedListener != null) {
      mOnDeleteButtonClickedListener.onDeleteButtonClicked();
    }
  }
}
