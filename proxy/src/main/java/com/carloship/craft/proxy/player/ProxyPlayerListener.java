package com.carloship.craft.proxy.player;

import com.carloship.craft.proxy.Main;
import com.carloship.craft.proxy.network.NetworkServerDirection;
import com.carloship.craft.proxy.player.event.ProxyLeavePlayerEvent;
import com.carloship.craft.proxy.player.event.ProxyLoadedPlayerEvent;
import com.carloship.craft.proxy.punish.PunishMessageListener;
import com.carloship.craft.sdk.SDKEnvironment;
import com.carloship.craft.sdk.server.player.BytePlayerData;
import com.carloship.craft.sdk.server.player.BytePlayerModule;
import com.carloship.craft.sdk.server.punish.PunishModule;
import com.carloship.craft.sdk.server.punish.data.PunishEntry;
import com.carloship.craft.sdk.server.punish.data.PunishType;
import com.carloship.craft.sdk.utils.MojangUserUtils;
import com.velocitypowered.api.event.EventTask;
import com.velocitypowered.api.event.ResultedEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.event.connection.PreLoginEvent;
import com.velocitypowered.api.proxy.Player;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.kyori.adventure.text.Component;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import static com.carloship.craft.sdk.utils.TextUtils.DMM;

@Slf4j
@RequiredArgsConstructor
public class ProxyPlayerListener {

    private final BytePlayerModule playerModule;
    private final PunishModule punishModule;

    //private final PunishModule punishModule;

    private final ConcurrentHashMap<String, UUID> playerCookies = new ConcurrentHashMap<>();

    private static final Component ERROR_ACCOUNT_CHECK = DMM("""
            <red><bold>BYTE NETWORK</bold></red>\\
            <gray>An error occurred while retrieving information about your client.\\
            """);

    private static final Component JOIN_PREM_IN_CRACK_ERROR = DMM("""
            <red><bold>BYTE NETWORK</bold></red>\s
            <gray>You cannot join with this account because another player is already using this account on our server. You are trying to join using a \\
            <gray>non-premium account's nickname with a premium account. \\
            
            <gray>This type of violation is not allowed on Byte Network.
            """);

    private static final Component JOIN_CRACK_IN_PREM_ERROR = DMM("""
            <red><bold>BYTE NETWORK</bold></red>\s
            <gray>You cannot join with this account because another player is already using this account on our server. You are trying to join using a \\
            <gray>premium account's nickname with a non-premium account. \\
            
            <gray>This type of violation is not allowed on Byte Network.
            """);

    @Subscribe(priority = Short.MAX_VALUE)
    public EventTask onPreLogin(PreLoginEvent event) {
        UUID uuid = event.getUniqueId();
        String username = event.getUsername();

        CompletableFuture<Boolean> firstFuture = MojangUserUtils.requestPlayerUuid(username).thenCompose(resultUuid -> {
            if (resultUuid == null) {
                //Por mais que nao esteja sendo usado, mas vamos deixar por aqui
/*                event.setResult(PreLoginEvent.PreLoginComponentResult.denied(DMM("""
                        <red><bold>BYTE NETWORK</bold></red>\s
                        <gray>Ocorreu um erro ao buscar as informações sobre o seu client\
                        """))
                );*/
                return CompletableFuture.completedFuture(true);
            }

            if (!resultUuid.equals(uuid)) {
                event.setResult(PreLoginEvent.PreLoginComponentResult.denied(DMM("""
                        <red><bold>BYTE NETWORK</bold></red>\s
                        <gray>You cannot bypass account restrictions on Byte Network. Please check your account and the client you are using.\s
                        
                        <gray>This type of violation is not allowed on Byte Network.
                        """))
                );
                return CompletableFuture.completedFuture(false);
            }

            playerCookies.put(username, uuid);
            return CompletableFuture.completedFuture(true);
        });

        return EventTask.withContinuation(continuation -> {
            firstFuture.thenAccept(canPass -> {
                if (!canPass) return;
            }).whenComplete((res, err) -> continuation.resume());
        });
    }

