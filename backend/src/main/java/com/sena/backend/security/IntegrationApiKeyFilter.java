package com.sena.backend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class IntegrationApiKeyFilter extends OncePerRequestFilter {

    @Value("${n8n.api.key}")
    private String configuredApiKey;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();

        if (path.startsWith("/api/integrations/")) {
            String requestApiKey = request.getHeader("X-API-Key"); // o "x-api-key", Tomcat suele normalizarlos
            if (configuredApiKey == null || requestApiKey == null || !configuredApiKey.equals(requestApiKey)) {
                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write("Acceso denegado: API Key inválida o faltante.");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}