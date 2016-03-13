// Copyright MyScript. All right reserved.

package com.myscript.atk.gew.sample.export;

import java.io.File;
import java.io.FileNotFoundException;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import com.myscript.atk.gew.sample.BuildConfig;

public class ExportContentProvider extends ContentProvider
{

  /** Content uri. */
  public static final Uri CONTENT_URI = Uri
      .parse("content://com.myscript.atk.gew.export.ExportContentProvider");

  /** Debug tag. */
  private static final String TAG = "ExportContentProvider";
  /** Debug flag. */
  private static final boolean DBG = true && BuildConfig.DEBUG;

  @Override
  public ParcelFileDescriptor openFile(final Uri uri, final String mode)
  {
    if (DBG)
      Log.d(TAG, "openFile " + uri + " Mode : " + mode);

    int imode = 0;
    if (mode.contains("w"))
      imode |= ParcelFileDescriptor.MODE_WRITE_ONLY;
    if (mode.contains("r"))
      imode |= ParcelFileDescriptor.MODE_READ_ONLY;
    if (mode.contains("+"))
      imode |= ParcelFileDescriptor.MODE_APPEND;
    try
    {
      return ParcelFileDescriptor.open(new File(getContext().getFilesDir(), uri.getEncodedPath()), imode);
    }
    catch (final FileNotFoundException e)
    {
      if (DBG)
        Log.e(TAG, "Error while reading file : " + e.getClass().getSimpleName(), e);
    }
    return null;
  }

  @Override
  public int delete(final Uri uri, final String s, final String[] as)
  {
    return 0;
  }

  @Override
  public String getType(final Uri uri)
  {
    return null;
  }

  @Override
  public Uri insert(final Uri uri, final ContentValues contentvalues)
  {
    return null;
  }

  @Override
  public boolean onCreate()
  {
    return true;
  }

  @Override
  public Cursor query(final Uri uri, final String[] projection, final String selection, final String[] selectionArgs,
      final String sortOrder)
  {
    return null;
  }

  @Override
  public int update(final Uri uri, final ContentValues contentvalues, final String s, final String[] as)
  {
    return 0;
  }
}