package com.example.config.filter;

import com.example.common.util.StringUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class MDCFilter extends OncePerRequestFilter {

    private final String TRACK_ID = "track-id";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            final String trackId = extractTrackId(request);
            MDC.put(TRACK_ID, trackId);

            if (StringUtils.isNotBlank(trackId)) {
                response.addHeader(TRACK_ID, trackId);
            }
            filterChain.doFilter(request, response);

        } finally {
            MDC.clear();
        }
    }

    private String extractTrackId(final HttpServletRequest request) {
        final String trackId;
        if (StringUtils.isNotBlank(request.getHeader(TRACK_ID))) {
            trackId = request.getHeader(TRACK_ID);
        } else {
            trackId = UUID.randomUUID().toString().toUpperCase().replace("-", "");
        }
        return trackId;
    }

}
