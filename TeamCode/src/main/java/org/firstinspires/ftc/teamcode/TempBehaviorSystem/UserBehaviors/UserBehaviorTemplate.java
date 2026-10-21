package org.firstinspires.ftc.teamcode.TempBehaviorSystem.UserBehaviors;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.TempBehaviorSystem.Behavior;

/**
 * Template for easily creating new behaviors. You can duplicate this class, rename it, and modify it
 * to your liking to quickly make a new robot capability.
 * @author Edson James
 */
public class UserBehaviorTemplate implements Behavior {

    // Custom label variable so users can label each instance of the behavior differently.
    // Probably don't touch this.
    private final String label;

    // You want this first constructor to take everything the full constructor takes EXCEPT for the label.
    // Then, this constructor can just call your full constructor with a hard-coded default label.
    public UserBehaviorTemplate() {
        this("User behavior");
    }

    // This is the full constructor, INCLUDING a parameter for the label.
    public UserBehaviorTemplate(String label) {
        this.label = label;
    }

    // enter() is called once when the behavior starts. It is where you store all the logic for when
    // your behavior first becomes "active."
    @Override
    public void enter() {

    }

    // update() is called repeatedly in the main loop when the behavior is active. It is where you
    // store all the logic for what your behavior should do every frame.
    @Override
    public void update() {

    }

    // isComplete() returns a boolean (true or false) value. It's used mostly in sequences or parallel
    // behaviors to decide when to move on to the next step. It's also used in TaskStates to decide when
    // to switch states. Some behaviors, such as WaitMS, have a well-defined "completed" condition.
    // Other behaviors, such as GamepadDrive, don't really have "completed" conditions
    // and thus should always return false here.
    @Override
    public boolean isComplete() {
        return false;
    }

    // exit() is where you store all the logic for when your behavior "ends." Cleaning up. For example,
    // GamepadDrive stops all driving motors in exit().
    @Override
    public void exit() {

    }

    // This is where you print information about what your behavior is doing.
    @Override
    public void processTelemetry(Telemetry telemetry, String prefix) {
        // ALWAYS make sure to add prefix to the beginning of new lines you print to telemetry.
        // The prefix parameter is used to nest behavior telemetry messages on the screen using
        // indentation, so it makes things look broken if you forget to add it to the beginning.
        telemetry.addLine(prefix + "(What is your behavior doing?)");
    }

    // Probably don't touch this; this method is what allows users to only optionally specify a custom
    // label for instances of this behavior.
    @Override
    public String getLabel() {
        return label;
    }
}
