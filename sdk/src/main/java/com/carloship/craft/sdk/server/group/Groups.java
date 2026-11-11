package com.carloship.craft.sdk.server.group;

import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.scoreboard.Team;

import java.util.List;

import static com.carloship.craft.sdk.utils.TextUtils.DMM;

@RequiredArgsConstructor
public enum Groups implements Group {

    MASTER(new GroupImpl("master", "01", 1, DMM("<gold>[Master]"), false, true)),
    ADMIN(new GroupImpl("admin", "02", 100, DMM("<red>[Admin]"), false, false)),
    MODERATOR(new GroupImpl("moderator", "03", 150, DMM("<dark_green>[Moderador]"), false, false)),
    HELPER(new GroupImpl("helper", "04", 200, DMM("<yellow>[Ajudante]"), false, false)),
    ULTRA(new GroupImpl("ultra", "05", 250, DMM("<dark_red>[Ultra]"), false, true)),
    MVP_PLUS(new GroupImpl("mvp_plus", "06", 300, DMM("<aqua>[MVP<yellow>+<aqua>]"), false, true)),
    MVP(new GroupImpl("mvp", "07", 350, DMM("<aqua>[MVP]"), false, true)),
    VIP_PLUS(new GroupImpl("vip_plus", "08", 400, DMM("<gold>[VIP<yellow>+<gold>]"), false, true)),
    VIP(new GroupImpl("vip", "09", 450, DMM("<gold>[VIP]"), false, true)),
    MEMBER(new GroupImpl("member", "10", Short.MAX_VALUE, Component.empty(), true, false));

    private final GroupImpl baseGroup;

    @Override
    public String getId() {
        return baseGroup.getId();
    }

    @Override
    public Component getTag() {
        return baseGroup.getTag();
    }

    @Override
    public boolean isDefaultGroup() {
        return baseGroup.isDefaultGroup();
    }

    @Override
    public boolean isPremium() {
        return baseGroup.isPremium();
    }

    @Override
    public Team getTeam() {
        return baseGroup.getTeam();
    }

    @Override
    public Component withName(String name) {
        if (isDefaultGroup()) {
            return Component.text(name).color(NamedTextColor.GRAY);
        }

        return getTag().appendSpace().append(Component.text(name));
    }

    @Override
    public int getWeight() {
        return baseGroup.getWeight();
    }

    public static List<Group> staffs() {
        return List.of(HELPER, MODERATOR, ADMIN, MASTER);
    }

    public static Groups fromId(String id) {
        for (Groups value : values()) {
            if (value.getId().equalsIgnoreCase(id)) return value;
        }

        return null;
    }
}
