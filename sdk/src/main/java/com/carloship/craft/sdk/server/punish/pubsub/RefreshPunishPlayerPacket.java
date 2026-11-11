package com.carloship.craft.sdk.server.punish.pubsub;

import java.util.UUID;

public record RefreshPunishPlayerPacket(
        UUID userUuid,
        boolean revoke,
        boolean punished
) {
}
