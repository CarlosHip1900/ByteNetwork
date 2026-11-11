package com.carloship.craft.sdk.server.leveling;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.function.BiConsumer;

@Data
@AllArgsConstructor
public class Leveling {

    private int level;
    private double xp;

    private LevelFormula formula;
    private XpCurve curve;

    // old level, new level
    private BiConsumer<Integer, Integer> onLevelUp;

    public Leveling(LevelFormula formula) {
        this(formula, null, 1, 0);
    }

    public Leveling(LevelFormula formula, int startLevel, double startXp) {
        this(formula, null, startLevel, startXp);
    }

    public Leveling(XpCurve curve) {
        this(null, curve, 1, 0);
    }

    public Leveling(XpCurve curve, int startLevel, double startXp) {
        this(null, curve, startLevel, startXp);
    }

    private Leveling(LevelFormula formula, XpCurve curve, int startLevel, double startXp) {
        if (formula == null && curve == null)
            throw new IllegalArgumentException("Formula or curve must be provided");

        this.formula = formula;
        this.curve = curve;
        this.level = Math.max(1, startLevel);
        this.xp = Math.max(0, startXp);
    }

    public void addLevel() {
        levelUp();
    }

    public void removeLevel() {
        if (level > 1)
            level--;
    }

    public void incLevel() {
        level++;
    }

    public void dcrLevel() {
        level = Math.max(1, level - 1);
    }

    public void powLevel() {
        level *= level;
    }

    public void addXP(double amount) {
        if (amount <= 0) return;

        xp += amount;

        while (canLevelUp()) {
            levelUp();
        }
    }

    public void removeXP(double amount) {
        if (amount <= 0) return;
        xp = Math.max(0, xp - amount);
    }

    public void powXP() {
        xp *= xp;
    }

    public void addXpToNextLevel() {
        xp = xpToNextLevel();
    }


    public boolean canLevelUp() {
        return xp >= xpToNextLevel();
    }

    public void levelUp() {
        if (!canLevelUp())
            return;

        double required = xpToNextLevel();
        xp -= required;

        int oldLevel = level;
        level++;

        if (onLevelUp != null)
            onLevelUp.accept(oldLevel, level);
    }

    private double xpToNextLevel() {
        if (formula != null)
            return formula.xpForLevel(level);

        return curve.xpForLevel(level);
    }
}
