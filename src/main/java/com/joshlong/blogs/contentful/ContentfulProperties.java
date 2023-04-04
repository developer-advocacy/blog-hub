package com.joshlong.blogs.contentful;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties (prefix = "blog.gateway.contentful")
public record ContentfulProperties (
        String personalAccessToken,
        String authorId,
        String spaceId,
        String environment
) {
}
