package com.eesoo.EESOO.shared.presentation.ratelimit;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.eesoo.EESOO.shared.presentation.config.RateLimitProperties;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimitProperties properties;
    private final ObjectMapper objectMapper;
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();
    
     public RateLimitFilter(
            RateLimitProperties properties,
            ObjectMapper objectMapper
    ) {
        this.properties = properties;
        this.objectMapper = objectMapper;
    }
    
    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException{

        String path = request.getRequestURI();
        String ip = request.getRemoteAddr();

        int limit = resolveLimit(path);

        if (limit <= 0) {
            filterChain.doFilter(request, response);
            return;
        }

        String bucketKey = ip + ":" + path;
        Bucket bucket = buckets.computeIfAbsent(bucketKey, k -> newBucket(limit));

          if (bucket.tryConsume(1)) {
            filterChain.doFilter(request, response);
        } else {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write(
                    objectMapper.writeValueAsString(
                            Map.of(
                                    "status", "error",
                                    "message", "Too many requests. Please try again later."
                            )
                    )
            );
        }

    }

    private int resolveLimit(String path) {
        if (path.equals("/api/v1/auth/login")) return properties.getLogin();
        if (path.equals("/api/v1/users/register")) return properties.getRegister();
        if (path.equals("/api/v1/auth/refresh")) return properties.getRefresh();
        if (path.equals("/api/v1/devices/store")) return properties.getDeviceStore();
        if (path.equals("/api/v1/devices/check")) return properties.getDeviceCheck();
        if (path.equals("/api/v1/auth/pin-reset/confirm-mobile")) return properties.getPinResetConfirmMobile();
        if (path.equals("/api/v1/auth/pin-reset/issue")) return properties.getPinResetIssue();
        return 0;
    }


    private Bucket newBucket(int limit) {
        return Bucket.builder()
                .addLimit(Bandwidth.builder()
                        .capacity(limit)
                        .refillGreedy(limit, Duration.ofMinutes(1))
                        .build())
                .build();
    }

}
