package com.myscript.atk.itc.sample.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.util.Log;

import com.myscript.atk.itc.SmartPage;
import com.myscript.atk.itc.SmartWord;
import com.myscript.atk.itc.StrokeRange;
import com.myscript.atk.itc.WordRange;
import com.myscript.atk.itc.sample.Debug;

public class FormattedDataManager
{
  // Debug
  private final static String TAG = "FormattedDataManager";
  
  // Matcher definitions
  public enum FormattedDataType
  {
    EMAIL_ADDRESS, PHONE, WEB_URL
  }

  public static class FormattedData
  {
    public WordRange wordRange;
    public String formattedText;
    public FormattedDataType type;
    public String fullText;
    public int startIndex;
    public int endIndex;
  }
  
  // Use of own compiled regular expression from Android 4.4.2 source code Patterns.java
  // This is due to matching issue on android 4.1.2 version  
  public static Pattern PHONE = Pattern.compile(															  // sdd = space, dot, or dash
    																							"(\\+[0-9]+[\\- \\.]*)?"      // +<digits><sdd>*
    																						+ "(\\([0-9]+\\)[\\- \\.]*)?"   // (<digits>)<sdd>*
    																						+ "([0-9][0-9\\- \\.]+[0-9])"); // <digit><digit|sdd>+<digit>

  public static List<FormattedData> identifyPageFormattedData(final SmartPage page)
  {
    final String pageText = page.getText();

    // Create the List for the identified formatted data
    List<FormattedData> formattedRanges = new ArrayList<FormattedData>();

    // Monitor formatted data such as mail
    final List<FormattedData> mailFd = monitorFormattedStrokeFromMatcher(android.util.Patterns.EMAIL_ADDRESS.matcher(pageText), FormattedDataType.EMAIL_ADDRESS, page, pageText, formattedRanges);
    if (mailFd != null)
      formattedRanges.addAll(mailFd);

    // Monitor formatted data such as phone numbers
    final List<FormattedData> phoneFd = monitorFormattedStrokeFromMatcher(PHONE.matcher(pageText), FormattedDataType.PHONE, page, pageText, formattedRanges);
    if (phoneFd != null)
      formattedRanges.addAll(phoneFd);

    // Monitor formatted data such as web URL
    final List<FormattedData> urlFd = monitorFormattedStrokeFromMatcher(android.util.Patterns.WEB_URL.matcher(pageText), FormattedDataType.WEB_URL, page, pageText, formattedRanges);
    if (urlFd != null)
      formattedRanges.addAll(urlFd);

    return formattedRanges;
  }
  
  private static List<FormattedData> monitorFormattedStrokeFromMatcher(final Matcher matcher, final FormattedDataType type, final SmartPage page, final String fullText, List<FormattedData> oldFormattedRanges)
  {
    List<FormattedData> formattedDataRanges = null;

    int matchCount = 0;
    // Parse all the matching string in the given text
    while (matcher.find())
    {
      // Initialize the match index
      int startMatcherIndex = matcher.start(0);
      int endMatcherIndex = matcher.end(0);

      if (Debug.DBG)
        Log.d(TAG, "matcher match index " + matchCount + " with TextRange: " + startMatcherIndex + "-" + (endMatcherIndex - 1));

      matchCount++;

      // Get the formatted data
      final StrokeRange formattedStrokeRange = page.getStrokeRange(startMatcherIndex, endMatcherIndex);
      final WordRange formattedWordRange = page.getWordRange(formattedStrokeRange, false);

      // Check for wordRange intersection
      if (!wordRangesIntersect(formattedWordRange, oldFormattedRanges))
      {
        // Store the formatted data
        final FormattedData fd = new FormattedData();
        fd.wordRange = formattedWordRange;
        fd.formattedText = matcher.group(0);
        fd.type = type;

        if (type != FormattedDataType.WEB_URL)
        {
          fd.fullText = fullText;
          fd.startIndex = startMatcherIndex;
          fd.endIndex = endMatcherIndex;
        }

        // Check for existing formatted formatted data ranges list and fill it
        if (formattedDataRanges == null)
          formattedDataRanges = new ArrayList<FormattedData>();
        formattedDataRanges.add(fd);
      }
    }

    return formattedDataRanges;
  }

  private static boolean wordRangesIntersect(final WordRange formattedWordRange, List<FormattedData> oldFormattedRanges)
  {
    final List<SmartWord> currentFormattedWords = formattedWordRange.getWords();

    // Check for WordRange intersection
    Iterator<FormattedData> wrIt = oldFormattedRanges.iterator();
    boolean ignoreIdentifiedRange = false;
    while (wrIt.hasNext())
    {
      WordRange gfr = wrIt.next().wordRange;

      final List<SmartWord> gfrWords = gfr.getWords();

      for (SmartWord gfrWord : gfrWords)
      {
        int index = currentFormattedWords.indexOf(gfrWord);

        if (index != -1)
        {
          final int currentBeginIndex = formattedWordRange.getBegin().get(index);
          final int currentEndIndex =  formattedWordRange.getEnd().get(index);
          final int gfrIndex = gfrWords.indexOf(gfrWord);
          final int gfrBeginIndex = gfr.getBegin().get(gfrIndex);
          final int gfrEndIndex = gfr.getEnd().get(gfrIndex);

          // Check for overlapping ranges
          if (!(currentBeginIndex > gfrEndIndex || currentEndIndex < gfrBeginIndex))
          {
            // The choice is to keep the largest identified range
            if (currentEndIndex-currentBeginIndex > gfrEndIndex-gfrBeginIndex)
            {
              // The current range is larger than the previously identified one so we need to ignore previous identification
              wrIt.remove();
            }
            else
            {
              ignoreIdentifiedRange = true;
            }
            break;
          }
        }
      }
      if (ignoreIdentifiedRange)
        break;
    }

    return ignoreIdentifiedRange;
  }
}
