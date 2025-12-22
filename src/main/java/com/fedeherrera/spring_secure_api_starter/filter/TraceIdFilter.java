package com.fedeherrera.spring_secure_api_starter.filter;
  

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
public class TraceIdFilter extends OncePerRequestFilter {

    private static final String TRACE_ID_KEY = "traceId";
    private static final String TRACE_ID_HEADER = "X-Trace-Id";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        // Generamos un ID corto para que sea fácil de leer en los logs
        String traceId = UUID.randomUUID().toString().split("-")[0];
        
        // Lo guardamos en el contexto de logs (MDC)
        MDC.put(TRACE_ID_KEY, traceId);
        
        // Lo enviamos en los headers de respuesta para que el cliente lo sepa
        response.addHeader(TRACE_ID_HEADER, traceId);

        try {
            filterChain.doFilter(request, response);
        } finally {
            // MUY IMPORTANTE: Limpiar el MDC al finalizar para evitar fugas de memoria
            MDC.remove(TRACE_ID_KEY);
        }
    }
}