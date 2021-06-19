package com.torrent.webclient.leecher;


import bt.Bt;
import bt.data.Storage;
import bt.data.file.FileSystemStorage;
import bt.magnet.MagnetUriParser;
import bt.metainfo.Torrent;
import bt.runtime.BtClient;
import bt.runtime.BtRuntime;
import bt.torrent.selector.RarestFirstSelector;
import com.torrent.webclient.entity.TaskEntity;
import com.torrent.webclient.repository.TaskEntityRepository;
import com.torrent.webclient.entity.TaskStats;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.io.File;
import java.nio.file.Paths;
import java.time.LocalDateTime;

@Slf4j
class Leecher {
    private static final MagnetUriParser MAGNET_URI_PARSER = MagnetUriParser.lenientParser();

    @Getter
    private final String torrentId;
    private final BtClient btClient;
    private final Sinks.Many<TaskStats> taskStatsReceiverSink;

    private Leecher(BtClient btClient, String torrentId, Sinks.Many<TaskStats> taskStatsReceiverSink) {
        this.btClient = btClient;
        this.torrentId = torrentId;
        this.taskStatsReceiverSink = taskStatsReceiverSink;
    }

    static Leecher buildLeecherFromBtRuntimeAndMagnetURI(final BtRuntime btRuntime,
                                                         final String magnetURI,
                                                         final String downloadFolder,
                                                         final Sinks.Many<TaskStats> taskStatsReceiverSink,
                                                         final TaskEntityRepository taskEntityRepository) {
        String torrentId = MAGNET_URI_PARSER.parse(magnetURI).getTorrentId().toString();
        TaskEntity taskEntity = TaskEntity.builder()
                .downloadTime(LocalDateTime.now())
                .magnetUri(magnetURI)
                .torrentId(torrentId)
                .downloadFolder(downloadFolder)
                .downloadTime(LocalDateTime.now())
                .build();

        boolean mkdir = new File(downloadFolder).mkdir();
        taskEntityRepository.findById(torrentId)
                .switchIfEmpty(Mono.defer(() -> taskEntityRepository.save(taskEntity)))
                .subscribe();

        Storage storage = new FileSystemStorage(Paths.get(downloadFolder));
        BtClient btClient = Bt.client(btRuntime)
                .storage(storage)
                .magnet(magnetURI)
                .stopWhenDownloaded()
                .selector(RarestFirstSelector.randomizedRarest())
                .afterTorrentFetched(torrent -> updateTorrentMetaData(torrent, taskEntityRepository))
                .build();

        return new Leecher(btClient, torrentId, taskStatsReceiverSink);
    }

    private static void updateTorrentMetaData(Torrent torrent, TaskEntityRepository taskEntityRepository) {
        taskEntityRepository.findById(torrent.getTorrentId().toString())
                .map(taskEntity -> {
                    taskEntity.setTaskName(torrent.getName());
                    return taskEntity;
                })
                .flatMap(taskEntityRepository::save)
                .subscribe();
    }

    void startDownload() {
        this.btClient.startAsync(state -> {
            if (state.getPiecesRemaining() == 0) {
                log.info("TODO: Leecher destroy");
                btClient.stop();
            }

            taskStatsReceiverSink.tryEmitNext(LeecherStatsManager.convertTorrentSessionStateToTaskStats(torrentId, state));
        }, LeecherStatsManager.STATS_EVENT_FREQUENCY_INTERVAL_IN_SEC * 1000);
    }
}