package com.carloship.craft.sheepwars;


import com.carloship.craft.sheepwars.player.SheepWarPlayerModule;

public class SheepWarsAPI {

    private static final SheepWarsServer INSTANCE = (SheepWarsServer) SheepWarsServer.INSTANCE;

    public static SheepWarPlayerModule sheepPlayerModule(){
        return INSTANCE.getSheepWarPlayerModule();
    }

}
