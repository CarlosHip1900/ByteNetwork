package com.carloship.craft.sdk.server.menu;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MenuButtonPattern {

    private final ConcurrentHashMap<Character, MenuButton> map = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Integer, MenuButton> t_map = new ConcurrentHashMap<>();

    private boolean t_data = false;

    public void reg(char c, MenuButton b) {
        map.put(c, b);
    }

    public void transfer(char[][] layout) {
        int slot = 0;
        for (char[] row : layout) {
            for (char c : row) {
                int currentSlot = slot++;
                if (map.containsKey(c)) {
                    t_map.put(currentSlot, map.get(c));
                }
            }
        }

        t_data = true;
    }

    public Map<Integer, MenuButton> getMap(){
        if (!t_data) return Map.of();
        return t_map;
    }

    public boolean contains(char c) {
        return map.containsKey(c);
    }

    public MenuButton get(char c) {
        return map.get(c);
    }

}
