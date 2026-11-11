package com.carloship.craft.sdk.server.leveling;

@FunctionalInterface
public interface LevelFormula {
    /**
     * @param level current level
     * @return XP required to go from this level to the next
     */
    double xpForLevel(int level);
}
