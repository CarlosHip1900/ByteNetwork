package com.carloship.craft.sdk.utils;

import java.util.Map;

public class SmallCaps {

    private static final Map<Character, Character> MAP = Map.ofEntries(
            Map.entry('a', 'ᴀ'),
            Map.entry('b', 'ʙ'),
            Map.entry('c', 'ᴄ'),
            Map.entry('d', 'ᴅ'),
            Map.entry('e', 'ᴇ'),
            Map.entry('f', 'ғ'),
            Map.entry('g', 'ɢ'),
            Map.entry('h', 'ʜ'),
            Map.entry('i', 'ɪ'),
            Map.entry('j', 'ᴊ'),
            Map.entry('k', 'ᴋ'),
            Map.entry('l', 'ʟ'),
            Map.entry('m', 'ᴍ'),
            Map.entry('n', 'ɴ'),
            Map.entry('o', 'ᴏ'),
            Map.entry('p', 'ᴘ'),
            Map.entry('q', 'ǫ'),
            Map.entry('r', 'ʀ'),
            Map.entry('s', 's'),
            Map.entry('t', 'ᴛ'),
            Map.entry('u', 'ᴜ'),
            Map.entry('v', 'ᴠ'),
            Map.entry('w', 'ᴡ'),
            Map.entry('x', 'x'),
            Map.entry('y', 'ʏ'),
            Map.entry('z', 'ᴢ')
    );

    public static String convert(String input) {
        StringBuilder out = new StringBuilder();

        for (char c : input.toLowerCase().toCharArray()) {
            out.append(MAP.getOrDefault(c, c));
        }

        return out.toString();
    }
}