package com.carloship.craft.sdk.server.report;

import java.util.UUID;

public record ReportNotificationPacket(
        UUID reportId,
        String targetName,
        String reporterName,
        String reason,
        String serverId
) {}
