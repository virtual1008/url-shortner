package com.ujjval.url_shortener.analytics.worker;

import com.ujjval.url_shortener.analytics.config.KafkaTopicConfig;
import com.ujjval.url_shortener.analytics.dto.ClickEventDto;
import com.ujjval.url_shortener.analytics.entity.UrlClick;
import com.ujjval.url_shortener.analytics.repository.UrlClickRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

// Add these two imports!
import ua_parser.Client;
import ua_parser.Parser;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

@Service
@RequiredArgsConstructor
public class AnalyticsBatchWorker {
    private static final Logger log = LoggerFactory.getLogger(AnalyticsBatchWorker.class);

    private final ConcurrentLinkedQueue<ClickEventDto> buffer = new ConcurrentLinkedQueue<>();
    private final UrlClickRepository urlClickRepository;
    private static final int MAX_BATCH_SIZE = 100;

    private final Parser uaParser = new Parser();

    @KafkaListener(topics = KafkaTopicConfig.RAW_CLICKS_TOPIC, groupId = "analytics-worker-group")
    public void consumeClickEvent(ClickEventDto event){
        buffer.add(event);

        if(buffer.size() >= MAX_BATCH_SIZE){
            flushBatch();
        }
    }

    @Scheduled(fixedRate = 5000)
    public void scheduledFlush(){
        if(!buffer.isEmpty()){
            log.info("Time threshold reached. Triggering scheduled analytics flush...");
            flushBatch();
        }
    }

    private synchronized void flushBatch(){
        if(buffer.isEmpty()) return;
        List<ClickEventDto> batchToPersist = new ArrayList<>();
        ClickEventDto currentEvent;

        while((currentEvent=buffer.poll())!=null){
            batchToPersist.add(currentEvent);
        }

        if(!batchToPersist.isEmpty()){
            try{
                log.info(">>> DATABASE BATCH FLUSH: Persisting {} click records to PostgreSQL in a single transaction!", batchToPersist.size());

                List<UrlClick> entities = batchToPersist.stream()
                        .map(dto -> {
                            UrlClick urlClick = new UrlClick(dto.getShortCode(), dto.getIpAddress(), dto.getTimestamp());

                            String rawUserAgent = dto.getUserAgent();
                            if (rawUserAgent != null && !rawUserAgent.equals("UNKNOWN_USER_AGENT")) {
                                Client parsedClient = uaParser.parse(rawUserAgent);

                                urlClick.setOs(parsedClient.os.family);
                                urlClick.setBrowser(parsedClient.userAgent.family);
                                urlClick.setDeviceType(parsedClient.device.family);
                            }
                            return urlClick;
                        })
                        .toList();

                urlClickRepository.saveAll(entities);
                log.info("Successfully flushed batch to database.");
            }catch (Exception e){
                log.error("Failed to persist analytics batch, returning items to queue", e);
                // Fallback: restore items if DB write fails entirely
                buffer.addAll(batchToPersist);
            }
        }
    }
}