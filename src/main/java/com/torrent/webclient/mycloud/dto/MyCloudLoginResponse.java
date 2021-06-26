package com.torrent.webclient.mycloud.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
public class MyCloudLoginResponse {
    @JsonProperty("id_token")
    private String idToken;
    private String scope;
    @JsonProperty("refresh_token")
    private String refreshToken;
    @JsonProperty("access_token")
    private String accessToken;
    @JsonProperty("token_type")
    private String tokenType;
    @JsonIgnore
    private long expireAtEpoch;

    public long getSecondsRemainingForTokenExpire() {
        final long secondRemaining = expireAtEpoch - Instant.now().getEpochSecond();
        return secondRemaining < 0 ? 0 : secondRemaining;
    }
}
