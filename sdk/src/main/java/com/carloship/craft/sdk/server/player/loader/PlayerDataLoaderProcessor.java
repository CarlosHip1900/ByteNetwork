package com.carloship.craft.sdk.server.player.loader;

import com.carloship.craft.sdk.server.player.BytePlayerCraft;
import net.minestom.server.MinecraftServer;
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import net.minestom.server.network.player.PlayerConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.carloship.craft.sdk.utils.TextUtils.DMM;

public class PlayerDataLoaderProcessor {

    public static final Logger LOG = LoggerFactory.getLogger(PlayerDataLoaderProcessor.class);

    private static final long TIMEOUT_MS = 5_000;

    private static final ConcurrentHashMap<PlayerConnection, FirstConfig> firstConfigByConnection = new ConcurrentHashMap<>();

    public static void runAsyncAfterLogin(PlayerConnection connection, String taskName, ConfigEventHandler task) {
        FirstConfig firstConfig = firstConfigByConnection.computeIfAbsent(connection, _ -> new FirstConfig());
        firstConfig.add(taskName, task);
    }

    public void tick() {
        long now = System.currentTimeMillis();
        firstConfigByConnection.values().removeIf(config -> {
            long elapsed = now - config.creation;
            return elapsed >= TIMEOUT_MS;
        });
    }

    public void registerLoadListener() {
        MinecraftServer.getGlobalEventHandler().addListener(AsyncPlayerConfigurationEvent.class, event -> {
            if (!event.isFirstConfig()) return;

            if (!(event.getPlayer() instanceof BytePlayerCraft player)) {
                LOG.error("Error while load player configuration, internal error, Player isn't FactionsPlayer");
                return;
            }

            PlayerConnection connection = player.getPlayerConnection();

            FirstConfig firstConfig = firstConfigByConnection.remove(connection);
            if (firstConfig == null) {
                player.kick(DMM("<red>Erro de leitura interno!"));
                LOG.error("Couldn't find first config data for {}", player);
                return;
            }

            for (Map.Entry<String, ConfigEventHandler> entry : firstConfig.tasks.entrySet()) {
                String name = entry.getKey();
                ConfigEventHandler runnable = entry.getValue();

                try {
                    runnable.handle(player, event);
                } catch (Throwable t) {
                    player.kick(DMM("<red>Erro de leitura interno!"));
                    LOG.error("Error with first-config task: \"{}\"", name, t);
                    return;
                }
            }
        });
    }


    private static class FirstConfig {

        private final Map<String, ConfigEventHandler> tasks = Collections.synchronizedMap(new HashMap<>());
        private final long creation;

        private FirstConfig() {
            this.creation = System.currentTimeMillis();
        }

        public void add(String name, ConfigEventHandler task) {
            tasks.put(name, task);
        }

    }

    public interface ConfigEventHandler {

        void handle(BytePlayerCraft player, AsyncPlayerConfigurationEvent event);

    }

}
