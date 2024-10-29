package yodelr;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import static java.util.regex.Pattern.compile;

public class TextUtils {
  private static final Pattern pattern = compile("#(?<topic>\\w+[\\w-]+)");

  public static Set<String> getUniqueHashTags(String input) {
    var tags = new HashSet<String>();
    var matcher = pattern.matcher(input);
    while (matcher.find())
      tags.add(matcher.group("topic"));

    return tags;
  }

  // normalizing limited to lowercase / remove special chars.
  // could benefit from stemming, but I'm not gonna re-implement Porter here...
  public static String normalizeTopic(String input) {
    return input
      .toLowerCase()
      .replace("_", "")
      .replace("-", "");
  }
}
