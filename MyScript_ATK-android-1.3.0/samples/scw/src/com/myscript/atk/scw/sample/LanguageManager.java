// Copyright MyScript

package com.myscript.atk.scw.sample;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.text.InputType;

public class LanguageManager {
  
  private final static String DEFAULT_LANGUAGE = "en_US";

  private final static String[] LANGUAGES = {
    // superimposed languages
    "af_ZA",
    "bg_BG",
    "cs_CZ",
    "de_AT",
    "de_DE",
    "el_GR",
    "en_CA",
    "en_GB",
    "en_US",
    "es_MX",
    "es_ES",
    "fi_FI",
    "fr_CA",
    "fr_FR",
    "ka_GE",
    "hu_HU",
    "is_IS",
    "id_ID",
    "it_IT",
    "lv_LV",
    "mk_MK",
    "ms_MY",
    "nl_BE",
    "nl_NL",
    "no_NO",
    "pl_PL",
    "pt_BR",
    "pt_PT",
    "ru_RU",
    "sk_SK",
    "sl_SI",
    "sv_SE",
    "tr_TR",
    "vi_VN",
    // isolated languages
    "az_AZ",
    "be_BY",
    "ca_ES",
    "da_DK",
    "et_EE",
    "eu_ES",
    "ga_IE",
    "gl_ES",
    "he_IL",
    "hr_HR",
    "hy_AM",
    "ja_JP",
    "kk_KZ",
    "ko_KR",
    "lt_LT",
    "mn_MN",
    "ro_RO",
    "sq_AL",
    "sr_Cyrl_RS",
    "sr_Latn_RS",
    "th_TH",
    "tt_RU",
    "uk_UA",
    "zh_CN",
    "zh_HK",
    "zh_TW",
  };
  
  private String mDefaultLanguage;
  
  private List<String> mAvailableLanguages;
  
  public LanguageManager(Context context) {
    String[] assetDirs;
    
    try {
      assetDirs = context.getAssets().list("");
    } catch (IOException e) {
      assetDirs = new String[] {};
    }
    
    File cacheDir = context.getExternalCacheDir();
    
    mAvailableLanguages = new ArrayList<String>();
    
    for (String language : LANGUAGES) {
      if (languageExists(language, assetDirs, cacheDir)) {
        mAvailableLanguages.add(language);
      }
    }
    
    if (mAvailableLanguages.isEmpty() || languageAvailable(DEFAULT_LANGUAGE)) {
      mDefaultLanguage = DEFAULT_LANGUAGE;
    } else {
      mDefaultLanguage = mAvailableLanguages.get(0);
    }
  }
  
  private boolean languageExists(String language, String[] assetDirs, File cacheDir)
  {
    // verify that language directory exists in package assets directory
    for (String dir : assetDirs) {
      if (dir.equals(language)) {
        return true;
      }
    }
    
    // verify that language directory exists in application cache directory
    if (new File(cacheDir, language).exists()) {
      return true;
    }
    
    return false;
  }
  
  private boolean languageAvailable(String which) {
    for (String language : mAvailableLanguages) {
      if (language.equals(which)) {
        return true;
      }
    }
    
    return false;
  }
  
  public String getDefaultLanguage() {
    return mDefaultLanguage;
  }
  
  public String getNextLanguage(String language) {
    int index = -1;
    for (int i=0; i<mAvailableLanguages.size(); i++) {
      if (mAvailableLanguages.get(i).equals(language)) {
        index = i;
        break;
      }
    }
    if (index == -1) {
      return mDefaultLanguage;
    } else if (index == mAvailableLanguages.size() - 1) {
      return mAvailableLanguages.get(0);
    } else {
      return mAvailableLanguages.get(index + 1);
    }
  }
  
