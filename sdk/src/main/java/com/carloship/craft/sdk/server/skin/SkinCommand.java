package com.carloship.craft.sdk.server.skin;

import com.carloship.craft.sdk.server.SDKCommandExecutor;
import com.carloship.craft.sdk.server.group.Group;
import com.carloship.craft.sdk.server.group.Groups;
import com.carloship.craft.sdk.server.player.BytePlayerCraft;
import com.carloship.craft.sdk.server.player.BytePlayerModule;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.arguments.ArgumentType;
import org.jetbrains.annotations.NotNull;

import static com.carloship.craft.sdk.utils.TextUtils.NEED_GROUP_ACTION;

public class SkinCommand extends Command {

    public SkinCommand(BytePlayerModule module) {
        super("skin", "");

        Argument<String> skinType = ArgumentType.String("<skin>");

        addSyntax(new SDKCommandExecutor() {
            @Override
            public void apply(@NotNull BytePlayerCraft player, @NotNull CommandContext context) {
                String skinName = context.get(skinType);

                var playerData = player.getPlayerData();

                if (playerData == null) {
                    player.sendErrorMessage("<red>Could not retrieve your player data.");
                    return;
                }

                if (!Group.has(player, Groups.VIP)) {
                    player.sendMessage(NEED_GROUP_ACTION(Groups.VIP));
                    return;
                }

                if (skinName.equalsIgnoreCase("remove") || skinName.equalsIgnoreCase("reset")) {

                    module.modify(player.getPlayerData(), edited -> {
                        edited.setSkinId(null);
                        SkinManager.setPlayerSkin(player, true);
                    });
                    return;
                }

                if (skinName.isBlank()) {
                    player.sendErrorMessage("Please enter a valid name!");
                    return;
                }

                var currentSkinId = player.getPlayerData().getSkinId();

                if (currentSkinId != null && currentSkinId.equalsIgnoreCase(skinName)) {
                    player.sendErrorMessage("You are already using this skin.");
                    return;
                }

                module.modify(player.getPlayerData(), edited -> {
                    edited.setSkinId(skinName);
                    SkinManager.setPlayerSkin(player, true);
                });
            }
        }, skinType);
    }
}