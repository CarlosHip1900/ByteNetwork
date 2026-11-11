package com.carloship.craft.sdk.server;

import com.carloship.craft.sdk.server.player.BytePlayerCraft;
import com.carloship.craft.sdk.utils.TextUtils;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.CommandExecutor;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.ArgumentWord;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.carloship.craft.sdk.utils.TextUtils.DMM;

public abstract class SDKCommandExecutor implements CommandExecutor {

    private final Cache<UUID, Boolean> cooldownCache = Caffeine.newBuilder()
            .expireAfterWrite(3, TimeUnit.SECONDS)
            .maximumSize(10_000)
            .build();

    @Override
    public final void apply(@NotNull CommandSender sender, @NotNull CommandContext context) {
        if (!(sender instanceof BytePlayerCraft player)) {
            sender.sendMessage(TextUtils.COMMAND_ONLY_PLAYERS);
            return;
        }

        UUID uuid = player.getUuid();

        if (cooldownCache.getIfPresent(uuid) != null) {
            player.sendErrorMessage("Please wait before executing this command again.");
            return;
        }

        cooldownCache.put(uuid, Boolean.TRUE);

        apply(player, context);
    }

    public abstract void apply(@NotNull BytePlayerCraft player, @NotNull CommandContext context);

    public static @NonNull ArgumentWord getArgumentWord() {
        ArgumentWord targetName = ArgumentType.Word("player");
        targetName.setSuggestionCallback((sender, context, suggestion) -> {
            String input = suggestion.getInput();

            for (Player onlinePlayer : MinecraftServer.getConnectionManager().getOnlinePlayers()) {
                if (onlinePlayer.getUsername().toLowerCase().startsWith(input.toLowerCase())) {
                    suggestion.addEntry(new SuggestionEntry(onlinePlayer.getUsername()));
                }
            }
        });
        return targetName;
    }
}
