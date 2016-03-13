package com.myscript.atk.itc.sample.controller;

import java.io.File;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.view.ActionProvider;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;

public class ChangeLanguageActionProvider extends ActionProvider
{
  private final Context mContext;

  private String mLastSelectedLanguage = "en_US";

  private File[] mLanguagesDirs = null;

  private MenuItem.OnMenuItemClickListener mOnMenuItemClickListener;

  public ChangeLanguageActionProvider(Context context)
  {
    super(context);
    mContext = context;
    mOnMenuItemClickListener = new MenuItem.OnMenuItemClickListener()
    {
      @Override
      public boolean onMenuItemClick(MenuItem menuItem)
      {
        mLastSelectedLanguage = menuItem.getTitle().toString();

        Intent intent = new Intent("change-language");
        intent.putExtra("lastSelectedLanguage", mLastSelectedLanguage);
        intent.putExtra("resources", mLanguagesDirs[menuItem.getItemId()].list());
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

    File languagesDir = mContext.getExternalCacheDir();
    mLanguagesDirs = languagesDir.listFiles();

    if (mLanguagesDirs != null && mLanguagesDirs.length > 0)
    {
      for (int i = 0; i < mLanguagesDirs.length; ++i)
      {
        String languageName = mLanguagesDirs[i].getName();
        if (!languageName.equals(mLastSelectedLanguage))
          subMenu.add(Menu.NONE, i , Menu.NONE, languageName).setOnMenuItemClickListener(mOnMenuItemClickListener);
      }
    }
  }

}
