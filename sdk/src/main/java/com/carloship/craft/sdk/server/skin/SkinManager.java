package com.carloship.craft.sdk.server.skin;

import com.github.benmanes.caffeine.cache.AsyncLoadingCache;
import com.github.benmanes.caffeine.cache.Caffeine;
import net.minestom.server.entity.PlayerSkin;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.PlayerSpawnEvent;
import net.minestom.server.tag.Tag;
import net.minestom.server.timer.TaskSchedule;
import com.carloship.craft.sdk.server.player.BytePlayerCraft;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static com.carloship.craft.sdk.utils.TextUtils.DMM;

public class SkinManager {

    public static final PlayerSkin DEFAULT_NPC_SKIN = new PlayerSkin(
            "ewogICJ0aW1lc3RhbXAiIDogMTcwMTM0NjA3NzMyOSwKICAicHJvZmlsZUlkIiA6ICI1ZWQ4OTJiN2UyZGU0ZjYyYjIyNmFjNjQwZDA0YmJiOSIsCiAgInByb2ZpbGVOYW1lIiA6ICJmcm9zdGVkc3Vuc2hpbmUiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTU4NGMzY2I2ODA4ZGRkYjIxNzkwNTgwMGYyYTQ1Y2FlODdkMDgxZGFjMzUwNTYzNTkyM2IzNjI4MWI3MTU2YyIKICAgIH0KICB9Cn0=",
            "xQQtiZaZIrW2H5s+eGA/bIqCimBs4y+m8xFCRpmI33IT0LBlnTwERB7Ezr+oB6UwoNhlt+/6N4T6cLxFTGPUgFeAZGS4ruejHg+52yEFoXyy7BXVXb5izXzc54Vczv0+dHKSZ49Pys/KZGUIXz3dultkYgJalsbvt6fQbj4pQMK9ipR/onDyhgu0She6tuU6MP0M3KnnBeai7sg/r6m3w6LGjv7S/Enwgkv/nbsY16SnQKDBjLxv2NcG2jywlPlD97wv0rPNR0H9l40mjcibW9k1THJPC+kvXIM4m99Yg2QpBtI9v16OzCoWg7bpTBpNJzGfEAXs0mldYnTvvA8nb8b7cMHzIgQC+9vaYbyL0o7FRwvKuY129W/dWbio7DXCdlIjLhvlhfIWj1UQdRQmnhifjVyR9gW/gioccxqfgUtYh7VG/gRZna4nJI2gSeYs7Sq0yzt/ki3fKOtkBWs3/aVK533/YN9/4PhZWVgLzZFE7a7XCKo7x5lgLZfpU+cKuOTl3FrJ61bqzDJ6CHyCpgwNZSvDO3Lga6dcOtSqftzfzvvm43ieseBBkREORuCS5/ibuZ1SqG4+gJyXD8wHVFFQzQYpLRcBPINcLGlzPBbq12KXehEsUqjJCPfBuZ5PDMoKrcGtobank4nSv/qFrfQHiyG1BoTAlDjK1bfNktg=");

    public static final Tag<Boolean> BYPASS_AUTO_SKIN = Tag.Boolean("bypass_auto_skin").defaultValue(false);

    private static final Logger LOG = LoggerFactory.getLogger(SkinManager.class);

    private static final AsyncLoadingCache<String, Optional<PlayerSkin>> SKIN_CACHE = Caffeine.newBuilder()
            .expireAfterWrite(Duration.ofHours(12))
            .maximumSize(2000)
            .buildAsync((key, executor) -> CompletableFuture.supplyAsync(() -> {
                try {
                    return Optional.ofNullable(PlayerSkin.fromUsername(key));
                } catch (Exception e) {
                    LOG.error("Failed to fetch skin from Mojang API for user: {}", key, e);
                    return Optional.empty();
                }
            }, executor));

    public static final EventNode<Event> EVENT_NODE = EventNode.all("skins-event")
            .addListener(PlayerSpawnEvent.class, event -> {
                if (!(event.getPlayer() instanceof BytePlayerCraft player)) return;

                if (event.isFirstSpawn()) {
                    player.scheduler().scheduleTask(() -> {
                        if (Boolean.TRUE.equals(player.getTag(BYPASS_AUTO_SKIN))) {
                            return TaskSchedule.stop();
                        }

                        setPlayerSkin(player, false);
                        return TaskSchedule.stop();
                    }, TaskSchedule.nextTick());
                }

            });


    public static CompletableFuture<Optional<PlayerSkin>> fetchSkin(String usernameOrId) {
        if (usernameOrId == null || usernameOrId.isBlank()) {
            return CompletableFuture.completedFuture(Optional.empty());
        }

        PlayerSkin cachedSkin = SkinCache.fetchSkin(usernameOrId);

        if (cachedSkin != null) {
            return CompletableFuture.completedFuture(Optional.of(cachedSkin));
        }

        return SKIN_CACHE.get(usernameOrId);
    }

    public static void setFakePlayerSkin(BytePlayerCraft player, String fakeName, Runnable runnable) {
        fetchSkin(fakeName).thenAccept(optionalSkin -> {
            if (optionalSkin.isPresent()) {
                applySkin(player, optionalSkin.get(), false);
            } else {
                setDefaultSkin(player, false);
            }

            runnable.run();
        }).exceptionally(ex -> {
            setDefaultSkin(player, false);
            LOG.error("Error setting skin for player {}", player.getUsername(), ex);
            return null;
        });
    }

    public static void setPlayerSkin(BytePlayerCraft player, boolean sendMessage) {
        if (player.getPlayerData() == null) {
            player.kick(DMM("<red>An error occurred while retrieving your data"));
            return;
        }

        if (player.getPlayerData().isOnlineMode()) {
            String skinId = player.getPlayerData().getSkinId();
            String targetName = (skinId != null && !skinId.contains("skin_cache"))
                    ? skinId
                    : player.getUsername();

            fetchSkin(targetName).thenAccept(optionalSkin -> {
                if (optionalSkin.isPresent()) {
                    applySkin(player, optionalSkin.get(), sendMessage);
                } else {
                    setDefaultSkin(player, sendMessage);
                }
            }).exceptionally(ex -> {
                setDefaultSkin(player, sendMessage);
                LOG.error("Error setting skin for player {}", player.getUsername(), ex);
                return null;
            });

            return;
        }

        setDefaultSkin(player, sendMessage);
    }


    private static void applySkin(BytePlayerCraft player, PlayerSkin skin, boolean sendMessage) {
        player.setSkin(skin);
        SkinCache.saveSkin(player.getUuid(), player.getUsername(), skin.textures(), skin.signature());

        if (sendMessage) {
            player.sendInfoMessage("Skin successfully changed!");
        }
    }

    private static void setDefaultSkin(BytePlayerCraft player, boolean sendMessage) {
        try {
            applySkin(player, DEFAULT_NPC_SKIN, sendMessage);
        } catch (Exception e) {
            player.sendErrorMessage("Oops... Could not change your skin.");
            LOG.error("Error setting player skin", e);
        }
    }
}
