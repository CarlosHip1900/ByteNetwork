package com.carloship.craft.sdk.server.report;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ReportCooldown {
    
    private static final ConcurrentHashMap<UUID, Long> cooldowns = new ConcurrentHashMap<>();
    private static final long COOLDOWN_MILLIS = Duration.ofMinutes(2).toMillis();

    public static boolean isPlayerInCooldown(UUID uuid) {
        long current = System.currentTimeMillis();
        if (cooldowns.containsKey(uuid)) {
            long expires = cooldowns.get(uuid) + COOLDOWN_MILLIS;
            return current < expires;
        }
        return false;
    }

    public static void applyCooldown(UUID uuid) {
        cooldowns.put(uuid, System.currentTimeMillis());
    }

    public static long getRemainingTimeSeconds(UUID uuid) {
        if (!cooldowns.containsKey(uuid)) return 0;
        long remaining = (cooldowns.get(uuid) + COOLDOWN_MILLIS) - System.currentTimeMillis();
        return Math.max(0, remaining / 1000);
    }
}
