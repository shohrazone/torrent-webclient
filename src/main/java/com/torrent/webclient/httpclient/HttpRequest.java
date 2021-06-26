package com.torrent.webclient.httpclient;

import org.springframework.http.HttpMethod;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface HttpRequest {
    String baseURL();

    String uri() default "";

    Class responseType() default Object.class;

    HttpMethod httpMethod() default HttpMethod.GET;

    Authentication authentication() default Authentication.UN_AUTHENTICATED;

    public enum Authentication {AUTHENTICATED, UN_AUTHENTICATED}
}
