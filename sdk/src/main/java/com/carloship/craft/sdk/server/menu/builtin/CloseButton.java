package com.carloship.craft.sdk.server.menu.builtin;

import net.kyori.adventure.text.Component;
import net.minestom.server.inventory.click.Click;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import com.carloship.craft.sdk.server.menu.MenuButton;
import com.carloship.craft.sdk.server.player.BytePlayerCraft;

import static com.carloship.craft.sdk.utils.TextUtils.CLICK_GREEN;
import static com.carloship.craft.sdk.utils.TextUtils.DMM;

public class CloseButton implements MenuButton {

    @Override
    public void clickButton(BytePlayerCraft player, Click clickType) {
        player.closeInventory();
    }

    @Override
    public ItemStack renderButton(BytePlayerCraft player) {
        return ItemStack.builder(Material.BARRIER)
                .customName(DMM("<white>Close"))
                .lore(
                        Component.empty(),
                        DMM(CLICK_GREEN + " Click to close")
                )
                .build();
    }
}
