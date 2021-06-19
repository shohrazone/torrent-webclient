package com.torrent.webclient.leecher;

import bt.dht.DHTConfig;
import bt.dht.DHTModule;
import bt.magnet.MagnetUri;
import bt.magnet.MagnetUriParser;
import bt.metainfo.TorrentId;
import bt.runtime.BtRuntime;
import bt.runtime.Config;
import com.google.inject.Module;
import com.torrent.webclient.repository.TaskEntityRepository;
import com.torrent.webclient.entity.TaskStats;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Sinks;

import javax.annotation.PreDestroy;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class LeecherFactory {
    private static final Config CONFIG;
    private static final Module DHT_MODULE;
    private static final BtRuntime RUNTIME;
    private static final MagnetUriParser MAGNET_URI_PARSER;
    private static final ConcurrentHashMap<String, Leecher> TORRENT_ID_TO_LEECHER_MAP;

    static {
        MAGNET_URI_PARSER = MagnetUriParser.lenientParser();
        TORRENT_ID_TO_LEECHER_MAP = new ConcurrentHashMap<>();
        CONFIG = new Config() {
            static final int MAX_PEER_CONNECTIONS = 5000;

            @Override
            public int getMaxPeerConnections() {
                return MAX_PEER_CONNECTIONS;
            }

            @Override
            public int getMaxPeerConnectionsPerTorrent() {
                return MAX_PEER_CONNECTIONS;
            }

            @Override
            public int getNumOfHashingThreads() {
                return Runtime.getRuntime().availableProcessors() * 2;
            }
        };

        DHT_MODULE = new DHTModule(new DHTConfig() {
            @Override
            public boolean shouldUseRouterBootstrap() {
                return true;
            }
        });
        RUNTIME = createRuntime();
    }

    @Qualifier("taskStatsReceiverSink")
    private final Sinks.Many<TaskStats> taskStatsReceiverSink;
    private final TaskEntityRepository taskEntityRepository;
    //TODO: CONFIG
    String downloadFolder = "/Users/shohraafaque/Downloads/seeder";


    private static BtRuntime createRuntime() {
        return BtRuntime
                .builder(CONFIG)
                .autoLoadModules()
                .module(DHT_MODULE)
                .disableAutomaticShutdown()
                .build();
    }


    private void addActiveLeecher(TorrentId torrentId, Leecher leecher) {
        TORRENT_ID_TO_LEECHER_MAP.put(torrentId.toString(), leecher);
    }

    private Leecher getActiveLeecherByTorrentId(TorrentId torrentId) {
        return TORRENT_ID_TO_LEECHER_MAP.get(torrentId.toString());
    }

    public String addTorrentByMagnetUri(String magnetUri) {
        MagnetUri parsedMagnetUri = MAGNET_URI_PARSER.parse(magnetUri);
        Leecher leecher = Leecher.buildLeecherFromBtRuntimeAndMagnetURI(RUNTIME,
                magnetUri,
                downloadFolder + "/" + parsedMagnetUri.getTorrentId().toString(),
                taskStatsReceiverSink,
                taskEntityRepository);
        addActiveLeecher(parsedMagnetUri.getTorrentId(), leecher);
        leecher.startDownload();

        return leecher.getTorrentId();
    }

    @PreDestroy
    public void destroy() {
        RUNTIME.shutdown();
    }
}
