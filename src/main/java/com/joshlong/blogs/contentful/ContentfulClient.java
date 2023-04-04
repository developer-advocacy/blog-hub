package com.joshlong.blogs.contentful;

import reactor.core.publisher.Mono;

public interface ContentfulClient {

    Mono<BlogPostDraft> createDraft(BlogPost blogPost);

    Mono<Boolean> publish(BlogPostDraft blogPostDraft);
}
