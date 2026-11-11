package com.carloship.craft.sdk.server.common;

import com.carloship.craft.sdk.SDKServerAPI;
import com.carloship.craft.sdk.SDKServerType;
import com.carloship.craft.sdk.server.menu.Menu;
import com.carloship.craft.sdk.server.menu.MenuButton;
import com.carloship.craft.sdk.server.menu.MenuSize;
import com.carloship.craft.sdk.server.player.BytePlayerCraft;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.minestom.server.inventory.click.Click;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;

import static com.carloship.craft.sdk.utils.TextUtils.DMM;
import static com.carloship.craft.sdk.utils.TextUtils.SDMM;

public class ServersMenu extends Menu {

    public ServersMenu() {
        super("Servidores", MenuSize.FOUR);
    }

    @Override
    protected void registerButtons() {
        reg(10, new MenuButton() {
            @Override
            public void clickButton(BytePlayerCraft player, Click click) {
                send(player, SDKServerType.MASTER_LOBBY);
            }

            @Override
            public ItemStack renderButton(BytePlayerCraft player) {
                return ItemStack.builder(Material.BOOKSHELF)
                        .customName(SDMM("<green><bold>MASTER LOBBY").decoration(TextDecoration.ITALIC, false))
                        .lore(
                                DMM("<dark_gray>Father of lobbies"),
                                Component.empty(),
                                DMM("<yellow>Click to go!"))
                        .build();
            }
        });

        reg(13, new MenuButton() {
            @Override
            public void clickButton(BytePlayerCraft player, Click click) {
                send(player, SDKServerType.SHEEP_WARS);
            }

            @Override
            public ItemStack renderButton(BytePlayerCraft player) {
                return ItemStack.builder(Material.SHEEP_SPAWN_EGG)
                        .customName(SDMM("<light_purple><bold>SHEEP WARS").decoration(TextDecoration.ITALIC, false))
                        .lore(
                                DMM("<gray>Throw, Spawn, Kill and do anything"),
                                DMM("<gray>with Sheep while happen War against"),
                                DMM("<gray>maybe 2, 4, 8 or 16 players!"),
                                Component.empty(),
                                DMM("<yellow>Click to go!"))
                        .build();
            }
        });
    }


    private static void send(BytePlayerCraft player, SDKServerType com){
        var current = SDKServerAPI.serverType();

        if (current == com) {
            player.sendErrorMessage("You already connected...");
            return;
        }

        player.closeInventory();
        SDKServerAPI.sendPlayerToGame(player, com);
    }
}
