package yodelr;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class YodelrTest {

  private Yodelr api = null;

  @BeforeEach
  public void setup() {
    api = Yodelr.make();
  }

  @Test
  void addUserShouldSucceed() {
    assertDoesNotThrow(() -> api.addUser("username"));

    // this test would fail if getPostsForUser fails, but it's the only
    // way to validate that the user was effectively added,
    // and the error would be detected by another test
    var messagesForUser =
      assertDoesNotThrow(() -> api.getPostsForUser("username"));
    assertIterableEquals(messagesForUser, List.of());
  }

  @Test
  void addUserShouldThrowIfUsernameAlreadyExists() {
    api.addUser("username");
    assertThrows(Throwable.class, () -> api.addUser("username"));
  }

  @Test
  void addPostShouldSucceed() {
    api.addUser("username");
    assertDoesNotThrow(() -> api.addPost("username", "the post", 1));
    var posts = assertDoesNotThrow(() -> api.getPostsForUser("username"));

    assertIterableEquals(List.of("the post"), posts);
  }

  @Test
  void addPostShouldThrowIfUserDoesNotExist() {
    assertThrows(Throwable.class, () -> api.addPost("username", "the post", 1));
  }

  @Test
  void addPostShouldThrowIfPostingAMessageInThePast() {
    api.addUser("username");
    api.addPost("username", "the post", 5);
    assertThrows(Throwable.class, () -> api.addPost("username", "the post 2", 4));
  }

  @Test
  void addPostShouldRegisterMentionedTopics() {
    api.addUser("username");
    api.addPost("username", "the post is mentioning an event from #Vespa happening in #oslo. #awesome! #norway", 1);
    var topics = api.getTrendingTopics(0, 10);

    assertTrue(
      topics.contains("vespa") &&
        topics.contains("oslo") &&
        topics.contains("awesome") &&
        topics.contains("norway")
    );
  }

  @Test
  void deleteUserShouldRemoveAllInformationAboutTheUser() {
    api.addUser("username");
    api.addPost("username", "the post is mentioning an event #Vespa happening in #oslo. #awesome! #norway", 1);

    api.addUser("username2");
    api.addPost("username2", "This one is about #americanElection of the new #president", 2);

    api.deleteUser("username");

    assertThrows(Throwable.class, () -> api.getPostsForUser("username"));
    var trendingTopics = api.getTrendingTopics(0, 10);

    assertFalse(
      trendingTopics.contains("vespa") ||
        trendingTopics.contains("oslo") ||
        trendingTopics.contains("awesome") ||
        trendingTopics.contains("norway")
    );

    assertIterableEquals(List.of(), api.getPostsForTopic("oslo"));
  }

  @Test
  void getPostsForUserShouldSucceed() {
    var post1 = "the post is mentioning an event #Vespa happening in #oslo. #awesome! #norway";
    var post2 = "This one is about #americanElection of the new #president";

    api.addUser("username");
    api.addPost("username", post1, 1);

    api.addUser("username2");
    api.addPost("username2", post2, 2);

    assertIterableEquals(List.of(post1), api.getPostsForUser("username"));
    assertIterableEquals(List.of(post2), api.getPostsForUser("username2"));
  }

  @Test
  void getPostsForUserShouldThrowIfUserDoesNotExist() {
    assertThrows(Throwable.class, () -> api.getPostsForUser("username"));
  }

  @Test
  void getPostsForTopicShouldSucceed() {
    var post1 = "the post is mentioning an event #Vespa happening in #oslo. #awesome! #norway";
    var post2 = "This one is about #americanElection of the new #president. #us";
    var post3 = "Election in #us are a total disaster.";

    api.addUser("username");
    api.addPost("username", post1, 1);

    api.addUser("username2");
    api.addPost("username2", post2, 2);
    api.addPost("username2", post3, 3);

    assertIterableEquals(List.of(post1), api.getPostsForTopic("norway"));
    assertIterableEquals(List.of(post2, post3), api.getPostsForTopic("us"));
    assertIterableEquals(List.of(), api.getPostsForTopic("some-topic"));
  }

  @Test
  void getTrendingTopicsShouldSucceed() {
    var post1 = "the post is mentioning an event #Vespa happening in #oslo.";
    var post2 = "Great stuff to visit in #oslo.";
    var post3 = "This one is about #us elections of the new president.";
    var post4 = "#us elections starting!";
    var post5 = "Election in #us are a total disaster.";
    var post6 = "new Vector db from #Vespa.";

    var post7 = "Heading to some great conf in #oslo";
    var post8 = "Another post mentioning the #Oslo.";

    api.addUser("username");
    api.addPost("username", post1, 1);
    api.addUser("username2");
    api.addPost("username2", post2, 2);
    api.addPost("username2", post3, 3);
    api.addPost("username", post4, 4);
    api.addUser("username3");
    api.addPost("username3", post5, 5);
    api.addPost("username3", post6, 6);

    api.addPost("username2", post7, 7);
    api.addPost("username2", post8, 8);

    assertIterableEquals(List.of("us", "oslo", "vespa"), api.getTrendingTopics(0, 6));
    assertIterableEquals(List.of("oslo", "us", "vespa"), api.getTrendingTopics(0, 10));
  }
}