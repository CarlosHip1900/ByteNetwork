package com.carloship.craft.sdk.utils;

import com.carloship.craft.sdk.server.group.Group;
import com.carloship.craft.sdk.server.player.BytePlayerCraft;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.color.TeamColor;
import net.minestom.server.network.packet.server.play.TeamsPacket;
import net.minestom.server.scoreboard.Team;
import net.minestom.server.scoreboard.TeamManager;
import org.jspecify.annotations.NonNull;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ServerUtils {

    private static final TeamManager TEAM_MANAGER = MinecraftServer.getTeamManager();
    private static final ConcurrentHashMap<UUID, String> PLAYER_TEAMS = new ConcurrentHashMap<>();

    public static TeamColor convertTeamColor(NamedTextColor color){
        return TeamColor.fromName(color.name());
    }

    public static @NonNull Team generateTeam(@NonNull String order, @NonNull Group group) {
        String teamName = order + "_" + group.getId();

        Team team = TEAM_MANAGER.createTeam(teamName);
        if (group.isDefaultGroup()) {
            team.setTeamColor(TeamColor.GRAY);
        } else {
            team.setTeamColor(ServerUtils.convertTeamColor(group.getColor()));
        }

        if (!group.isDefaultGroup()) {
            team.setPrefix(group.getTag().appendSpace());
        }

        team.setCollisionRule(TeamsPacket.CollisionRule.NEVER);
        team.setNameTagVisibility(TeamsPacket.NameTagVisibility.ALWAYS);
        return team;
    }

    public static void removeTeam(BytePlayerCraft player) {
        UUID uuid = player.getUuid();
        String teamName = PLAYER_TEAMS.remove(uuid);

        if (teamName != null) {
            Team team = TEAM_MANAGER.getTeam(teamName);
            if (team != null) {
                team.removeMember(player.getUsername());
                team.sendUpdatePacket();
            }
        }
    }

    public static void updateTeam(BytePlayerCraft player) {
        UUID uuid = player.getUuid();

        Group activeGroup =  player.getGroup();
        Component activeNametag = player.getNametag();

        Team targetTeam = activeGroup.getTeam();
        String newTeamName = targetTeam != null ? targetTeam.getTeamName() : null;
        String currentTeamName = PLAYER_TEAMS.get(uuid);

        if (newTeamName != null && newTeamName.equals(currentTeamName)) {
            player.setDisplayName(activeNametag);
            return;
        }

        if (currentTeamName != null) {
            removeTeam(player);
        }

        if (targetTeam != null) {
            targetTeam.addMember(player.getUsername());

            PLAYER_TEAMS.put(uuid, targetTeam.getTeamName());
            targetTeam.sendUpdatePacket();
        }

        player.setDisplayName(activeNametag);
    }


}
