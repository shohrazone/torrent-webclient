package com.torrent.webclient.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(value = "TaskEntity")
public class TaskEntity {
    @Id
    private String torrentId;
    private String taskName;
    private String magnetUri;
    private String downloadFolder;
    private LocalDateTime downloadTime;
}
