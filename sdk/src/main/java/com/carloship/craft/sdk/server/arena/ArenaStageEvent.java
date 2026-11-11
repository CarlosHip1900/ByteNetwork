package com.carloship.craft.sdk.server.arena;

import java.time.Duration;

public interface ArenaStageEvent {

    /**
     * Returns the time at which this substage should execute,
     * measured from the beginning of its parent stage.
     *
     * @return execution time
     */
    Duration getStartTime();

    /**
     * Executes this substage.
     */
    void onStart();

    /**
     * Executes every tick while this substage is active.
     *
     * This method is optional for one-time events.
     */
    default void onTick() {
    }

    /**
     * Called when this substage finishes.
     */
    default void onStop() {
    }

    /**
     * Indicates whether this substage has completed.
     *
     * @return true if completed
     */
    default boolean isComplete() {
        return true;
    }
}