package com.carloship.craft.sdk.server.arena;

import com.carloship.craft.sdk.server.player.BytePlayerCraft;
import com.carloship.craft.sdk.SDKServer;

import lombok.Getter;

import net.minestom.server.timer.SchedulerManager;
import net.minestom.server.timer.Task;
import net.minestom.server.timer.TaskSchedule;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Getter
public abstract class ArenaEngine<P extends BytePlayerCraft> {

    private static final long TICKS_PER_SECOND = 20L;

    private final List<ArenaStage> stages;
    private final SDKServer<P> server;
    private final SchedulerManager schedulerManager;

    private int currentStageIndex = -1;

    private ArenaStage currentStage;

    private long stageElapsedTicks;

    private boolean running;
    private boolean canSkip;
    private boolean frozen;

    private Task scheduledTask;

    protected ArenaEngine(SDKServer<P> server) {
        this.server = Objects.requireNonNull(server, "The Root Server is null");
        this.schedulerManager = server.getSchedulerManager();
        this.stages = new ArrayList<>();
    }

    /**
     * Registers a stage.
     *
     * Stages must be registered before the engine starts.
     *
     * @param stage the stage to register
     * @return this engine
     */
    public ArenaEngine<P> addStage(ArenaStage stage) {
        Objects.requireNonNull(stage, "stage");

        if (running) {
            throw new IllegalStateException(
                    "Cannot register stages while the engine is running."
            );
        }

        if (stage.getDuration() == null || stage.getDuration().isNegative()) {
            throw new IllegalArgumentException(
                    "Stage duration cannot be null or negative."
            );
        }

        stages.add(stage);

        return this;
    }

    /**
     * Starts the engine.
     */
    public void startEngine() {
        if (running) {
            return;
        }

        if (stages.isEmpty()) {
            throw new IllegalStateException(
                    "Cannot start an arena engine without stages."
            );
        }

        running = true;
        frozen = false;
        canSkip = false;
        currentStageIndex = -1;
        stageElapsedTicks = 0L;

        try {
            onStart();

            startNextStage();

        } catch (RuntimeException exception) {
            stopEngine();
            throw exception;
        }
    }

    /**
     * Starts the next registered stage.
     */
    private void startNextStage() {
        if (!running) {
            return;
        }

        if (currentStage != null) {
            ArenaStage previousStage = currentStage;

            currentStage = null;
            canSkip = false;

            previousStage.onStop();
        }

        if (!running) {
            return;
        }

        currentStageIndex++;

        if (currentStageIndex >= stages.size()) {
            stopEngine();
            return;
        }

        currentStage = stages.get(currentStageIndex);
        stageElapsedTicks = 0L;
        frozen = currentStage.isFrozen();

        canSkip = true;

        currentStage.onStart();

        if (!running) {
            return;
        }

        ensureScheduled();
    }

    /**
     * Creates the repeating tick task if necessary.
     */
    private void ensureScheduled() {
        if (scheduledTask != null || !running) {
            return;
        }

        scheduledTask = schedulerManager.buildTask(this::tick)
                .repeat(TaskSchedule.tick(1))
                .schedule();
    }

    /**
     * Processes the current stage.
     */
    private void tick() {
        if (!running || currentStage == null || frozen) {
            return;
        }

        ArenaStage stage = currentStage;

        onTick();

        if (!running || currentStage != stage || frozen) {
            return;
        }

        stage.onTick();

        if (!running || currentStage != stage || frozen) {
            return;
        }

        stageElapsedTicks++;

        stage.tickSubStages(
                Duration.ofMillis(stageElapsedTicks * 50L)
        );

        if (stageElapsedTicks >= durationToTicks(stage.getDuration())) {
            startNextStage();
        }
    }

    /**
     * Converts a duration into Minecraft ticks.
     *
     * Positive durations are rounded up to the next tick.
     *
     * @param duration the duration to convert
     * @return duration in ticks
     */
    private long durationToTicks(Duration duration) {
        long seconds = duration.getSeconds();
        int nanos = duration.getNano();

        long wholeTicks = Math.multiplyExact(seconds, TICKS_PER_SECOND);

        long fractionalTicks = (nanos * TICKS_PER_SECOND + 999_999_999L)
                / 1_000_000_000L;

        return Math.addExact(wholeTicks, fractionalTicks);
    }

    /**
     * Skips the current stage if skipping is allowed.
     */
    public void skipStage() {
        if (!running || !canSkip || currentStage == null) {
            return;
        }

        startNextStage();
    }

    /**
     * Freezes the current stage.
     */
    public void freeze() {
        if (!running || frozen || currentStage == null) {
            return;
        }

        frozen = true;

        currentStage.onFreeze();
    }

    /**
     * Resumes the current stage.
     */
    public void resume() {
        if (!running || !frozen || currentStage == null) {
            return;
        }

        frozen = false;

        currentStage.onResume();
    }

    /**
     * Stops the engine and cancels its scheduled task.
     */
    public void stopEngine() {
        if (!running) {
            return;
        }

        running = false;
        canSkip = false;
        frozen = false;

        Task task = scheduledTask;
        scheduledTask = null;

        if (task != null) {
            task.cancel();
        }

        ArenaStage stage = currentStage;
        currentStage = null;

        try {
            if (stage != null) {
                stage.onStop();
            }
        } finally {
            onStop();
        }
    }

    /**
     * Returns the number of registered stages.
     *
     * @return the stage count
     */
    public int getMaxStage() {
        return stages.size();
    }

    /**
     * Returns the number of the current stage, starting at 1.
     *
     * @return current stage number, or 0 if no stage is active
     */
    public int getCurrentStageNumber() {
        return currentStage == null ? 0 : currentStageIndex + 1;
    }

    /**
     * Returns the remaining duration of the current stage.
     *
     * @return remaining duration, or Duration.ZERO if no stage is active
     */
    public Duration getRemainingDuration() {
        if (currentStage == null) {
            return Duration.ZERO;
        }

        long totalTicks = durationToTicks(currentStage.getDuration());
        long remainingTicks = Math.max(0L, totalTicks - stageElapsedTicks);

        return Duration.ofMillis(
                (remainingTicks * 1000L + TICKS_PER_SECOND - 1)
                        / TICKS_PER_SECOND
        );
    }

    /**
     * Called when the engine starts.
     */
    protected abstract void onStart();

    /**
     * Called when the engine stops.
     */
    protected abstract void onStop();

    /**
     * Called every tick while the engine is running.
     */
    protected abstract void onTick();
}