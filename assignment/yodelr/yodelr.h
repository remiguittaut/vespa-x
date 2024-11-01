/*
 * Copyright Vespa.ai.
 * Not for redistribution outside of candidate interview context.
 */

#pragma once

#include <string>
#include <vector>
#include <stdint.h>

namespace yodelr {

using PostTexts = std::vector<std::string>;
using Topics = std::vector<std::string>;

/**
 * The Yodelr service interface.
 *
 * This allows adding and deleting users, adding and retrieving posts
 * and getting trending topics.
 */
class Yodelr {
public:
    virtual ~Yodelr() {}

    virtual void addUser(const std::string &userName) = 0;

    virtual void addPost(const std::string &userName,
                         const std::string &postText,
                         uint64_t timestamp) = 0;

    virtual void deleteUser(const std::string &userName) = 0;

    virtual PostTexts getPostsForUser(const std::string &userName) const = 0;

    virtual PostTexts getPostsForTopic(const std::string &topic) const = 0;

    virtual Topics getTrendingTopics(uint64_t fromTimestamp,
                                     uint64_t toTimestamp) const = 0;
};

}
