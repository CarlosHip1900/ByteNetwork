package com.carloship.craft.sdk.server.scoreboard;

import net.kyori.adventure.text.Component;
import net.minestom.server.scoreboard.Sidebar;
import com.carloship.craft.sdk.server.player.BytePlayerCraft;

import java.util.List;

public final class ScoreBoardSession<P extends BytePlayerCraft> {

    private final P player;
    private final Sidebar sidebar;
    private final ScoreBoardTemplate<P> template;

    private final Component[] cache;

    public ScoreBoardSession(P player, ScoreBoardTemplate<P> template) {
        this.player = player;
        this.template = template;
        this.sidebar = new Sidebar(template.animation().current());
        this.cache = new Component[15];

        create();
    }

    private void create() {
        List<Component> lines = template.renderLines(player);

        for (int i = 0; i < lines.size(); i++) {
            if (i >= cache.length) break;

            Component component = lines.get(i);
            cache[i] = component;

            sidebar.createLine(
                    new Sidebar.ScoreboardLine(
                            "line" + i,
                            component,
                            lines.size() - i
                    )
            );
        }

        sidebar.addViewer(player);
    }

    public void destroy() {
        sidebar.removeViewer(player);
    }

    public void updateTitle() {
        sidebar.setTitle(template.animation().current());
    }

    public void updateLines() {
        List<Component> lines = template.renderLines(player);

        for (int i = 0; i < lines.size(); i++) {
            if (i >= cache.length) break;

            Component next = lines.get(i);

            if (next.equals(cache[i]))
                continue;

            cache[i] = next;

            sidebar.updateLineContent(
                    "line" + i,
                    next
            );
        }
    }
}