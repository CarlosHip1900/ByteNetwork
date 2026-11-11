package com.carloship.craft.sdk.server.instance;


import com.carloship.craft.sdk.SDKServer;
import com.carloship.craft.sdk.server.group.Groups;
import lombok.Data;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.player.PlayerChatEvent;
import net.minestom.server.event.player.PlayerMoveEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.anvil.AnvilLoader;
import net.minestom.server.item.ItemStack;
import net.minestom.server.timer.Scheduler;
import net.minestom.server.timer.Task;
import net.minestom.server.timer.TaskSchedule;
import com.carloship.craft.sdk.server.hologram.Hologram;
import com.carloship.craft.sdk.server.hologram.HologramManager;
import com.carloship.craft.sdk.server.instance.event.ServerInstanceCreateEvent;
import com.carloship.craft.sdk.server.instance.event.ServerInstanceDestroyEvent;
import com.carloship.craft.sdk.server.npc.NPCManager;
import com.carloship.craft.sdk.server.npc.impl.FakePlayer;
import com.carloship.craft.sdk.server.player.BytePlayerCraft;
import com.carloship.craft.sdk.server.scoreboard.ScoreBoardTemplate;
import com.carloship.craft.sdk.utils.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import static com.carloship.craft.sdk.utils.TextUtils.DMM;

@Data
@Slf4j
public abstract class ServerInstance<P extends BytePlayerCraft> {

    private int id = 0;
    private Task tickTask;

    private final Logger logger;

    private final UUID serverId = UUID.randomUUID();
    private final ServerInstanceMode instanceMode;

    private final Instance instance;

    private boolean reply = false;

    private final NPCManager npcManager;
    private final HologramManager hologramManager;
    private final Scheduler scheduler;

    private final SDKServer<P> root;

    public ServerInstance(SDKServer<P> root, ServerInstanceMode instanceMode, String instanceFolder) {
        this.logger = LoggerFactory.getLogger(this.getClass());
        this.instanceMode = instanceMode;
        this.instance = MinecraftServer.getInstanceManager().createInstanceContainer(new AnvilLoader(Path.of(instanceFolder), Key.key("byte","game")));
        this.scheduler = instance.scheduler();
        this.npcManager = new NPCManager(instance);
        this.hologramManager = new HologramManager(instance);

        this.root = root;
    }

    public void startServer() {
        registryContent();

        npcManager.initHolograms(hologramManager);

        npcManager.spawnAll();
        hologramManager.spawnAll();

        onStart(root);
        log.info("[LOADED] Instance {}={}/[{}]", instanceMode.getId(), id, serverId);

        EventDispatcher.call(new ServerInstanceCreateEvent(this));

        this.tickTask = scheduler.scheduleTask(
                this::onTick,
                TaskSchedule.immediate(),
                TaskSchedule.tick(20)
        );
    }

    public void stopServer() {
        npcManager.destroyAll();
        hologramManager.destroyAll();

        onStop(root);

        EventDispatcher.call(new ServerInstanceDestroyEvent<>(this, null));
    }

    public abstract void onStart(SDKServer<P> root);

    public abstract void onStop(SDKServer<P> root);

    public abstract void registryContent();

    public abstract void onPlayerJoin(P player, boolean firstLogin);

    public abstract void onPlayerLeave(P player);

    public abstract void onPlayerMove(P player, PlayerMoveEvent event);

    public abstract void onPlayerInteract(P player, @Nullable ItemStack itemStack, @Nullable Entity entity);

    public abstract void onTick();

    public abstract ScoreBoardTemplate<P> getScoreboard();

    public abstract Pair<Component, Component> getTabList();

    @Nullable
    public BiFunction<P, PlayerChatEvent, Component> getChatFormatter() {
        return null;
    }

    @NotNull
    public abstract BiFunction<@NonNull ServerInstanceMode, BytePlayerCraft, @NonNull Component> nametagFunction();

    protected abstract Class<P> getPlayerClass();

    @NotNull
    public abstract Pos getSpawnPoint();


    public void addNPC(FakePlayer npc) {
        npcManager.register(npc);
    }

    public void removeNPC(FakePlayer fakePlayer){
        npcManager.removerFakePlayer(fakePlayer.getUUID(), hologramManager);
    }

    public void addHologram(Hologram hologram) {
        hologramManager.register(hologram);
    }

    public int getPlayerCount() {
        return getPlayers().size();
    }

    public Set<P> getPlayers() {
        return getInstance().getPlayers().stream()
                .filter(getPlayerClass()::isInstance)
                .map(getPlayerClass()::cast)
                .collect(Collectors.toSet());
    }

    public void broadcastInstance(Component message){
        getPlayers().forEach(p -> p.sendMessage(message));
    }

    public void loadPlayer(P player, boolean firstLogin) {
        root.getScoreBoardManager().hide(player);

        if (getScoreboard() != null) {
            root.getScoreBoardManager().show(player, getScoreboard());
        }

        onPlayerJoin(player, firstLogin);

        getHologramManager().addViewer(player);

        player.showPlayers();

        getRoot().getInstanceProfileManager().onInstanceJoin(player, this);
    }

    protected void announcePlayerJoin(P player){
        getPlayers().forEach(po -> infoPlayerJoinMessage(player, po));
    }

    private void infoPlayerJoinMessage(P player, Player onlinePlayer){
        var gp = player.getGroup();

        var msg = player.getNametag().append(DMM("<yellow> joined the lobby!"));

        if (gp == Groups.ADMIN || gp == Groups.MASTER){
            onlinePlayer.sendMessage("");
            onlinePlayer.sendMessage(msg);
            onlinePlayer.sendMessage("");
            return;
        }

        if (gp != Groups.MEMBER){
            onlinePlayer.sendMessage(msg);
        }


    }
}
