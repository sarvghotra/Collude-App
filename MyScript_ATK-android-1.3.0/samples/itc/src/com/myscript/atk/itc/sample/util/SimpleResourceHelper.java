// Copyright MyScript

package com.myscript.atk.itc.sample.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.util.Log;

/**
 * This class takes care of extracting handwriting resource files from the
 * assets folder.
 */
public class SimpleResourceHelper
{
  /** Debug tag. */
  public final static String TAG = "SimpleResourceHelper";

  /** A context to read into assets folder. */
  private Context mContext;

  /**
   * Instantiates the SimpleResourceHelper.
   * 
   * @param context
   *          The context used to retrieve resources.
   */
  public SimpleResourceHelper(final Context context)
  {
    mContext = context;
  }

  /**
   * Retrieves an array containing the absolute paths of the given resources
   * locations. Extracts the resources from assets if required.
   * 
   * @param resources
   *          A list of resources names (e.g. : "en_US/en_US-ak-cur.lite.res").
   * @return A list of the resources absolute paths.
   */
  public List<String> getResourcePaths(final List<String> resources)
  {
    final List<String> paths = new ArrayList<String>();
    final File cacheDir = mContext.getExternalCacheDir();

    for (final String res : resources)
    {
      final File resourceFile = new File(cacheDir, res);
      if (!resourceFile.exists())
        copyResourceFromAssets(res, resourceFile);
      paths.add(resourceFile.getAbsolutePath());
    }
    return paths;
  }

  /**
   * Copies a handwriting resource file from assets to the given location.
   * 
   * @param resource
   *          The resource file name (relative to the assets folder).
   * @param dest
   *          The destination file for the resource.
   */
  private void copyResourceFromAssets(final String resource, final File dest)
  {
    try
    {
      dest.getParentFile().mkdirs();

      final BufferedInputStream in = new BufferedInputStream(mContext.getAssets().open(resource));
      final BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(dest));

      final byte[] buf = new byte[8192];
      int size;
      while ((size = in.read(buf)) != -1)
        out.write(buf, 0, size);

      in.close();
      out.close();
    }
    catch (FileNotFoundException e)
    {
      Log.e(TAG, "Cannot create file " + dest);
    }
    catch (IOException e)
    {
      Log.e(TAG, "Error while extracting resource " + resource + " from assets.");
    }
  }
}