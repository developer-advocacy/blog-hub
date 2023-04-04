package com.joshlong.bloggateway;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.HashMap;
import java.util.Locale;
import java.util.function.Consumer;
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
    void joshlongDotComBlogPost() throws Exception {

        var webClienBuilder = WebClient.builder();
        var client = buildWebClient(webClienBuilder, this.spaceId, "testing",
                this.personalAccessToken);
        var body = """
                                
                ## Hello World
                        
                Now is the time for [a link](https://spring.io/blog).
                """;
        var body2 = "Hello, world! ";
        var response = post(client, this.authorId, "a simple title", body2);
        response.subscribe(new Consumer<String>() {
            @Override
            public void accept(String response) {
                System.out.println("got a response! " + response);
            }
        });

        Thread.sleep(10 * 1000);



        /*curl --include \
     --request POST \
     --header 'Authorization: Bearer $YOUR_PERSONAL_ACCESS_TOKEN' \
     --header 'Content-Type: application/vnd.contentful.management.v1+json' \
     --header 'X-Contentful-Content-Type: blogPost' \
     --data-binary '{
       "fields": {
         "title": {
           "en-US": "Test Title"
         },
         "body": {
           "en-US": "Your blog body"
         },
         "author": {
           "en-US": {
              "sys": {
              "type": "Link",
              "linkType": "Entry",
              "id": "$AUTHOR_ID"
            }
          }
         },
         "slug": {
           "en-US": "title-that-is-hyphenated"
         },
         "category": {
           "en-US": "Your category (either News/Engineering/Releases)"
         }
       }
     }' \
     https://api.contentful.com/spaces/$SPACEID/environments/testing/entries/
*/
    }


}


record BlogPost(String title, String author, Instant published, Instant scheduled) {
}