  public static String[] getResourcesForLanguage(String language, int type) {
    List<String> resources = new ArrayList<String>();
    
    // alphabet knowledge resource
    
    resources.add(getAkResourceForLanguage(language));
    
    // data format resources based on input type, defaults to text
    
    if (type == InputType.TYPE_CLASS_NUMBER) {
      resources.add("mul/mul-lk-number.res");
      resources.add(getGrmResourceForLanguage(language));
    } else if (type == InputType.TYPE_CLASS_PHONE) {
      resources.add("mul/mul-lk-phone_number.res");
      resources.add(getGrmResourceForLanguage(language));
    } else if (type == (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS)) {
      resources.add("mul/mul-lk-email.res");
      resources.add(getGrmResourceForLanguage(language));
    } else if (type == (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI)) {
      resources.add("mul/mul-lk-uri.res");
      resources.add(getGrmResourceForLanguage(language));
    } else if (type == (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_NORMAL)) {
      resources.add(getLkTextResourceForLanguage(language));
    }
    
    // gesture resource
    if(!isCJK(language)) {
      resources.add("mul/mul-lk-gesture.res");
    }
    
    String[] array = new String[resources.size()];
    return resources.toArray(array);
  }

  public static boolean isCJK(String language) {
    return language.startsWith("zh") || language.startsWith("ja") || language.startsWith("ko");
  }

