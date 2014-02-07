package edu.dartmouth.cs.myruns5;


import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import android.content.Context;
import android.util.Pair;

public class SPFLevelIconUtils {

  private static final String SPF_0 = "NONE";
    private static final String SPF_15 = "SPF_15";
    private static final String SPF_30 = "SPF_30";
    private static final String SPF_8 = "SPF_8";
    private static final String SPF_45 = "SPF_45";
    private static final String SPF_50 = "SPF_50";

    private SPFLevelIconUtils() {}

    private static LinkedHashMap<String, Pair<Integer, Integer>>
        map = new LinkedHashMap<String, Pair<Integer, Integer>>();

    static {
      map.put(SPF_0,new Pair<Integer, Integer>(R.string.spf_0 , R.drawable.spf_0));
      map.put(SPF_8,new Pair<Integer, Integer>(R.string.spf_8 , R.drawable.spf_8));
      map.put(SPF_15, new Pair<Integer, Integer>(R.string.spf_15 , R.drawable.spf_15));
      map.put(SPF_30, new Pair<Integer, Integer>(R.string.spf_30 , R.drawable.spf_30));
      map.put(SPF_45, new Pair<Integer, Integer>(R.string.spf_45 , R.drawable.spf_45));
      map.put(SPF_50, new Pair<Integer, Integer>(R.string.spf_50 , R.drawable.spf_50));
    }

    private static int[] spf_0 = new int[] { R.string.spf_0};
    private static int[] spf_8 = new int[] { R.string.spf_8};
    private static int[] spf_15 = new int[] { R.string.spf_15};
    private static int[] spf_30 = new int[] { R.string.spf_30 };
    private static int[] spf_45 = new int[] {R.string.spf_30 };
    private static int[] spf_50 = new int[] { R.string.spf_50 };
    
    /**
     * Gets the icon drawable.
     * 
     * @param iconValue the icon value
     */
    public static int getIconDrawable(String iconValue) {
      if (iconValue == null || iconValue.equals("")) {
        return R.drawable.spf;
      }
      Pair<Integer, Integer> pair = map.get(iconValue);
      return pair == null ? R.drawable.spf : pair.second;
    }

    /**
     * Gets the icon spf type.
     * 
     * @param iconValue the icon value
     */
    public static int getIconSPFLevel(String iconValue) {
      if (iconValue == null || iconValue.equals("")) {
        return R.string.generic_spf_level;
      }
      Pair<Integer, Integer> pair = map.get(iconValue);
      return pair == null ? R.string.generic_spf_level : pair.first;
    }

    /**
     * Gets all icon values.
     */
    public static List<String> getAllIconValues() {
      List<String> values = new ArrayList<String>();
      for (String value : map.keySet()) {
        values.add(value);
      }
      return values;
    }

    /**
     * Gets the icon value.
     * 
     * @param context the context
     * @param spfType the activity type
     */
    public static String getIconValue(Context context, String spfType) {
      if (inList(context, spfType, spf_0)) {
        return SPF_0;
      }     
      if (inList(context, spfType, spf_8)) {
        return SPF_8;
      }
      if (inList(context, spfType, spf_15)) {
        return SPF_15;
      }
      if (inList(context, spfType, spf_30)) {
        return SPF_30;
      }
      if (inList(context, spfType, spf_45)) {
        return SPF_45;
      }
      if (inList(context, spfType, spf_50)) {
        return SPF_50;
      }
      return "";
    }

    /**
     * Returns true if the activity type is in the list.
     * 
     * @param context the context
     * @param spfType the activity type
     * @param list the list
     */
    private static boolean inList(Context context, String spfType, int[] list) {
      for (int i : list) {
        if (context.getString(i).equals(spfType)) {
          return true;
        }
      }
      return false;
    }
  }
