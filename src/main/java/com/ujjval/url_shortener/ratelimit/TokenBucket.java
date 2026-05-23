package com.ujjval.url_shortener.ratelimit;

public class TokenBucket {
    private final long maxBucketSize;
    private final double refillRatePerSecond;

    private double currentTokens;
    private long lastRefillTimestamp;
    public TokenBucket(long maxBucketSize , double refillRatePerSecond){
        this.maxBucketSize = maxBucketSize;
        this.refillRatePerSecond = refillRatePerSecond;
        this.currentTokens = maxBucketSize;
        this.lastRefillTimestamp = System.currentTimeMillis();
    }
    public synchronized  boolean tryConsume(){
        refill();
        if(currentTokens>=1.0){
            currentTokens -= 1.0;
            return true;
        }
        return false;
    }
    private void refill(){
        long now = System.currentTimeMillis();
        long elapsedTimeInMillis = now - lastRefillTimestamp;
        double tokensToAdd = (elapsedTimeInMillis/1000.0)*refillRatePerSecond;
        if(tokensToAdd>0){
            currentTokens = Math.min(maxBucketSize,currentTokens+tokensToAdd);
            lastRefillTimestamp = now;
        }
    }
}
