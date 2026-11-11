package com.carloship.craft.sdk.server.scoreboard;

import net.kyori.adventure.text.Component;
import com.carloship.craft.sdk.server.player.BytePlayerCraft;

import java.util.List;

@FunctionalInterface
public interface ScoreBoardLinesProvider<P extends BytePlayerCraft> {

    List<Component> render(P player);

}