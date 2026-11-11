package com.carloship.craft.sdk.server.scoreboard;

import net.kyori.adventure.text.Component;
import com.carloship.craft.sdk.server.player.BytePlayerCraft;

import java.util.List;

public final class ScoreBoardTemplate<P extends BytePlayerCraft> {

    private final ScoreBoardAnimation animation;
    private final ScoreBoardLinesProvider<P> linesProvider;

    public ScoreBoardTemplate(ScoreBoardAnimation animation, ScoreBoardLinesProvider<P> linesProvider) {
        this.animation = animation;
        this.linesProvider = linesProvider;
    }

    public ScoreBoardTemplate(ScoreBoardAnimation animation, List<ScoreBoardLineProvider> lines) {
        this.animation = animation;
        this.linesProvider = player -> lines.stream()
                .map(provider -> provider.render(player))
                .toList();
    }

    public ScoreBoardAnimation animation() {
        return animation;
    }

    public List<Component> renderLines(P player) {
        return linesProvider.render(player);
    }
}