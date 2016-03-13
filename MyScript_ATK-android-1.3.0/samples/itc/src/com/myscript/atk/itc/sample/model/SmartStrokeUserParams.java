package com.myscript.atk.itc.sample.model;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import android.content.res.Resources;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;

import com.myscript.atk.itc.IUserParams;
import com.myscript.atk.itc.sample.R;
import com.myscript.atk.itc.Rect;
import com.myscript.atk.itc.sample.util.FormattedDataManager;

public class SmartStrokeUserParams implements IUserParams
{
  private RectF mFrame;
  private Path mPath;
  private Paint mPaint;
  private Rect mSelectedRect;
  private boolean mRecognized;
  private boolean mIsDrawing;
  private boolean mUnderlined;
  private FormattedDataManager.FormattedData mFd;
  private Resources mRes;

  public SmartStrokeUserParams(RectF frame, Path path, Paint paint, boolean isDrawing, Resources res)
  {
    mFrame = frame;
    mPath = path;
    mPaint = paint;
    mIsDrawing = isDrawing;
    mRes = res;
  }

  public SmartStrokeUserParams(SmartStrokeUserParams strokeUserParams, Resources res)
  {
    mFrame = new RectF(strokeUserParams.getFrame());
    mPath = new Path(strokeUserParams.getPath());
    mPaint = new Paint(strokeUserParams.getPaint());

    mSelectedRect = strokeUserParams.getSelectedRect();
    mRecognized = strokeUserParams.isRecognized();
    mFd = strokeUserParams.getFormattedData();
    mUnderlined = strokeUserParams.isUnderlined();
    mIsDrawing = strokeUserParams.isDrawingStroke();
    mRes = res;
  }

  public SmartStrokeUserParams(byte[] bytes, Resources res)
  {
    initUserParametersWithBytes(bytes);
    mRes = res;
  }
  
  public void updateStrokeUserParameters(Path path, RectF frame, Paint paint, Resources res)
  {
    mPath = path;
    mFrame = frame;
    mPaint = paint;

    // Monitor paint color
    if (mIsDrawing)
      mPaint.setColor(mRes.getColor(R.color.ITC_DRAWING_INK_COLOR));
    else if (mRecognized)
    {
      if (mFd == null)
        mPaint.setColor(mRes.getColor(R.color.ITC_RECOGNIZED_INK_COLOR));
      else
        mPaint.setColor(mRes.getColor(R.color.ITC_TINT_COLOR));
    }
  }

  // ----------------------------------------------------------------------
  // Parcelable interface implementation

  // TODO: Parcelable
/*
  public SmartStrokeUserParams(Parcel in)
  {
    // Read the user parameter byte array length
    int userParamsBytesLength = in.readInt();

    // Read the user parameter byte array itself
    byte[] userParamsBytes = new byte[userParamsBytesLength];
    in.readByteArray(userParamsBytes);

    // Wrap it in a byte array input stream
    ByteArrayInputStream bais = new ByteArrayInputStream(userParamsBytes);

    // Create a page input archive
    DataInputStream ia = new DataInputStream(bais);

    // Initialize variables
    mFrame = null;
    mPath = null;
    mPaint = null;

    try
    {
      mRecognized = ia.readBoolean();
      mIsDrawing = ia.readBoolean();
      mSelected = ia.readBoolean();
      mUnderlined = ia.readBoolean();
      mFormattedData = ia.readBoolean();
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
  }

  @Override
  public void writeToParcel(Parcel out, int flags)
  {
    // Get the byte array
    byte[] strokeUserParamsBytes = toByteArray();

    // Write the byte array length
    out.writeInt(strokeUserParamsBytes.length);

    // Write the stroke user parameters byte array
    out.writeByteArray(strokeUserParamsBytes);
  }

  public static final Parcelable.Creator<VOStrokeUserParams> CREATOR = new Parcelable.Creator<VOStrokeUserParams>()
  {
    @Override
    public VOStrokeUserParams createFromParcel(Parcel in)
    {
      return new VOStrokeUserParams(in);
    }

    @Override
    public VOStrokeUserParams[] newArray(int size)
    {
      return new VOStrokeUserParams[size];
    }
  };

  @Override
  public int describeContents()
  {
    return 0;
  }

  public void initStrokeUserParams(SmartStroke stroke)
  {
    // Get the user parameters factory
    VOUserParamsFactory vupf = VOUserParamsFactory.getInstance(null);

    if (vupf != null)
    {
      // Create the main path
      vupf.computeUserParamsForStroke(stroke);

      // Assign computed valued stored in the stroker
      Stroker stroker = vupf.getStroker();
      mFrame = new RectF(stroker.getBoundingRect());
      mPath = new Path(stroker.getPath());
      mPaint = new Paint(stroker.getPaint());

      // Check for computed selected path
      if (mSelected && mSelectedPath == null)
      {
        // Compute the the selected path
        mSelectedPath = vupf.createStrokeSubPath(stroke, mBeginSelectedPath, mEndSelectedPath);
      }

      // Check for computed underlined paths
      if (mUnderlined && mUnderlinedPath.isEmpty())
      {
        // Parse the underline paths boundaries and compute the paths
        int underlinePathsSize = mBeginUnderlinedPath.size();
        for (int i = 0; i < underlinePathsSize; ++i)
          mUnderlinedPath.add(vupf.createStrokeSubPath(stroke, mBeginUnderlinedPath.get(i), mEndUnderlinedPath.get(i)));
      }
    }
  }*/

