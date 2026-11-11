package com.carloship.craft.proxy.player;

import com.carloship.craft.proxy.Main;
import com.carloship.craft.proxy.player.event.ProxyLoadedPlayerEvent;
import com.carloship.craft.sdk.server.player.BytePlayerData;
import com.velocitypowered.api.event.EventTask;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;

import java.time.Duration;

import static com.carloship.craft.sdk.utils.TextUtils.DMM;

public class PlayerEmailService {

    private final ProxyServer proxy = Main.proxy();

    @Subscribe
    public EventTask onEntrance(ProxyLoadedPlayerEvent event) {
        var proxyPlayer = event.proxiedPlayer();
        var networkPlayer = event.networkPlayer();

        return EventTask.async(() -> {
            BytePlayerData account = Main.getInstance().getPlayerModule().fetchEntitySync(proxyPlayer.getUniqueId());
            if (account.getEmail() == null) {
                Main.proxy().getScheduler().buildTask(Main.getInstance(), () -> {
                            Sound plingSound = Sound.sound()
                                    .type(Key.key("block.note_block.pling"))
                                    .source(Sound.Source.MASTER)
                                    .volume(1.0f)
                                    .pitch(1.0f)
                                    .build();

                            proxyPlayer.playSound(plingSound);
                            proxyPlayer.sendMessage(DMM("<gray>We noticed that you haven't linked an <yellow>Email <gray>to your account yet. You can do so now using /linkemail"));
                        })
                        .delay(Duration.ofSeconds(10))
                        .schedule();
            }
        });
    }
}
