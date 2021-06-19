package com.torrent.webclient.controllers;

import com.torrent.webclient.leecher.LeecherFactory;
import com.torrent.webclient.entity.TaskStats;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

@RestController
@RequiredArgsConstructor
public class HomeController {

    private final LeecherFactory leecherFactory;
    @Qualifier("taskStatsClientNotifierSink")
    private final Sinks.Many<TaskStats> taskStatsClientNotifierSink;

    @GetMapping(path = "/get", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<TaskStats> addMagnetUri() {
        return taskStatsClientNotifierSink.asFlux();
    }

    @PostMapping(path = "/add")
    public Mono<String> addMagnetUri(@RequestBody AddMagnetUriRequest addMagnetUriRequest) {
        return Mono.just(leecherFactory.addTorrentByMagnetUri(addMagnetUriRequest.magnetUri));
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    private static class AddMagnetUriRequest {
        private String magnetUri;
    }
}
