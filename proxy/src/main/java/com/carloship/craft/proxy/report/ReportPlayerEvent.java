package com.carloship.craft.proxy.report;

import com.velocitypowered.api.proxy.Player;

public record ReportPlayerEvent(Player player, String targetName, String reason) {
}
