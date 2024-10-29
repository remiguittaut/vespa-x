package yodelr;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static yodelr.TextUtils.getUniqueHashTags;

public class YodelrImpl implements Yodelr {

  public static int MaxPostLength = 140;

  // we do not have any concurrency, we settle with mutable structures.

  // PostId=PostTimestamp -> post.
  // We need it sorted for efficient key range lookups => TreeMap
  private final NavigableMap<Long, Post> posts = new TreeMap<>();

  // Hashtag -> posts
  // we don't care about the order => HashMap more efficient than TreeMap
  private final Map<String, Set<Long>> hashTagIndex = new HashMap<>();

  // User -> posts
  // we don't care about the order => HashMap more efficient than TreeMap
  private final Map<String, Set<Long>> userPosts = new HashMap<>();

  @Override
  public void addUser(String userName) {
    assert userName != null;

    if (userPosts.containsKey(userName))
      throw new IllegalArgumentException("User name already exists");

    userPosts.put(userName, Set.of());
  }

  @Override
  public void addPost(String userName, String postText, long timestamp) {
    assert userName != null;
    assert postText != null;

    if (postText.length() > MaxPostLength)
      throw new IllegalArgumentException("Post text too long");
    else if (!userPosts.containsKey(userName))
      throw new IllegalArgumentException("User does not exist");
    else {
      var lastEntry = posts.lastEntry();
      if(lastEntry != null &&  lastEntry.getKey() >= timestamp)
        throw new IllegalArgumentException("Post is late");
    }

    var hashTags = getUniqueHashTags(postText);

    userPosts
      .merge(userName,
        Set.of(timestamp),
        (old, newVal) ->
          Stream.concat(old.stream(), newVal.stream())
            .collect(Collectors.toSet())
      );

    posts.put(timestamp, new Post(postText, hashTags));

    hashTags.stream().map(TextUtils::normalizeTopic).forEach(tag ->
      hashTagIndex.merge(
        tag,
        Set.of(timestamp),
        (old, newVal) ->
          Stream.concat(old.stream(), newVal.stream())
            .collect(Collectors.toSet())
      )
    );
  }

  private record PostTopics(Long id, Set<String> hashTags) { }

  @Override
  public void deleteUser(String userName) {
    assert userName != null;

    // seems overkill, multiple traversals, but no full-scan => going only through keys
    // nested foreach, but on low cardinality

    var orphanIds = userPosts.remove(userName);

    if (orphanIds == null)
      throw new IllegalArgumentException("User does not exist");

    var orphans = orphanIds
      .stream()
      .map(id -> {
        var p = posts.remove(id);
        if (p == null)
          return null;
        return new PostTopics(id, p.hashTags());
      })
      .filter(Objects::nonNull)
      .collect(Collectors.toSet());

    var refsPerTopic = groupPostsPerTopic(orphans);

    refsPerTopic.forEach((topic, refs) ->
      hashTagIndex.computeIfPresent(topic,
        (t, allRefs) -> {
          var newRefs = allRefs.stream().filter(r -> !refs.contains(r)).collect(Collectors.toSet());
          if(!newRefs.isEmpty()) return newRefs;
          return null;
        }
      )
    );
  }

  @Override
  public List<String> getPostsForUser(String userName) {
    assert userName != null;

    var refs = userPosts.get(userName);

    if (refs == null)
      throw new IllegalArgumentException("User does not exist");
    else
      return
        refs
          .stream()
          .map(posts::get)
          .filter(Objects::nonNull)
          .map(Post::text)
          .collect(Collectors.toList());
  }

  @Override
  public List<String> getPostsForTopic(String topic) {
    assert topic != null;

    var topicPostsRefs = hashTagIndex.get(TextUtils.normalizeTopic(topic));

    if(topicPostsRefs == null)
      return List.of();
    else
      return topicPostsRefs
        .stream()
        .map(posts::get)
        .filter(Objects::nonNull)
        .map(Post::text)
        .collect(Collectors.toList());
  }

  @Override
  public List<String> getTrendingTopics(long fromTimestamp, long toTimestamp) {
    // all of this seems unnecessarily inefficient, going through streams and re-enumerating
    // many times. It might be me trying to do in java what I would do in scala...

    var filteredPosts = posts.subMap(fromTimestamp, true, toTimestamp, true);

    return groupPostsPerTopic(
      filteredPosts
        .entrySet()
        .stream()
        .map(entry -> new PostTopics(entry.getKey(), entry.getValue().hashTags()))
        .collect(Collectors.toSet())
    ).entrySet()
      .stream()
      .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().size()))
      .entrySet()
      .stream()
      .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
      .map(Map.Entry::getKey)
      .collect(Collectors.toList());
  }

  private Map<String, Set<Long>> groupPostsPerTopic(Set<PostTopics> postTopics) {
    // compute list of postRefs per hashtag. mutable, no sort

    Map<String, Set<Long>> refsPerTopic = new HashMap<>();

    postTopics.forEach(p -> {
      p.hashTags()
        .stream().map(TextUtils::normalizeTopic)
        .forEach(t ->
          refsPerTopic.merge(t, Set.of(p.id),
            (existing, current) ->
              Stream
                .concat(existing.stream(), current.stream())
                .collect(Collectors.toSet())
          )
        );
    });

    return refsPerTopic;
  }
}
