package com.carloship.craft.sdk.server.menu.builtin;

import com.carloship.craft.sdk.server.menu.Menu;
import com.carloship.craft.sdk.server.menu.MenuButton;
import com.carloship.craft.sdk.server.player.BytePlayerCraft;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.minestom.server.inventory.click.Click;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;

import static com.carloship.craft.sdk.utils.TextUtils.CLICK_GREEN;
import static com.carloship.craft.sdk.utils.TextUtils.DMM;

@RequiredArgsConstructor
public class BackButton implements MenuButton {

    private final Menu menu;

    @Override
    public void clickButton(BytePlayerCraft player, Click clickType) {
        menu.showPlayer(player);
    }

    @Override
    public ItemStack renderButton(BytePlayerCraft player) {
        return ItemStack.builder(Material.ARROW)
                .customName(DMM("<white>Back"))
                .lore(Component.empty(),
                        DMM(CLICK_GREEN + " Click to go back"))
                .build();
    }
}
