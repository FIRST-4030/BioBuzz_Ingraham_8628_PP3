package org.firstinspires.ftc.teamcode.TempBehaviorSystem;

import org.firstinspires.ftc.robotcore.external.Telemetry;

import java.util.List;

/**
 * A behavior which runs a list of behaviors, one at a time, moving on to the next behavior
 * once the current one is complete. This behavior completes when the final behavior in the list
 * completes.
 * @author Edson James
 * @see ParallelGroup
 */
public class SequentialGroup implements Behavior {
    private final List<Behavior> behaviors;
    private int activeBehaviorIndex = 0;
    private final String label;

    public SequentialGroup(List<Behavior> behaviors, String label) {
        this.behaviors = behaviors;
        this.label = label;
    }

    /**
     * Resets the active behavior index to the first item, and enters the first behavior in the list.
     */
    @Override
    public void enter() {
        activeBehaviorIndex = 0;

        if (getActiveBehavior() != null) {
            getActiveBehavior().enter();
        }
    }

    /**
     * Updates the active behavior, and moves on to the next behavior if the active behavior is complete.
     */
    @Override
    public void update() {
        while (!isComplete() && getActiveBehavior() != null) {
            getActiveBehavior().update();

            if (getActiveBehavior().isComplete()) {
                nextBehavior();
                // Loop continues so that transitions between behaviors can occur in a single frame.
            } else {
                break;
            }
        }
    }

    /**
     * Returns whether the active behavior index is past the last item in the list of behaviors,
     * indicating that the last behavior in the list completed.
     * @return Whether the active behavior index is past the last item in the list of behaviors
     */
    @Override
    public boolean isComplete() {
        return ( activeBehaviorIndex >= behaviors.size() );
    }

    /**
     * Exits the active behavior if there is one, and resets the active behavior index to 0.
     */
    @Override
    public void exit() {
        if (getActiveBehavior() != null) {
            getActiveBehavior().exit();
        }
        activeBehaviorIndex = 0; // Maybe not necessary?
    }

    @Override
    public void processTelemetry(Telemetry telemetry, String prefix) {
        int currentMS = (int) (System.currentTimeMillis());

        for (int i = 0; i < behaviors.size(); i ++) {
            Behavior behavior = behaviors.get(i);

            // There's some crazy jazz here that just makes the arrows look pretty lol
            String listPrefix = "-     ";
            if (getActiveBehavior() == behavior) {
                if (currentMS % 500 > 250) {
                    listPrefix = "--->";
                } else {
                    listPrefix = "--> ";
                }
            }
            if (activeBehaviorIndex > i) {
                listPrefix = "✔   ";
            }
            telemetry.addLine(prefix + listPrefix + behavior.getLabel());

            if (getActiveBehavior() == behavior) {
                behavior.processTelemetry(telemetry, prefix + "    ");
            }
        }
    }

    @Override
    public String getLabel() {
//        return "SEQ: " + label;
        return label;
    }

    @Override
    public void processSimpleTelemetry(Telemetry telemetry, String prefix) {
        if (getActiveBehavior() != null) {
            getActiveBehavior().processSimpleTelemetry(telemetry, prefix);
        }
    }

    /**
     * Exits the active behavior, increments active behavior index by 1, and enters the next active
     * behavior if there is one.
     */
    private void nextBehavior() {
        if (getActiveBehavior() != null) {
            getActiveBehavior().exit();
        }
        activeBehaviorIndex += 1;

        if (isComplete()) { return; }

        if (getActiveBehavior() != null) {
            getActiveBehavior().enter();
        }
    }

    /**
     * Gets the active behavior, or null is there is none.
     * @return The active behavior.
     */
    private Behavior getActiveBehavior() {
        if (activeBehaviorIndex >= 0 && activeBehaviorIndex < behaviors.size()) {
            return behaviors.get(activeBehaviorIndex);
        }
        return null;
    }
}
