package com.myscript.atk.itc.sample.controller;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Handler;
import android.widget.Toast;

import com.myscript.atk.itc.PageInterpreter;
import com.myscript.atk.itc.sample.R;
import com.myscript.atk.itc.Rect;
import com.myscript.atk.itc.SmartGestureType;
import com.myscript.atk.itc.SmartPage;
import com.myscript.atk.itc.SmartPageGestureAdapter;
import com.myscript.atk.itc.SmartStroke;
import com.myscript.atk.itc.SmartWord;
import com.myscript.atk.itc.WordFactory;
import com.myscript.atk.itc.WordRange;
import com.myscript.atk.itc.sample.SampleActivity;
import com.myscript.atk.itc.sample.controller.MainViewController.ITCModelItemListener;
import com.myscript.atk.itc.sample.model.SmartStrokeUserParams;
import com.myscript.atk.itc.sample.model.SmartWordUserParams;
import com.myscript.atk.itc.sample.model.factory.UserParamsFactory;
import com.myscript.atk.itc.sample.model.factory.UserParamsFactory.WordProperty;
import com.myscript.atk.itc.sample.util.FormattedDataManager;
import com.myscript.atk.itc.sample.view.GuidelinesView;

import java.util.ArrayList;
import java.util.List;

public class GestureManager extends SmartPageGestureAdapter
{
  private Context mContext;

  // Gestures configuration items
  private boolean[][] mGesturesConfig;
  private final static String[] gesturesNotifString = {
    "Insert gesture",
    "Join gesture",
    "Erase gesture",
    "Overwrite gesture",
    "Single tap gesture",
    "Selection gesture",
    "Underline gesture",
    "Return gesture"
  };

  // Selection gesture WordRange
  private WordRange mSelectedWordRange;

  // User parameters factory
  private UserParamsFactory mUserParamsFactory;

  // Return gesture helper
  private GuidelinesView mGuidelinesView;

  // Interactive Text Component item
  private PageInterpreter mPageInterpreter;

  // ITC model item listener
  private ITCModelItemListener mITCModelItemListener;

  public GestureManager(Context context, GuidelinesView guidelinesView, ITCModelItemListener itcModelItemListener, UserParamsFactory userParamsFactory)
  {
    mContext = context;
    mITCModelItemListener = itcModelItemListener;
    mGuidelinesView = guidelinesView;
    mUserParamsFactory = userParamsFactory;
  }

  public void setPageInterpreter(PageInterpreter pageInterpreter)
  {
    mPageInterpreter = pageInterpreter;

    if (mPageInterpreter != null)
    	configureGestures();
  }

  private void configureGestures()
  {
    // Initialize the gesture configuration array with 8 gesture type entries
    mGesturesConfig = new boolean[8][2];

    // Configure each gesture requested behavior for application start 
    // Insert gesture
    mGesturesConfig[0][0] = true;
    mGesturesConfig[0][1] = false;
    // Join gesture
    mGesturesConfig[1][0] = true;
    mGesturesConfig[1][1] = false;
    // Erase gesture
    mGesturesConfig[2][0] = true;
    mGesturesConfig[2][1] = true;
    // Overwrite gesture
    mGesturesConfig[3][0] = true;
    mGesturesConfig[3][1] = true;
    // Single tap gesture
    mGesturesConfig[4][0] = true;
    mGesturesConfig[4][1] = false;
    // Selection gesture
    mGesturesConfig[5][0] = true;
    mGesturesConfig[5][1] = false;
    // Underline gesture
    mGesturesConfig[6][0] = true;
    mGesturesConfig[6][1] = false;
    // Return gesture
    mGesturesConfig[7][0] = true;
    mGesturesConfig[7][1] = false;

    // Initialize gestures behavior in the PageInterpreter
    for (int i = 1; i <= mGesturesConfig.length; ++i)
    {
      mPageInterpreter.setGestureEnabled(i, mGesturesConfig[i-1][0]);
      mPageInterpreter.setGestureDefaultProcessing(i, mGesturesConfig[i-1][1]);
    }
  }

