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

    private final String personalAccessToken = ConfigUtils.personalAccessToken();
    private final String authorId = ConfigUtils.authorId();
    private final String spaceId = ConfigUtils.spaceId();

    private static WebClient buildWebClient(WebClient.Builder builder, String spaceId,
                                            String environment,
                                            String accessToken) {
        return builder
                .baseUrl("https://api.contentful.com/spaces/" + spaceId + "/environments/" + environment +
                         "/entries/")
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @Deprecated
    private static Mono<String> post(WebClient webClient, String authorId,
                                     String title, String body) {

        var requestBody = "{\n"
                          + "  \"fields\": {\n"
                          + "    \"title\": {\n"
                          + "      \"en-US\": \"" + title + "\"\n"
                          + "    },\n"
                          + "    \"body\": {\n"
                          + "      \"en-US\": \"" + body + "\"\n"
                          + "    },\n"
                          + "    \"author\": {\n"
                          + "      \"en-US\": {\n"
                          + "        \"sys\": {\n"
                          + "          \"type\": \"Link\",\n"
                          + "          \"linkType\": \"Entry\",\n"
                          + "          \"id\": \"" + authorId + "\"\n"
                          + "        }\n"
                          + "      }\n"
                          + "    },\n"
                          + "    \"slug\": {\n"
                          + "      \"en-US\": \"title-that-is-hyphenated\"\n"
                          + "    },\n"
                          + "    \"category\": {\n"
                          + "      \"en-US\": \"Engineering\"\n"
                          + "    }\n"
                          + "  }\n"
                          + "}";


        return webClient.post()
                .body(BodyInserters.fromValue(requestBody))
                .header("X-Contentful-Content-Type", "blogPost")
                .retrieve()
                .bodyToMono(String.class);
    }

    private static Mono<String> ooPost(ObjectMapper objectMapper, WebClient webClient, BlogPost post) {

        var requestBody = "{ " + post.toJsonNode(objectMapper).toPrettyString() +
                          "}";
        return webClient.post()
                .body(BodyInserters.fromValue(requestBody))
                .header("X-Contentful-Content-Type", "blogPost")
                .retrieve()
                .bodyToMono(String.class);
    }

    private static String hyphenateTitle(String title) {
        var sb = new StringBuffer();
        var replacements = new HashMap<Predicate<Character>, Function<Character, String>>();
        replacements.put(Character::isWhitespace, c -> "-");
        replacements.put(c -> Character.isAlphabetic(c) || Character.isDigit(c), c -> (Character.toString(c)).toLowerCase(Locale.getDefault()));
        for (var c : title.toCharArray()) {
            for (var test : replacements.keySet())
                if (test.test(c))
                    sb.append(replacements.get(test).apply(c));
        }
        return sb.toString();
    }

    @Test
    void hyphenate() {
        var hyphenatedTitle = hyphenateTitle("Hyphenate!!! ...It's gonna be all riiight... Hyphenate!");
        Assertions.assertEquals("hyphenate-its-gonna-be-all-riiight-hyphenate", hyphenatedTitle);
    }

    @Test
    void contentful1() throws Exception {

        var om = new ObjectMapper();
        var webClienBuilder = WebClient.builder();
        var client = buildWebClient(webClienBuilder, this.spaceId, "testing",
                this.personalAccessToken);
        var body = """
                                
                ## Hello World
                        
                Now is the time for [a link](https://spring.io/blog).
                """;
        var body2 = "Hello, world! ";
        var response = ooPost(om, client, new BlogPost(new Author(this.authorId), "this is an OOP title",
                "Engineering", "this-is-an-oop-slug", body));
        response.subscribe(response1 -> System.out.println("got a response! " + response1));
        Thread.sleep(5 * 1000);


    }


    @Test
    void blogPostToJson() {
        var objectMapper = new ObjectMapper();
        var num = System.currentTimeMillis();
        var bp = new BlogPost(new Author(this.authorId),
                "this is a title # " + num, "Engineering", "this-is-a-title", "Hello, world!");
        var jsonNode = bp.toJsonNode(objectMapper);
        Assertions.assertEquals(jsonNode.get("author").get("en-US").get("sys").get("id").textValue(),
                "author-id");
    }

}


record BlogPost(Author author,
                String title, String category, String slug, String body) {

    @SneakyThrows
    public JsonNode toJsonNode(ObjectMapper objectMapper) {

        var ow = objectMapper.createObjectNode();
        ow.put("category", singleFieldObjectNode(objectMapper, this.category));
        ow.put("title", singleFieldObjectNode(objectMapper, this.title));
        ow.put("title", singleFieldObjectNode(objectMapper, this.title));
        ow.put("slug", singleFieldObjectNode(objectMapper, this.slug));
        ow.put("body", singleFieldObjectNode(objectMapper, this.body));
        var sys = Map.of("sys", Map.of("type", "Link", "linkType", "Entry", "id", this.author.id()));
        var enUs = Map.of("en-US", sys);
        var jsonForAuthor = objectMapper.writeValueAsString(enUs);
        ow.put("author", objectMapper.readTree(jsonForAuthor));
        return ow;
    }

    private ObjectNode singleFieldObjectNode(ObjectMapper objectMapper,
                                             String value) {
        var categoryON = objectMapper.createObjectNode();
        categoryON.put("en-US", value);
        return categoryON;

    }
}


record Author(String id) {
}
