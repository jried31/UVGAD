package edu.dartmouth.cs.myruns5;




import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import android.content.Context;
import android.util.Pair;

public class SkinTypeIconUtils {

    private static final String TYPE_I = "I";
    private static final String TYPE_II = "II";
    private static final String TYPE_III = "III";
    private static final String TYPE_IV = "IV";
    private static final String TYPE_V = "V";
    private static final String TYPE_VI = "VI";

    private SkinTypeIconUtils() {}

    private static LinkedHashMap<String, Pair<Integer, Integer>>
        map = new LinkedHashMap<String, Pair<Integer, Integer>>();

    static {
      map.put(TYPE_I,new Pair<Integer, Integer>(R.string.skin_type_I , R.drawable.fitzpatrick_type_1));
      map.put(TYPE_II, new Pair<Integer, Integer>(R.string.skin_type_II , R.drawable.fitzpatrick_type_2));
      map.put(TYPE_III, new Pair<Integer, Integer>(R.string.skin_type_III , R.drawable.fitzpatrick_type_3));
      map.put(TYPE_IV, new Pair<Integer, Integer>(R.string.skin_type_IV , R.drawable.fitzpatrick_type_4));
      map.put(TYPE_V, new Pair<Integer, Integer>(R.string.skin_type_V , R.drawable.fitzpatrick_type_5));
      map.put(TYPE_VI, new Pair<Integer, Integer>(R.string.skin_type_VI , R.drawable.fitzpatrick_type_6));
    }

    private static int[] type_I = new int[] { R.string.skin_type_I};
    private static int[] type_II = new int[] { R.string.skin_type_II};
    private static int[] type_III = new int[] { R.string.skin_type_III };
    private static int[] type_IV = new int[] {R.string.skin_type_IV };
    private static int[] type_V = new int[] { R.string.skin_type_V };
    private static int[] type_VI = new int[] {R.string.skin_type_VI};
    
    /**
     * Gets the icon drawable.
     * 
     * @param iconValue the icon value
     */
    public static int getIconDrawable(String iconValue) {
      if (iconValue == null || iconValue.equals("")) {
        return R.drawable.skin_type;
      }
      Pair<Integer, Integer> pair = map.get(iconValue);
      return pair == null ? R.drawable.skin_type : pair.second;
    }

    /**
     * Gets the icon skin type.
     * 
     * @param iconValue the icon value
     */
    public static int getIconSkinType(String iconValue) {
      if (iconValue == null || iconValue.equals("")) {
        return R.string.generic_skin_type;
      }
      Pair<Integer, Integer> pair = map.get(iconValue);
      return pair == null ? R.string.generic_skin_type : pair.first;
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
     * @param activityType the activity type
     */
    public static String getIconValue(Context context, String activityType) {
      if (inList(context, activityType, type_I)) {
        return TYPE_I;
      }
      if (inList(context, activityType, type_II)) {
        return TYPE_II;
      }
      if (inList(context, activityType, type_III)) {
        return TYPE_III;
      }
      if (inList(context, activityType, type_IV)) {
        return TYPE_IV;
      }
      if (inList(context, activityType, type_V)) {
        return TYPE_V;
      }
      if (inList(context, activityType, type_VI)) {
        return TYPE_VI;
      }
      return "";
    }

    /**
     * Returns true if the activity type is in the list.
     * 
     * @param context the context
     * @param activityType the activity type
     * @param list the list
     */
    private static boolean inList(Context context, String activityType, int[] list) {
      for (int i : list) {
        if (context.getString(i).equals(activityType)) {
          return true;
        }
      }
      return false;
    }
  }
