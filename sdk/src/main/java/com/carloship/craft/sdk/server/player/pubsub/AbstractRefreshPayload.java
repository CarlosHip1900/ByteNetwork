package com.carloship.craft.sdk.server.player.pubsub;

import java.util.UUID;

public record AbstractRefreshPayload(
        UUID uuid,
        String action,
        String target
) {
}