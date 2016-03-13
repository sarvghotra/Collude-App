// Copyright MyScript

package com.myscript.atk.sltw.sample.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.content.Context;
import android.util.Log;

/** This class takes care of extracting handwriting resource files from the assets folder. */
public class SimpleResourceHelper
{
  public static final String TAG = "SimpleResourceHelper";

  private Context mContext;
  
  public SimpleResourceHelper(Context context)
  {
    mContext = context;
  }

  /** Get absolute an array of absolute paths for the given resources.
   * Extract resources from assets if required. */
  public String[] getResourcePaths(final String[] resources)
  {
    final String[] paths = new String[resources.length];
    
    final File cacheDir = mContext.getExternalCacheDir();
    
    for (int i=0; i<resources.length; i++) {
      File resourceFile = new File(cacheDir, resources[i]);
      if (!resourceFile.exists()) {
        copyResourceFromAssets(resources[i], resourceFile);
      }
      paths[i] = resourceFile.getAbsolutePath();
    }
    
    return paths;
  }
  
  /** Copy a handwriting resource file from assets to given destination file. */
  private void copyResourceFromAssets(final String resource, final File dest)
  {
    try {
      dest.getParentFile().mkdirs();
      
      BufferedInputStream in = new BufferedInputStream(mContext.getAssets().open(resource));
      BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(dest));
      
      byte[] buf = new byte[8192];
      int size;
      while ((size = in.read(buf)) != -1) {
        out.write(buf, 0, size);
      }
  
      in.close();
      out.close();
    } catch (FileNotFoundException e) {
      Log.e(TAG, "Cannot create file " + dest);
    } catch (IOException e) {
      Log.e(TAG, "Error while extracting resource " + resource + " from assets");
    }
  }
}
