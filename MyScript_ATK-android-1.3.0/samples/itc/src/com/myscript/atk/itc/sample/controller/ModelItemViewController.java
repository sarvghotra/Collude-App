package com.myscript.atk.itc.sample.controller;

import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.myscript.atk.itc.sample.R;
import com.myscript.atk.itc.Rect;
import com.myscript.atk.itc.SmartStroke;
import com.myscript.atk.itc.SmartWord;
import com.myscript.atk.itc.sample.model.SmartStrokeUserParams;
import com.myscript.atk.itc.sample.model.SmartWordUserParams;
import com.myscript.atk.itc.sample.view.PageView;
import com.myscript.atk.itc.sample.view.SmartStrokeLayoutView;
import com.myscript.atk.itc.sample.view.TypesetSmartWordLayoutView;
import com.myscript.atk.itc.sample.view.UnderlineView;

public class ModelItemViewController implements MainViewController.ITCModelItemListener
{
  private FrameLayout mPageView;
  private SmartStrokeLayoutView mStrokeLayoutView;
  private TypesetSmartWordLayoutView mTypesetWordLayoutView;

  private HashMap<SmartWord, TextView> mWordCandidateViews;
  private HashMap<SmartWord, UnderlineView> mWordsUnderlineView;

  private TextView mPageNumberView;
  private boolean mShowCandidateViews;

  private Context mContext;

  public ModelItemViewController(Context context, FrameLayout pageView)
  {
    // Store the application context
    mContext = context;

    // Set the views
    mPageView = pageView;
    mPageNumberView = (TextView) mPageView.findViewById(R.id.itc_pagenumberview);
    mStrokeLayoutView = (SmartStrokeLayoutView) mPageView.findViewById(R.id.itc_strokelayoutview);
    mTypesetWordLayoutView = (TypesetSmartWordLayoutView) mPageView.findViewById(R.id.itc_typesetwordlayoutview);

    // Initialize the word Map
    mWordCandidateViews = new HashMap<SmartWord, TextView>();
    mWordsUnderlineView = new HashMap<SmartWord, UnderlineView>();
    mShowCandidateViews= true;

    // Initialize the page number
    mPageNumberView.setText("0");
  }
  
  // Model change listeners
  // --------------------------------------------------------------------------------

  @Override
  public void onPageNumberChanged(String pageNumber)
  {
    mPageNumberView.setText(pageNumber);
  }

  @Override
  public void onStrokeAdded(SmartStroke stroke)
  {
    if (!mStrokeLayoutView.contains(stroke))
    {
      // Check for existing SmartStroke user parameters
      SmartStrokeUserParams vsup = (SmartStrokeUserParams)stroke.getUserParams();
      if (vsup != null)
        mStrokeLayoutView.addStroke(stroke);
    }
    else
      mStrokeLayoutView.invalidate(stroke);
  }

  @Override
  public void onStrokeRemoved(SmartStroke stroke)
  {
    if (mStrokeLayoutView.contains(stroke))
      mStrokeLayoutView.removeStroke(stroke);
  }

  @Override
  public void onWordAdded(SmartWord word)
  {
    boolean isUnderLinedWord = false;
    
    // User parameters must be associated to word strokes for display monitoring in this implementation
    final int smartWordType = word.getType(); 
    if (smartWordType == SmartWord.Type.RAW || smartWordType == SmartWord.Type.MIX)
    {
      // Get the the user parameters of the word strokes if any
      SmartStrokeUserParams sup = associatedWordStrokesUserParams(word);

      if (sup != null)
      {
        // Parse all word strokes to draw them
        final List<SmartStroke> wordStrokes = word.getStrokes();
        for (SmartStroke wordStroke : wordStrokes)
        {
          // Set the stroke as recognized as part of a word
          ((SmartStrokeUserParams) wordStroke.getUserParams()).setRecognized(true);

          onStrokeAdded(wordStroke);
        }
        
        // Get underlined value
        isUnderLinedWord = sup.isUnderlined();
      }
    }
    
    if (smartWordType == SmartWord.Type.TYPESET || smartWordType == SmartWord.Type.MIX)
    {
      if (!mTypesetWordLayoutView.contains(word))
        mTypesetWordLayoutView.addWord(word);
      else
        mTypesetWordLayoutView.invalidate(word);
      
      // Get underlined value
      SmartWordUserParams swup = associatedWordCharacterUserParams(word); 
      isUnderLinedWord =  swup != null && swup.isUnderlined();
    }
    
    // Toggle SmartWord underline property
    toggleUnderline(isUnderLinedWord, word);
    
    // Show word candidate view
    if (mShowCandidateViews)
      showCandidateViewForWord(word);
  }
  
  private SmartStrokeUserParams associatedWordStrokesUserParams(SmartWord word)
  {
    final List<SmartStroke> wordStrokes = word.getStrokes();
    if (!wordStrokes.isEmpty())
      return (SmartStrokeUserParams)wordStrokes.get(0).getUserParams();
    return null;
  }
  
