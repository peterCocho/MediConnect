package com.sena.backend.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * Custom entry point for handling unauthorized access attempts.
 * <p>
 * This component intercepts authentication failures and returns a standardized
 * JSON error response to the client, ensuring the API consistently communicates
 * 401 Unauthorized status codes instead of default security defaults.
 */
@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    /**
     * Commences the authentication process upon a failure.
     * <p>
     * Sets the response status to 401 and writes a structured error message
     * containing the exception details in JSON format.
     *
     * @param request The HTTP request that triggered the authentication failure.
     * @param response The HTTP response to be sent back to the client.
     * @param authException The exception that caused the authentication failure.
     */
    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException {

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");

        PrintWriter writer = response.getWriter();
        writer.println(
                "{\"error\": \"Authentication failed\", "
                        + "\"message\": \"" + authException.getMessage() + "\", "
                        + "\"status\": 401}"
        );
    }
}