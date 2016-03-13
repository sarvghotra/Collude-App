// Copyright MyScript. All right reserved.

package com.myscript.atk.gew.sample.export;

import java.io.File;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;

import com.myscript.atk.gew.sample.export.ShareUtils;
import com.myscript.atk.gew.sample.BuildConfig;

/**
 * Manage content sharing.
 */
public class BitmapMakerTask extends AsyncTask<Void, Void, File>
{
  /**
   * Callback for Async bitmap creation.
   */
  public interface BitmapMakerCallback
  {
    /**
     * Called when load is complete.
     * 
     * @param result
     *          The result
     */
    void onResult(final File result);
  }

  /** Debug tag. */
  private static final boolean DBG = BuildConfig.DEBUG;
  private static final String TAG = "MainActivity$BitmapMakerTask";

  /** Associated activity. */
  private Context mContext;
  private BitmapMakerCallback mCallback;

  /** Bitmap to share. */
  private Bitmap mBitmap;

  /**
   * Constructor.
   * 
   * @param callback
   *          The callback.
   */
  public BitmapMakerTask(final Context context, final Bitmap bitmap, final BitmapMakerCallback callback)
  {
    if (DBG)
      Log.d(TAG, "BitmapMakerTask " + callback);
    mContext = context;
    mCallback = callback;
    mBitmap = bitmap;
  }

  @Override
  protected void onPreExecute()
  {
    if (DBG)
      Log.d(TAG, "onPreExecute");
  }

  @Override
  protected File doInBackground(final Void... params)
  {
    if (DBG)
      Log.d(TAG, "doInBackground");

    if (mBitmap == null)
      return null;

    final File shareFile = ShareUtils.writeBitmapToCache(mContext, mBitmap);

    return shareFile;
  }

  @Override
  protected void onCancelled()
  {
    if (DBG)
      Log.d(TAG, "onCancelled");
    mCallback = null;
  }

  @Override
  protected void onPostExecute(final File result)
  {
    if (isCancelled() == false && mCallback != null)
    {
      mCallback.onResult(result);
    }
    else
    {
      if (DBG)
        Log.w(TAG, "onPostExecute : cancelled or no callback.");
    }
  }
}
