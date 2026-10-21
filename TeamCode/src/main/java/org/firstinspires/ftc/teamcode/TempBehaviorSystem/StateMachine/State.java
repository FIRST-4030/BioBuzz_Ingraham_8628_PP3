package org.firstinspires.ftc.teamcode.TempBehaviorSystem.StateMachine;

import org.firstinspires.ftc.teamcode.TempBehaviorSystem.Behavior;

/**
 * States are just behaviors that have a getNextState() method. They are intended for use in a StateMachine.
 * @author Edson James
 */
public interface State extends Behavior {
    /**
     * Should return the next State that should be active in the StateMachine and handle the logic
     * to decide that. If the active State should not change, this method should return its own State.
     *
     * IMPORTANT: This method should NOT have side effects that persist across multiple calls.
     * Avoid checking "one-shot" events (like gamepad.a.wasPressed()) here if they are consumed upon
     * calling. Instead, check those events in update() and store the result, or ensure this method
     * is only called once per loop by the owner (e.g. StateMachine).
     *
     * @return The next State that should be active.
     */
    State getNextState();

    /**
     * By default, states do not "complete." They are intended to transition to other states via
     * getNextState(), and the StateMachine itself handles the lifecycle.
     *
     * @return false.
     */
    @Override
    default boolean isComplete() {
        return false;
    }
}