  private void initUserParametersWithBytes(byte[] userParamsBytes)
  {
    ByteArrayInputStream bais = new ByteArrayInputStream(userParamsBytes);

    // Create a page input archive
    DataInputStream ia = new DataInputStream(bais);

    // Recognized monitoring
    try
    {
      setRecognized(ia.readBoolean());
      setDrawing(ia.readBoolean());
      setUnderlined(ia.readBoolean());

      if (ia.readBoolean())
      {
        int bytesLength = ia.readInt();
        if (bytesLength > 0)
        {
          byte[] bytes = new byte[bytesLength];
          if (ia.read(bytes) > 0)
          {
            FormattedDataManager.FormattedData fd = new FormattedDataManager.FormattedData();
            fd.formattedText = new String(bytes);
            fd.type = FormattedDataManager.FormattedDataType.values()[ia.readInt()];
            bytesLength = ia.readInt();
            if (bytesLength > 0)
            {
              bytes = new byte[bytesLength];
              if (ia.read(bytes) > 0)
              {
                fd.fullText = new String(bytes);
                fd.startIndex = ia.readInt();
                fd.endIndex = ia.readInt();
              }
            }
            setFormattedData(fd);
          }
        }
      }
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
  }

  // ----------------------------------------------------------------------
  // Public methods implementation
  
  public Paint getPaint()
  {
    return mPaint;
  }
  
  public RectF getFrame()
  {
    return mFrame;
  }
  
  public Path getPath()
  {
    return mPath;
  }

  public boolean isDrawingStroke()
  {
    return mIsDrawing;
  }

  public void setDrawing(boolean isDrawing)
  {
    if (mIsDrawing != isDrawing)
    {
      mIsDrawing = isDrawing;
      if (mPaint != null && mIsDrawing)
        mPaint.setColor(mRes.getColor(R.color.ITC_DRAWING_INK_COLOR));
    }
  }
  
  public void setRecognized(boolean recognized)
  {
    mRecognized = recognized;

    // Monitor colors
    if (mPaint != null && mRecognized && mFd == null)
      mPaint.setColor(mRes.getColor(R.color.ITC_RECOGNIZED_INK_COLOR));
  }

  public boolean isRecognized()
  {
    return mRecognized;
  }
  
  public void setSelectedRect(final Rect selectedRect)
  {
    mSelectedRect = selectedRect;
  }

  public Rect getSelectedRect()
  {
    return mSelectedRect;
  }

  public void setFormattedData(final FormattedDataManager.FormattedData fd)
  {
    mFd = fd;

    // Monitor colors
    if (mPaint != null)
    {
      if (mFd != null)
        mPaint.setColor(mRes.getColor(R.color.ITC_TINT_COLOR));
      else
      {
        if (mIsDrawing)
          mPaint.setColor(mRes.getColor(R.color.ITC_DRAWING_INK_COLOR));
        else if (mRecognized)
          mPaint.setColor(mRes.getColor(R.color.ITC_RECOGNIZED_INK_COLOR));
      }
    }
  }

  public FormattedDataManager.FormattedData getFormattedData()
  {
    return mFd;
  }
  
  public void setUnderlined(boolean underlined)
  {
    if (mUnderlined != underlined)
      mUnderlined = underlined;
  }

  public boolean isUnderlined()
  {
    return mUnderlined;
  }
  
  @Override
  public byte[] toByteArray()
  {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();

    // Create a page input archive 
    DataOutputStream oa = new DataOutputStream(baos);

    try
    {
      oa.writeBoolean(mRecognized);
      oa.writeBoolean(mIsDrawing);
      oa.writeBoolean(mUnderlined);

      // Check for existing formatted data
      if (mFd != null)
      {
        oa.writeBoolean(true);

        // Get the mText String bytes length
        byte[] localeBytes = ((mFd.formattedText != null) ? mFd.formattedText.getBytes() : null);
        if (localeBytes != null)
        {
          oa.writeInt(localeBytes.length);
          oa.write(localeBytes);
          oa.writeInt(mFd.type.ordinal());
          localeBytes = ((mFd.fullText != null) ? mFd.fullText.getBytes() : null);
          if (localeBytes != null)
          {
            oa.writeInt(localeBytes.length);
            oa.write(localeBytes);
            oa.writeInt(mFd.startIndex);
            oa.writeInt(mFd.endIndex);
          }
          else
            oa.writeInt(0);
        }
        else
          oa.writeInt(0);
      }
      else
        oa.writeBoolean(false);

      oa.close();
    }
    catch (IOException e1)
    {
      e1.printStackTrace();
    }

    return baos.toByteArray();
  }
}
