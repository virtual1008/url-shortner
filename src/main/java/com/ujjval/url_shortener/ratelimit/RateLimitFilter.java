package com.ujjval.url_shortener.ratelimit;

import com.ujjval.url_shortener.ratelimit.strategy.RateLimiterStrategy;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "application.ratelimit.enabled", havingValue = "true", matchIfMissing = true)
public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimiterStrategy rateLimiterStrategy;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        // 1. Extract the Real Client IP
        // If the app sits behind a Load Balancer (like AWS ELB) or proxy, getRemoteAddr() will return the Load Balancer's IP.
        // The original user's IP is instead forwarded in the 'X-Forwarded-For' header.
        String clientIp = request.getHeader("X-Forwarded-For");

        if (clientIp != null && !clientIp.isEmpty()) {
            // Note: If the request passes through multiple proxies (e.g., VPN -> CDN -> Load Balancer),
            // the header contains a comma-separated list of IPs. The first IP is always the original client.
            clientIp = clientIp.split(",")[0].trim();
        } else {
            // Fallback for direct connections (e.g., local development testing)
            clientIp = request.getRemoteAddr();
        }

        // 2. Enforce the Rate Limit Policy
        if (!rateLimiterStrategy.allowRequest(clientIp)) {
            log.warn("Rate limit exceeded for IP: {}", clientIp);

            // Short-circuit the request and return a 429 response immediately
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Too many requests. Please slow down.\"}");
            return;
        }

        // 3. Allow valid traffic to proceed to the Controllers
        filterChain.doFilter(request, response);
    }
}