  private static String getAkResourceForLanguage(String language) {
    
    // superimposed languages
    
    if ("af_ZA".equals(language)) return "latin/latin-ak-superimposed.lite.res";
    if ("bg_BG".equals(language)) return "cyrillic/cyrillic-ak-superimposed.lite.res";
    if ("cs_CZ".equals(language)) return "latin/latin-ak-superimposed.lite.res";
    if ("de_AT".equals(language)) return "latin/latin-ak-superimposed.lite.res";
    if ("de_DE".equals(language)) return "latin/latin-ak-superimposed.lite.res";
    if ("en_CA".equals(language)) return "latin/latin-ak-superimposed.lite.res";
    if ("en_GB".equals(language)) return "latin/latin-ak-superimposed.lite.res";
    if ("en_US".equals(language)) return "latin/latin-ak-superimposed.lite.res";
    if ("es_ES".equals(language)) return "latin/latin-ak-superimposed.lite.res";
    if ("es_MX".equals(language)) return "latin/latin-ak-superimposed.lite.res";
    if ("el_GR".equals(language)) return "el_GR/el_GR-ak-superimposed.lite.res";
    if ("fi_FI".equals(language)) return "latin/latin-ak-superimposed.lite.res";
    if ("fr_CA".equals(language)) return "latin/latin-ak-superimposed.lite.res";
    if ("fr_FR".equals(language)) return "latin/latin-ak-superimposed.lite.res";
    if ("hu_HU".equals(language)) return "latin/latin-ak-superimposed.lite.res";
    if ("id_ID".equals(language)) return "latin/latin-ak-superimposed.lite.res";
    if ("is_IS".equals(language)) return "latin/latin-ak-superimposed.lite.res";
    if ("it_IT".equals(language)) return "latin/latin-ak-superimposed.lite.res";
    if ("ka_GE".equals(language)) return "ka_GE/ka_GE-ak-superimposed.lite.res";
    if ("lv_LV".equals(language)) return "latin/latin-ak-superimposed.lite.res";
    if ("mk_MK".equals(language)) return "cyrillic/cyrillic-ak-superimposed.lite.res";
    if ("ms_MY".equals(language)) return "latin/latin-ak-superimposed.lite.res";
    if ("nl_BE".equals(language)) return "latin/latin-ak-superimposed.lite.res";
    if ("nl_NL".equals(language)) return "latin/latin-ak-superimposed.lite.res";
    if ("no_NO".equals(language)) return "latin/latin-ak-superimposed.lite.res";
    if ("pl_PL".equals(language)) return "latin/latin-ak-superimposed.lite.res";
    if ("pt_BR".equals(language)) return "latin/latin-ak-superimposed.lite.res";
    if ("pt_PT".equals(language)) return "latin/latin-ak-superimposed.lite.res";
    if ("ru_RU".equals(language)) return "cyrillic/cyrillic-ak-superimposed.lite.res";
    if ("sk_SK".equals(language)) return "latin/latin-ak-superimposed.lite.res";
    if ("sl_SI".equals(language)) return "latin/latin-ak-superimposed.lite.res";
    if ("sv_SE".equals(language)) return "latin/latin-ak-superimposed.lite.res";
    if ("tr_TR".equals(language)) return "latin/latin-ak-superimposed.lite.res";
    if ("vi_VN".equals(language)) return "latin/latin-ak-superimposed.lite.res";
    if ("zh_CN".equals(language)) return "zh_CN/zh_CN-ak-superimposed.lite.res";
    if ("zh_HK".equals(language)) return "zh_HK/zh_HK-ak-superimposed.lite.res";
    if ("zh_TW".equals(language)) return "zh_TW/zh_TW-ak-superimposed.lite.res";
    if ("ja_JP".equals(language)) return "ja_JP/ja_JP-ak-superimposed.lite.res";
    if ("ko_KR".equals(language)) return "ko_KR/ko_KR-ak-superimposed.lite.res";

      // isolated languages
    
    if ("az_AZ".equals(language)) return "az_AZ/az_AZ-ak-iso.lite.res";
    if ("be_BY".equals(language)) return "be_BY/be_BY-ak-iso.lite.res";
    if ("ca_ES".equals(language)) return "ca_ES/ca_ES-ak-iso.lite.res";
    if ("da_DK".equals(language)) return "da_DK/da_DK-ak-iso.lite.res";
    if ("et_EE".equals(language)) return "et_EE/et_EE-ak-iso.lite.res";
    if ("eu_ES".equals(language)) return "eu_ES/eu_ES-ak-iso.lite.res";
    if ("ga_IE".equals(language)) return "ga_IE/ga_IE-ak-iso.lite.res";
    if ("gl_ES".equals(language)) return "gl_ES/gl_ES-ak-iso.lite.res";
    if ("he_IL".equals(language)) return "he_IL/he_IL-ak-iso.lite.res";
    if ("hr_HR".equals(language)) return "hr_HR/hr_HR-ak-iso.lite.res";
    if ("hy_AM".equals(language)) return "hy_AM/hy_AM-ak-iso.lite.res";
    if ("ja_JP".equals(language)) return "ja_JP/ja_JP-ak-iso.lite.res";
    if ("kk_KZ".equals(language)) return "kk_KZ/kk_KZ-ak-iso.lite.res";
    if ("ko_KR".equals(language)) return "ko_KR/ko_KR-ak-iso.lite.res";
    if ("lt_LT".equals(language)) return "lt_LT/lt_LT-ak-iso.lite.res";
    if ("mn_MN".equals(language)) return "mn_MN/mn_MN-ak-iso.lite.res";
    if ("ro_RO".equals(language)) return "ro_RO/ro_RO-ak-iso.lite.res";
    if ("sq_AL".equals(language)) return "sq_AL/sq_AL-ak-iso.lite.res";
    if ("sr_Cyrl_RS".equals(language)) return "sr_Cyrl_RS/sr_Cyrl_RS-ak-iso.lite.res";
    if ("sr_Latn_RS".equals(language)) return "sr_Latn_RS/sr_Latn_RS-ak-iso.lite.res";
    if ("th_TH".equals(language)) return "th_TH/th_TH-ak-iso.lite.res";
    if ("tt_RU".equals(language)) return "tt_RU/tt_RU-ak-iso.lite.res";
    if ("uk_UA".equals(language)) return "uk_UA/uk_UA-ak-iso.lite.res";
    if ("zh_CN".equals(language)) return "zh_CN/zh_CN-ak-iso.lite.res";
    if ("zh_HK".equals(language)) return "zh_HK/zh_HK-ak-iso.lite.res";
    if ("zh_TW".equals(language)) return "zh_TW/zh_TW-ak-iso.lite.res";

    return null;
  }
  
