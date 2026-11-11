package com.carloship.craft.server.sheepwar;

import com.carloship.craft.server.sheepwar.player.SheepWarPlayerModule;

public class SheepWars {

    private static final SheepWarServer INSTANCE = (SheepWarServer) SheepWarServer.INSTANCE;

    public static SheepWarPlayerModule sheepPlayerModule(){
        return INSTANCE.getSheepWarPlayerModule();
    }

}
