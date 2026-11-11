package com.carloship.craft.sdk.server.menu;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import org.jetbrains.annotations.NotNull;

@Getter
public class MenuInventory extends Inventory {

    private final Menu menu;

    public MenuInventory(InventoryType inventoryType, @NotNull Component title, Menu menu) {
        super(inventoryType, title);
        this.menu = menu;
    }

    public MenuInventory(InventoryType inventoryType, @NotNull String title, Menu menu) {
        super(inventoryType, title);
        this.menu = menu;
    }
}
