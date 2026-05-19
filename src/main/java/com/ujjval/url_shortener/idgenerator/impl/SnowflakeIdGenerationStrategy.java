package com.ujjval.url_shortener.idgenerator.impl;
import com.ujjval.url_shortener.idgenerator.IdGenerationStrategy;


public class SnowflakeIdGenerationStrategy implements IdGenerationStrategy {
    private static final long CUSTOM_EPOCH = 1775001600000L;
    private static final long NODE_ID_BITS = 10L;
    private static final long SEQUENCE_BITS = 12L;
    private static final long MAX_SEQUENCE = (1L << SEQUENCE_BITS) - 1;
    private static final long NODE_ID_SHIFT = SEQUENCE_BITS;
    private static final long TIMESTAMP_SHIFT = NODE_ID_BITS + SEQUENCE_BITS;
    private final long nodeId;
    private long sequence = 0L;
    private long lastTimestamp = -1L;
    public SnowflakeIdGenerationStrategy(long nodeId) {
        validateNodeId(nodeId);
        this.nodeId = nodeId;
    }
    @Override
    public synchronized long generateId() {
        long currentTimestamp = System.currentTimeMillis() - CUSTOM_EPOCH;
        if (currentTimestamp < lastTimestamp) {
            throw new RuntimeException("Clock moved backwards");
        }
        if (currentTimestamp == lastTimestamp) {
            sequence = (sequence + 1) & MAX_SEQUENCE;
            if (sequence == 0) {
                currentTimestamp = waitForNextMillis(currentTimestamp);
            }
        } else {
            sequence = 0L;
        }
        lastTimestamp = currentTimestamp;
        return (currentTimestamp << TIMESTAMP_SHIFT) | (nodeId << NODE_ID_SHIFT) | sequence;
    }
    private long waitForNextMillis(long currentTimestamp) {
        while (currentTimestamp <= lastTimestamp) {
            currentTimestamp = System.currentTimeMillis() - CUSTOM_EPOCH;
        }
        return currentTimestamp;
    }

    private void validateNodeId(long nodeId) {
        long maxNodeId = (1L << NODE_ID_BITS) - 1;
        if (nodeId < 0 || nodeId > maxNodeId) {
            throw new IllegalArgumentException("Node ID must be between 0 and " + maxNodeId);
        }
    }
}