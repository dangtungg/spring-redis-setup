package com.example.config.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Filter này đặt ở cuối cùng của filter chains để log request và response
 */
@Slf4j
@Component
@Order(Ordered.LOWEST_PRECEDENCE)
public class BasicRequestLoggingFilter extends OncePerRequestFilter {

    private static final List<String> SENSITIVE_PARAMS = Arrays.asList(
            "password", "token", "secret", "authorization",
            "refresh_token", "access_token", "id_token"
    );

    private static final List<String> IGNORE_PATHS = Arrays.asList(
            "/swagger-ui",
            "/v3/api-docs",
            "/redoc",
            "/actuator",
            "/favicon.ico",
            "/assets/",
            "/css/",
            "/js/",
            "/images/"
    );

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return IGNORE_PATHS.stream().anyMatch(path::contains);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        log.info("REQUEST >>> {}", getFullRequestPath(request));
        filterChain.doFilter(request, response);
        log.info("RESPONSE <<< Status: {}", response.getStatus());
    }

    private static String getFullRequestPath(HttpServletRequest request) {
        String method = request.getMethod();
        String contextPath = request.getContextPath();
        String requestURI = request.getRequestURI();
        String queryString = request.getQueryString();

        StringBuilder sb = new StringBuilder();
        sb.append(method).append(" | ");
        if (!requestURI.contains(contextPath)) {
            sb.append(contextPath).append(requestURI);
        } else {
            sb.append(requestURI);
        }

        if (queryString != null) {
            sb.append("?").append(maskSensitiveData(queryString));
        }

        return sb.toString();
    }

    private static String maskSensitiveData(String queryString) {
        for (String param : SENSITIVE_PARAMS) {
            // Tìm pattern dạng: password=abc hoặc password=abc&other=xyz
            String pattern = param + "=[^&]*";
            queryString = queryString.replaceAll(pattern, param + "=******");
        }
        return queryString;
    }

}
