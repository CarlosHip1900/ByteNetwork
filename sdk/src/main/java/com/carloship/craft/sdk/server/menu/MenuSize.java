package com.carloship.craft.sdk.server.menu;

import lombok.Getter;
import net.minestom.server.inventory.InventoryType;

@Getter
public enum MenuSize {

    ONE(InventoryType.CHEST_1_ROW, 4),
    TWO(InventoryType.CHEST_2_ROW, 4),
    THREE(InventoryType.CHEST_3_ROW, 13),
    FOUR(InventoryType.CHEST_4_ROW, 13),
    FIVE(InventoryType.CHEST_5_ROW, 22),
    SIX(InventoryType.CHEST_6_ROW, 22);

    private final InventoryType inventoryType;
    private final int middleCenter;

    MenuSize(InventoryType inventoryType, int middleCenter) {
        this.inventoryType = inventoryType;
        this.middleCenter = middleCenter;
    }

    public int getMaxSlots() {
        return inventoryType.getSize();
    }

    public int minus(int slots) {
        return getMaxSlots() - 1 - slots;
    }

    public int last() {
        return getMaxSlots() - 1;
    }

    public int middleCenter() {
        return middleCenter;
    }

    public int bottomCenter() {
        return minus(4);
    }
}