package com.joshlong.bloggateway;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.time.Instant;
import java.util.Properties;

@Slf4j
class BlogGatewayApplicationTests {


    private final Properties properties = new Properties();

    BlogGatewayApplicationTests() throws Exception {
        try (var in = new ClassPathResource("/application.properties")
                .getInputStream()) {
            this.properties.load(in);
        }
    }

    @Test
    void joshlongDotComBlogPost() {
        var pat = this.properties.get("blog.gateway.contentful.personal-access-token");
        log.info("the pat is {}", pat);
    }

    @Test
    void contentfulBlogPost() {

    }

}


record BlogPost(String title, String author, Instant published, Instant scheduled) {
}

