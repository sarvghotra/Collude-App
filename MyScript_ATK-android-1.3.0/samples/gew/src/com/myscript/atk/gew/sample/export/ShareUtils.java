// Copyright MyScript. All right reserved.

package com.myscript.atk.gew.sample.export;

import java.io.File;
import java.io.FileOutputStream;
import java.util.UUID;

import org.apache.commons.io.IOUtils;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import com.myscript.atk.gew.sample.BuildConfig;

/**
 * Helping methods for sharing things.
 */
public abstract class ShareUtils
{

  /** Debug tag. */
  private static final String TAG = "ShareUtils";
  /** Debug flag. */
  private static final boolean DBG = true && BuildConfig.DEBUG;

  /** Compression value. */
  private static final int PNG_COMPRESS_VALUE = 90;

  /** Utils. */
  private ShareUtils()
  {

  }

  /**
   * Return if the external storage in mounted (available).
   * 
   */
  public static final boolean isExternalStorageMounted() {
      boolean mExternalStorageAvailable = false;
      boolean mExternalStorageWriteable = false;

      String state = Environment.getExternalStorageState();
      if (Environment.MEDIA_MOUNTED.equals(state)) {
          mExternalStorageAvailable = mExternalStorageWriteable = true;
      } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
          mExternalStorageAvailable = true;
          mExternalStorageWriteable = false;
      } else {
          mExternalStorageAvailable = mExternalStorageWriteable = false;
      }
      return (mExternalStorageAvailable & mExternalStorageWriteable);
  }

  /**
   * Write the bitmap to share to the cache folder.
   * 
   * @param context
   *          The context
   * @param bitmap
   *          The bitmap to share
   * @return The created file
   */
  public static final File writeBitmapToCache(final Context context, final Bitmap bitmap)
  {
    FileOutputStream out = null;
    File shareFile = null;
    try
    {    	
      final File cacheDir = context.getExternalCacheDir();
      if (cacheDir.exists() == false)
        cacheDir.mkdirs();
      File exportdir=new File(cacheDir, "export");
      if (exportdir.exists() == false)
    	  exportdir.mkdirs();
      
      // Old file created in cache can be locked by another app.
      // try to delete old files to avoid leaks.
      if (exportdir.isDirectory())
      {
        String[] children = exportdir.list();
        for (int i = 0; i < children.length; i++)
        {
          new File(exportdir, children[i]).delete();
        }
      }
      
      // Create new file with random filename,
      // unique filename can be locked by a previous export.
  	  final String filename = "tempExportFiles" + UUID.randomUUID().toString().substring(0, 8/* max value for UUID split */) + ".png";

      shareFile = new File(cacheDir, filename);
      out = new FileOutputStream(shareFile);
      bitmap.compress(Bitmap.CompressFormat.PNG, PNG_COMPRESS_VALUE, out);

    }
    catch (final Exception e)
    {
      if (DBG)
        Log.e(TAG, "Fail to create bitmap to cache", e);
    }
    finally
    {
      IOUtils.closeQuietly(out);
    }

    return shareFile;
  }

  /**
   * Share a text.
   * 
   * @param context
   *          The context.
   * @param text
   *          The text to share.
   */
  public static final void shareText(final Context context, final String text)
  {
    final Intent sendIntent = new Intent();
    sendIntent.setAction(Intent.ACTION_SEND);
    sendIntent.setType("text/plain");
    sendIntent.putExtra(Intent.EXTRA_TEXT, text);
    context.startActivity(sendIntent);
  }

  /**
   * Share a bitmap with external storage method (because Facebook sucks !).<br />
   * Need WRITE_EXTERNAL_STORAGE permission.
   * 
   * @param context
   *          The context.
   * @param text
   *          The text to share.
   * @param shareFile
   *          The png file to share.
   * @return {@code true} if the bitmap is shared, {@code false} if an exception has occurred.
   */
  public static final boolean shareBitmap(final Context context, final String text, final File shareFile)
  {

    // Record this image in media store (this is mandatory for google+)
    final ContentValues values = new ContentValues(2);
    values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
    values.put(MediaStore.Images.Media.DATA, shareFile.getAbsolutePath());
    final Uri uri = context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

    // Prepare the intent
    final Intent sendIntent = new Intent();
    sendIntent.setAction(Intent.ACTION_SEND);
    sendIntent.setType("image/png");
    sendIntent.putExtra(Intent.EXTRA_TEXT, text);
    sendIntent.putExtra(Intent.EXTRA_STREAM, uri);
    context.startActivity(sendIntent);

    return true;
  }

}
