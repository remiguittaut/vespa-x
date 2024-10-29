package yodelr;

import java.io.DataInput;
import java.io.IOException;
import java.time.Instant;
import java.util.regex.Pattern;

import static java.lang.System.out;
import static java.util.regex.Pattern.compile;
import static yodelr.ConsoleUtils.showHelp;

public class Cli {

  // to make it more realistic, but a simple incremented long would be ok.
  private static final long epochBase = Instant.now().toEpochMilli() / 1000;

  private static final Pattern addUserPattern = compile("^adduser (?<user>\\w+[\\w-]+)$");
  private static final Pattern trendingTopicsPattern = compile("^trending (?<from>\\d+) to (?<to>\\d+)$");
  private static final Pattern deleteUserPattern = compile("^deleteuser (?<user>\\w+[\\w-]+)$");
  private static final Pattern postsForTopicPattern = compile("^topicposts (?<topic>\\w+[\\w-]+)$");
  private static final Pattern postsForUserPattern = compile("^userposts (?<user>\\w+[\\w-]+)$");
  private static final Pattern postMessagePattern = compile("^post (?<user>\\w+[\\w-]+) <(?<post>.+)>$");

  public static void loop(DataInput in, Yodelr api) throws IOException {
    out.print("_> ");
    var cmd = in.readLine();

    if(cmd == null) {
      out.println("Can't read from standard input. Exiting.");
      return;
    }

    try {
      if (cmd.equals("help")) showHelp();
      else if (cmd.equals("quit")) System.exit(0);
      else if (addUserPattern.matcher(cmd).matches()) {
        var matcher = addUserPattern.matcher(cmd);

        if (matcher.find()) {
          var user = matcher.group("user");
          api.addUser(user);
          out.printf("Added user: %s%n", user);
        } else
          out.println("Could not add user: " + cmd);
      } else if (deleteUserPattern.matcher(cmd).matches()) {
        var matcher = deleteUserPattern.matcher(cmd);

        if (matcher.find()) {
          var user = matcher.group("user");
          api.deleteUser(user);
          out.printf("Removed user: %s%n", user);
        } else
          out.println("Could not remove user: " + cmd);
      } else if (postsForUserPattern.matcher(cmd).matches()) {
        var matcher = postsForUserPattern.matcher(cmd);

        if (matcher.find()) {
          var user = matcher.group("user");
          out.println(api.getPostsForUser(user));
        } else
          out.println("Could not display posts");
      } else if (postsForTopicPattern.matcher(cmd).matches()) {
        var matcher = postsForTopicPattern.matcher(cmd);

        if (matcher.find()) {
          var topic = matcher.group("topic");
          out.println(api.getPostsForTopic(topic));
        } else
          out.println("Could not display posts");
      } else if (trendingTopicsPattern.matcher(cmd).matches()) {
        var matcher = trendingTopicsPattern.matcher(cmd);

        if(matcher.find()) {
          var from = Long.parseLong(matcher.group("from"));
          var to = Long.parseLong(matcher.group("to"));
          out.println(api.getTrendingTopics(from, to));
        } else
          out.println("Could not display trending topics");
      } else if (postMessagePattern.matcher(cmd).matches()) {
        var matcher = postMessagePattern.matcher(cmd);

        if(matcher.find()) {
          var user = matcher.group("user");
          var post = matcher.group("post");
          var ts = (Instant.now().toEpochMilli() / 1000) - epochBase;
          api.addPost(user, post, ts);
          out.printf("Added post for user %s: \"%s\" at timestamp %d%n", user, post, ts);
        } else
          out.println("Could not add post");
      } else
        out.println("Unknown command: " + cmd);
    } catch (Throwable t) {
      out.println("Something went wrong while handling command: " + t.getMessage());
    }
    loop(in, api);
  }
}
