package com.carloship.craft.proxy.commands.infra;

import com.carloship.craft.proxy.Main;
import com.carloship.craft.sdk.server.group.Group;
import com.carloship.craft.sdk.server.group.Groups;
import com.carloship.craft.sdk.server.player.BytePlayerData;
import com.carloship.craft.sdk.server.player.BytePlayerModule;
import com.carloship.craft.sdk.utils.TextUtils;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.Component;

import java.util.Arrays;
import java.util.List;

import static com.carloship.craft.sdk.utils.TextUtils.DMM;

public class MotdCommand implements SimpleCommand {

    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();
        BytePlayerModule playerModule = Main.getInstance().getPlayerModule();

        if (!(source instanceof Player player)) {
            source.sendMessage(TextUtils.COMMAND_ONLY_PLAYERS);
            return;
        }

        BytePlayerData playerNetwork = playerModule.fetchEntitySync(player.getUniqueId());
        if (!Group.hasPermission(playerNetwork.getGroup(), Groups.ADMIN)) {
            player.sendMessage(TextUtils.noPermission());
            return;
        }

        String[] args = invocation.arguments();

        if (args.length == 1){
            if (args[0].equalsIgnoreCase("preview")){
                player.sendMessage(Component.empty());
                player.sendMessage(Main.getInstance().getSettings().motd());
                player.sendMessage(Component.empty());
            }

            if (args[0].equalsIgnoreCase("reset")){
                player.sendMessage(DMM("<green>You've reset the motd!"));
                Main.getInstance().getSettings().resetMotd();
            }

            if (args[0].equalsIgnoreCase("setfirst")){
                player.sendMessage(DMM("<red>Use: /motd setfirst [text]"));
            }

            if (args[0].equalsIgnoreCase("setsecond")){
                player.sendMessage(DMM("<red>Use: /motd second [text]"));
            }
        }

        if (args.length > 1){
            String message = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
            if (args[0].equalsIgnoreCase("setfirst")){
                Main.getInstance().getSettings().setFirstLine(DMM(message));
                player.sendMessage(DMM("<green>You've set the first line of motd to:"));
                player.sendMessage(DMM(message));
            }

            if (args[0].equalsIgnoreCase("setsecond")){
                Main.getInstance().getSettings().setSecondLine(DMM(message));
                player.sendMessage(DMM("<green>You've set the second line of motd to:"));
                player.sendMessage(DMM(message));
            }
        }
    }


    @Override
    public List<String> suggest(Invocation invocation) {
        CommandSource source = invocation.source();
        BytePlayerModule playerModule = Main.getInstance().getPlayerModule();

        if (!(source instanceof Player player)) return List.of();

        BytePlayerData playerNetwork = playerModule.fetchEntitySync(player.getUniqueId());
        if (!Group.hasPermission(playerNetwork.getGroup(), Groups.ADMIN)) return List.of();

        if (invocation.arguments().length == 0){
            return List.of("preview","reset","setfirst", "setsecond");
        }

        return List.of();
    }
}
