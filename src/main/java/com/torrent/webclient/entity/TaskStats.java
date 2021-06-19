package com.torrent.webclient.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(value = "TaskStats")
public class TaskStats {
    @Id
    private String torrentId;
    private Integer totalPiece;
    private long totalSizeInBytes;
    private double downloadedPercent;
    private long downloadedSizeInBytes;
    private long speedInBytesPerSecond;
    private Integer numberOfConnectedPeers;
    @Transient
    private StatEventType statEventType = StatEventType.UNKNOWN;

    private enum StatEventType {UNKNOWN, TASK_CREATED, STATS_UPDATE}
}
