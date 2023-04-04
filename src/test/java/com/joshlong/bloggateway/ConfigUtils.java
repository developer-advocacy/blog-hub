package com.joshlong.bloggateway;


import lombok.SneakyThrows;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.Assert;

import java.util.Properties;

/**
 * source the personal access token for tests.
 */
abstract class ConfigUtils {

    @SneakyThrows
    private static String sourceValue(String propertyName, String envName) {
        var properties = new Properties();
        try (var in = new ClassPathResource("/application.properties").getInputStream()) {
            properties.load(in);
        }
        var pat = System.getenv().getOrDefault(
                envName,
                (String) properties.getOrDefault(propertyName, null));
        Assert.notNull(pat, "the personal access token should not be null!");
        return pat;
    }

    public static String authorId() {
        return sourceValue("blog.gateway.contentful.author-id", "BLOG_GATEWAY_CONTENTFUL_AUTHOR_ID");
    }

    public static String spaceId() {
        return sourceValue("blog.gateway.contentful.space-id", "BLOG_GATEWAY_CONTENTFUL_SPACE_ID");
    }

    public static String personalAccessToken() {
        return sourceValue("blog.gateway.contentful.personal-access-token", "BLOG_GATEWAY_CONTENTFUL_PERSONAL_ACCESS_TOKEN");
    }
}
