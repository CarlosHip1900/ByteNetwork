package com.carloship.craft.sdk.utils.message;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class EnglishProfanityFilter implements IProfanityFilter {

    private static final Set<String> SWEAR_WORDS = new HashSet<>(Arrays.asList(
            "fuck","fucker","fucking","shit","bullshit",
            "ass","asshole","bitch","bastard",
            "dick","pussy","motherfucker",
            "damn","dumbass","jerk","slut","whore","crap"
    ));

    private static final List<String> SWEAR_PHRASES = Arrays.asList(
            "son of a bitch",
            "fuck you",
            "piece of shit",
            "go to hell"
    );

    @Override
    public boolean containsProfanity(String text) {

        if (text == null || text.isEmpty()) {
            return false;
        }

        String normalized = text.toLowerCase();

        for (String phrase : SWEAR_PHRASES) {
            if (normalized.contains(phrase)) {
                return true;
            }
        }

        String[] tokens = normalized.split("\\s+");

        for (String token : tokens) {

            String cleaned = token.replaceAll("[^a-z]", "");

            if (SWEAR_WORDS.contains(cleaned)) {
                return true;
            }
        }

        return false;
    }
}