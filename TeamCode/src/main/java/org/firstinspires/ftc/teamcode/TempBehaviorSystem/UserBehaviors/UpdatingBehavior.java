package org.firstinspires.ftc.teamcode.TempBehaviorSystem.UserBehaviors;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.TempBehaviorSystem.Behavior;

/**
 * Used to create behaviors that execute a Runnable each frame they are active.
 * @author Edson James
 */
public class UpdatingBehavior implements Behavior {

    private final String label;
    private final Runnable updateRunnable;

    /**
     * Used to create behaviors that execute a Runnable each frame they are active.
     * @param updateRunnable The Runnable to be executed each frame this behavior is active
     */
    public UpdatingBehavior(Runnable updateRunnable) {
        this(updateRunnable, "Updating behavior");
    }

    /**
     * Used to create behaviors that execute a Runnable each frame they are active.
     * @param updateRunnable The Runnable to be executed each frame this behavior is active
     * @param label Label for this behavior
     */
    public UpdatingBehavior(
            Runnable updateRunnable,
            String label
    ) {
        this.updateRunnable = updateRunnable;
        this.label = label;
    }

    @Override
    public void enter() {
    }

    @Override
    public void update() {
        updateRunnable.run();
    }

    @Override
    public boolean isComplete() {
        return false;
    }

    @Override
    public void exit() {}

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public void processTelemetry(Telemetry telemetry, String prefix) {}
}
