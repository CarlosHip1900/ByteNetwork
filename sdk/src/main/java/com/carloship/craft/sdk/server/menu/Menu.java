package com.carloship.craft.sdk.server.menu;

import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.inventory.click.Click;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import com.carloship.craft.sdk.server.player.BytePlayerCraft;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static com.carloship.craft.sdk.utils.TextUtils.DMM;

@Getter
@Setter
public abstract class Menu {

    public static final Logger LOG = LoggerFactory.getLogger(Menu.class);
    
    protected final MenuSize size;

    private final Map<Integer, MenuButton> buttons = new HashMap<>();

    private Function<BytePlayerCraft, Component> titleFunction;
    private ItemStack fillItem = ItemStack.builder(Material.AIR).build();

    private boolean loaded = false;

    public Menu(String title) {
        this(DMM(title));
    }

    public Menu(Component title) {
        this(title, MenuSize.SIX);
    }

    public Menu(String title, MenuSize size) {
        this(DMM(title), size);
    }

    public Menu(Component title, MenuSize size) {
        this.size = size;
        this.titleFunction = p -> title;
    }

    protected final void reg(int slot, MenuButton button) {
        if (slot >= size.getMaxSlots()) {
            LOG.warn("Cannot put button {} outside of {} at slot {}", button.getClass(), size.name(), slot);
            return;
        }

        buttons.put(slot, button);
    }

    protected final void reg(int slot, @NonNull Function<Integer, MenuButton> slotToButton) {
        MenuButton button = slotToButton.apply(slot);
        reg(slot, button);
    }

    public final Menu reload(BytePlayerCraft player) {
        buttons.clear();
        this.loaded = false;
        load();
        return showPlayer(player);
    }

    public final void load() {
        if (loaded) return;
        loaded = true;

        try {
            registerButtons();
        } catch (Throwable e) {
            LOG.error("Error registering buttons in {}: {}", getClass(), e.getMessage());
        }
    }

    public Menu showPlayer(BytePlayerCraft player) {
        Component title = createTitle(player);
        load();
        MenuInventory inventory = new MenuInventory(size.getInventoryType(), title, this);
        render(inventory, player);
        player.openInventory(inventory);
        return this;
    }

    public final Component createTitle(BytePlayerCraft player) {
        try {
            return titleFunction.apply(player);
        } catch (Throwable e) {
            LOG.error("Error generating title in {}: {}", getClass(), e.getMessage());
            return DMM("<red>INVALID TITLE");
        }
    }


    public final void render(MenuInventory inventory, BytePlayerCraft player) {
        for (int slot = 0; slot < size.getMaxSlots(); ++slot) {
            MenuButton button = buttons.get(slot);

            inventory.setItemStack(
                    slot,
                    button == null ?
                            fillItem :
                            renderButton(slot, button, player)
            );

        }

        inventory.setTitle(createTitle(player));
    }

    public final @NonNull ItemStack renderButton(int slot, MenuButton button, BytePlayerCraft player) {
        try {
            ItemStack renderedButton = button.renderButton(player);
            if (renderedButton == null) {
                renderedButton = ItemStack.builder(Material.BARRIER)
                        .customName(DMM("<red>In: " + button.getClass().getSimpleName()))
                        .lore(List.of(DMM("<red>This should be edited")))
                        .build();
            }

            return renderedButton;
        } catch (Throwable e) {
            LOG.error("Error rendering button {} in {} at slot {}", button.getClass().getSimpleName(), getClass(), slot);
            LOG.error(e.getMessage());
            return ItemStack.builder(Material.BARRIER)
                    .customName(DMM("<red>Error!"))
                    .lore(List.of(
                            DMM("<gray>An error occurred while rendering"),
                            DMM("<gray>the button in this inventory")
                    )).build();
        }
    }

    public void handleWhenClicked(@NonNull InventoryPreClickEvent event) {
        BytePlayerCraft clicker = (BytePlayerCraft) event.getPlayer();
        event.setCancelled(true);

        MenuButton button = buttons.get(event.getSlot());
        if (button != null) {
            try {
                Click clickType = event.getClick();
                button.clickButton(clicker, clickType);
            } catch (Throwable e) {
                LOG.error("Error clicking button {} in menu {}", button.getClass().getSimpleName(), getClass().getSimpleName(), e);
                clicker.sendMessage(DMM("<red>Hmm... An error occurred while processing the button action."));
            }

        }
    }

    protected abstract void registerButtons();

    protected final void clearButtons() {
        buttons.clear();
    }

}
