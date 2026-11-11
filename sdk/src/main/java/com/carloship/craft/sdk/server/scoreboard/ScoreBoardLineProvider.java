package com.carloship.craft.sdk.server.scoreboard;

import net.kyori.adventure.text.Component;
import com.carloship.craft.sdk.server.player.BytePlayerCraft;

@FunctionalInterface
public interface ScoreBoardLineProvider {

    Component render(BytePlayerCraft player);

}