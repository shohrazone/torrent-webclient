package com.torrent.webclient.mycloud;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Getter
@Component
public class MyCloudConfig {
    private static final ObjectMapper OBJECT_MAPPER;

    static {
        OBJECT_MAPPER = new ObjectMapper();
    }

    private final String myCloudConfigURL;

    @Autowired(required = false)
    private String authURL;
    @Autowired(required = false)
    private String deviceURL;

    public MyCloudConfig(@Value("${mycloud.config.url}") String myCloudConfigURL) {
        this.myCloudConfigURL = myCloudConfigURL;
    }

    private static String extractAuthUrlFromMyCloudConfig(JsonNode myCloudConfig) {
        return myCloudConfig.get("service.auth0.url")
                            .asText();
    }

    private static String extractDeviceUrlFromMyCloudConfig(JsonNode myCloudConfig) {
        return myCloudConfig.get("service.device.url")
                            .asText();
    }

    public Mono<JsonNode> loadMyCloudConfigMapFromConfigJsonString(ObjectMapper objectMapper, String myCloudJsonConfig) {
        JsonNode unmarshaledJsonNode;
        try {
            unmarshaledJsonNode = objectMapper.readTree(myCloudJsonConfig);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Unable to parse wd config to JSON", e);
        }

        return Mono.just(unmarshaledJsonNode)
                   .map(myCloudConfig -> myCloudConfig.get("data")
                                                      .get("componentMap")
                                                      .get("cloud.service.urls"))
                   .doOnNext(myCloudConfig -> this.authURL = extractAuthUrlFromMyCloudConfig(myCloudConfig))
                   .doOnNext(myCloudConfig -> this.deviceURL = extractDeviceUrlFromMyCloudConfig(myCloudConfig));
    }
}

