package com.sena.backend.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
/**
 * Utility component for JSON Web Token (JWT) management.
 * <p>
 * This service facilitates the generation and validation of tokens using
 * the HMAC-SHA256 algorithm. It provides the necessary abstraction for
 * securing stateless communication within the application's authentication flow.
 */
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration_minutes:60}")
    private long expMinutes;

    private final ObjectMapper om = new ObjectMapper();

    public String generateToken(String username) throws Exception {
        long exp = Instant.now().plusSeconds(expMinutes * 60).getEpochSecond();
        Map<String, Object> header = new HashMap<>(); header.put("alg","HS256"); header.put("typ","JWT");
        Map<String, Object> payload = new HashMap<>(); payload.put("sub", username); payload.put("exp", exp);
        String headerJson = om.writeValueAsString(header);
        String payloadJson = om.writeValueAsString(payload);
        String headerB64 = base64UrlEncode(headerJson.getBytes(StandardCharsets.UTF_8));
        String payloadB64 = base64UrlEncode(payloadJson.getBytes(StandardCharsets.UTF_8));
        String signingInput = headerB64 + "." + payloadB64;
        String sig = base64UrlEncode(hmacSha256(signingInput.getBytes(StandardCharsets.UTF_8), secret.getBytes(StandardCharsets.UTF_8)));
        return signingInput + "." + sig;
    }

    public boolean validateToken(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) return false;
            String headerB64 = parts[0];
            String payloadB64 = parts[1];
            String sigB64 = parts[2];
            String signingInput = headerB64 + "." + payloadB64;
            String expectedSig = base64UrlEncode(hmacSha256(signingInput.getBytes(StandardCharsets.UTF_8), secret.getBytes(StandardCharsets.UTF_8)));
            if (!constantTimeEquals(expectedSig, sigB64)) return false;
            // parse payload
            String payloadJson = new String(base64UrlDecode(payloadB64), StandardCharsets.UTF_8);
            Map<String, Object> payload = om.readValue(payloadJson, Map.class);
            Number exp = (Number) payload.get("exp");
            if (exp == null) return false;
            long now = Instant.now().getEpochSecond();
            return now <= exp.longValue();
        } catch (Exception e) {
            return false;
        }
    }

    public String getUsernameFromToken(String token) throws Exception {
        String[] parts = token.split("\\.");
        String payloadJson = new String(base64UrlDecode(parts[1]), StandardCharsets.UTF_8);
        Map<String, Object> payload = om.readValue(payloadJson, Map.class);
        return (String) payload.get("sub");
    }

    private static byte[] hmacSha256(byte[] data, byte[] key) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(key, "HmacSHA256"));
        return mac.doFinal(data);
    }

    private static String base64UrlEncode(byte[] data){
        return Base64.getUrlEncoder().withoutPadding().encodeToString(data);
    }
    private static byte[] base64UrlDecode(String s){
        return Base64.getUrlDecoder().decode(s);
    }

    private static boolean constantTimeEquals(String a, String b){
        if (a.length() != b.length()) return false;
        byte[] aa = a.getBytes(StandardCharsets.UTF_8);
        byte[] bb = b.getBytes(StandardCharsets.UTF_8);
        int res = 0;
        for (int i=0;i<aa.length;i++) res |= aa[i] ^ bb[i];
        return res == 0;
    }
}
