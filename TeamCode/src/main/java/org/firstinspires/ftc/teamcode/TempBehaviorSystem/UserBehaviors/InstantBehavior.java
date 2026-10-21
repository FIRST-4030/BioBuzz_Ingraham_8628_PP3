package org.firstinspires.ftc.teamcode.TempBehaviorSystem.UserBehaviors;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.TempBehaviorSystem.Behavior;

/**
 * Used to create behaviors that complete immediately using a Runnable instead of entire new
 * behavior implementation classes.
 * @author Edson James
 */
public class InstantBehavior implements Behavior {

    private final String label;
    private final Runnable actionRunnable;

    /**
     * Used to create behaviors that complete immediately using a Runnable instead of entire new
     * behavior implementation classes.
     * @param actionRunnable The Runnable for the instant action to be taken
     */
    public InstantBehavior(
            Runnable actionRunnable
    ) {
        this(
                actionRunnable,
                "Instant Behavior"
        );
    }

    /**
     * Used to create behaviors that complete immediately using a Runnable instead of entire new
     * behavior implementation classes.
     * @param actionRunnable The Runnable for the instant action to be taken
     * @param label Label for this behavior
     */
    public InstantBehavior(
            Runnable actionRunnable,
            String label
    ) {
        this.actionRunnable = actionRunnable;
        this.label = label;
    }

    @Override
    public void enter() {
        actionRunnable.run();
    }

    @Override
    public void update() {}

    @Override
    public boolean isComplete() {
        return true;
    }

    @Override
    public void exit() {}

    @Override
    public String getLabel() {
        return label;
    }

    // There's nothing to print because this behavior is never active for more than a frame
    @Override
    public void processTelemetry(Telemetry telemetry, String prefix) {}
}
