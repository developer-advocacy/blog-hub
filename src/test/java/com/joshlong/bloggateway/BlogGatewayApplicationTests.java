package com.joshlong.bloggateway;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

@Slf4j
class BlogGatewayApplicationTests {

    private final ContentfulClient client;

    BlogGatewayApplicationTests() {
        var personalAccessToken = ConfigUtils.personalAccessToken();
        var spaceId = ConfigUtils.spaceId();
        var environment = "testing";
        var contentfulConfiguration = new ContentfulConfiguration();
        var objectMapper = contentfulConfiguration.objectMapper();
        var builder = contentfulConfiguration.webClientBuilder();
        var webClient = contentfulConfiguration.webClient(builder, spaceId, environment, personalAccessToken);
        this.client = contentfulConfiguration.contentfulClient(objectMapper, webClient);
    }

    @Test
    void contentful() throws Exception {
        var body = """
                    ## Hello World
                    Now is the time for [a link](https://spring.io/blog).
                """;
        var num = System.currentTimeMillis();
        var blogPost = new BlogPost(new Author(ConfigUtils.authorId()), "this is an OOP title [" + num + "]", "Engineering", body);
        client.createDraft(blogPost).subscribe(post -> log.info(System.lineSeparator() + "==========" + System.lineSeparator() + post + System.lineSeparator() + "=========="));
        Thread.sleep(5 * 1000);
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

@Slf4j
class DefaultContentfulClient implements ContentfulClient {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    DefaultContentfulClient(ObjectMapper objectMapper, WebClient webClient) {
        this.webClient = webClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<BlogPostDraft> createDraft(BlogPost blogPost) {
        var requestBody = blogPost.toJsonNode(this.objectMapper).toPrettyString();
        return this.webClient//
                .post()//
                .body(BodyInserters.fromValue(requestBody))//
                .header("X-Contentful-Content-Type", "blogPost")//
                .retrieve()//
                .bodyToMono(JsonNode.class)//
                .mapNotNull(root -> {
                    var id = root.get("sys").get("id").asText();
                    return new BlogPostDraft(id, blogPost);
                });

    }


}

record BlogPostDraft(String draftId, BlogPost post) {
}

class ContentfulConfiguration {


    WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }

    ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    ContentfulClient contentfulClient(ObjectMapper objectMapper, WebClient webClient) {
        return new DefaultContentfulClient(objectMapper, webClient);
    }

    WebClient webClient(WebClient.Builder builder, String spaceId, String environment, String accessToken) {
        return builder.baseUrl("https://api.contentful.com/spaces/" + spaceId + "/environments/" + environment + "/entries/").defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken).defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE).build();
    }
}

interface ContentfulClient {

    Mono<BlogPostDraft> createDraft(BlogPost blogPost);
}

record BlogPost(Author author, String title, String category, String body) {

    public String slug() {
        var sb = new StringBuffer();
        var replacements = new HashMap<Predicate<Character>, Function<Character, String>>();
        replacements.put(Character::isWhitespace, c -> "-");
        replacements.put(c -> Character.isAlphabetic(c) || Character.isDigit(c), c -> Character.toString(c).toLowerCase(Locale.getDefault()));
        for (var c : this.title().toCharArray()) {
            for (var test : replacements.keySet())
                if (test.test(c))
                    sb.append(replacements.get(test).apply(c));
        }
        return sb.toString();
    }

    @SneakyThrows
    public JsonNode toJsonNode(ObjectMapper objectMapper) {
        var ow = objectMapper.createObjectNode();
        ow.put("category", singleFieldObjectNode(objectMapper, this.category));
        ow.put("title", singleFieldObjectNode(objectMapper, this.title));
        ow.put("title", singleFieldObjectNode(objectMapper, this.title));
        ow.put("slug", singleFieldObjectNode(objectMapper, this.slug()));
        ow.put("body", singleFieldObjectNode(objectMapper, this.body));
        var sys = Map.of("sys", Map.of("type", "Link", "linkType", "Entry", "id", this.author.id()));
        var enUs = Map.of("en-US", sys);
        var jsonForAuthor = objectMapper.writeValueAsString(enUs);
        ow.put("author", objectMapper.readTree(jsonForAuthor));
        var fields = objectMapper.createObjectNode();
        fields.put("fields", ow);
        return fields;
    }

    private ObjectNode singleFieldObjectNode(ObjectMapper objectMapper, String value) {
        var categoryON = objectMapper.createObjectNode();
        categoryON.put("en-US", value);
        return categoryON;
    }
}


record Author(String id) {
}
