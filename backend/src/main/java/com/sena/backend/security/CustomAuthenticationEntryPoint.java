package com.sena.backend.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.PrintWriter;

@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException {
        // Configura el código de estado HTTP 401 (Unauthorized) en lugar de 403
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        // Establece el tipo de contenido para JSON
        response.setContentType("application/json");

        // Envía un mensaje personalizado al cliente
        PrintWriter writer = response.getWriter();
        writer.println(
                "{\"error\": \"Authentication failed\", "
                        + "\"message\": \"" + authException.getMessage() + "\", "
                        + "\"status\": 401}"
        );
    }
}