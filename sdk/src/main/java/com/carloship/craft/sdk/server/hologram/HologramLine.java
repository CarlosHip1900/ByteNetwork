package com.carloship.craft.sdk.server.hologram;

import lombok.Builder;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.minestom.server.entity.Player;

import java.util.function.Consumer;
import java.util.function.Function;

import static com.carloship.craft.sdk.utils.TextUtils.DMM;

@Getter
@Builder
public final class HologramLine {

    private final Function<Player, Component> renderer;

    @Builder.Default
    private final Consumer<Player> leftClick = player -> {};

    @Builder.Default
    private final Consumer<Player> rightClick = player -> {};

    public static HologramLine of(String text) {
        return HologramLine.builder()
                .renderer(player -> DMM(text))
                .build();
    }

    public static HologramLine of(Component component) {
        return HologramLine.builder()
                .renderer(player -> component)
                .build();
    }

    public static HologramLine of(Function<Player, Component> renderer) {
        return HologramLine.builder()
                .renderer(renderer)
                .build();
    }

    public HologramLineEntity createEntity() {
        return new HologramLineEntity(this);
    }
}