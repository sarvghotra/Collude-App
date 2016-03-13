package com.myscript.atk.itc.sample.controller;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.view.ActionProvider;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;

import com.myscript.atk.itc.SmartWord;
import com.myscript.atk.itc.WordRange;

import java.util.List;

public class ChangeCandidateActionProvider extends ActionProvider
{
  private final Context mContext;

  private WordRange mSelectedWordRange;

  private MenuItem.OnMenuItemClickListener mOnMenuItemClickListener;

  public ChangeCandidateActionProvider(Context context)
  {
    super(context);

    mContext = context;

    mOnMenuItemClickListener = new MenuItem.OnMenuItemClickListener() {
      @Override
      public boolean onMenuItemClick(MenuItem menuItem)
      {
        Intent intent = new Intent("change-candidate");
        intent.putExtra("candidateIndex", menuItem.getItemId());
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);

        return true;
      }
    };
  }

  @Override
  public View onCreateActionView()
  {
    return null;
  }

  @Override
  public View onCreateActionView(MenuItem menuItem)
  {
    return null;
  }

  @Override
  public boolean hasSubMenu()
  {
    return true;
  }

  @Override
  public void onPrepareSubMenu(SubMenu subMenu)
  {
    // Clear the sub menu
    subMenu.clear();

    // Get the long pressed word range and check for multiple only one word to enable the feature
    List<SmartWord> selectedWords = mSelectedWordRange.getWords();

    if (!selectedWords.isEmpty() && selectedWords.size() == 1)
    {
      SmartWord selectedWord = selectedWords.get(0);

      int selectedIndex = selectedWord.getSelectedCandidateIndex();

      // Populate the sub menu with the word candidates
      List<String> wordCandidates = selectedWord.getCandidates();
      for (int i = 0; i < wordCandidates.size(); ++i)
        if (i != selectedIndex)
          subMenu.add(Menu.NONE, i , Menu.NONE, wordCandidates.get(i)).setOnMenuItemClickListener(mOnMenuItemClickListener);
    }
  }

  public void setSelectedData(WordRange selectedRange)
  {
    mSelectedWordRange = selectedRange;
  }
}
