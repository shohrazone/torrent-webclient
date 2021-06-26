package com.torrent.webclient.mycloud;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.torrent.webclient.util.WebFluxUtility;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;

@Slf4j
@Component
@RequiredArgsConstructor
class MyCloudWebApi {

    private final ObjectMapper objectMapper;
    private final MyCloudConfig myCloudConfig;
    private final MyCloudAuthApi myCloudAuthApi;

    @PostConstruct
    private void loadConfigAsJson() {
        final WebClient.RequestBodySpec reqBodySpec = WebClient.builder()
                                                               .baseUrl(myCloudConfig.getMyCloudConfigURL())
                                                               .filter(WebFluxUtility.logRequest()) // here is the magic
                                                               .build()
                                                               .method(HttpMethod.GET)
                                                               .uri("")
                                                               .accept(MediaType.APPLICATION_JSON);

        final WebClient.ResponseSpec responseSpec =
                reqBodySpec.retrieve()
                           .onStatus(HttpStatus::is4xxClientError,
                                     response ->
                                             Mono.error(new NullPointerException("4xx error encountered while making remote request")))
                           .onStatus(HttpStatus::is5xxServerError,
                                     response ->
                                             Mono.error(new NullPointerException("5xx error encountered while making remote request")));

        responseSpec.bodyToMono(String.class)
                    .flatMap(configJsonString ->
                                     myCloudConfig.loadMyCloudConfigMapFromConfigJsonString(objectMapper, configJsonString))
                    .flatMap(config -> myCloudAuthApi.login())
                    .subscribe();
    }
}
