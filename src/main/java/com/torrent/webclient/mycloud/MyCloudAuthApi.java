package com.torrent.webclient.mycloud;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.torrent.webclient.mycloud.dto.MyCloudLoginRequest;
import com.torrent.webclient.mycloud.dto.MyCloudLoginResponse;
import com.torrent.webclient.util.StreamUtility;
import com.torrent.webclient.util.WebFluxUtility;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Base64;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Component
@RequiredArgsConstructor
public class MyCloudAuthApi {
    private static final Base64.Decoder base64Decoder;
    private static final AtomicBoolean refreshing;
    private static final ScheduledExecutorService refreshAccessTokenExecutor;

    static {
        base64Decoder = Base64.getDecoder();
        refreshing = new AtomicBoolean(true);
        refreshAccessTokenExecutor = Executors.newScheduledThreadPool(1);
    }

    private final Environment environment;
    private final ObjectMapper objectMapper;
    private final MyCloudConfig myCloudConfig;

    @Setter(AccessLevel.PRIVATE)
    private MyCloudLoginResponse myCloudLoginResponse;

    private void evaluateAndUpdateTokenExpireTime(final MyCloudLoginResponse myCloudLoginResponse) {
        refreshing.set(true);
        final Optional<JsonNode> accessTokenPayload =
                Optional.ofNullable(myCloudLoginResponse)
                        .map(MyCloudLoginResponse::getAccessToken)
                        .map(accessToken -> accessToken.split("\\."))
                        .filter(accessTokenArray -> accessTokenArray.length > 1)
                        .map(accessTokenArray -> accessTokenArray[1])
                        .map(base64Decoder::decode)
                        .map(String::new)
                        .map(jsonString -> StreamUtility.safeObjectMapperReadTree(objectMapper, jsonString));

        final long expireAtEpoch =
                accessTokenPayload.map(accessToken -> accessToken.get("exp"))
                                  .map(JsonNode::asLong)
                                  .orElse(0L);

        assert myCloudLoginResponse != null;
        this.myCloudLoginResponse.setExpireAtEpoch(expireAtEpoch);
    }

    private void scheduleTokenRefresh(final MyCloudLoginResponse myCloudLoginResponse) {
        refreshAccessTokenExecutor.schedule(() -> {
                                                refreshing.set(false);
                                                log.info("Refreshing MyCloud access token.");
                                                this.login().subscribe();
                                            },
                                            myCloudLoginResponse.getSecondsRemainingForTokenExpire(),
                                            TimeUnit.SECONDS);
    }

    Mono<MyCloudLoginResponse> login() {
        final WebClient.RequestBodySpec reqBodySpec = WebClient.builder()
                                                               .baseUrl(myCloudConfig.getAuthURL())
                                                               .filter(WebFluxUtility.logRequest()) // here is the magic
                                                               .build()
                                                               .method(HttpMethod.POST)
                                                               .uri("/oauth/token")
                                                               .accept(MediaType.APPLICATION_JSON);

        reqBodySpec.body(MyCloudLoginRequest.constructFromAppProperties(environment), MyCloudLoginRequest.class);

        final WebClient.ResponseSpec responseSpec =
                reqBodySpec.retrieve()
                           .onStatus(HttpStatus::is4xxClientError,
                                     response ->
                                             Mono.error(new NullPointerException("4xx error encountered while making remote request")))
                           .onStatus(HttpStatus::is5xxServerError,
                                     response ->
                                             Mono.error(new NullPointerException("5xx error encountered while making remote request")));

        return responseSpec.bodyToMono(MyCloudLoginResponse.class)
                           .log()
                           .doOnNext(this::setMyCloudLoginResponse)
                           .doOnNext(this::evaluateAndUpdateTokenExpireTime)
                           .doOnNext(this::scheduleTokenRefresh);
    }
}
