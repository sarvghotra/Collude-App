// Copyright MyScript

package com.myscript.atk.itc.sample.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;

import com.myscript.atk.itc.Char;
import com.myscript.atk.itc.sample.R;
import com.myscript.atk.itc.SmartWord;
import com.myscript.atk.itc.SmartWordCharDisplayInfo;
import com.myscript.atk.itc.sample.Debug;
import com.myscript.atk.itc.sample.model.SmartWordUserParams;

import java.util.ArrayList;
import java.util.List;

public class TypesetSmartWordView
{
  private SmartWord mWord;
  private List<RectF> mWordCharacterBoxes;
  private Rect mFrame;
  private Paint mPaintSelection;
  private Paint mPaintDebug;
  private List<SmartWordCharDisplayInfo> mDisplayList;
  private char[] mReshapedChars;
  private Context mContext;

  public TypesetSmartWordView(SmartWord word, Context context)
  {
    mContext = context;

    mWord = word;
    
    // Initialize the Paint debug
    mPaintDebug = new Paint();
    mPaintDebug.setStyle(Paint.Style.STROKE);
    mPaintDebug.setStrokeWidth(3);

    // Initialize the Paint selection
    mPaintSelection  = new Paint();
    mPaintSelection.setColor(Color.BLUE);
    mPaintSelection.setAlpha(50);

    // Get the character boxes of the word for debugging purpose
    final List<com.myscript.atk.itc.Rect> charBoxes = mWord.getCharBoxes();
    mWordCharacterBoxes = new ArrayList<RectF>(charBoxes.size());
    for(com.myscript.atk.itc.Rect box : charBoxes)
      mWordCharacterBoxes.add(itcRectToAndroidRectF(box));
    
    // Get the frame of the word used in case of selection and other debugging purpose 
    mFrame = new Rect();
    mWord = word; 
    com.myscript.atk.itc.Rect rect = mWord.getDisplayFrame(); 
    new RectF(rect.getLeft(), rect.getTop(), rect.getRight(), rect.getBottom()).roundOut(mFrame);

    // Convert the list of UTF-8 encoded Char to an array of UTF-16 Java char
    mReshapedChars = convertUTF8CharToUTF16(mWord.getReshapedChars());

    mDisplayList = mWord.getCharDisplayInfoList();
  }

  private static char[] convertUTF8CharToUTF16(List<Char> utf8_chars)
  {
    List<Character> convertedCharacters = new ArrayList<Character>();
    for (Char utf8_char : utf8_chars)
    {
      char[] convertedChars = Character.toChars((int)utf8_char.unicode());
      for (char convertedChar : convertedChars)
        convertedCharacters.add(convertedChar);
    }

    // Create the char array
    char[] chars = new char[convertedCharacters.size() + 1];
    for (int i = 0; i < convertedCharacters.size(); ++i) {
      Character character = convertedCharacters.get(i);
      chars[i] = character;
    }

    return chars;
  }
  
  private static RectF itcRectToAndroidRectF(com.myscript.atk.itc.Rect itcRect)
  {
    return new RectF(itcRect.getLeft(), itcRect.getTop(), itcRect.getRight(), itcRect.getBottom());
  }
  
  private void drawRightToLeftWord(final Canvas canvas)
  {
    for (SmartWordCharDisplayInfo item : mDisplayList)
    {
      // Store values to avoid JNI calls while drawing
      final int itemIndex = item.getIndex();
      final int itemCount = item.getCount();
      final int itemEndIndex = itemIndex + itemCount;
      char savedChar = mReshapedChars[itemEndIndex];
      mReshapedChars[itemEndIndex] = '\u200f';
      drawWordChars(mReshapedChars, itemIndex, itemCount+1, item.getX(), canvas);
      mReshapedChars[itemEndIndex] = savedChar;
    }
  }

  private void drawLeftToRightWord(final Canvas canvas)
  {
    for (SmartWordCharDisplayInfo charDisplayInfo : mDisplayList)
      drawWordChars(mReshapedChars, charDisplayInfo.getIndex(), charDisplayInfo.getCount(), charDisplayInfo.getX(), canvas);
  }

  private void drawWordChars(final char[] chars, final int start, final int length, final float leftBound, final Canvas canvas)
  {
    canvas.drawText(chars, start, length, leftBound, mWord.getBaseLine(), ((SmartWordUserParams) mWord.getUserParams(start)).getPaint());
  }
  
  private boolean isSelected()
  {
    final List<SmartWordCharDisplayInfo> swcdi = mWord.getCharDisplayInfoList();
    if (!swcdi.isEmpty())
    {
      SmartWordUserParams swup = (SmartWordUserParams) mWord.getUserParams(swcdi.get(0).getIndex());
      return swup.isSelected();
    }
    return false;
  }
  
  public Rect getFrame()
  {
    return mFrame;
  }
  
  public void draw(Canvas canvas)
  {
    if (Debug.DBG_DRAW)
    {
      // word frame
      mPaintDebug.setColor(mContext.getResources().getColor(R.color.ITC_DEBUG_COLOR_GREEN));
      canvas.drawRect(mFrame, mPaintDebug);
      
      // word character boxes
      mPaintDebug.setColor(mContext.getResources().getColor(R.color.ITC_DEBUG_COLOR_RED));
      for (RectF wordCharBox : mWordCharacterBoxes)
        if (!wordCharBox.isEmpty())
          canvas.drawRect(wordCharBox, mPaintDebug);
    }
    
    // Monitor selection
    if (isSelected())
      canvas.drawRect(mFrame, mPaintSelection);

    // Check for right to left languages
    final String locale = mWord.getLocale();
    if (locale.equals("ar") ||
        locale.equals("he_IL") ||
        locale.equals("ur_PK") ||
        locale.equals("fa_IR"))
    {
      drawRightToLeftWord(canvas);
    }
    else
      drawLeftToRightWord(canvas);
  }
}
