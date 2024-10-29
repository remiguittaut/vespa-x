package yodelr;

import static java.lang.System.out;

public class ConsoleUtils {
  public static void showHelp() {
    out.println("=== Help ===");
    out.println("_> help: this help");
    out.println("_> adduser <user>: add a user");
    out.println("_> deleteuser <user>: delete a user with his posts");
    out.println("_> post <user> '<'<post>'>': post a new msg (example: post myser <my message>)");
    out.println("_> trending <from timestamp> to <to timestamp>: display trending topics in a period");
    out.println("_> userposts <user>: display user posts");
    out.println("_> topicposts <topic>: display topic posts");
    out.println("_> quit: quit the cli");
    out.println("============");
    out.println();
  }
}
