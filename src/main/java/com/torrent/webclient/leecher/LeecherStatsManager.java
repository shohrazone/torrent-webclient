package com.torrent.webclient.leecher;

import bt.torrent.TorrentSessionState;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.torrent.webclient.entity.TaskStats;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Schedulers;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Component
public class LeecherStatsManager {
    static final int STATS_EVENT_FREQUENCY_INTERVAL_IN_SEC;
    private static final ObjectMapper OBJECT_MAPPER;
    private static final int NUMBER_OF_THREADS_USED_FOR_PROCESSOR;
    private static final Map<String, AtomicLong> TORRENT_ID_TO_DOWNLOADED_BYTES;

    static {
        OBJECT_MAPPER = new ObjectMapper();
        STATS_EVENT_FREQUENCY_INTERVAL_IN_SEC = 1;
        NUMBER_OF_THREADS_USED_FOR_PROCESSOR = 10;
        TORRENT_ID_TO_DOWNLOADED_BYTES = new ConcurrentHashMap<>();
    }

    private final Sinks.Many<TaskStats> taskStatsSink;
    private final Sinks.Many<TaskStats> taskStatsClientNotifierSink;

    public LeecherStatsManager(@Qualifier("taskStatsReceiverSink") Sinks.Many<TaskStats> taskStatsSink,
                               @Qualifier("taskStatsClientNotifierSink") Sinks.Many<TaskStats> taskStatsClientNotifierSink) {
        this.taskStatsSink = taskStatsSink;
        this.taskStatsClientNotifierSink = taskStatsClientNotifierSink;
    }

    static TaskStats convertTorrentSessionStateToTaskStats(String torrentId, TorrentSessionState torrentSessionState) {
        TORRENT_ID_TO_DOWNLOADED_BYTES.computeIfAbsent(torrentId, e -> new AtomicLong(0L));
        long downloadDelta = torrentSessionState.getDownloaded() - TORRENT_ID_TO_DOWNLOADED_BYTES.get(torrentId).getAndSet(torrentSessionState.getDownloaded());
        float downloadPercent = (torrentSessionState.getPiecesComplete() * 100.0f) / torrentSessionState.getPiecesTotal();

        return TaskStats.builder()
                .torrentId(torrentId)
                .downloadedPercent(downloadPercent)
                .downloadedSizeInBytes(torrentSessionState.getDownloaded())
                .speedInBytesPerSecond(downloadDelta / STATS_EVENT_FREQUENCY_INTERVAL_IN_SEC)
                .totalSizeInBytes((int) (torrentSessionState.getDownloaded() * 100.0f / downloadPercent))
                .numberOfConnectedPeers(torrentSessionState.getConnectedPeers().size())
                .totalPiece(torrentSessionState.getPiecesTotal())
                .build();
    }

    private static TaskStats logStatsEvents(final TaskStats taskStats) {
        try {
            log.info("Currently the this much is downloaded:" + OBJECT_MAPPER.writeValueAsString(taskStats));
        } catch (JsonProcessingException e) {
            log.error("Unable to convert stats to String", e);
        }
        return taskStats;
    }

    @PostConstruct
    public void init() {
        taskStatsSink.asFlux()
                .publishOn(Schedulers.newBoundedElastic(NUMBER_OF_THREADS_USED_FOR_PROCESSOR, 100, "StatsProcessorThread"))
                .map(LeecherStatsManager::logStatsEvents)
                .map(taskStatsClientNotifierSink::tryEmitNext)
                .subscribe();
    }
}