  private SmartWordUserParams associatedWordCharacterUserParams(SmartWord word)
  {
    final List<Rect> charBoxes = word.getCharBoxes();
    final int charBoxesSize = charBoxes.size();
    for (int i = 0; i < charBoxesSize; ++i)
    {
      SmartWordUserParams swup = (SmartWordUserParams)word.getUserParams(i);
      if (swup != null)
        return swup; 
    }
    return null;
  }

  @Override
  public void onWordRemoved(SmartWord word)
  {
    if (word.getType() == SmartWord.Type.RAW || word.getType() == SmartWord.Type.MIX)
    {
      final List<SmartStroke> wordStrokes = word.getStrokes();
      if (!wordStrokes.isEmpty())
        for (SmartStroke wordStroke : wordStrokes)
          onStrokeRemoved(wordStroke);
    }

    if (word.getType() == SmartWord.Type.TYPESET || word.getType() == SmartWord.Type.MIX)
      if (mTypesetWordLayoutView.contains(word))
        mTypesetWordLayoutView.removeWord(word);
    
    // Remove the underline of the word
    removeUnderline(word);

    // Remove the text of the word
    removeCandidateViewForWord(word);
  }

  @Override
  public void onToggleShowHidePageText(List<SmartWord> words)
  {
    if (!mShowCandidateViews)
    {
      clearPageCandidateViews();
      showCandidateViewForWords(words);
      mShowCandidateViews = true;
    }
    else
    {
      clearPageCandidateViews();
      mShowCandidateViews = false;
    }
  }

  // SmartWord text views monitoring
  // --------------------------------------------------------------------------------

  private void showCandidateViewForWords(List<SmartWord> pageWords)
  {
    for (SmartWord word : pageWords)
      showCandidateViewForWord(word);
  }

  private void showCandidateViewForWord(SmartWord word)
  {
    if (!mWordCandidateViews.containsKey(word))
    {
      // Get its selected candidate
      String wordSelectedCandidate = word.getSelectedCandidate();

      boolean isSpace = wordSelectedCandidate.compareTo(" ") == 0;
      boolean isLineBreak = wordSelectedCandidate.compareTo("\n") == 0;

      // Check if it has been displayed already
      if (!isSpace && !isLineBreak)
      {
        final TextView tv = new TextView(mContext);
        tv.setText(wordSelectedCandidate + " - " + word.getBaseLine());
        tv.setTextSize(20);
        tv.setGravity(Gravity.CENTER);

        final int specY = PageView.MeasureSpec.makeMeasureSpec(PageView.LayoutParams.WRAP_CONTENT, PageView.MeasureSpec.UNSPECIFIED);
        final int specX = PageView.MeasureSpec.makeMeasureSpec(PageView.LayoutParams.WRAP_CONTENT, PageView.MeasureSpec.UNSPECIFIED);
        tv.measure(specX, specY);
        tv.layout(0, 0, tv.getMeasuredWidth(), tv.getMeasuredHeight());

        // Set left position
        final Rect wordBoundingRect = word.getBoundingRect();
        PageView.LayoutParams lp = new PageView.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        lp.leftMargin = (int) ((wordBoundingRect.getLeft() + wordBoundingRect.getWidth() / 2) - tv.getWidth() / 2);
        lp.topMargin = (int) (word.getBaseLine() + word.getMidlineShift() / 2 + 2);
        tv.setLayoutParams(lp);
        mPageView.addView(tv);

        // Add textViews to the maintained list
        mWordCandidateViews.put(word, tv);
      }
    }
  }

  private void removeCandidateViewForWord(SmartWord smartWord)
  {
    mPageView.removeView(mWordCandidateViews.get(smartWord));
    mWordCandidateViews.remove(smartWord);
  }
  
  private void clearPageCandidateViews()
  {
    for (TextView textView : mWordCandidateViews.values())
      mPageView.removeView(textView);
    mWordCandidateViews.clear();
  }
  
  private void toggleUnderline(boolean isUnderlined, SmartWord smartWord)
  {
    if (isUnderlined)
      addUnderline(smartWord);
    else
      removeUnderline(smartWord);
  }
  
  private void addUnderline(SmartWord smartWord)
  {
    // Create the underline view
    final float underline = smartWord.getBaseLine() + smartWord.getMidlineShift() / 2;
    UnderlineView ulv = new UnderlineView(mContext, underline, smartWord.getLeftBound(), smartWord.getRightBound());
    UnderlineView oldUlv = mWordsUnderlineView.put(smartWord, ulv);
    if (oldUlv != null)
      mPageView.removeView(oldUlv);
    mPageView.addView(ulv);
    ulv.invalidate();
  }
  
  private void removeUnderline(SmartWord smartWord)
  {
    mPageView.removeView(mWordsUnderlineView.get(smartWord));
    mWordsUnderlineView.remove(smartWord);
  }
}