  private static String getLkTextResourceForLanguage(String language) {
    
    // superimposed languages
    
    if ("af_ZA".equals(language)) return "af_ZA/af_ZA-lk-text.lite.res";
    if ("bg_BG".equals(language)) return "bg_BG/bg_BG-lk-text.lite.res";
    if ("cs_CZ".equals(language)) return "cs_CZ/cs_CZ-lk-text.lite.res";
    if ("de_AT".equals(language)) return "de_AT/de_AT-lk-text.lite.res";
    if ("de_DE".equals(language)) return "de_DE/de_DE-lk-text.lite.res";
    if ("el_GR".equals(language)) return "el_GR/el_GR-lk-text.lite.res";
    if ("en_CA".equals(language)) return "en_CA/en_CA-lk-text.lite.res";
    if ("en_GB".equals(language)) return "en_GB/en_GB-lk-text.lite.res";
    if ("en_US".equals(language)) return "en_US/en_US-lk-text.lite.res";
    if ("es_ES".equals(language)) return "es_ES/es_ES-lk-text.lite.res";
    if ("es_MX".equals(language)) return "es_MX/es_MX-lk-text.lite.res";
    if ("fi_FI".equals(language)) return "fi_FI/fi_FI-lk-text.lite.res";
    if ("fr_CA".equals(language)) return "fr_CA/fr_CA-lk-text.lite.res";
    if ("fr_FR".equals(language)) return "fr_FR/fr_FR-lk-text.lite.res";
    if ("hu_HU".equals(language)) return "hu_HU/hu_HU-lk-text.lite.res";
    if ("id_ID".equals(language)) return "id_ID/id_ID-lk-text.lite.res";
    if ("is_IS".equals(language)) return "is_IS/is_IS-lk-text.lite.res";
    if ("it_IT".equals(language)) return "it_IT/it_IT-lk-text.lite.res";
    if ("ka_GE".equals(language)) return "ka_GE/ka_GE-lk-text.lite.res";
    if ("lv_LV".equals(language)) return "lv_LV/lv_LV-lk-text.lite.res";
    if ("mk_MK".equals(language)) return "mk_MK/mk_MK-lk-text.lite.res";
    if ("ms_MY".equals(language)) return "ms_MY/ms_MY-lk-text.lite.res";
    if ("nl_NL".equals(language)) return "nl_NL/nl_NL-lk-text.lite.res";
    if ("nl_BE".equals(language)) return "nl_BE/nl_BE-lk-text.lite.res";
    if ("no_NO".equals(language)) return "no_NO/no_NO-lk-text.lite.res";
    if ("pl_PL".equals(language)) return "pl_PL/pl_PL-lk-text.lite.res";
    if ("pt_BR".equals(language)) return "pt_BR/pt_BR-lk-text.lite.res";
    if ("pt_PT".equals(language)) return "pt_PT/pt_PT-lk-text.lite.res";
    if ("ru_RU".equals(language)) return "ru_RU/ru_RU-lk-text.lite.res";
    if ("sk_SK".equals(language)) return "sk_SK/sk_SK-lk-text.lite.res";
    if ("sl_SI".equals(language)) return "sl_SI/sl_SI-lk-text.lite.res";
    if ("sv_SE".equals(language)) return "sv_SE/sv_SE-lk-text.lite.res";
    if ("tr_TR".equals(language)) return "tr_TR/tr_TR-lk-text.lite.res";
    if ("vi_VN".equals(language)) return "vi_VN/vi_VN-lk-text.lite.res";

    // isolated languages
    
    if ("az_AZ".equals(language)) return "az_AZ/az_AZ-lk-text.lite.res";
    if ("be_BY".equals(language)) return "be_BY/be_BY-lk-text.lite.res";
    if ("ca_ES".equals(language)) return "ca_ES/ca_ES-lk-text.lite.res";
    if ("da_DK".equals(language)) return "da_DK/da_DK-lk-text.lite.res";
    if ("et_EE".equals(language)) return "et_EE/et_EE-lk-text.lite.res";
    if ("eu_ES".equals(language)) return "eu_ES/eu_ES-lk-text.lite.res";
    if ("ga_IE".equals(language)) return "ga_IE/ga_IE-lk-text.lite.res";
    if ("gl_ES".equals(language)) return "gl_ES/gl_ES-lk-text.lite.res";
    if ("he_IL".equals(language)) return "he_IL/he_IL-lk-text.lite.res";
    if ("hr_HR".equals(language)) return "hr_HR/hr_HR-lk-text.lite.res";
    if ("hy_AM".equals(language)) return "hy_AM/hy_AM-lk-text.lite.res";
    if ("ja_JP".equals(language)) return "ja_JP/ja_JP-lk-text.lite.res";
    if ("kk_KZ".equals(language)) return "kk_KZ/kk_KZ-lk-text.lite.res";
    if ("ko_KR".equals(language)) return "ko_KR/ko_KR-lk-text.lite.res";
    if ("lt_LT".equals(language)) return "lt_LT/lt_LT-lk-text.lite.res";
    if ("mn_MN".equals(language)) return "mn_MN/mn_MN-lk-text.lite.res";
    if ("ro_RO".equals(language)) return "ro_RO/ro_RO-lk-text.lite.res";
    if ("sq_AL".equals(language)) return "sq_AL/sq_AL-lk-text.lite.res";
    if ("sr_Cyrl_RS".equals(language)) return "sr_Cyrl_RS/sr_Cyrl_RS-lk-text.lite.res";
    if ("sr_Latn_RS".equals(language)) return "sr_Latn_RS/sr_Latn_RS-lk-text.lite.res";
    if ("th_TH".equals(language)) return "th_TH/th_TH-lk-text.lite.res";
    if ("tt_RU".equals(language)) return "tt_RU/tt_RU-lk-text.lite.res";
    if ("uk_UA".equals(language)) return "uk_UA/uk_UA-lk-text.lite.res";
    if ("zh_CN".equals(language)) return "zh_CN/zh_CN-lk-text.lite.res";
    if ("zh_HK".equals(language)) return "zh_HK/zh_HK-lk-text.lite.res";
    if ("zh_TW".equals(language)) return "zh_TW/zh_TW-lk-text.lite.res";

    return null;
  }
  
