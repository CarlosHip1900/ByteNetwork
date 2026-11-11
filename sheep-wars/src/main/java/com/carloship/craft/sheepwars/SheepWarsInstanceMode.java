package com.carloship.craft.sheepwars;

import com.carloship.craft.sdk.server.instance.ServerInstanceMode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum SheepWarsInstanceMode implements ServerInstanceMode {

    LOBBY(0),
    ROOM_BATTLE(24),
    ROOM_MEGA(16),
    ROOM_GROUP(8);

    private final int teamSize;

    @Override
    public String getId() {
        return name().toLowerCase();
    }
}
