package br.com.cb.security.context;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

public record LoggedUser(
        String userId,
        String email,
        Set<String> roles,
        Map<String, Object> claims
) {

    public boolean hasRole(String role) {
        return roles.contains(role);
    }

    public boolean hasClaim(String key) {
        return claims.containsKey(key);
    }

    public Object getClaim(String key) {
        return claims.get(key);
    }

    public <T> T getClaim(String key, Class<T> type) {
        Object value = claims.get(key);
        return value != null ? type.cast(value) : null;
    }

    public String getClaimAsString(String key) {
        Object value = claims.get(key);
        return value != null ? value.toString() : null;
    }

    public UUID getClaimAsUUID(String key) {
        String value = getClaimAsString(key);
        return value != null ? UUID.fromString(value) : null;
    }

    public Long getClaimAsLong(String key) {
        Object value = claims.get(key);

        if (value == null) {
            return null;
        }

        if (value instanceof Number number) {
            return number.longValue();
        }

        return Long.parseLong(value.toString());
    }

    public Integer getClaimAsInteger(String key) {
        Object value = claims.get(key);

        if (value == null) {
            return null;
        }

        if (value instanceof Number number) {
            return number.intValue();
        }

        return Integer.parseInt(value.toString());
    }

    public Boolean getClaimAsBoolean(String key) {
        Object value = claims.get(key);

        if (value == null) {
            return null;
        }

        if (value instanceof Boolean bool) {
            return bool;
        }

        return Boolean.parseBoolean(value.toString());
    }
}