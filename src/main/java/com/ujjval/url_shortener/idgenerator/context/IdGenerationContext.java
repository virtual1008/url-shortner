package com.ujjval.url_shortener.idgenerator.context;

import com.ujjval.url_shortener.idgenerator.IdGenerationStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class IdGenerationContext {

    private final IdGenerationStrategy idGenerationStrategy;

    public long generateId() {
        return idGenerationStrategy.generateId();
    }
}