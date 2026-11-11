package com.carloship.craft.sdk.server.punish;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Duration;

@AllArgsConstructor
@Getter
public class PunishTime {

    private final boolean mute;
    private final Duration time;
    private final boolean permanent;

    public static PunishTime of(boolean mute, Duration time, boolean permanent){
        return new PunishTime(mute, time, permanent);
    }

    public static PunishTime of(boolean mute, Duration time){
        return new PunishTime(mute, time, false);
    }

    public static PunishTime of(int days){
        return new PunishTime(false, Duration.ofDays(days), false);
    }

    public static PunishTime PERMANENT = PunishTime.of(false, Duration.ofDays(10000), true);

}


