package com.carloship.craft.sdk.server.scoreboard;

import net.kyori.adventure.text.Component;

import java.util.List;

public final class ScoreBoardAnimation {

    private final List<Component> frames;
    private int index;

    public ScoreBoardAnimation(List<Component> frames) {
        this.frames = List.copyOf(frames);
    }

    public Component current() {
        return frames.get(index);
    }

    public void next() {
        index++;
        if (index >= frames.size()) {
            index = 0;
        }
    }
}