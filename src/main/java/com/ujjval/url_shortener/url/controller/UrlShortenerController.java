package com.ujjval.url_shortener.url.controller;

import com.ujjval.url_shortener.url.dto.UrlRequestDto;
import com.ujjval.url_shortener.url.dto.UrlResponseDto;
import com.ujjval.url_shortener.url.service.UrlService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/url")
@RequiredArgsConstructor
public class UrlShortenerController {

    private final UrlService urlService;

    @PostMapping("/shorten")
    public ResponseEntity<UrlResponseDto> shortenUrl(
            @Valid @RequestBody UrlRequestDto requestDto
    ) {

        return ResponseEntity.ok(
                urlService.shortenUrl(requestDto)
        );
    }

    @GetMapping("/{shortCode}")
    public void redirectToOriginalUrl(
            @PathVariable String shortCode,
            HttpServletResponse response
    ) throws IOException {

        String originalUrl =
                urlService.getOriginalUrl(shortCode);

        response.sendRedirect(originalUrl);
    }

    @DeleteMapping("/{shortCode}")
    public ResponseEntity<String> deleteUrl(@PathVariable String shortCode) {
        urlService.softDelete(shortCode);
        return ResponseEntity.ok("URL deleted successfully (soft delete)");
    }

    public ResponseEntity<String> restore(@PathVariable String shortCode){
       urlService.restoreUrl(shortCode);
       return ResponseEntity.ok("URL restored successfully");
    }
}