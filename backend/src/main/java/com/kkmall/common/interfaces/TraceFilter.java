package com.kkmall.common.interfaces;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;

@Component
public class TraceFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {
        String traceId = UUID.randomUUID().toString().replace("-", "");
        request.setAttribute("traceId", traceId);
        response.setHeader("X-Trace-Id", traceId);
        filterChain.doFilter(request, response);
    }
}
