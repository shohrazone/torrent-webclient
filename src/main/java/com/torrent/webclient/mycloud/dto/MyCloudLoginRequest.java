package com.torrent.webclient.mycloud.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import org.springframework.core.env.Environment;
import reactor.core.publisher.Mono;

@Getter
@Builder(access = AccessLevel.PRIVATE)
public class MyCloudLoginRequest {
    @JsonProperty("grant_type")
    private String grantType;
    private String realm;
    private String audience;
    private String username;
    private String password;
    private String scope;
    @JsonProperty("client_id")
    private String clientId;
    @JsonProperty("client_secret")
    private String clientSecret;

    public static Mono<MyCloudLoginRequest> constructFromAppProperties(Environment environment) {
        MyCloudLoginRequest myCloudLoginRequest = builder()
                .scope(environment.getProperty("mycloud.auth.scope"))
                .realm(environment.getProperty("mycloud.auth.realm"))
                .audience(environment.getProperty("mycloud.auth.audience"))
                .username(environment.getProperty("mycloud.auth.username"))
                .password(environment.getProperty("mycloud.auth.password"))
                .clientId(environment.getProperty("mycloud.auth.client_id"))
                .grantType(environment.getProperty("mycloud.auth.grant_type"))
                .clientSecret(environment.getProperty("mycloud.auth.client_secret"))
                .build();
        return Mono.just(myCloudLoginRequest);
    }
}
