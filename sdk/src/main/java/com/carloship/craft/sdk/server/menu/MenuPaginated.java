package com.carloship.craft.sdk.server.menu;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.kyori.adventure.text.Component;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import com.carloship.craft.sdk.server.menu.builtin.InfoButton;
import com.carloship.craft.sdk.server.menu.builtin.NextPageButton;
import com.carloship.craft.sdk.server.menu.builtin.PreviousPageButton;
import com.carloship.craft.sdk.server.player.BytePlayerCraft;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.carloship.craft.sdk.utils.TextUtils.DMM;

@Slf4j
public abstract class MenuPaginated<T extends MenuButton> extends Menu{

    private static final char CHAR_ITEM = 'X';
    private static final char CHAR_PREVIOUS = '<';
    private static final char CHAR_NEXT = '>';

    private final char[][] layout;
    private final List<MenuButton> itemsButtons;
    private final int maxPageItems;

    private final MenuButtonPattern pattern;

    private int page = 0;

    @Setter
    private int centerLayout = size.middleCenter();

    @Setter
    private MenuButton failButton = new InfoButton(
            ItemStack.builder(Material.PUFFERFISH)
                    .customName(DMM("<red>Failed to load"))
                    .lore(DMM("<gray>The item list is empty"))
                    .build());

    @Setter @Getter
    private MenuButton frameButton;

    public MenuPaginated(String title, MenuSize size, char[][] layout) {
        super(title, size);
        this.layout = layout;
        this.itemsButtons = new ArrayList<>();
        this.maxPageItems = countChar();
        this.pattern = new MenuButtonPattern();
    }

    private int countChar() {
        int count = 0;
        for (char[] row : layout) {
            for (char c : row) {
                if (c == CHAR_ITEM) count++;
            }
        }
        return count;
    }

    public void regBCD(char c, MenuButton button){
        pattern.reg(c, button);
    }

    @NotNull
    protected abstract List<T> getItems();

    private <I extends MenuButton> void setItems(List<I> items) {
        this.itemsButtons.clear();
        this.itemsButtons.addAll(items);
    }

    @Override
    protected void registerButtons() {
        int width = layout[0].length;

        var items = getItems();
        if (items.isEmpty()){
            //adicionar um item
            reg(centerLayout, failButton);
            pattern.transfer(layout);
            pattern.getMap().forEach(this::reg);
            return;
        }

        setItems(items);

        int itemIndex = page * maxPageItems;
        Iterator<MenuButton> iterator = itemsButtons.listIterator(itemIndex);

        int slot = 0;
        for (char[] row : layout) {
            for (char c : row) {
                int currentSlot = slot++;
                switch (c) {
                    case CHAR_ITEM -> {
                        if (iterator.hasNext()) {
                            MenuButton item = iterator.next();
                            reg(currentSlot, item);
                        } else {
                            reg(currentSlot, new InfoButton(
                                    ItemStack.builder(Material.GRAY_STAINED_GLASS_PANE)
                                            .customName(Component.empty())
                                            .build()
                            ));
                        }
                    }
                    case CHAR_PREVIOUS -> {
                        if (page > 0) {
                            reg(currentSlot, new PreviousPageButton(this));
                        } else {
                            //Algum tem aqui
                            if (frameButton != null){
                                reg(currentSlot, frameButton);
                            }
                        }
                    }
                    case CHAR_NEXT -> {
                        if (hasNextPage()) {
                            reg(currentSlot, new NextPageButton(this));
                        } else {
                            if (frameButton != null){
                                reg(currentSlot, frameButton);
                            }
                        }
                    }
                    default -> {
                        if (pattern.contains(c)){
                            reg(currentSlot, pattern.get(c));
                        } else {
                            if (frameButton != null){
                                reg(currentSlot, frameButton);
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public MenuPaginated<T> showPlayer(BytePlayerCraft player) {
        return (MenuPaginated<T>) super.showPlayer(player);
    }

    public int getPageCount() {
        return (int) Math.ceil((double) itemsButtons.size() / maxPageItems);
    }

    public int getMaxPageIndex() {
        return Math.max(0, getPageCount() - 1);
    }

    public boolean hasNextPage() {
        return page < getMaxPageIndex();
    }

    public int getCurrentPage(){
        return page;
    }

    public int previousPage() {
        return page - 1;
    }

    public int nextPage() {
        return page + 1;
    }

    private int clampPage(int page) {
        int min = 0;
        int max = getMaxPageIndex();
        return Math.max(min, Math.min(page, max));
    }

    public void goPage(int newPage, BytePlayerCraft player) {
        int targetPage = clampPage(newPage);

        if (this.page == targetPage) {
            log.error("no op page {} / target {} / pageCount {} / maxPages {}", page, targetPage, getPageCount(), getMaxPageIndex());
            return; // no-op
        }

        this.page = targetPage;
        reload(player);
        var m = showPlayer(player);
    }
}
