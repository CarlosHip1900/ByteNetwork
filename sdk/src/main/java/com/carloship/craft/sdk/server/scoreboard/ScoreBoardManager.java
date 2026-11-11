package com.carloship.craft.sdk.server.scoreboard;

import com.carloship.craft.sdk.server.player.BytePlayerCraft;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public final class ScoreBoardManager {

    private final Map<BytePlayerCraft, ScoreBoardSession<?>> sessions = new ConcurrentHashMap<>();
    private final List<ScoreBoardAnimation> animations = new CopyOnWriteArrayList<>();

    public void registerAnimation(ScoreBoardAnimation animation) {
        animations.add(animation);
    }

    public <P extends BytePlayerCraft> void show(P player, ScoreBoardTemplate<P> template) {
        hide(player);

        sessions.put(player, new ScoreBoardSession<>(player, template));
    }

    public void hide(BytePlayerCraft player) {
        ScoreBoardSession<?> session = sessions.remove(player);

        if (session != null)
            session.destroy();
    }

    public void updateTitles() {
        animations.forEach(ScoreBoardAnimation::next);
        sessions.values().forEach(ScoreBoardSession::updateTitle);
    }

    public void updateLines() {
        sessions.values().forEach(ScoreBoardSession::updateLines);
    }
}