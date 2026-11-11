package com.carloship.craft.sdk.utils;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.format.NamedTextColor;

@Getter
@RequiredArgsConstructor
public enum MinecraftColors {

    DEFAULT("gray", "Default", NamedTextColor.GRAY, "§7"),
    RED("red", "Red", NamedTextColor.RED, "§c"),
    BLUE("blue", "Blue", NamedTextColor.BLUE, "§9"),
    GREEN("green", "Green", NamedTextColor.GREEN, "§a"),
    YELLOW("yellow", "Yellow", NamedTextColor.YELLOW, "§e"),
    AQUA("aqua", "Light Blue", NamedTextColor.AQUA, "§b"),
    GOLD("gold", "Gold", NamedTextColor.GOLD, "§6"),
    WHITE("white", "White", NamedTextColor.WHITE, "§f"),
    DARK_RED("dark_red", "Dark Red", NamedTextColor.DARK_RED, "§4"),
    DARK_BLUE("dark_blue", "Dark Blue", NamedTextColor.DARK_BLUE, "§1"),
    DARK_GREEN("dark_green", "Dark Green", NamedTextColor.DARK_GREEN, "§2"),
    LIGHT_PURPLE("light_purple", "Light Purple", NamedTextColor.LIGHT_PURPLE, "§d"),
    DARK_PURPLE("dark_purple", "Dark Purple", NamedTextColor.DARK_PURPLE, "§5"),
    BLACK("black", "Black", NamedTextColor.BLACK, "§0"),
    DARK_GRAY("dark_gray", "Dark Gray", NamedTextColor.DARK_GRAY, "§0");

    private final String identifier;
    private final String displayName;
    private final NamedTextColor color;
    private final String legacyColor;

    public static MinecraftColors fromId(String id) {
        for (MinecraftColors value : values()) {
            if (value.getIdentifier().equalsIgnoreCase(id)) return value;
        }

        return null;
    }

    public String tagColor() {
        return "<" + getIdentifier() + ">";
    }
}