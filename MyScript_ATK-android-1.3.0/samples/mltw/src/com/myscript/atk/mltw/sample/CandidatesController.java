// Copyright MyScript

package com.myscript.atk.mltw.sample;

import java.util.HashSet;
import java.util.Set;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.myscript.atk.mltw.model.Word;

/**
 * Controller managing the candidate bar.
 */
public class CandidatesController implements OnClickListener
{
  /**
   * Candidate click listener.
   */
  public interface OnCandidateClickListener
  {
    /**
     * Called when a candidate button is clicked.
     *
     * @param word
     *          The current word.
     * @param candidate
     *          The clicked candidate string.
     */
    void onCandidateClick(Word word, String candidate);
  }

  /** The current context. */
  private final Context mContext;

  /** The candidates bar view. */
  private final LinearLayout mCandidatesBar;

  /** Set of visible candaites. */
  private final Set<String> mCandidates;

  /** LayoutInflater used to create the candidate views. */
  private final LayoutInflater mLayoutInflater;

  /** The candidate click listener. */
  private OnCandidateClickListener mOnCandidateClickListener;

  /** The currently selected word. */
  private Word mCurrentWord;

  /**
   * Creates a candidates controller.
   *
   * @param context
   *          The current context.
   * @param candidatesBar
   *          The candidates bar view.
   */
  public CandidatesController(final Context context, final LinearLayout candidatesBar)
  {
    mContext = context;
    mCandidatesBar = candidatesBar;
    mCandidatesBar.setMinimumHeight(getCandidateMinHeight(context));

    mCandidates = new HashSet<String>();

    mLayoutInflater = LayoutInflater.from(mContext);
  }

  /**
   * Registers a click listener for the candidates.
   *
   * @param l
   *          A listener
   */
  public void setOnCandidateClickListener(final OnCandidateClickListener l)
  {
    mOnCandidateClickListener = l;
  }

  /**
   * Clears the candidate bar.
   */
  public void clear()
  {
    mCandidatesBar.removeAllViews();
    mCandidates.clear();
  }

  /**
   * Displays a word and its candidates.
   *
   * @param word
   *          The word to display.
   */
  public void onWordSelected(final Word word)
  {
    clear();

    mCurrentWord = word;

    for (final String cand : word.getCandidates())
      addCandidate(cand, cand.equals(word.getText()));
  }

  /**
   * Adds a candidate to the candidate bar.
   *
   * @param label
   *          The candidate label.
   * @param selected
   *          {@code true} if the given text is the selected candidate.
   */
  private void addCandidate(final String label, final boolean selected)
  {
    if (mCandidates.contains(label))
      return;

    final String text = isPrintable(label) ? label : "\uFFFD";
    final int resId = selected ? R.layout.mltw_candidate_main : R.layout.mltw_candidate;

    final TextView textView = (TextView) mLayoutInflater.inflate(resId, mCandidatesBar, false);
    textView.setOnClickListener(this);
    textView.setText(text);

    mCandidatesBar.addView(textView);
    mCandidates.add(label);
  }

  @Override
  public void onClick(final View v)
  {
    final TextView candidateView = (TextView) v;
    final String candidate = candidateView.getText().toString();

    if (mOnCandidateClickListener != null)
      mOnCandidateClickListener.onCandidateClick(mCurrentWord, candidate);
  }

  /**
   * Computes the height of the candidate view.
   *
   * @param context
   *          The current context.
   * @return The candidate view minimum height considering the paint used to write the candidates.
   */
  private int getCandidateMinHeight(final Context context)
  {
    final int[] attrs = {android.R.attr.textSize, android.R.attr.padding, android.R.attr.layout_margin};
    final TypedArray ta = context.obtainStyledAttributes(R.style.MltwCandidate, attrs);
    final int textSize = ta.getDimensionPixelSize(0, 0);
    final int padding = ta.getDimensionPixelSize(1, 0);
    final int margin = ta.getDimensionPixelSize(2, 0);
    ta.recycle();

    final Paint paint = new Paint();
    paint.setTextSize(textSize);
    final int textHeight = (int) Math.ceil(paint.getFontMetrics().bottom - paint.getFontMetrics().top);

    return textHeight + padding * 2 + margin * 2;
  }

  /**
   * <p>
   * Return true if the text is printable on Android.
   * </p>
   * Some Asiatic characters are not supported by the Android font.<br>
   * According to the installed fonts, the system can :
   * <ul>
   * <li>draw the String by using a special character called a "REPLACEMENT CHARACTER" <i>(generally \uFFFD)</i>.</li>
   * <li>insert a blank character. In that case, the current method will return false.</li>
   * </ul>
   *
   * @param text
   *          The text to test
   * @return {@code true} if the text is printable, false otherwise.
   */
  private boolean isPrintable(final String text)
  {
    if (text == null || text.length() == 0)
      return false;

    if (" ".equals(text))
      return true;

    // Test if the displayed text rect is empty
    final Paint paint = new Paint();
    final Rect rect = new Rect();
    paint.getTextBounds(text, 0, text.length(), rect);

    return !rect.isEmpty();
  }
}