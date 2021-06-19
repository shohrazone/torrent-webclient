package com.torrent.webclient.service;

import com.torrent.webclient.entity.TaskStats;
import com.torrent.webclient.repository.TaskStatsRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Sinks;

import javax.annotation.PostConstruct;

@Service
public class TaskEntityService {
    private final Sinks.Many<TaskStats> clientNotifierTaskStatsSink;
    private final TaskStatsRepository taskStatsRepository;

    public TaskEntityService(@Qualifier("taskStatsClientNotifierSink") Sinks.Many<TaskStats> clientNotifierTaskStatsSink,
                             TaskStatsRepository taskStatsRepository) {
        this.clientNotifierTaskStatsSink = clientNotifierTaskStatsSink;
        this.taskStatsRepository = taskStatsRepository;
    }

    @PostConstruct
    public void init() {
        clientNotifierTaskStatsSink.asFlux()
                .flatMap(taskStatsRepository::save)
                .map(clientNotifierTaskStatsSink::tryEmitNext)
                .subscribe();
    }
}
