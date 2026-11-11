package com.carloship.craft.sdk.server.punish;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;

@Getter
@RequiredArgsConstructor
public enum PunishmentTable {

    BUGS("Bug Abuse", PunishTime.of(15), PunishTime.PERMANENT, null, null),
    THREAT("Threats", PunishTime.of(3), PunishTime.of(5), PunishTime.of(7), PunishTime.of(15)),
    ANTI_GAME("Unfair Gameplay", PunishTime.of(3), PunishTime.of(7), PunishTime.of(15), PunishTime.of(30)),
    HARASSMENT("Harassment", PunishTime.of(90), PunishTime.of(180), PunishTime.PERMANENT, null),
    MARKETING("Advertising", PunishTime.of(true, Duration.ofHours(12)), PunishTime.of(true, Duration.ofDays(1)), PunishTime.of(3), PunishTime.of(5)),
    FAKE_ACCOUNT("Fake Account", PunishTime.PERMANENT, null, null, null),
    FAKE_ACCOUNT_ACT("Fake Account for Illegal Activities", PunishTime.of(15), PunishTime.PERMANENT, null, null),
    SEXUAL_CONTENT("Explicit Sexual Content", PunishTime.of(true, Duration.ofHours(12)), PunishTime.of(true, Duration.ofDays(1)), PunishTime.of(3), PunishTime.of(7)),
    DISINFORMATION("Disinformation", PunishTime.of(true, Duration.ofDays(1)), PunishTime.of(3), PunishTime.of(7), PunishTime.of(15)),
    DISCRIMINATION("Discrimination", PunishTime.of(30), PunishTime.of(60), PunishTime.of(90), PunishTime.PERMANENT),
    GRAVE_SHARE("Serious Sharing", PunishTime.of(7), PunishTime.of(15), PunishTime.of(30), PunishTime.PERMANENT),
    SIMPLE_SHARE("Minor Sharing", PunishTime.of(true, Duration.ofHours(12)), PunishTime.of(1), PunishTime.of(3), PunishTime.of(7)),
    PAYMENT("Payment Chargeback", PunishTime.PERMANENT, null, null, null),
    FAKE_PROVES("Evidence Forgery", PunishTime.PERMANENT, null, null, null),
    FLOOD_SPAM("Flood or Spam", PunishTime.of(true, Duration.ofHours(6)), PunishTime.of(true, Duration.ofHours(12)), PunishTime.of(1), PunishTime.of(3)),
    FAKE_NICKNAME_NOT_PREMIUM("Inappropriate Nickname (Non-Premium Accounts)", PunishTime.PERMANENT, null, null, null),
    FAKE_NICKNAME_PREMIUM("Inappropriate Nickname (Premium Account)", PunishTime.of(30), null, null, null),
    INSULT_STAFF("Insulting Staff or the Server", PunishTime.of(7), PunishTime.of(15), PunishTime.of(30), PunishTime.PERMANENT),
    INSULT_PLAYER("Insulting a Player", PunishTime.of(true, Duration.ofHours(12)), PunishTime.of(1), PunishTime.of(7), PunishTime.of(15)),
    SKIN_INPRO("Inappropriate Skin", PunishTime.of(1), PunishTime.of(7), PunishTime.of(15), PunishTime.PERMANENT),
    TEAM("Teamwork or Alliances", PunishTime.of(1), PunishTime.of(3), PunishTime.of(5), PunishTime.of(7)),
    HACK("Cheating", PunishTime.of(180), PunishTime.PERMANENT, null, null),
    UNKNOWN("Other", null, null, null, null);

    private final String prefix;
    private final PunishTime first;
    private final PunishTime second;
    private final PunishTime third;
    private final PunishTime fourth;

    public String getId() {
        return name().toLowerCase();
    }

    @NotNull
    public static PunishmentTable fromId(String id) {
        for (PunishmentTable value : values()) {
            if (value.getId().equalsIgnoreCase(id)) return value;
        }

        return UNKNOWN;
    }
}