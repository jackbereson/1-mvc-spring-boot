package com.coremvc.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

@Component
public class LoggingInterceptor implements HandlerInterceptor {
    
    private static final Logger logger = LoggerFactory.getLogger(LoggingInterceptor.class);
    private static final String START_TIME = "startTime";
    
    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) 
            throws Exception {
        request.setAttribute(START_TIME, System.currentTimeMillis());
        
        // Log request details
        logger.info("========== API REQUEST START ==========");
        logger.info("Method: {}", request.getMethod());
        logger.info("URL: {}", request.getRequestURI());
        
        // Log query parameters
        if (request.getQueryString() != null) {
            logger.info("Query Parameters: {}", request.getQueryString());
        }
        
        // Log headers
        logger.info("Headers:");
        request.getHeaderNames().asIterator().forEachRemaining(headerName -> {
            logger.info("  {}: {}", headerName, request.getHeader(headerName));
        });
        
        return true;
    }
    
    @Override
    public void postHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler, 
            @Nullable ModelAndView modelAndView) throws Exception {
        // Can be used for additional processing
    }
    
    @Override
    public void afterCompletion(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler, 
            @Nullable Exception ex) throws Exception {
        // Calculate execution time
        long startTime = (long) request.getAttribute(START_TIME);
        long endTime = System.currentTimeMillis();
        long executionTime = endTime - startTime;
        
        // Log response details
        logger.info("========== API RESPONSE ==========");
        logger.info("Status Code: {}", response.getStatus());
        logger.info("Execution Time: {} ms", executionTime);
        
        if (ex != null) {
            logger.error("Exception: {}", ex.getMessage(), ex);
        }
        
        logger.info("========== API REQUEST END ==========\n");
    }
}
