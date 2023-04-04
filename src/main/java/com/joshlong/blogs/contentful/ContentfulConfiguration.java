package com.joshlong.blogs.contentful;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@EnableConfigurationProperties(ContentfulProperties.class)
class ContentfulConfiguration {

    @Bean
    ContentfulClient contentfulClient(ObjectMapper objectMapper, WebClient webClient,
                                      ContentfulProperties properties) {
        return new DefaultContentfulClient(objectMapper, webClient, properties.spaceId(),
                properties.environment());
    }

    @Bean
    WebClient webClient(WebClient.Builder builder, ContentfulProperties contentfulProperties) {
        return builder//
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + contentfulProperties.personalAccessToken())//
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)//
                .build();
    }
}
