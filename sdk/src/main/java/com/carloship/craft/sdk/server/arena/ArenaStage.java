package com.carloship.craft.sdk.server.arena;

import net.kyori.adventure.text.Component;

import java.time.Duration;
import java.util.*;

public abstract class ArenaStage {

    private final List<ArenaStageEvent> subStages = new ArrayList<>();
    private final Set<ArenaStageEvent> startedSubStages = new HashSet<>();

    public abstract Component getTitle();

    public abstract Duration getDuration();

    public abstract void onStart();

    public abstract void onStop();

    public abstract void onTick();

    public abstract void onFreeze();

    public abstract void onResume();

    public abstract boolean isFrozen();

    public final void tickSubStages(Duration elapsed) {
        for (ArenaStageEvent subStage : subStages) {

            if (startedSubStages.contains(subStage)) {
                continue;
            }

            if (elapsed.compareTo(subStage.getStartTime()) < 0) {
                continue;
            }

            startedSubStages.add(subStage);

            subStage.onStart();
        }
    }

    /**
     * Registers a substage.
     *
     * @param subStage the substage to register
     */
    protected final void addSubStage(ArenaStageEvent subStage) {
        if (subStage.getStartTime().isNegative()) {
            throw new IllegalArgumentException(
                    "Substage start time cannot be negative."
            );
        }

        if (subStage.getStartTime().compareTo(getDuration()) > 0) {
            throw new IllegalArgumentException(
                    "Substage cannot start after the stage duration."
            );
        }

        subStages.add(subStage);

        subStages.sort(
                Comparator.comparing(ArenaStageEvent::getStartTime)
        );
    }

    public final List<ArenaStageEvent> getSubStages() {
        return List.copyOf(subStages);
    }
}