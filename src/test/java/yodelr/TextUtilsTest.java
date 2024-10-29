package yodelr;

import org.junit.jupiter.api.Test;

import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TextUtilsTest {

  @Test
  void getUniqueHashTagsShoudExtraHashTags() {
    var post = "#First place, other #hash-tags should be parsed #without_problem!";

    assertIterableEquals(List.of("First", "hash-tags", "without_problem"),
      TextUtils.getUniqueHashTags(post)
        .stream()
        .sorted()
        .toList()
    );
  }

  @Test
  void normalizeTopicShouldNormalizeTopicNames() {
    assertTrue(
      TextUtils.normalizeTopic("First").equals("first") &&
        TextUtils.normalizeTopic("hash-tags").equals("hashtags") &&
        TextUtils.normalizeTopic("without_problem").equals("withoutproblem")
    );
  }
}