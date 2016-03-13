package com.myscript.atk.itc.sample.model;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import android.content.res.Resources;
import android.graphics.Paint;
import android.graphics.Typeface;

import com.myscript.atk.itc.IUserParams;
import com.myscript.atk.itc.sample.R;
import com.myscript.atk.itc.sample.util.FormattedDataManager;

public class SmartWordUserParams implements IUserParams
{
  // ----------------------------------------------------------------------
  // Variables

  /** Paint used for word character drawing **/
  private Paint mPaint;

  private boolean mSelected;
  private boolean mUnderlined;
  private FormattedDataManager.FormattedData mFd;
  private Resources mRes;

  // ----------------------------------------------------------------------
  // Variables

  /** Return a new VOWordCharacterUserParams with the given paint. */
  public SmartWordUserParams(Paint paint, Resources res)
  {
    mPaint = paint;
    mRes = res;
  }

  /** Return a copy of the given VOWordCharacterUserParamswith. */
  public SmartWordUserParams(SmartWordUserParams wcup, Resources res)
  {
    mPaint = wcup.getPaint();
    mSelected = wcup.isSelected();
    mUnderlined = wcup.isUnderlined();
    mFd = wcup.getFormattedData();
    mRes = res;
  }

  public SmartWordUserParams(byte[] bytes, Resources res)
  {
    initUserParametersWithBytes(bytes);
    mRes = res;
  }

  // ----------------------------------------------------------------------
  // Parcelable interface implementation

  // TODO: Parcelable
/*
  private SmartWordUserParams(Parcel in)
  {
    // Read the user parameter byte array length
    int userParamsBytesLength = in.readInt();

    // Read the user parameter byte array itself
    byte[] userParamsBytes = new byte[userParamsBytesLength];
    in.readByteArray(userParamsBytes);

    // Initialize the different properties
    initUserParametersWithBytes(userParamsBytes);
  }

  public void writeToParcel(Parcel out, int flags)
  {
    // Get the byte array
    byte[] wordCharUserParamsBytes = toByteArray();

    // Write the byte array length
    out.writeInt(wordCharUserParamsBytes.length);

    // Write the stroke user parameters byte array
    out.writeByteArray(wordCharUserParamsBytes);
  }

  public static final Parcelable.Creator<SmartWordUserParams> CREATOR = new Parcelable.Creator<SmartWordUserParams>()
  {
    public SmartWordUserParams createFromParcel(Parcel in)
    {
      return new SmartWordUserParams(in);
    }

    public SmartWordUserParams[] newArray(int size)
    {
      return new SmartWordUserParams[size];
    }
  };

  public int describeContents()
  {
    return 0;
  }*/

  private void initUserParametersWithBytes(byte[] userParamsBytes)
  {
    mPaint = new Paint();
    mPaint.setAntiAlias(true);
    mPaint.setTypeface(Typeface.DEFAULT);

    ByteArrayInputStream bais = new ByteArrayInputStream(userParamsBytes);

    // Create a page input archive
    DataInputStream ia = new DataInputStream(bais);

    try
    {
      mPaint.setTextSize(ia.readFloat());
      setSelected(ia.readBoolean());
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

  /** Return the paint of this word character user parameters. */
  public Paint getPaint()
  {
    return mPaint;
  }

  public void setSelected(boolean selected)
  {
    if (mSelected != selected)
      mSelected = selected;
  }

  public boolean isSelected()
  {
    return mSelected;
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

  public void setFormattedData(final FormattedDataManager.FormattedData fd)
  {
    if (mPaint != null)
    {
      mFd = fd;
      if (mFd != null)
        mPaint.setColor(mRes.getColor(R.color.ITC_TINT_COLOR));
      else
        mPaint.setColor(mRes.getColor(R.color.ITC_TO_BE_RECOGNIZED_INK_COLOR));
    }
  }

  public FormattedDataManager.FormattedData getFormattedData()
  {
    return mFd;
  }

  @Override
  public byte[] toByteArray()
  {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();

    // Create a page input archive 
    DataOutputStream oa = new DataOutputStream(baos);

    try
    {
      oa.writeFloat(mPaint.getTextSize());
      oa.writeBoolean(mSelected);
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
