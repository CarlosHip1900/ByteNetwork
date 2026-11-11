package com.carloship.craft.sdk.server.leveling;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.function.BiConsumer;

@Data
@AllArgsConstructor
public class LevelingPrestige {

    private int level;
    private int prestige;
    private double xp;

    private final int maxLevel;
    private final XpCurvePrestige curve;

    // old level, new level
    private BiConsumer<Integer, Integer> onLevelUp;

    private BiConsumer<Integer, Integer> onPrestige;

    public LevelingPrestige(XpCurvePrestige curve, int maxLevel) {
        this(curve, maxLevel, 1, 0, 0);
    }

    public LevelingPrestige(XpCurvePrestige curve, int maxLevel, int startLevel, double startXp, int startPrestige) {
        if (curve == null)
            throw new IllegalArgumentException("XP curve cannot be null");

        this.curve = curve;
        this.maxLevel = Math.max(1, maxLevel);
        this.level = Math.max(1, startLevel);
        this.xp = Math.max(0, startXp);
        this.prestige = Math.max(0, startPrestige);
    }

    public synchronized int getLevel() {
        return level;
    }

    public synchronized int getPrestige() {
        return prestige;
    }

    public synchronized double getXP() {
        return xp;
    }

    public synchronized void addXP(double amount) {
        if (amount <= 0) return;

        if (isMaxLevel()) {
            xp = Math.min(xp + amount, xpToNextLevel());
            return;
        }

        xp += amount;

        while (canLevelUp()) {
            levelUpInternal();
        }
    }

    public synchronized void removeXP(double amount) {
        if (amount <= 0) return;
        xp = Math.max(0, xp - amount);
    }

    public synchronized void addXpToNextLevel() {
        xp = xpToNextLevel();
    }

    public synchronized boolean canLevelUp() {
        return !isMaxLevel() && xp >= xpToNextLevel();
    }

    public synchronized boolean isMaxLevel() {
        return level >= maxLevel;
    }

    private void levelUpInternal() {
        if (isMaxLevel())
            return;

        double required = xpToNextLevel();
        xp -= required;

        int old = level;
        level++;

        if (onLevelUp != null)
            onLevelUp.accept(old, level);
    }

    private double xpToNextLevel() {
        if (isMaxLevel())
            return curve.xpForLevel(maxLevel, prestige);

        return curve.xpForLevel(level, prestige);
    }

    public synchronized boolean canPrestige() {
        return isMaxLevel();
    }

    public synchronized void prestige() {
        if (!canPrestige())
            return;

        int oldPrestige = prestige;
        prestige++;

        level = 1;
        xp = 0;

        if (onPrestige != null)
            onPrestige.accept(oldPrestige, prestige);
    }
}

