package com.joshlong.blogs.contentful;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import reactor.test.StepVerifier;


@SpringBootTest
@Import(ContentfulConfiguration.class)
class DefaultContentfulClientTest {

    private final ContentfulClient client;

    private final ContentfulProperties contentfulProperties;

    DefaultContentfulClientTest(@Autowired ContentfulClient client,
                                @Autowired ContentfulProperties contentfulProperties) {
        this.client = client;
        this.contentfulProperties = contentfulProperties;
    }

    @Test
    void contentful() {
        var body = """
                ## Hello World
                Now is the time for [a link](https://spring.io/blog).
                """.stripIndent();
        var num = System.currentTimeMillis();
        var blogPost = new BlogPost(new Author(this.contentfulProperties.authorId()),
                "this is an OOP title [" + num + "]", "Engineering", body);
        var published = this.client.createDraft(blogPost).flatMap(this.client::publish);
        StepVerifier
                .create(published)
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    void blogPostToJson() {
        var objectMapper = new ObjectMapper();
        var num = System.currentTimeMillis();
        var title = "this is a title #" + num;
        var bp = new BlogPost(new Author("test"), title, "Engineering", "Hello, world!");
        var jsonNode = bp.toJsonNode(objectMapper);
        Assertions.assertEquals(jsonNode.get("fields").get("author").get("en-US").get("sys").get("id").textValue(), "test");
        Assertions.assertEquals(jsonNode.get("fields").get("title").get("en-US").textValue(), title);
    }
}