  private static String getGrmResourceForLanguage(String language) {
    
    // superimposed languages
    
    if ("af_ZA".equals(language)) return "af_ZA/af_ZA-lk-grm.res";
    if ("bg_BG".equals(language)) return "bg_BG/bg_BG-lk-grm.res";
    if ("cs_CZ".equals(language)) return "cs_CZ/cs_CZ-lk-grm.res";
    if ("de_DE".equals(language)) return "de_DE/de_DE-lk-grm.res";
    if ("de_AT".equals(language)) return "de_AT/de_AT-lk-grm.res";
    if ("el_GR".equals(language)) return "el_GR/el_GR-lk-grm.res";
    if ("en_CA".equals(language)) return "en_CA/en_CA-lk-grm.res";
    if ("en_GB".equals(language)) return "en_GB/en_GB-lk-grm.res";
    if ("en_US".equals(language)) return "en_US/en_US-lk-grm.res";
    if ("es_ES".equals(language)) return "es_ES/es_ES-lk-grm.res";
    if ("es_MX".equals(language)) return "es_MX/es_MX-lk-grm.res";
    if ("fi_FI".equals(language)) return "fi_FI/fi_FI-lk-grm.res";
    if ("fr_CA".equals(language)) return "fr_CA/fr_CA-lk-grm.res";
    if ("fr_FR".equals(language)) return "fr_FR/fr_FR-lk-grm.res";
    if ("hu_HU".equals(language)) return "hu_HU/hu_HU-lk-grm.res";
    if ("id_ID".equals(language)) return "id_ID/id_ID-lk-grm.res";
    if ("is_IS".equals(language)) return "is_IS/is_IS-lk-grm.res";
    if ("it_IT".equals(language)) return "it_IT/it_IT-lk-grm.res";
    if ("ka_GE".equals(language)) return "ka_GE/ka_GE-lk-grm.res";
    if ("lv_LV".equals(language)) return "lv_LV/lv_LV-lk-grm.res";
    if ("mk_MK".equals(language)) return "mk_MK/mk_MK-lk-grm.res";
    if ("ms_MY".equals(language)) return "ms_MY/ms_MY-lk-grm.res";
    if ("nl_BE".equals(language)) return "nl_BE/nl_BE-lk-grm.res";
    if ("nl_NL".equals(language)) return "nl_NL/nl_NL-lk-grm.res";
    if ("no_NO".equals(language)) return "no_NO/no_NO-lk-grm.res";
    if ("pl_PL".equals(language)) return "pl_PL/pl_PL-lk-grm.res";
    if ("pt_BR".equals(language)) return "pt_BR/pt_BR-lk-grm.res";
    if ("pt_PT".equals(language)) return "pt_PT/pt_PT-lk-grm.res";
    if ("ru_RU".equals(language)) return "ru_RU/ru_RU-lk-grm.res";
    if ("sk_SK".equals(language)) return "sk_SK/sk_SK-lk-grm.res";
    if ("sl_SI".equals(language)) return "sl_SI/sl_SI-lk-grm.res";
    if ("sv_SE".equals(language)) return "sv_SE/sv_SE-lk-grm.res";
    if ("tr_TR".equals(language)) return "tr_TR/tr_TR-lk-grm.res";
    if ("vi_VN".equals(language)) return "vi_VN/vi_VN-lk-grm.res";
    
    // isolated languages
    
    if ("az_AZ".equals(language)) return "az_AZ/az_AZ-lk-grm.res";
    if ("be_BY".equals(language)) return "be_BY/be_BY-lk-grm.res";
    if ("ca_ES".equals(language)) return "ca_ES/ca_ES-lk-grm.res";
    if ("da_DK".equals(language)) return "da_DK/da_DK-lk-grm.res";
    if ("et_EE".equals(language)) return "et_EE/et_EE-lk-grm.res";
    if ("eu_ES".equals(language)) return "eu_ES/eu_ES-lk-grm.res";
    if ("ga_IE".equals(language)) return "ga_IE/ga_IE-lk-grm.res";
    if ("gl_ES".equals(language)) return "gl_ES/gl_ES-lk-grm.res";
    if ("he_IL".equals(language)) return "he_IL/he_IL-lk-grm.res";
    if ("hr_HR".equals(language)) return "hr_HR/hr_HR-lk-grm.res";
    if ("hy_AM".equals(language)) return "hy_AM/hy_AM-lk-grm.res";
    if ("ja_JP".equals(language)) return "ja_JP/ja_JP-lk-grm.res";
    if ("kk_KZ".equals(language)) return "kk_KZ/kk_KZ-lk-grm.res";
    if ("ko_KR".equals(language)) return "ko_KR/ko_KR-lk-grm.res";
    if ("lt_LT".equals(language)) return "lt_LT/lt_LT-lk-grm.res";
    if ("mn_MN".equals(language)) return "mn_MN/mn_MN-lk-grm.res";
    if ("ro_RO".equals(language)) return "ro_RO/ro_RO-lk-grm.res";
    if ("sq_AL".equals(language)) return "sq_AL/sq_AL-lk-grm.res";
    if ("sr_Cyrl_RS".equals(language)) return "sr_Cyrl_RS/sr_Cyrl_RS-lk-grm.res";
    if ("sr_Latn_RS".equals(language)) return "sr_Latn_RS/sr_Latn_RS-lk-grm.res";
    if ("th_TH".equals(language)) return "th_TH/th_TH-lk-grm.res";    
    if ("tt_RU".equals(language)) return "tt_RU/tt_RU-lk-grm.res";
    if ("uk_UA".equals(language)) return "uk_UA/uk_UA-lk-grm.res";
    if ("zh_CN".equals(language)) return "zh_CN/zh_CN-lk-grm.res";
    if ("zh_HK".equals(language)) return "zh_HK/zh_HK-lk-grm.res";
    if ("zh_TW".equals(language)) return "zh_TW/zh_TW-lk-grm.res";

    return null;
  }

}
