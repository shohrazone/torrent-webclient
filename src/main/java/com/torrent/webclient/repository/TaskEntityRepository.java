package com.torrent.webclient.repository;

import com.torrent.webclient.entity.TaskEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface TaskEntityRepository extends ReactiveCrudRepository<TaskEntity, String> {
}
