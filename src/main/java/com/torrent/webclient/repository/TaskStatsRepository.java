package com.torrent.webclient.repository;

import com.torrent.webclient.entity.TaskStats;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface TaskStatsRepository extends ReactiveCrudRepository<TaskStats, String> {
}
