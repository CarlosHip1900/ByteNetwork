package com.carloship.craft.sdk.server.common;

import com.carloship.craft.sdk.server.player.BytePlayerCraft;
import net.minestom.server.MinecraftServer;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.event.item.ItemDropEvent;
import net.minestom.server.event.player.PlayerUseItemEvent;
import net.minestom.server.item.ItemStack;
import net.minestom.server.tag.Tag;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

public class GameHotBarManager {

    private static final Tag<String> ITEM_TAG = Tag.String("hotbar_item");

    private final Map<Integer, HotBarItem> items = new HashMap<>();
    private final Map<String, HotBarItem> namespaceItems = new HashMap<>();

    public GameHotBarManager() {
        EventNode<Event> EVENT_NODE = EventNode.all("hot-bar")
                .addListener(PlayerUseItemEvent.class, event -> {
                    if (!(event.getPlayer() instanceof BytePlayerCraft serverPlayer)) return;
                    ItemStack item = event.getItemStack();

                    if (handleClick(serverPlayer, item)) {
                        event.setCancelled(true);
                    }
                })
                .addListener(InventoryPreClickEvent.class, event -> {
                    ItemStack clicked = event.getClickedItem();

                    if (shouldLockItem(clicked)) {
                        event.setCancelled(true);
                    }
                })
                .addListener(ItemDropEvent.class, event -> {
                    ItemStack clicked = event.getItemStack();

                    if (shouldLockItem(clicked)) {
                        event.setCancelled(true);
                    }
                });

        if (EVENT_NODE.getParent() == null) {
            MinecraftServer.getGlobalEventHandler().addChild(EVENT_NODE);
        }
    }

    public GameHotBarManager addItem(int slot, HotBarItem item) {
        if (item == null) {
            return this;
        }

        items.put(slot, item);
        namespaceItems.put(item.namespace(), item);

        return this;
    }

    public void sendPlayer(BytePlayerCraft player) {
        items.forEach((slot, hotBarItem) -> {
            ItemStack stack = hotBarItem.itemStack().apply(player);

            if (stack == null) {
                return;
            }

            player.getInventory().setItemStack(
                    slot,
                    stack.withTag(ITEM_TAG, hotBarItem.namespace())
            );
        });
    }

    public void updatePlayer(BytePlayerCraft player) {
        sendPlayer(player);
    }

    public boolean shouldLockItem(ItemStack stack) {
        return stack.hasTag(ITEM_TAG);
    }

    public boolean handleClick(BytePlayerCraft player, ItemStack clickedItem) {
        if (clickedItem == null || clickedItem.isAir()) {
            return false;
        }

        String namespace = clickedItem.getTag(ITEM_TAG);

        if (namespace == null) {
            return false;
        }

        HotBarItem item = namespaceItems.get(namespace);

        if (item == null) {
            return false;
        }

        item.consumer().accept(player);
        return true;
    }

    public record HotBarItem(
            Function<BytePlayerCraft, ItemStack> itemStack,
            String namespace,
            Consumer<BytePlayerCraft> consumer
    ) {
    }
}