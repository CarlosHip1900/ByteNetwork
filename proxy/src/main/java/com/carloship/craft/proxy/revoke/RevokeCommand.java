package com.carloship.craft.proxy.revoke;

import com.carloship.craft.proxy.Main;
import com.carloship.craft.sdk.server.group.Group;
import com.carloship.craft.sdk.server.group.Groups;
import com.carloship.craft.sdk.server.player.BytePlayerData;
import com.carloship.craft.sdk.server.player.BytePlayerModule;
import com.carloship.craft.sdk.utils.TextUtils;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import static com.carloship.craft.sdk.utils.TextUtils.DMM;

@Slf4j
public class RevokeCommand implements SimpleCommand {

    private final BytePlayerModule playerModule = Main.getInstance().getPlayerModule();

    @Override
    public void execute(Invocation invocation) {
        if (!(invocation.source() instanceof Player source)) return;
        BytePlayerData sourceNetwork = playerModule.fetchEntitySync(source.getUniqueId());
        if (!Group.hasPermission(sourceNetwork.getGroup(), Groups.MODERATOR)) {
            source.sendMessage(TextUtils.noPermission());
            return;
        }

        var args = invocation.arguments();

        if (args.length == 2) {

            String targetName = args[0];
            String reason = args[1];

            if (reason == null || reason.isBlank()) {
                source.sendMessage(DMM("<red>You must provide a reason!"));
                return;
            }

            RevokeHandler.handle(targetName, source, sourceNetwork, reason);
        } else {
            source.sendMessage(DMM("Use: /revogar <player> <reason>"));
        }
    }


    @Override
    public List<String> suggest(Invocation invocation) {
        if (!(invocation.source() instanceof Player source)) return List.of();

        BytePlayerData sourceNetwork = playerModule.fetchEntitySync(source.getUniqueId());
        if (!Group.hasPermission(sourceNetwork.getGroup(), Groups.MODERATOR)) {
            return List.of();
        }

        String[] args = invocation.arguments();

        return switch (args.length) {
            case 0 -> Main.getOnlinePlayerNames();
            default -> List.of();
        };
    }

}
