package org.firstinspires.ftc.teamcode.TempBehaviorSystem.UserBehaviors;
import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.TempBehaviorSystem.Behavior;

import java.util.function.BooleanSupplier;

/**
 * Behavior that completes when a given condition (a BooleanSupplier) returns true. Could be used
 * as the first behavior in a parallelGroup in conjunction with other behaviors for action to be
 * taken while waiting for a condition.
 * @author Edson James
 */
public class WaitUntil implements Behavior {
    private final BooleanSupplier condition;
    private final String label;

    /**
     * Behavior that completes when a given condition (a BooleanSupplier) returns true. Could be used
     * as the first behavior in a parallelGroup in conjunction with other behaviors for action to be
     * taken while waiting for a condition.
     * @param condition The BooleanSupplier that reflects the condition you want to wait for.
     */
    public WaitUntil(BooleanSupplier condition) {
        this(condition, "Wait Until");
    }

    /**
     * Behavior that completes when a given condition (a BooleanSupplier) returns true. Could be used
     * as the first behavior in a parallelGroup in conjunction with other behaviors for action to be
     * taken while waiting for a condition.
     * @param condition The BooleanSupplier that reflects the condition you want to wait for.
     * @param label The label for this behavior.
     */
    public WaitUntil(BooleanSupplier condition, String label) {
        this.condition = condition;
        this.label = label;
    }

    @Override
    public void enter() {}

    @Override
    public void update() {}

    /**
     * Returns whether the condition is true.
     * @return Whether the condition is true.
     */
    @Override
    public boolean isComplete() {
        return condition.getAsBoolean();
    }

    @Override
    public void exit() {};

    @Override
    public void processTelemetry(Telemetry telemetry, String prefix) {
        telemetry.addLine(prefix + "(Waiting for condition...)");
    }

    @Override
    public String getLabel() {
        return label;
    }
}
