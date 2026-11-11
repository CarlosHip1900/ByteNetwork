package com.carloship.craft.sdk.server.menu.builtin;

import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.minestom.server.inventory.click.Click;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import com.carloship.craft.sdk.server.menu.MenuButton;
import com.carloship.craft.sdk.server.menu.MenuPaginated;
import com.carloship.craft.sdk.server.player.BytePlayerCraft;

import static com.carloship.craft.sdk.utils.TextUtils.*;

@RequiredArgsConstructor
public class PreviousPageButton implements MenuButton {

    private final MenuPaginated<?> paginated;

    @Override
    public void clickButton(BytePlayerCraft player, Click clickType) {
        if (clickType instanceof Click.Left){
            paginated.goPage(paginated.getCurrentPage() - 1, player);
        }

        if (clickType instanceof Click.Right){
            paginated.goPage(0, player);
        }
    }

    @Override
    public ItemStack renderButton(BytePlayerCraft player) {
        return ItemStack.builder(Material.GREEN_CANDLE)
                .customName(DMM("<dark_green>Previous Page"))
                .lore(
                        DMM("<gray>(" + paginated.previousPage() + "/" + paginated.getMaxPageIndex() + ")"),
                        Component.empty(),
                        DMM("<gray>Current page: <white>" + paginated.getCurrentPage()),
                        Component.empty(),
                        DMM(LEFT_CLICK_GREEN + " Left-click: Previous page"),
                        DMM(RIGHT_CLICK_GREEN + " Right-click: First page")
                )
                .build();
    }
}
