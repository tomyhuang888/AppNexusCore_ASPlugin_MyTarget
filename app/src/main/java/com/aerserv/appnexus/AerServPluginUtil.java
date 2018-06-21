package com.aerserv.appnexus;

import android.util.Pair;

import com.aerserv.sdk.utils.AerServLog;

import org.json.JSONObject;
import com.aerserv.sdk.AerServConfig;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AerServPluginUtil {

  private static final String LOG_TAG = AerServPluginUtil.class.getSimpleName();
  private static final String CANNOT_PARSE_KEYWORDS = "Cannot parse customeKeywords in " +
      "TargetingParameters.";
  private static final String CANNOT_PARSE_SERVER_PARAMETERS = "Cannot parse the parameters from " +
      "the server.";

  public static Map<String, String> getHashMap(List<Pair<String, String>> appNexusKeywords) {
    if(appNexusKeywords != null) {
      try {
        Map<String, String> publisherKeywords = new HashMap<>();
        for (Pair<String, String> keyword : appNexusKeywords) {
          publisherKeywords.put(keyword.first, keyword.second);
        }
        return publisherKeywords;
      } catch (Exception e) {
        AerServLog.d(LOG_TAG, CANNOT_PARSE_KEYWORDS);
      }
    }
    return null;
  }

  public static Integer getInteger(String key, String serverParameter) {
    Integer value;
    try {
      JSONObject json = new JSONObject(serverParameter);
      value = (Integer) json.get(key);
    } catch (Exception e) {
      AerServLog.d(LOG_TAG, CANNOT_PARSE_SERVER_PARAMETERS);
      return null;
    }
    return value;
  }
}
