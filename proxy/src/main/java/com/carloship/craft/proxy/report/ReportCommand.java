package com.carloship.craft.proxy.report;

import com.carloship.craft.proxy.Main;
import com.carloship.craft.sdk.server.punish.PunishmentTable;
import com.carloship.craft.sdk.utils.TextUtils;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.List;

import static com.carloship.craft.sdk.utils.TextUtils.DMM;

@Slf4j
public class ReportCommand implements SimpleCommand {

    @Override
    public void execute(Invocation invocation) {
        if (!(invocation.source() instanceof Player player)) {
            invocation.source().sendMessage(TextUtils.COMMAND_ONLY_PLAYERS);
            return;
        }

        String[] args = invocation.arguments();

        if (args.length < 2) {
            player.sendMessage(DMM("<red>Use: /report <player> <reason>"));
            return;
        }

        String targetName = args[0];
        String reason = String.join(" ", Arrays.copyOfRange(args, 1, args.length));

        Main.fireEvent(new ReportPlayerEvent(player, targetName, reason));
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        return switch (invocation.arguments().length) {
            case 1 -> Main.getOnlinePlayerNames();
            case 2 -> Arrays.stream(PunishmentTable.values()).map(PunishmentTable::getId).toList();
            default -> List.of();
        };
    }
}
