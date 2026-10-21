package org.firstinspires.ftc.teamcode.TempBehaviorSystem.UserBehaviors;

import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.TempBehaviorSystem.Behavior;

/**
 * Behavior that completes after a given duration in milliseconds. Keep in mind, you must call enter()
 * on this behavior before it can ever complete.
 * @author Edson James
 */
public class WaitMS implements Behavior {
    private final ElapsedTime elapsedTime = new ElapsedTime();
    private final double waitTimeMS;
    private boolean entered = false;
    private final String label;

    /**
     * Behavior that completes after a given duration in milliseconds. Keep in mind, you must call enter()
     * on this behavior before it can ever complete.
     * @param waitTimeMS How many milliseconds must elapse since the behavior starts before it completes.
     */
    public WaitMS(double waitTimeMS) {
        this(waitTimeMS, "Wait MS");
    }

    /**
     * Behavior that completes after a given duration in milliseconds. Keep in mind, you must call enter()
     * on this behavior before it can ever complete.
     * @param waitTimeMS How many milliseconds must elapse since the behavior starts before it completes.
     * @param label The label for this behavior.
     */
    public WaitMS(double waitTimeMS, String label) {
        this.waitTimeMS = waitTimeMS;
        this.label = label;
    }

    /**
     * Returns the current time remaining in milliseconds.
     * @return the current time remaining in milliseconds.
     */
    double getTimeRemainingMS() {
        return waitTimeMS - elapsedTime.milliseconds();
    }

    /**
     * Resets the timer.
     */
    @Override
    public void enter() {
        elapsedTime.reset();
        entered = true;
    }

    @Override
    public void update() {}

    /**
     * Returns whether the given duration has elapsed.
     * @return whether the given duration has elapsed.
     */
    @Override
    public boolean isComplete() {
        return (elapsedTime.milliseconds() >= waitTimeMS && entered);
    }

    @Override
    public void exit() {
        entered = false;
    }

    @Override
    public void processTelemetry(Telemetry telemetry, String prefix) {
        if (entered) {
            telemetry.addLine(prefix + "(MS remaining: " + Math.floor(getTimeRemainingMS()) + " MS)");
        } else {
            telemetry.addLine(prefix + "(Has not begun waiting yet)");
        }
    }

    @Override
    public String getLabel() {
        return label;
    }
}
