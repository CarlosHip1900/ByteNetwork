package com.carloship.craft.sdk.server.menu;

import com.carloship.craft.sdk.server.menu.builtin.InfoButton;
import net.kyori.adventure.text.Component;
import net.minestom.server.inventory.click.Click;
import net.minestom.server.item.ItemStack;
import com.carloship.craft.sdk.server.player.BytePlayerCraft;
import net.minestom.server.item.Material;

public interface MenuButton {

    void clickButton(BytePlayerCraft player, Click click);

    ItemStack renderButton(BytePlayerCraft player);

    static ItemStack STACK_BLACK_CLASS = ItemStack
            .builder(Material.BLACK_STAINED_GLASS_PANE)
            .customName(Component.empty())
            .build();

    static InfoButton BUTTON_BLACK_GLASS = new InfoButton(MenuButton.STACK_BLACK_CLASS);

}
