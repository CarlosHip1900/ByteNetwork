package com.carloship.craft.sdk.server.menu.builtin;

import lombok.RequiredArgsConstructor;
import net.minestom.server.entity.Player;
import net.minestom.server.inventory.click.Click;
import net.minestom.server.item.ItemStack;
import com.carloship.craft.sdk.server.menu.MenuButton;
import com.carloship.craft.sdk.server.player.BytePlayerCraft;

import java.util.function.Function;

@RequiredArgsConstructor
public class FunctionInfoButton implements MenuButton {

    private final Function<Player, ItemStack> item;

    @Override
    public void clickButton(BytePlayerCraft player, Click clickType) {

    }

    @Override
    public ItemStack renderButton(BytePlayerCraft player) {
        return item.apply(player);
    }

}
