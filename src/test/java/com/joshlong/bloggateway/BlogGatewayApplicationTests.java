package com.joshlong.bloggateway;

import org.junit.jupiter.api.Test;

import java.time.Instant;


class BlogGatewayApplicationTests {

    @Test
    void joshlongDotComBlogPost() {

    }

    @Test
    void contentfulBlogPost() {

    }

}




record BlogPost (String title, String author, Instant published , Instant scheduled){}

