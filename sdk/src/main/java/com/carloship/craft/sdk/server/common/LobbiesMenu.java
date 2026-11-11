package com.carloship.craft.sdk.server.common;

import com.carloship.craft.sdk.SDKServerAPI;
import com.carloship.craft.sdk.SDKServerType;
import com.carloship.craft.sdk.server.instance.ServerInstance;
import com.carloship.craft.sdk.server.instance.ServerInstanceMode;
import com.carloship.craft.sdk.server.instance.event.player.PlayerSwitchInstanceEvent;
import com.carloship.craft.sdk.server.menu.Menu;
import com.carloship.craft.sdk.server.menu.MenuButton;
import com.carloship.craft.sdk.server.menu.MenuSize;
import com.carloship.craft.sdk.server.menu.builtin.InfoButton;
import com.carloship.craft.sdk.server.player.BytePlayerCraft;
import net.kyori.adventure.text.Component;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.inventory.click.Click;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;

import java.util.List;

import static com.carloship.craft.sdk.utils.TextUtils.CLICK_GREEN;
import static com.carloship.craft.sdk.utils.TextUtils.DMM;

public class LobbiesMenu extends Menu {

    private final Pos defaultSpawn;
    private final ServerInstanceMode type;

    public LobbiesMenu(Pos defaultSpawn, ServerInstanceMode type) {
        super("Lobbies", MenuSize.FOUR);
        setFillItem(InfoButton.STACK_BLACK_CLASS);
        this.defaultSpawn = defaultSpawn;
        this.type = type;
    }

    @Override
    protected void registerButtons() {
        List<ServerInstance<BytePlayerCraft>> instances = SDKServerAPI.runningInstancesFrom(type);

        for (int i = 0; i < instances.size(); i++) {
            ServerInstance<BytePlayerCraft> lobby = instances.get(i);
            reg(i, new MenuButton() {

                @Override
                public void clickButton(BytePlayerCraft p, Click click) {
                    if (p.getCurrentServerInstanceId().equals(lobby.getServerId())) return;

                    p.closeInventory();

                    var currentInstance = SDKServerAPI.instanceFrom(p);
                    if (currentInstance == null) {
                        p.sendErrorMessage("Could not retrieve your current instance.");
                        return;
                    }

                    EventDispatcher.call(new PlayerSwitchInstanceEvent<>(p, currentInstance, lobby, defaultSpawn));
                }

                @Override
                public ItemStack renderButton(BytePlayerCraft player) {
                    if (player.getCurrentServerInstanceId().equals(lobby.getServerId())) {
                        return ItemStack.builder(Material.GRAY_SHULKER_BOX)
                                .customName(DMM("<green>Lobby: <yellow>#" + lobby.getId()))
                                .lore(
                                        Component.empty(),
                                        DMM("<gray>Players: <yellow>" + lobby.getPlayers().size() + "<dark_gray>/<yellow>100"),
                                        Component.empty(),
                                        DMM("<red>You are already here.")
                                ).build();
                    }

                    return ItemStack.builder(Material.LIGHT_BLUE_SHULKER_BOX)
                            .customName(DMM("<green>Lobby: <yellow>#" + lobby.getId()))
                            .lore(
                                    Component.empty(),
                                    DMM("<gray>Players: <yellow>" + lobby.getPlayers().size() + "<dark_gray>/<yellow>100"),
                                    Component.empty(),
                                    DMM(CLICK_GREEN + " Click to join!")
                            ).build();
                }
            });
        }
    }
}