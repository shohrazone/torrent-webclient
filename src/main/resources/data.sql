CREATE TABLE IF NOT EXISTS TaskEntity
(
    torrentId              VARCHAR(20)                         NOT NULL,
    totalPiece             BIGINT(11)                          NULL,
    downloadedPiece        BIGINT(11)                          NULL,
    downloadBytes          BIGINT(11)                          NULL,
    numberOfConnectedPeers BIGINT(11)                          NULL,
    downloadFolder         VARCHAR(200)                        NOT NULL,
    taskName               VARCHAR(200)                        NULL,
    magnetUri              TEXT                                NOT NULL,
    downloadTime           TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    UNIQUE KEY entityNum (torrentId)
);