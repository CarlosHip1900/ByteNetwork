package com.carloship.craft.server.sheepwar;

import com.carloship.craft.sdk.server.instance.ServerInstanceMode;

public enum SheepWarsInstanceMode implements ServerInstanceMode {

    LOBBY, ROOM;

    @Override
    public String getId() {
        return name().toLowerCase();
    }
}