    @Subscribe
    public EventTask onJoin(LoginEvent event) {
        UUID fakeUuid = event.getPlayer().getUniqueId();
        UUID MOJANG_UUID = playerCookies.getOrDefault(event.getPlayer().getUsername(), fakeUuid);

        String username = event.getPlayer().getUsername();

        CompletableFuture<BytePlayerData> networkFuture = playerModule.fetchEntityAsync(fakeUuid);

        long NOW = System.currentTimeMillis();

        //Tornar isso mais rapido, pegando provalmente direto do redis, um callback do account do player in 30m
        return EventTask.withContinuation(continuation -> {

            MojangUserUtils.isPremiumAccount(MOJANG_UUID)

                    .thenCompose(isPremium ->

                            networkFuture.thenApply(account -> {

                                // PREMIUM ACCOUNT
                                if (isPremium) {

                                    if (account != null && !account.isOnlineMode()) {
                                        event.setResult(ResultedEvent.ComponentResult.denied(JOIN_PREM_IN_CRACK_ERROR));
                                        return null;
                                    }

                                    if (account == null) {
                                        account = BytePlayerData.create(fakeUuid, username, true);
                                        playerModule.cacheEntity(account);
                                    }

                                    playerModule.modify(account, acc -> {
                                        acc.setLastLogin(NOW);
                                        acc.setOnlineMode(true);
                                    });

                                    NetworkServerDirection.directPlayer(fakeUuid, SDKEnvironment.PROXY_JOIN_REDIRECT_LOBBY.getString("master_lobby"));

                                    return account;
                                }

                                // CRACKED ACCOUNT
                                if (account != null && account.isOnlineMode()) {
                                    event.setResult(ResultedEvent.ComponentResult.denied(JOIN_CRACK_IN_PREM_ERROR));

                                    return null;
                                }

                                if (account == null) {

                                    account = BytePlayerData.create(fakeUuid, username, false);
                                    playerModule.cacheEntity(account);
                                }

                                BytePlayerData finalAccount = account;

                                playerModule.modify(finalAccount, acc -> {
                                    acc.setLastLogin(NOW);
                                    acc.setOnlineMode(false);
                                });

                                NetworkServerDirection.directPlayer(fakeUuid, "limbo");

                                return finalAccount;
                            })

                    )

                    .thenCompose(v -> loadPlayerPunish(fakeUuid))
                    .thenCompose(punish -> {

                        if (punish != null && punish.getType() == PunishType.BAN) {

                            event.setResult(ResultedEvent.ComponentResult.denied(PunishMessageListener.createBanMessage(punish)));

                            return CompletableFuture.completedFuture(null);
                        }

                        return networkFuture.thenApply(network -> {

                            if (network == null) {
                                return null;
                            }

                            playerModule.modify(network, player -> {

                                boolean firstLogin = player.getFirstJoin() == 0 || player.getLastLogin() == 0;

                                if (firstLogin) {
                                    player.setFirstJoin(System.currentTimeMillis());
                                }

                                player.setLastLogin(System.currentTimeMillis());
                            });

                            playerCookies.remove(network.getUsername());
                            Main.fireEvent(new ProxyLoadedPlayerEvent(event.getPlayer(), network));

                            return network;
                        });
                    })

                    .whenComplete((res, err) -> {

                        if (err != null) {
                            log.error("Login pipeline error {}", fakeUuid, err);
                            event.setResult(ResultedEvent.ComponentResult.denied(ERROR_ACCOUNT_CHECK));

                            continuation.resumeWithException(err);
                            return;
                        }

                        continuation.resume();
                    });
        });
    }

    @Subscribe
    public EventTask onLeave(DisconnectEvent event) {
        Player player = event.getPlayer();
        NetworkServerDirection.undirectPlayer(player.getUniqueId());

        return EventTask.withContinuation(continuation -> {
            BytePlayerData playerNetwork = playerModule.fetchEntitySync(event.getPlayer().getUniqueId());
            playerModule.modifyAsync(playerNetwork, pN -> {
                pN.setLastLogout(System.currentTimeMillis());
            });

            Main.fireEvent(new ProxyLeavePlayerEvent(player.getUniqueId(), playerNetwork));
        });

    }

    private CompletableFuture<PunishEntry> loadPlayerPunish(UUID uuid) {
        return punishModule.getLastPlayerPunish(uuid).thenApply(punish -> {
            if (punish == null) return null;
            if (punish.isPassed()) return null;
            if (!punish.isActive()) return null;

            log.info("Last punishment {}", punish);
            return punish;
        });
    }
}
