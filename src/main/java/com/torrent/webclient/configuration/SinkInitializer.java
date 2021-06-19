package com.torrent.webclient.configuration;

import com.torrent.webclient.entity.TaskStats;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Sinks;

@Configuration
public class SinkInitializer {
    @Bean
    public Sinks.Many<TaskStats> taskStatsReceiverSink() {
        return Sinks.many().multicast().directBestEffort();
    }

    @Bean
    public Sinks.Many<TaskStats> taskStatsClientNotifierSink() {
        return Sinks.many().multicast().directBestEffort();
    }
}
