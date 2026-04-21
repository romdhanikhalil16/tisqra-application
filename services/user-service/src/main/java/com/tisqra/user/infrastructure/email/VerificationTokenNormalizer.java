package com.tisqra.user.infrastructure.email;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

/**
 * Normalizes verification tokens from Postman / email clients.
 * Users sometimes paste the full URL instead of only the token value.
 */
public final class VerificationTokenNormalizer {

    private VerificationTokenNormalizer() {
    }

    public static String normalize(String raw) {
        if (raw == null) {
            return "";
        }
        String t = raw.trim();
        if (t.startsWith("\"") && t.endsWith("\"") && t.length() >= 2) {
            t = t.substring(1, t.length() - 1).trim();
        }
        int idx = t.indexOf("token=");
        if (idx >= 0) {
            t = t.substring(idx + "token=".length());
            int amp = t.indexOf('&');
            if (amp >= 0) {
                t = t.substring(0, amp);
            }
            int hash = t.indexOf('#');
            if (hash >= 0) {
                t = t.substring(0, hash);
            }
        }
        try {
            return URLDecoder.decode(t, StandardCharsets.UTF_8).replace(" ", "").trim();
        } catch (Exception e) {
            return t.replace(" ", "").trim();
        }
    }
}
