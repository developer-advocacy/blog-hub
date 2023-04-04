package com.joshlong.blogs.contentful;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
class DefaultContentfulClient implements ContentfulClient {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final String spaceId, environment;

    DefaultContentfulClient(ObjectMapper objectMapper, WebClient webClient, String spaceId,  String environment) {
        this.webClient = webClient;
        this.objectMapper = objectMapper;
        this.spaceId = spaceId;
        this.environment = environment;
    }

    @Override
    public Mono<Boolean> publish(BlogPostDraft blogPostDraft) {
        return this.webClient//
                .put()//
                .uri("https://api.contentful.com/spaces/{spaceId}/environments/{env}/entries/{entryId}/published",
                        this.spaceId, this.environment, blogPostDraft.draftId())//
                .header("X-Contentful-Version", "1")//
                .exchangeToMono(clientResponse -> clientResponse.bodyToMono(String.class))
                .map(json -> true);
    }

    @Override
    public Mono<BlogPostDraft> createDraft(BlogPost blogPost) {
        var requestBody = blogPost.toJsonNode(this.objectMapper).toPrettyString();
        return this.webClient//
                .post()//
                .uri("https://api.contentful.com/spaces/{spaceId}/environments/{env}/entries/", this.spaceId, this.environment)//
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