  public void showGesturesSettingsDialog()
  {
    final AlertDialog.Builder gesturesSettingsDialogBuilder = new AlertDialog.Builder(mContext);
    gesturesSettingsDialogBuilder.setTitle(R.string.gesture_settings_dial);
    gesturesSettingsDialogBuilder.setItems(R.array.gesture_source_dlg_items, new DialogInterface.OnClickListener()
    {
      @Override
      public void onClick(final DialogInterface dialog, final int which)
      {
        switch (which)
        {
          case 0 :
            showDialogForGesture(SmartGestureType.INSERT, R.string.insert_gesture);
            break;
          case 1 :
            showDialogForGesture(SmartGestureType.JOIN, R.string.join_gesture);
            break;
          case 2 :
            showDialogForGesture(SmartGestureType.ERASE, R.string.erase_gesture);
            break;
          case 3 :
            showDialogForGesture(SmartGestureType.OVERWRITE, R.string.overwrite_gesture);
            break;
          case 4 :
            showDialogForGesture(SmartGestureType.SINGLE_TAP, R.string.singletap_gesture);
            break;
          case 5 :
            showDialogForGesture(SmartGestureType.SELECTION, R.string.selection_gesture);
            break;
          case 6 :
            showDialogForGesture(SmartGestureType.UNDERLINE, R.string.underline_gesture);
            break;
          case 7 :
            showDialogForGesture(SmartGestureType.RETURN, R.string.return_gesture);
            break;
          default :
            break;
        }
      }
    });
    final AlertDialog gesturesSettingsDialog = gesturesSettingsDialogBuilder.create();
    gesturesSettingsDialog.show();
  }

