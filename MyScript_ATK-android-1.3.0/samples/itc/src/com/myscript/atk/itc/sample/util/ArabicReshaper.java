package com.myscript.atk.itc.sample.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public final class ArabicReshaper
{
  /**
   * Arabic letters with their distinct forms <br/>
   * key is letter <br/>
   * value[0] is the number of distinct forms the letter has<br/>
   * value[1] is isolated form <br/>
   * value[2] is initial form <br/>
   * value[3] is middle form <br/>
   * value[4] is final form
   */
  private final static HashMap<Integer, Integer[]> LETTERS                       = new HashMap<Integer, Integer[]>();
  private final static int                         LETTERS_NBFORMS_INDEX         = 0;
  private final static int                         LETTERS_ISOLATED_FORM_INDEX   = 1;
  private final static int                         LETTERS_FINAL_FORM_INDEX      = 2;
  private final static int                         LETTERS_INITIAL_FORM_INDEX    = 3;
  private final static int                         LETTERS_MIDDLE_FORM_INDEX     = 4;
  
  private final static int                         LETTERS_DEFAULT_NBFORMS       = 2;
  
  /**
   * LamAlef ligatures with their distinct forms<br/>
   * key is isolated value[0] is middle form <br/>
   * value[1] is final form <br/>
   */
  private final static HashMap<Integer, Integer[]> LAM_ALEF_LIGATURES            = new HashMap<Integer, Integer[]>();
  private final static int                         LIGATURES_ISOLATED_FORM_INDEX = 0;
  private final static int                         LIGATURES_FINAL_FORM_INDEX    = 1;
  private final static int                         ALEF                          = 0x0627;
  private final static int                         LAM                           = 0x0644;
  
  /**
   * Allah ligature (Lam + Lam + Heh)
   */
  private final static int                         HEH                           = 0x0647;
  private final static int                         ALLAH_LIGATURE                = 0xFDF2;
  
  /**
   * Singleton instance
   */
  private static ArabicReshaper sInstance;
  
  /**
   * Private constructor (for singleton). Instantiates the Maps
   */
  private ArabicReshaper()
  {
    LETTERS.put(0x0621, new Integer[]{3, 0xFE80, 0xFE80, 0xFE8B, 0xFE8C}); // HAMZA
    LETTERS.put(0x0622, new Integer[]{2, 0xFE81, 0xFE82, 0xFE81, 0xFE82}); // ALEF UPPER MADDA
    LETTERS.put(0x0623, new Integer[]{2, 0xFE83, 0xFE84, 0xFE83, 0xFE84}); // ALEF UPPER HAMZA
    LETTERS.put(0x0624, new Integer[]{2, 0xFE85, 0xFE86, 0xFE85, 0xFE86}); // WAW UPPER HAMZA
    LETTERS.put(0x0625, new Integer[]{2, 0xFE87, 0xFE88, 0xFE87, 0xFE88}); // ALEF LOWER HAMZA
    LETTERS.put(0x0626, new Integer[]{2, 0xFE89, 0xFE8A, 0xFE89, 0xFE8A}); // YEH UPPER HAMZA
    LETTERS.put(0x0627, new Integer[]{2, 0xFE8D, 0xFE8E, 0xFE8D, 0xFE8E}); // ALEF
    LETTERS.put(0x0628, new Integer[]{4, 0xFE8F, 0xFE90, 0xFE91, 0xFE92}); // BEH
    LETTERS.put(0x0629, new Integer[]{2, 0xFE93, 0xFE94, 0xFE93, 0xFE94}); // TEH MARBUTA
    LETTERS.put(0x062A, new Integer[]{4, 0xFE95, 0xFE96, 0xFE97, 0xFE98}); // TEH
    LETTERS.put(0x062B, new Integer[]{4, 0xFE99, 0xFE9A, 0xFE9B, 0xFE9C}); // THEH
    LETTERS.put(0x062C, new Integer[]{4, 0xFE9D, 0xFE9E, 0xFE9F, 0xFEA0}); // JEEM
    LETTERS.put(0x062D, new Integer[]{4, 0xFEA1, 0xFEA2, 0xFEA3, 0xFEA4}); // HAH
    LETTERS.put(0x062E, new Integer[]{4, 0xFEA5, 0xFEA6, 0xFEA7, 0xFEA8}); // KHAH
    LETTERS.put(0x062F, new Integer[]{2, 0xFEA9, 0xFEAA, 0xFEA9, 0xFEAA}); // DAL
    LETTERS.put(0x0630, new Integer[]{2, 0xFEAB, 0xFEAC, 0xFEAB, 0xFEAC}); // THAL
    LETTERS.put(0x0631, new Integer[]{2, 0xFEAD, 0xFEAE, 0xFEAD, 0xFEAE}); // REH
    LETTERS.put(0x0632, new Integer[]{2, 0xFEAF, 0xFEB0, 0xFEAF, 0xFEB0}); // ZAIN
    LETTERS.put(0x0633, new Integer[]{4, 0xFEB1, 0xFEB2, 0xFEB3, 0xFEB4}); // SEEN
    LETTERS.put(0x0634, new Integer[]{4, 0xFEB5, 0xFEB6, 0xFEB7, 0xFEB8}); // SHEEN
    LETTERS.put(0x0635, new Integer[]{4, 0xFEB9, 0xFEBA, 0xFEBB, 0xFEBC}); // SAD
    LETTERS.put(0x0636, new Integer[]{4, 0xFEBD, 0xFEBE, 0xFEBF, 0xFEC0}); // DAD
    LETTERS.put(0x0637, new Integer[]{4, 0xFEC1, 0xFEC2, 0xFEC3, 0xFEC4}); // TAH
    LETTERS.put(0x0638, new Integer[]{4, 0xFEC5, 0xFEC6, 0xFEC7, 0xFEC8}); // ZAH
    LETTERS.put(0x0639, new Integer[]{4, 0xFEC9, 0xFECA, 0xFECB, 0xFECC}); // AIN
    LETTERS.put(0x063A, new Integer[]{4, 0xFECD, 0xFECE, 0xFECF, 0xFED0}); // GHAIN
    LETTERS.put(0x0641, new Integer[]{4, 0xFED1, 0xFED2, 0xFED3, 0xFED4}); // FEH
    LETTERS.put(0x0642, new Integer[]{4, 0xFED5, 0xFED6, 0xFED7, 0xFED8}); // QAF
    LETTERS.put(0x0643, new Integer[]{4, 0xFED9, 0xFEDA, 0xFEDB, 0xFEDC}); // KAF
    LETTERS.put(0x0644, new Integer[]{4, 0xFEDD, 0xFEDE, 0xFEDF, 0xFEE0}); // LAM
    LETTERS.put(0x0645, new Integer[]{4, 0xFEE1, 0xFEE2, 0xFEE3, 0xFEE4}); // MEEM
    LETTERS.put(0x0646, new Integer[]{4, 0xFEE5, 0xFEE6, 0xFEE7, 0xFEE8}); // NOON
    LETTERS.put(0x0647, new Integer[]{4, 0xFEE9, 0xFEEA, 0xFEEB, 0xFEEC}); // HEH
    LETTERS.put(0x0648, new Integer[]{2, 0xFEED, 0xFEEE, 0xFEED, 0xFEEE}); // WAW
    LETTERS.put(0x0649, new Integer[]{2, 0xFEEF, 0xFEF0, 0xFEEF, 0xFEF0}); // ALEF MAKSURA
    LETTERS.put(0x064A, new Integer[]{4, 0xFEF1, 0xFEF2, 0xFEF3, 0xFEF4}); // YEH
//  LETTERS.put(0x066E, new Integer[]{                                 }); // BEH WITHOUT DOT
//  LETTERS.put(0x066F, new Integer[]{                                 }); // QAF WITHOUT DOT
    LETTERS.put(0x0671, new Integer[]{2, 0xFB50, 0xFB51, 0xFB50, 0xFB51}); // ALEF WASLA
//  LETTERS.put(0x0672, new Integer[]{                                 }); // ALEF UPPER WAVY HAMZA
//  LETTERS.put(0x0673, new Integer[]{                                 }); // ALEF LOWER WAVY HAMZA
//  LETTERS.put(0x0675, new Integer[]{                                 }); // ALEF HIGH HAMZA
//  LETTERS.put(0x0676, new Integer[]{                                 }); // WAW HIGH HAMZA
//  LETTERS.put(0x0677, new Integer[]{                                 }); // U UPPER HAMZA
//  LETTERS.put(0x0678, new Integer[]{                                 }); // YEH HIGH HAMZA
    LETTERS.put(0x0679, new Integer[]{4, 0xFB66, 0xFB67, 0xFB68, 0xFB69}); // TTEH
    LETTERS.put(0x067A, new Integer[]{4, 0xFB5E, 0xFB5F, 0xFB60, 0xFB61}); // TTEHEH
    LETTERS.put(0x067B, new Integer[]{4, 0xFB52, 0xFB53, 0xFB54, 0xFB55}); // BEEH
//  LETTERS.put(0x067C, new Integer[]{                                 }); // TEH LOWER RING
//  LETTERS.put(0x067D, new Integer[]{                                 }); // TEH UPPER 3-DOTS
    LETTERS.put(0x067E, new Integer[]{4, 0xFB56, 0xFB57, 0xFB58, 0xFB59}); // PEH
    LETTERS.put(0x067F, new Integer[]{4, 0xFB62, 0xFB63, 0xFB64, 0xFB65}); // TEHEH
    LETTERS.put(0x0680, new Integer[]{4, 0xFB5A, 0xFB5B, 0xFB5C, 0xFB5D}); // BEHEH
//  LETTERS.put(0x0681, new Integer[]{                                 }); // HAH UPPER HAMZA
//  LETTERS.put(0x0682, new Integer[]{                                 }); // HAH UPPER 2-VERTICAL-DOTS
    LETTERS.put(0x0683, new Integer[]{4, 0xFB76, 0xFB77, 0xFB78, 0xFB79}); // NYEH
    LETTERS.put(0x0684, new Integer[]{4, 0xFB72, 0xFB73, 0xFB74, 0xFB75}); // DYEH
//  LETTERS.put(0x0685, new Integer[]{                                 }); // HAH UPPER 3-DOTS
    LETTERS.put(0x0686, new Integer[]{4, 0xFB7A, 0xFB7B, 0xFB7C, 0xFB7D}); // TCHEH
    LETTERS.put(0x0687, new Integer[]{4, 0xFB7E, 0xFB7F, 0xFB80, 0xFB81}); // TCHEHEH
    LETTERS.put(0x0688, new Integer[]{2, 0xFB88, 0xFB89, 0xFB88, 0xFB89}); // DDAL
//  LETTERS.put(0x0689, new Integer[]{                                 }); // DAL LOWER RING
//  LETTERS.put(0x068A, new Integer[]{                                 }); // DAL LOWER DOT
//  LETTERS.put(0x068B, new Integer[]{                                 }); // DAL LOWER DOT UPPER SMALL TAH
    LETTERS.put(0x068C, new Integer[]{2, 0xFB84, 0xFB85, 0xFB84, 0xFB85}); // DAHAL
    LETTERS.put(0x068D, new Integer[]{2, 0xFB82, 0xFB83, 0xFB82, 0xFB83}); // DDAHAL
    LETTERS.put(0x068E, new Integer[]{2, 0xFB86, 0xFB87, 0xFB86, 0xFB87}); // DUL
//  LETTERS.put(0x068F, new Integer[]{                                 }); // DAL UPPER 3-DOTS
//  LETTERS.put(0x0690, new Integer[]{                                 }); // DAL UPPER 4-DOTS
    LETTERS.put(0x0691, new Integer[]{2, 0xFB8C, 0xFB8D, 0xFB8C, 0xFB8D}); // RREH
//  LETTERS.put(0x0692, new Integer[]{                                 }); // REH UPPER SMALL V
//  LETTERS.put(0x0693, new Integer[]{                                 }); // REH LOWER RING
//  LETTERS.put(0x0694, new Integer[]{                                 }); // REH LOWER DOT
//  LETTERS.put(0x0695, new Integer[]{                                 }); // REH LOWER SMALL V
//  LETTERS.put(0x0696, new Integer[]{                                 }); // REH UPPER DOT LOWER DOT
//  LETTERS.put(0x0697, new Integer[]{                                 }); // REH UPPER 2-DOTS
    LETTERS.put(0x0698, new Integer[]{2, 0xFB8A, 0xFB8B, 0xFB8A, 0xFB8B}); // JEH
//  LETTERS.put(0x0699, new Integer[]{                                 }); // REH UPPER 4-DOTS
//  LETTERS.put(0x069A, new Integer[]{                                 }); // SEEN UPPER DOT LOWER DOT
//  LETTERS.put(0x069B, new Integer[]{                                 }); // SEEN LOWER 3-DOTS
//  LETTERS.put(0x069C, new Integer[]{                                 }); // SEEN UPPER 3-DOTS LOWER 3-DOTS
//  LETTERS.put(0x069D, new Integer[]{                                 }); // SAD LOWER 2-DOTS
//  LETTERS.put(0x069E, new Integer[]{                                 }); // SAD UPPER 3-DOTS
//  LETTERS.put(0x069F, new Integer[]{                                 }); // TAH UPPER 3-DOTS
//  LETTERS.put(0x06A0, new Integer[]{                                 }); // AIN UPPER 3-DOTS
//  LETTERS.put(0x06A1, new Integer[]{                                 }); // FEH WITHOUT DOT
//  LETTERS.put(0x06A2, new Integer[]{                                 }); // FEW WITH DOT MOVED BELOW
//  LETTERS.put(0x06A3, new Integer[]{                                 }); // FEH LOWER DOT
    LETTERS.put(0x06A4, new Integer[]{4, 0xFB6A, 0xFB6B, 0xFB6C, 0xFB6D}); // VEH
//  LETTERS.put(0x06A5, new Integer[]{                                 }); // FEH LOWER 3-DOTS
    LETTERS.put(0x06A6, new Integer[]{4, 0xFB6E, 0xFB6F, 0xFB70, 0xFB71}); // PEHEH
//  LETTERS.put(0x06A7, new Integer[]{                                 }); // QAF UPPER DOT
//  LETTERS.put(0x06A8, new Integer[]{                                 }); // QAF UPPER 3-DOTS
    LETTERS.put(0x06A9, new Integer[]{4, 0xFB8E, 0xFB8F, 0xFB90, 0xFB91}); // KEHEH
//  LETTERS.put(0x06AA, new Integer[]{                                 }); // SWASH KAF
//  LETTERS.put(0x06AB, new Integer[]{                                 }); // KAF UPPER RING
//  LETTERS.put(0x06AC, new Integer[]{                                 }); // KAF UPPER DOT
    LETTERS.put(0x06AD, new Integer[]{4, 0xFBD3, 0xFBD4, 0xFBD5, 0xFBD6}); // NG
//  LETTERS.put(0x06AE, new Integer[]{                                 }); // KAF LOWER 3-DOTS
    LETTERS.put(0x06AF, new Integer[]{4, 0xFB92, 0xFB93, 0xFB94, 0xFB95}); // GAF
//  LETTERS.put(0x06B0, new Integer[]{                                 }); // GAF UPPER RING
    LETTERS.put(0x06B1, new Integer[]{4, 0xFB9A, 0xFB9B, 0xFB9C, 0xFB9D}); // NGOEH
//  LETTERS.put(0x06B2, new Integer[]{                                 }); // GAF LOWER 2-DOTS
    LETTERS.put(0x06B3, new Integer[]{4, 0xFB96, 0xFB97, 0xFB98, 0xFB99}); // GUEH
//  LETTERS.put(0x06B4, new Integer[]{                                 }); // GAF UPPER 3-DOTS
//  LETTERS.put(0x06B5, new Integer[]{                                 }); // LAM UPPER SMALL V
//  LETTERS.put(0x06B6, new Integer[]{                                 }); // LAM UPPER DOT
//  LETTERS.put(0x06B7, new Integer[]{                                 }); // LAM UPPER 3-DOTS
//  LETTERS.put(0x06B8, new Integer[]{                                 }); // LAM LOWER 3-DOTS
//  LETTERS.put(0x06B9, new Integer[]{                                 }); // NOON LOWER 3-DOTS
    LETTERS.put(0x06BA, new Integer[]{2, 0xFB9E, 0xFB9F, 0xFB9E, 0xFB9F}); // NOON GHUNNA
    LETTERS.put(0x06BB, new Integer[]{4, 0xFBA0, 0xFBA1, 0xFBA2, 0xFBA3}); // RNOON
//  LETTERS.put(0x06BC, new Integer[]{                                 }); // NOON LOWER RING
//  LETTERS.put(0x06BD, new Integer[]{                                 }); // NOON UPPER 3-DOTS
    LETTERS.put(0x06BE, new Integer[]{4, 0xFBAA, 0xFBAB, 0xFBAC, 0xFBAD}); // HEH DOACHASHMEE
//  LETTERS.put(0x06BF, new Integer[]{                                 }); // TCHEH UPPER DOT
    LETTERS.put(0x06C0, new Integer[]{2, 0xFBA4, 0xFBA5, 0xFBA4, 0xFBA5}); // HEH UPPER YEH
    LETTERS.put(0x06C1, new Integer[]{4, 0xFBA6, 0xFBA7, 0xFBA8, 0xFBA9}); // HEH GOAL
//  LETTERS.put(0x06C2, new Integer[]{                                 }); // HEH GOAL UPPER HAMZA
//  LETTERS.put(0x06C3, new Integer[]{                                 }); // TEH MARBUTA GOAL
//  LETTERS.put(0x06C4, new Integer[]{                                 }); // WAW LOWER RING
    LETTERS.put(0x06C5, new Integer[]{2, 0xFBE0, 0xFBE1, 0xFBE0, 0xFBE1}); // KIRGHIZ OE
    LETTERS.put(0x06C6, new Integer[]{2, 0xFBD9, 0xFBDA, 0xFBD9, 0xFBDA}); // OE
    LETTERS.put(0x06C7, new Integer[]{2, 0xFBD7, 0xFBD8, 0xFBD7, 0xFBD8}); // U
    LETTERS.put(0x06C8, new Integer[]{2, 0xFBDB, 0xFBDC, 0xFBDB, 0xFBDC}); // YU
    LETTERS.put(0x06C9, new Integer[]{2, 0xFBE2, 0xFBE3, 0xFBE2, 0xFBE3}); // KIRGHIZ YU
//  LETTERS.put(0x06CA, new Integer[]{                                 }); // WAW UPPER 2-DOTS
    LETTERS.put(0x06CB, new Integer[]{2, 0xFBDE, 0xFBDF, 0xFBDE, 0xFBDF}); // VE
    LETTERS.put(0x06CC, new Integer[]{4, 0xFBFC, 0xFBFD, 0xFBFE, 0xFBFF}); // FARSI YEH
//  LETTERS.put(0x06CD, new Integer[]{                                 }); // YEH WITH TAIL
//  LETTERS.put(0x06CE, new Integer[]{                                 }); // YEH UPPER SMALL V
//  LETTERS.put(0x06CF, new Integer[]{                                 }); // WAW UPPER DOT
    LETTERS.put(0x06D0, new Integer[]{4, 0xFBE4, 0xFBE5, 0xFBE6, 0xFBE7}); // E
//  LETTERS.put(0x06D1, new Integer[]{                                 }); // YEH LOWER 3-DOTS
    LETTERS.put(0x06D2, new Integer[]{2, 0xFBAE, 0xFBAF, 0xFBAE, 0xFBAF}); // YEH BARREE 
    LETTERS.put(0x06D3, new Integer[]{2, 0xFBB0, 0xFBB1, 0xFBB0, 0xFBB1}); // YEH BARREE UPPER HAMZA
//  LETTERS.put(0x06D5, new Integer[]{                                 }); // AE

    // Lam Alef ligatures
    LAM_ALEF_LIGATURES.put(0x0622, new Integer[]{0xFEF5, 0xFEF6}); // LAM-ALEF UPPER MADDA
    LAM_ALEF_LIGATURES.put(0x0623, new Integer[]{0xFEF7, 0xFEF8}); // LAM-ALEF UPPER HAMZA
    LAM_ALEF_LIGATURES.put(0x0625, new Integer[]{0xFEF9, 0xFEFA}); // LAM-ALEF LOWER HAMZA
    LAM_ALEF_LIGATURES.put(0x0627, new Integer[]{0xFEFB, 0xFEFC}); // LAM-ALEF
  }
  
  /**
   * Singleton getter
   * 
   * @return The singleton instance
   */
  public static ArabicReshaper getInstance()
  {
    if (sInstance == null)
      sInstance = new ArabicReshaper();
    return sInstance;
  }
  
  /**
   * Reshapes a string by changing each character with its correct form
   * 
   * @param toReshape
   *          The string to reshape
   * @return The reshaped char-array with 0 chars when letters are ligatured
   */
  public char[] reshapeToCharArray(final String toReshape)
  {
    final int dirtyLength = toReshape.length();
    final char[] dirtyLetters = new char[dirtyLength];
    final char[] cleanLetters = new char[dirtyLength];
    
    toReshape.getChars(0, dirtyLength, dirtyLetters, 0);
    
    // Speedups
    if (dirtyLength == 0)
      return new char[0];
    if (dirtyLength == 1)
      return new char[]{reshapeLetter(dirtyLetters[0], LETTERS_ISOLATED_FORM_INDEX)};
    
    // Loop through the string
    for (int i = 0; i < dirtyLength; i++)
    {
      if (i == 0)
      {
        // First letter should be in initial form
        cleanLetters[0] = reshapeLetter(dirtyLetters[0], LETTERS_INITIAL_FORM_INDEX);
      }
      else
      {
        final char prePrePreviousLetter = (i>=3) ? dirtyLetters[i-3] : 0;
        final char prePreviousLetter = (i>=2) ? dirtyLetters[i-2] : 0;
        final char previousLetter = dirtyLetters[i-1];
        final char currentLetter = dirtyLetters[i];
        
        if (isLongAllahLigature(prePrePreviousLetter, prePreviousLetter, previousLetter, currentLetter))
        {
          // Allah long ligature
          cleanLetters[i-3] = ALLAH_LIGATURE;
          cleanLetters[i-2] = 0;
          cleanLetters[i-1] = 0;
          cleanLetters[i] = 0;
        }
        else if (isShortAllahLigature(prePreviousLetter, previousLetter, currentLetter))
        {
          // Allah short ligature (no first alef)
          cleanLetters[i-2] = ALLAH_LIGATURE;
          cleanLetters[i-1] = 0;
          cleanLetters[i] = 0;
        }
        else if (isLamAlefLigature(previousLetter, currentLetter))
        {
          // Put the LamAlef in final form if the letter before has only 2 forms
          final boolean previousDefaultForm = (getLetterNbForms(toReshape, i-2) == LETTERS_DEFAULT_NBFORMS) || cleanLetters[i-2] == 0;
          cleanLetters[i-1] = getLamAlefLigature(previousLetter, currentLetter, !previousDefaultForm);
          cleanLetters[i] = 0;
        }
        else
        {
          // Letter
          final boolean previousIsDefaultForm = (getLetterNbForms(toReshape, i-1) == LETTERS_DEFAULT_NBFORMS) || cleanLetters[i-1] == 0;
          final boolean isLastChar = isLastChar(i, toReshape);
          
          int formIndex = LETTERS_ISOLATED_FORM_INDEX;
          
          if (previousIsDefaultForm && !isLastChar)
            formIndex = LETTERS_INITIAL_FORM_INDEX;
          else if (!previousIsDefaultForm && !isLastChar)
            formIndex = LETTERS_MIDDLE_FORM_INDEX;
          else if (previousIsDefaultForm && isLastChar)
            formIndex = LETTERS_ISOLATED_FORM_INDEX;
          else if (!previousIsDefaultForm && isLastChar)
            formIndex = LETTERS_FINAL_FORM_INDEX;
          
          cleanLetters[i] = reshapeLetter(dirtyLetters[i], formIndex);
        }
      }
    }
    
    return cleanLetters;
  }
  
  /**
   * Reshapes a string by changing each character with its correct form
   * 
   * @param toReshape
   *          The string to reshape
   * @return The reshaped string
   */
  public String reshapeToString(final String toReshape)
  {
    final String[] words = toReshape.split(" ");
    final StringBuilder sb = new StringBuilder();
    
    for (int i = 0; i < words.length; i++)
    {
      if (sb.length() != 0)
        sb.append(" ");
      
      sb.append(getString(reshapeToCharArray(words[i])));
    }
    
    return sb.toString();
  }
  
  /**
   * Generate the String corresponding to the reshaped char-array in parameter,
   * deleting 0 char for ligatures
   * 
   * @param letters 
   *          A reshaped char-array
   * @return The reshaped String
   */
  public String getString(final char[] letters)
  {
    // Remove the ligatured letters
    final StringBuffer cleanString = new StringBuffer("");
    for (int i = 0; i < letters.length; i++)
    {
      if (letters[i] != 0)
        cleanString.append(letters[i]);
    }
    
    return cleanString.toString();
  }
  
  /**
   * Parses a string for lamAlef ligatures
   * 
   * @param reshapedString
   *          A reshaped string
   * @return A list of the indexes of the lamalef ligatures in the input string
   */
  public List<Integer> getLamAlefIndexes(final String reshapedString)
  {
    final int count = reshapedString.length();
    final ArrayList<Integer> indexes = new ArrayList<Integer>();
    for (int i = 0; i < count; i++)
    {
      if (isLamAlef(reshapedString.charAt(i)))
        indexes.add(i);
    }
    return indexes;
  }
  
  /**
   * Parses a string for Allah ligatures
   * 
   * @param reshapedString
   *          A reshaped string
   * @return A list of the indexes of the allah ligatures in the input string
   */
  public List<Integer> getAllahIndexes(final String reshapedString)
  {
    final int count = reshapedString.length();
    final ArrayList<Integer> indexes = new ArrayList<Integer>();
    for (int i = 0; i < count; i++)
    {
      if ((int) reshapedString.charAt(i) == ALLAH_LIGATURE)
        indexes.add(i);
    }
    return indexes;
  }
  
  /**
   * Parses a string for arabic diacritics
   * 
   * @param reshapedString
   *          A reshaped string
   * @return A list of the indexes of the arabic diacritics in the input string
   */
  public List<Integer> getDiacriticIndexes(final String reshapedString)
  {
    final int count = reshapedString.length();
    final ArrayList<Integer> indexes = new ArrayList<Integer>();
    for (int i = 0; i < count; i++)
    {
      if (isDiacritic(reshapedString.charAt(i)))
        indexes.add(i);
    }
    return indexes;
  }
  
  /**
   * Get the specified form of a letter
   * 
   * @param letter
   *          The character to reshape
   * @param formIndex
   *          The index of the form
   * @return The letter with its right shape
   */
  private char reshapeLetter(final char letter, final int formIndex)
  {
    final Integer[] forms = LETTERS.get((int) letter);
    if (forms == null)
      return letter;
    return (char) forms[formIndex].intValue();
  }
  
  /**
   * Gets the number of distinct forms for a letter
   * 
   * @param dirtyString
   *          The entire String in which the character is
   * @param location
   *          Index of the character
   * @return The number of distinct forms for that letter
   */
  private int getLetterNbForms(final String dirtyString, final int location)
  {
    if (location < 0)
      return LETTERS_DEFAULT_NBFORMS;
    
    final char target = dirtyString.charAt(location);
    final Integer[] forms = LETTERS.get((int) target);
    
    if (forms == null)
      return getLetterNbForms(dirtyString, location - 1);
    
    return forms[LETTERS_NBFORMS_INDEX].intValue();
  }
  
  /**
   * Retrieves a LamAlef ligature
   * 
   * @param lam
   *          The Lam letter
   * @param alef
   *          The Alef letter
   * @param finalForm
   *          {@code true} if the ligature is at the end of a word to return the
   *          final form
   * @return The LamAlef ligature corresponding to the parameters
   */
  private char getLamAlefLigature(final char lam, final char alef,
      final boolean finalForm)
  {
    final int formIndex = finalForm
        ? LIGATURES_FINAL_FORM_INDEX
        : LIGATURES_ISOLATED_FORM_INDEX;
    final Integer[] alefForms = LAM_ALEF_LIGATURES.get((int) alef);
    
    return (char) alefForms[formIndex].intValue();
  }
  
  /**
   * Checks if the letters could be a LamAlef ligature
   * 
   * @param lam
   *          The letter that is supposed to be Lam
   * @param alef
   *          The letter that is supposed to be Alef
   * @return {@code true} if the letter is LamAlef
   */
  private boolean isLamAlefLigature(final char lam, final char alef)
  {
    return ((int) lam == LAM) && LAM_ALEF_LIGATURES.get((int) alef) != null;
  }
  
  /**
   * Checks if the letters could be an Allah ligature
   * 
   * @param alef
   *          The letter that is supposed to be the Alef
   * @param lam1
   *          The letter that is supposed to be the first Lam
   * @param lam2
   *          The letter that is supposed to be the second Lam
   * @param heh
   *          The letter that is supposed to be heh
   * @return {@code true} if the letter is Allah
   */
  private boolean isLongAllahLigature(final char alef, final char lam1, final char lam2, final char heh)
  {
    return (((int) alef) == ALEF) && isShortAllahLigature(lam1, lam2, heh);
  }
  
  /**
   * Checks if the letters could be an Allah ligature
   * 
   * @param lam1
   *          The letter that is supposed to be the first Lam
   * @param lam2
   *          The letter that is supposed to be the second Lam
   * @param heh
   *          The letter that is supposed to be heh
   * @return {@code true} if the letter is Allah
   */
  private boolean isShortAllahLigature(final char lam1, final char lam2, final char heh)
  {
    return ((int) lam1 == LAM) && ((int) lam2 == LAM) && ((int) heh == HEH);
  }
  
  /**
   * Checks if the letter is a reshaped LamAlef ligature
   * 
   * @param lamAlef
   *          The letter to check
   * @return {@code true} if the letter is LamAlef
   */
  private boolean isLamAlef(final char lamAlef)
  {
    boolean result = false;
    
    for (HashMap.Entry<Integer, Integer[]> entry : LAM_ALEF_LIGATURES.entrySet())
      for (Integer form : entry.getValue())
        result |= form == (int) lamAlef;
    
    return result;
  }
  
  /**
   * Checks if the character represents an arabic diacritic
   * 
   * @param letter
   *          The letter to check
   * @return {@code true} if the letter is an arabic diacritic
   */
  public boolean isDiacritic(final char letter)
  {
    final int charCode = (int) letter;
    
    boolean result = false;
    
    result |= (charCode >= 0x0610 && charCode <= 0x061A);
    result |= (charCode >= 0x064B && charCode <= 0x065F);
    result |= (charCode == 0x0670);
    result |= (charCode >= 0x06D6 && charCode <= 0x06DC);
    result |= (charCode >= 0x06DF && charCode <= 0x06E4);
    result |= (charCode >= 0x06E7 && charCode <= 0x06E8);
    result |= (charCode >= 0x06EA && charCode <= 0x06ED);
    
    return result;
  }
  
  /**
   * Checks if the character is the last letter of the String
   * 
   * @param index 
   *          The index of the char in the String
   * @param toReshape 
   *          A String
   * @return {@code true} if the character is the last letter of the String
   */
  private boolean isLastChar(final int index, final String toReshape)
  {
    if (index == (toReshape.length() - 1))
      return true;
    
    final Set<Integer> letters = LETTERS.keySet();
    
    boolean isLast = true;
    for (int i = index + 1; i < toReshape.length(); i++)
    {
      isLast &= !letters.contains((int) toReshape.charAt(i));
    }
    return isLast;
  }
}