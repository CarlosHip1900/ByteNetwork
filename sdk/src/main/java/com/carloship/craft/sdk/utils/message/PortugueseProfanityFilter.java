package com.carloship.craft.sdk.utils.message;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PortugueseProfanityFilter implements IProfanityFilter {

    private static final Set<String> SWEAR_WORDS = new HashSet<>(Arrays.asList(
            "cu","cuzao","cuzinho","caralho","porra","puta","puto","putaria",
            "buceta","pica","rola","arrombado","desgracado","fdp","viado",
            "veado","bosta","merda","cacete","foder","fodeu","fudido",
            "otario","idiota","babaca"
    ));

    private static final List<String> SWEAR_PHRASES = Arrays.asList(
            "filho da puta",
            "pau no cu",
            "vai se foder",
            "vai tomar no cu"
    );

    @Override
    public boolean containsProfanity(String text) {

        if (text == null || text.isEmpty()) {
            return false;
        }

        String normalized = normalize(text);

        for (String phrase : SWEAR_PHRASES) {
            if (normalized.contains(phrase)) {
                return true;
            }
        }

        String[] tokens = normalized.split("\\s+");

        for (String token : tokens) {

            String cleaned = removeSymbols(token);

            if (SWEAR_WORDS.contains(cleaned)) {
                return true;
            }
        }

        return false;
    }

    private String normalize(String text) {

        text = text.toLowerCase();
        text = Normalizer.normalize(text, Normalizer.Form.NFD);
        text = text.replaceAll("\\p{M}", "");

        return text;
    }

    private String removeSymbols(String word) {

        return word.replaceAll("[^a-z]", "");
    }
}