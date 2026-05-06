package com.kkmall.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;

@Service
public class JwtService {
    private final String secret;
    private final long ttlSeconds;

    public JwtService(@Value("${kkmall.auth.secret}") String secret,
                      @Value("${kkmall.auth.token-ttl-seconds}") long ttlSeconds) {
        this.secret = secret;
        this.ttlSeconds = ttlSeconds;
    }

    public String issue(Long userId, String phone, String role) {
        long exp = Instant.now().getEpochSecond() + ttlSeconds;
        String payload = userId + ":" + phone + ":" + role + ":" + exp;
        return encode(payload) + "." + sign(payload);
    }

    public AuthUser parse(String token) {
        String[] parts = token.split("\\.");
        if (parts.length != 2) throw new IllegalArgumentException("AUTH_REQUIRED");
        String payload = new String(Base64.getUrlDecoder().decode(parts[0]), StandardCharsets.UTF_8);
        if (!sign(payload).equals(parts[1])) throw new IllegalArgumentException("AUTH_REQUIRED");
        String[] fields = payload.split(":");
        if (fields.length != 4 || Long.parseLong(fields[3]) < Instant.now().getEpochSecond()) {
            throw new IllegalArgumentException("AUTH_REQUIRED");
        }
        return new AuthUser(Long.parseLong(fields[0]), fields[1], fields[2]);
    }

    private String encode(String value) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    private String sign(String payload) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(mac.doFinal(payload.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }
}