  private void showDialogForGesture(final int index, final int titleId)
  {
    final AlertDialog.Builder gestureDialogBuilder = new AlertDialog.Builder(mContext);
    gestureDialogBuilder.setTitle(titleId);

    // OK button monitoring
    gestureDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int buttonIndex) {
        mPageInterpreter.setGestureEnabled(index, mGesturesConfig[index - 1][0]);
        mPageInterpreter.setGestureDefaultProcessing(index, mGesturesConfig[index - 1][1]);
      }
    });

    gestureDialogBuilder.setMultiChoiceItems(R.array.gesture_settings_source_dlg_items, mGesturesConfig[index - 1], new DialogInterface.OnMultiChoiceClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int paramIndex, boolean isChecked) {
        mGesturesConfig[index - 1][paramIndex]=isChecked;  
      }
    });

    final AlertDialog gestureDialog = gestureDialogBuilder.create();
    gestureDialog.show();
  }

  // Gestures callback
  @Override
  public void insertGesture(SmartPage smartPage, List<SmartStroke> gestureStrokes, float x, SmartWord nearestWord, int charIndex)
  {
    if (!mGesturesConfig[SmartGestureType.INSERT - 1][1])
    {
      if (nearestWord != null)
      {
        // Specific case of the right to left
        final String locale = nearestWord.getLocale();
        boolean rtl = locale.equals("ar") ||
            locale.equals("fa_IR") ||
            locale.equals("he_IL") ||
            locale.equals("ur_PK");

        // Get the nearest word line number
        final int nearestWordLineNumber = smartPage.getLineNumber(new WordRange(nearestWord));

        // Get the words at the same line
        final List<SmartWord> lineWords = smartPage.getWordsAtLineNumber(nearestWordLineNumber);

        // Compute the list of word impacted by the gesture according to the current orientation
        final List<SmartWord> removedWords = new ArrayList<SmartWord>(lineWords.size());
        final float nearestWordLeftBound = nearestWord.getLeftBound();
        final float nearestWordRightBound = nearestWord.getRightBound();

        for (SmartWord lineWord : lineWords)
        {
          if ((lineWord.getBoundingRect().getLeft() > nearestWordLeftBound && !rtl)
              || (lineWord.getBoundingRect().getLeft() + lineWord.getBoundingRect().getWidth() < nearestWordRightBound && rtl))
          { 
            removedWords.add(lineWord);
          }
        }

        // Insert the nearest word in the impacted word list if necessary
        // Also check for inside word gesture
        boolean isGestureInsideWord = false;
        if (charIndex == 0)
        {
          if (!rtl)
            removedWords.add(0, nearestWord);
        }
        else if (charIndex != nearestWord.getSelectedCandidate().length())
          isGestureInsideWord = true;

        // Add automatically the word at the left of the gesture in rtl
        if (rtl && nearestWordLeftBound < x && !isGestureInsideWord)
        {
          removedWords.add(0 ,nearestWord);
        }

        // Initialize final removed word list size
        final int removedWordsSize = removedWords.size();
        final int newWordsSize = removedWordsSize + (isGestureInsideWord ? 2 : 0);

        float maxDx = Float.MAX_VALUE;
        final List<Rect> charBoxes = nearestWord.getCharBoxes();
        if (charIndex >= 0 && charIndex < charBoxes.size())
        {
          Rect charRect = charBoxes.get(charIndex);
          maxDx = charRect.getWidth();
        }

        // Compute x translation value
        float dx = nearestWord.getMidlineShift();
        for (SmartWord word : removedWords)
        {
          dx += word.getMidlineShift();
        }
        dx /= removedWordsSize + 1;

        // set a maximum gap
        if (dx > maxDx)
          dx = maxDx;

        if (rtl)
        {
          dx = -dx;
        }

        // -------------------------------------------------------------------------------------------
        // New words creation

        // Get the WordFactory
        WordFactory wordFactory = mPageInterpreter.getPage().getWordFactory();

        // Create the new words list
        final List<SmartWord> addedWords = new ArrayList<SmartWord>(newWordsSize);

        // Create the sub-words from the nearest word if necessary
        if (isGestureInsideWord)
        {
          SmartWord firstPartWord = wordFactory.createSubWord(nearestWord, 0, charIndex, nearestWord.getSpaceBefore());
          SmartWord secondPartWord = wordFactory.createSubWord(nearestWord, charIndex, nearestWord.getCharLabels().size(), nearestWord.getSpaceBefore());
          secondPartWord = wordFactory.createMovedWord(secondPartWord, dx, 0, secondPartWord.getSpaceBefore() + 1);

          // In the specific case of right to left language we need to re-typeset the word to calculate the right boxes for ligatures
          if (rtl)
          {
            int firstPartWordType = firstPartWord.getType(); 
            if (firstPartWordType == SmartWord.Type.MIX || firstPartWordType == SmartWord.Type.TYPESET)
              firstPartWord = wordFactory.createTypeSetWord(firstPartWord, firstPartWord.getBoundingRect().getX(), firstPartWord.getBaseLine(), firstPartWord.getSpaceBefore());
            int secondPartWordType = secondPartWord.getType();
            if (secondPartWordType == SmartWord.Type.MIX || secondPartWordType == SmartWord.Type.TYPESET)
              secondPartWord = wordFactory.createTypeSetWord(secondPartWord, secondPartWord.getBoundingRect().getX(), secondPartWord.getBaseLine(), secondPartWord.getSpaceBefore());
          }

          addedWords.add(firstPartWord);
          addedWords.add(secondPartWord);
          removedWords.add(nearestWord);
        }

        // Start creating moved word from removed word list
        for (int i = 0; i < removedWordsSize; ++i)
        {
          SmartWord removedWord = removedWords.get(i);
          if (!isGestureInsideWord && i == 0)
            addedWords.add(wordFactory.createMovedWord(removedWord, dx, 0, removedWord.getSpaceBefore() + 1));
          else
            addedWords.add(wordFactory.createMovedWord(removedWord, dx, 0, removedWord.getSpaceBefore()));
        }

        // Update the UI with new computed SmartWords
        updateSmartWordUIInMainThread(removedWords, addedWords);
      }
    }
    triggerGestureUINotifiaction(SmartGestureType.INSERT - 1);
  }
  
  @Override
  public void joinGesture(SmartPage smartPage, List<SmartStroke> gestureStrokes, float x, SmartWord nearestWord, int charIndex)
  {
    if (!mGesturesConfig[SmartGestureType.JOIN - 1][1])
    {
      if (nearestWord != null)
      {
        // Specific case of the right to left
        final String locale = nearestWord.getLocale();
        boolean rtl = locale.equals("ar") ||
            locale.equals("fa_IR") ||
            locale.equals("he_IL") ||
            locale.equals("ur_PK");

        // Get the nearest word line number
        final int nearestWordLineNumber = smartPage.getLineNumber(new WordRange(nearestWord));        
        // Get the words at the same line
        final List<SmartWord> lineWords = smartPage.getWordsAtLineNumber(nearestWordLineNumber);
        final List<SmartWord> impactedLineWords = new ArrayList<SmartWord>(lineWords.size());

        // Compute the words on the right or on the left of the nearest words according to the current orientation        
        final float nearestWordLeftBound = nearestWord.getLeftBound();
        final float nearestWordRightBound = nearestWord.getRightBound();

        // Add automatically the word at the right of the gesture in rtl
        if (rtl && nearestWordRightBound < x)
        {
          impactedLineWords.add(nearestWord);
        }

        for (SmartWord lineWord : lineWords)
          if ( (lineWord.getBoundingRect().getLeft() > nearestWordLeftBound && !rtl)
              || (lineWord.getBoundingRect().getLeft() + lineWord.getBoundingRect().getWidth() < nearestWordRightBound && rtl))
            impactedLineWords.add(lineWord);

        boolean isGestureInsideWord = (charIndex != 0) && (charIndex != nearestWord.getCharBoxes().size());

        // Insert the nearest word in the impacted word list if necessary
        if (charIndex == 0 && !rtl)
          impactedLineWords.add(0, nearestWord);

        // Only add the nearest word if contained in the impacted words (means with index not equal to the word length)
        int newWordsArraySize = impactedLineWords.size() + (isGestureInsideWord ? 2 : 0);
        final List<SmartWord> newWords = new ArrayList<SmartWord>(newWordsArraySize);

        int indexOfNearestWordInLine = lineWords.indexOf(nearestWord);

        // Initialize x translation value
        float dx = Float.MAX_VALUE;

        // One/First word in line case
        if (charIndex == 0 && nearestWord.equals(lineWords.get(0))  && (!rtl || (rtl && impactedLineWords.contains(nearestWord))))
        {
          if (rtl)
            dx = x - nearestWordRightBound;
          else
            dx = x - nearestWordLeftBound;
        }
        else
        {
          if (charIndex == 0 && indexOfNearestWordInLine > 0)
          {
            if (rtl)
              dx = lineWords.get(indexOfNearestWordInLine - 1).getLeftBound() - nearestWordRightBound;
            else
              dx = lineWords.get(indexOfNearestWordInLine - 1).getRightBound() - nearestWordLeftBound;
          }
          else if (charIndex == nearestWord.getCharBoxes().size())
          {
            if (rtl)
            {
              if (indexOfNearestWordInLine > 0)
              {
                SmartWord word = lineWords.get(indexOfNearestWordInLine - 1);
                dx = nearestWord.getRightBound() - word.getLeftBound();
                dx = -dx;
              }
              else
              {
                SmartWord word = impactedLineWords.get(0);
                dx = nearestWordLeftBound - word.getRightBound();
              }
            }
            else
            {
              if (indexOfNearestWordInLine < lineWords.size() - 1)
              {
                SmartWord word = lineWords.get(indexOfNearestWordInLine + 1);
                dx = nearestWord.getRightBound() - word.getLeftBound();
              }
            }
          }
          else
          {
            if (charIndex > 0)
            {
              final List<Rect> charBoxes = nearestWord.getCharBoxes();
              final Rect charBoxRect = charBoxes.get(charIndex);
              final Rect previousCharBoxRect = charBoxes.get(charIndex-1);
              if (rtl)
                dx = previousCharBoxRect.getX() - (charBoxRect.getX() + charBoxRect.getWidth());
              else
                dx = (previousCharBoxRect.getX() + previousCharBoxRect.getWidth()) - charBoxRect.getX();
            }
            else if (rtl)
            {
              SmartWord word = impactedLineWords.get(0);
              dx = nearestWordLeftBound - word.getRightBound();
            }
          }
        }

        float maxDx = Float.MAX_VALUE;
        if (charIndex >= 0 && charIndex < nearestWord.getCharBoxes().size() && (!rtl || (rtl && isGestureInsideWord)))
        {
          final Rect charBoxRect = nearestWord.getCharBoxes().get(charIndex);
          maxDx = charBoxRect.getWidth();
        }

        // set a maximum gap
        if (dx > maxDx)
          dx = maxDx;

        // Get the WordFactory
        WordFactory wordFactory = mPageInterpreter.getPage().getWordFactory();

        // Create the sub-words from the nearest word if necessary
        if (isGestureInsideWord)
        {
          newWords.add(wordFactory.createSubWord(nearestWord, 0, charIndex, nearestWord.getSpaceBefore()));
          SmartWord tempo = wordFactory.createSubWord(nearestWord, charIndex, nearestWord.getCharLabels().size(), nearestWord.getSpaceBefore());
          tempo = wordFactory.createMovedWord(tempo, dx, 0, nearestWord.getSpaceBefore());
          newWords.add(tempo);

          for (int i = 2; i < newWordsArraySize; ++i)
          {
            final SmartWord lineWord = impactedLineWords.get(i - 2);
            newWords.add(wordFactory.createMovedWord(lineWord, dx, 0, lineWord.getSpaceBefore()));
          }
        }
        else
        {
          for (int i = 0; i < newWordsArraySize; ++i)
          {
            final SmartWord lineWord = impactedLineWords.get(i);
            newWords.add(wordFactory.createMovedWord(lineWord, dx, 0, (i == 0) ? 0 : lineWord.getSpaceBefore()));
          }
        }

        if (isGestureInsideWord)
          impactedLineWords.add(nearestWord);
        
        // Update the UI with new computed SmartWords
        updateSmartWordUIInMainThread(impactedLineWords, newWords);
      }
    }
    triggerGestureUINotifiaction(SmartGestureType.JOIN - 1);
  }

  @Override
  public void singleTapGesture(SmartPage smartPage, List<SmartStroke> gestureStrokes, float x, float y)
  {
    if (!mGesturesConfig[SmartGestureType.SINGLE_TAP - 1][1])
    {
      // Get the character index in the SmartPage text
      final WordRange wordRange = smartPage.getWordRangeAt(x, y);

      if (wordRange != null)
      {
        // Get the first word of the selected range
        final List<SmartWord> words = wordRange.getWords();
        final SmartWord word = words.get(0);
        final List<SmartStroke> strokes = word.getStrokes();

        // Extract formatted data
        FormattedDataManager.FormattedData fd;
        if (strokes == null)
          fd = ((SmartWordUserParams)word.getUserParams(0)).getFormattedData();
        else
          fd = ((SmartStrokeUserParams)strokes.get(0).getUserParams()).getFormattedData();

        // Monitor single tap event
        if (fd != null)
          monitorSingleTapGestureIntentCalls(fd, mContext);
        else
        {
          // If single tapped word not formatted then google it
          mContext.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.google.com/#q=" + word.getSelectedCandidate())));
        }
      }
    }
    triggerGestureUINotifiaction(SmartGestureType.SINGLE_TAP - 1);
  }

  @Override
  public void selectionGesture(SmartPage smartPage, List<SmartStroke> gestureStrokes, WordRange wordRange)
  {
    if (!mGesturesConfig[SmartGestureType.SELECTION - 1][1])
    {
      mSelectedWordRange = wordRange;

      // Apply the underline property on the given word range
      mUserParamsFactory.applyWordPropertyOnWordRange(wordRange, WordProperty.SELECTED, null);

      ((SampleActivity) mContext).onSelectedWordRange();
    }
    triggerGestureUINotifiaction(SmartGestureType.SELECTION - 1);
  }

  @Override
  public void underlineGesture(SmartPage smartPage, List<SmartStroke> gestureStrokes, final WordRange wordRange)
  {    
    if (!mGesturesConfig[SmartGestureType.UNDERLINE - 1][1])
    {
      // Apply the underline property on the given word range
      mUserParamsFactory.applyWordPropertyOnWordRange(wordRange, WordProperty.UNDERLINED, null);

      // Invalidate the UI with the updated SmartWord
      Handler mainHandler = new Handler(mContext.getMainLooper());
      mainHandler.post(new Runnable()
      {
        @Override
        public void run()
        {
          // Invalidate each SmartWord view
          final List<SmartWord> updatedWords = wordRange.getWords();
          for (final SmartWord smartWord : updatedWords)
            mITCModelItemListener.onWordAdded(smartWord);
        }
      });
    }
    triggerGestureUINotifiaction(SmartGestureType.UNDERLINE - 1);
  }

  @Override
  public void returnGesture(SmartPage smartPage, List<SmartStroke> gestureStrokes, float x, SmartWord nearestWord, int charIndex)
  {
    if (!mGesturesConfig[SmartGestureType.RETURN - 1][1])
    {
      if (nearestWord != null)
      {
        // Specific case of the right to left
        final String locale = nearestWord.getLocale();
        boolean rtl = locale.equals("ar") ||
            locale.equals("fa_IR") ||
            locale.equals("he_IL") ||
            locale.equals("ur_PK");

        // Get the nearest word line number
        int nearestWordLineNumber = smartPage.getLineNumber(new WordRange(nearestWord));

        // Get the words at the same line
        List<SmartWord> lineWords = smartPage.getWordsAtLineNumber(nearestWordLineNumber);

        // Compute the words on the right or on the left of the nearest words according to the current orientation
        final List<SmartWord> impactedLineWords = new ArrayList<SmartWord>();
        final float nearestWordLeftBound = nearestWord.getLeftBound();
        for (SmartWord lineWord : lineWords)
        {
          if ((rtl && (lineWord.getBoundingRect().getLeft() + lineWord.getBoundingRect().getWidth() < x))
              || (!rtl && (lineWord.getBoundingRect().getLeft() > nearestWordLeftBound)))
            impactedLineWords.add(lineWord);
        }

        boolean isGestureInsideWord = (charIndex != 0) && (charIndex != nearestWord.getSelectedCandidate().length());

        // Insert the nearest word in the impacted word list if necessary
        if (charIndex == 0)
          impactedLineWords.add(0, nearestWord);

        // Add all the words from the line after the given index
        for (int i = nearestWordLineNumber + 1; i < mPageInterpreter.getPage().getLineCount(); ++i)
          impactedLineWords.addAll(mPageInterpreter.getPage().getWordsAtLineNumber(i));

        // Only add the nearest word if contained in the impacted words (means with index not equal to the word length)
        int newWordsArraySize = impactedLineWords.size() + (isGestureInsideWord ? 2 : 0);
        final List<SmartWord> newWords = new ArrayList<SmartWord>(newWordsArraySize);

        float dy = (mGuidelinesView.getGap() != -1) ? mGuidelinesView.getGap() : (3 * nearestWord.getMidlineShift());

        // Get the WordFactory
        WordFactory wordFactory = mPageInterpreter.getPage().getWordFactory();

        // Create the sub-words from the nearest word if necessary
        if (isGestureInsideWord)
        {
          newWords.add(wordFactory.createSubWord(nearestWord, 0, charIndex, nearestWord.getSpaceBefore()));
          SmartWord tempo = wordFactory.createSubWord(nearestWord, charIndex, nearestWord.getCharLabels().size(),
              nearestWord.getSpaceBefore());
          tempo = wordFactory.createMovedWord(tempo, 0, dy, 0);
          newWords.add(tempo);

          for (int i = 2; i < newWordsArraySize; ++i)
          {
            SmartWord smartWord = impactedLineWords.get(i - 2);
            newWords.add(wordFactory.createMovedWord(smartWord, 0, dy, smartWord.getSpaceBefore()));
          }
        }
        else
        {
          for (int i = 0; i < newWordsArraySize; ++i)
          {
            SmartWord smartWord = impactedLineWords.get(i);
            int spaceBefore = (i == 0) ? 0 : smartWord.getSpaceBefore();
            SmartWord newWord = wordFactory.createMovedWord(smartWord, 0, dy, spaceBefore);
            newWords.add(newWord);
          }
        }

        if (isGestureInsideWord)
          impactedLineWords.add(nearestWord);

        // Update the UI with new computed SmartWords
        updateSmartWordUIInMainThread(impactedLineWords, newWords);        
      }
    }
    triggerGestureUINotifiaction(SmartGestureType.RETURN - 1);
  }
  
  @Override
  public void eraseGesture(SmartPage smartPage, List<SmartStroke> gestureStrokes, WordRange wordRange)
  {
    triggerGestureUINotifiaction(SmartGestureType.ERASE - 1);
  }

  @Override
  public void overwriteGesture(SmartPage smartPage, WordRange wordRange)
  {
    triggerGestureUINotifiaction(SmartGestureType.OVERWRITE - 1);
  }

  public void cancelWordsSelection()
  {
    // Apply WordProperty on the given WordRange
    mUserParamsFactory.applyWordPropertyOnWordRange(mSelectedWordRange, WordProperty.NOT_SELECTED, null);
    
    mSelectedWordRange = null;
  }

  public WordRange getSelectedWordRange()
  {
    return mSelectedWordRange;
  }
  
  public WordRange updateSelectedWordRange(final List<SmartWord> selectedWords)
  {
    mSelectedWordRange = new WordRange(selectedWords);
    return mSelectedWordRange;
  }
  
  private void updateSmartWordUIInMainThread(final List<SmartWord> removedWords, final List<SmartWord> addedWords)
  {
    Handler mainHandler = new Handler(mContext.getMainLooper());
    mainHandler.post(new Runnable()
    {
      @Override
      public void run()
      {
        mPageInterpreter.getPage().replaceWords(removedWords, addedWords);
      }
    });
  }
  
  private void triggerGestureUINotifiaction(final int gesturesNotifStringIndex)
  {
    Handler mainHandler = new Handler(mContext.getMainLooper());
    mainHandler.post(new Runnable()
    {
      @Override
      public void run()
      {
        // Notify the gesture
        Toast.makeText(mContext, gesturesNotifString[gesturesNotifStringIndex], Toast.LENGTH_SHORT).show();
      }
    });
  }


  private static void monitorSingleTapGestureIntentCalls(final FormattedDataManager.FormattedData fd, Context context)
  {
    if (fd.type == FormattedDataManager.FormattedDataType.PHONE)
    {
      // SMS intent
      Intent smsIntent = new Intent(Intent.ACTION_VIEW);
      smsIntent.setType("vnd.android-dir/mms-sms");
      if (context.getPackageManager().queryIntentActivities(smsIntent, PackageManager.MATCH_DEFAULT_ONLY).size() > 0)
      {
        smsIntent.setData(Uri.parse("smsto:" + fd.formattedText));
        boolean textAfterData = fd.endIndex < fd.fullText.length();
        String bodyString = (fd.startIndex <= 0 ? "" : fd.fullText.substring(0, textAfterData ? fd.startIndex : fd.startIndex - 1));
        bodyString += (textAfterData ? fd.fullText.substring(fd.endIndex + 1, fd.fullText.length()) : "");

        smsIntent.putExtra("sms_body", bodyString);
        context.startActivity(smsIntent);
      }
      else
      {
        // Phone call intent
        Uri number = Uri.parse("tel:" + fd.formattedText);
        Intent callIntent = new Intent(Intent.ACTION_DIAL, number);
        if (context.getPackageManager().queryIntentActivities(callIntent, PackageManager.MATCH_DEFAULT_ONLY).size() > 0)
          context.startActivity(callIntent);
        else
        {
          // Display an AlertDialog informing that no phone feature is available
          AlertDialog.Builder builder = new AlertDialog.Builder(context);
          builder.setMessage(fd.formattedText);
          builder.setTitle("No phone feature available for...");
          AlertDialog dialog = builder.create();
          dialog.show();
        }
      }
    }
    else if (fd.type == FormattedDataManager.FormattedDataType.EMAIL_ADDRESS)
    {
      // Mail intent
      Intent mailIntent = new Intent(Intent.ACTION_SENDTO);
      mailIntent.setData(Uri.parse("mailt" + "o:" + fd.formattedText));
      boolean textAfterData = fd.endIndex < fd.fullText.length();
      String bodyString = (fd.startIndex <= 0 ? "" : fd.fullText.substring(0, textAfterData ? fd.startIndex : fd.startIndex - 1));
      bodyString += (textAfterData ? fd.fullText.substring(fd.endIndex + 1, fd.fullText.length()) : "");
      mailIntent.putExtra(Intent.EXTRA_TEXT, bodyString);
      context.startActivity(mailIntent);
    }
    else if (fd.type == FormattedDataManager.FormattedDataType.WEB_URL)
    {
      // Url intent
      String url = fd.formattedText;
      if (!url.startsWith("http://") && !url.startsWith("https://"))
        url = "http://" + url;
      Uri uri = Uri.parse(url);
      Intent intent = new Intent(Intent.ACTION_VIEW, uri);
      context.startActivity(intent);
    }
  }
}
