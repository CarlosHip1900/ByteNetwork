package com.carloship.craft.sdk.utils.message;

import java.util.List;

public class ProfanityFilter {

    private final static EnglishProfanityFilter ENGLISH_PROFANITY_FILTER;
    private final static PortugueseProfanityFilter PORTUGUESE_PROFANITY_FILTER;

    static {
        ENGLISH_PROFANITY_FILTER = new EnglishProfanityFilter();
        PORTUGUESE_PROFANITY_FILTER = new PortugueseProfanityFilter();
    }

    private static final List<IProfanityFilter> FILTERS = List.of(
            PORTUGUESE_PROFANITY_FILTER,
            ENGLISH_PROFANITY_FILTER
    );

    public static boolean checkPT(String msg){
        return PORTUGUESE_PROFANITY_FILTER.containsProfanity(msg);
    }

    public static boolean checkEN(String msg){
        return ENGLISH_PROFANITY_FILTER.containsProfanity(msg);
    }

    public static boolean checkAll(String msg) {
        for (IProfanityFilter filter : FILTERS) {
            if (filter.containsProfanity(msg)) {
                return true;
            }
        }
        return false;
    }

}
