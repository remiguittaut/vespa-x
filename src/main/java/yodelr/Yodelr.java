/**
 * Copyright Vespa.ai.
 * Not for redistribution outside of candidate interview context.
 */

package yodelr;

import java.util.List;

/**
 * The Yodelr service interface.
 *
 * This allows adding and deleting users, adding and retrieving posts
 * and getting trending topics.
 */
public interface Yodelr {

    void addUser(String userName);

    void addPost(String userName, String postText, long timestamp);

    void deleteUser(String userName);

    List<String> getPostsForUser(String userName);

    List<String> getPostsForTopic(String topic);

    List<String> getTrendingTopics(long fromTimestamp, long toTimestamp);

    static Yodelr make() { return new YodelrImpl(); }

}

