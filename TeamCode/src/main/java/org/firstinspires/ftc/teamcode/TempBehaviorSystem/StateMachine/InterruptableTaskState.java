package org.firstinspires.ftc.teamcode.TempBehaviorSystem.StateMachine;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.TempBehaviorSystem.Behavior;

import java.util.function.Supplier;

/**
 * Class used to easily create states that execute one behavior and then automatically switch to a
 * specified state once that behavior completes, without too much boilerplate.
 * @author Edson James
 * @see BaseState
 */
public class InterruptableTaskState implements State {
    private final Behavior behavior;
    private final Supplier<State> onCompleteNextStateSupplier;
    private final Supplier<State> interruptingNextStateSupplier;
    private final Supplier<String> additionalTelemetrySupplier;

    /**
     * Class used to easily create states that execute one behavior and then automatically switch to a
     * specified state once that behavior completes, but with the option to interrupt the behavior early
     * and switch to another state before it completes.
     * @param behavior The behavior to execute.
     * @param onCompleteNextStateSupplier The supplier for what the next state should be once the behavior
     *                                    completes.
     * @param interruptingNextStateSupplier State supplier for handling cases where the state should switch
     *                                   BEFORE this state has completed, such as a manual override from the
     *                                   user to cancel an action.
     */
    public InterruptableTaskState(Behavior behavior, Supplier<State> onCompleteNextStateSupplier, Supplier<State> interruptingNextStateSupplier) {
        this(behavior, onCompleteNextStateSupplier, interruptingNextStateSupplier, () -> (""));
    }

    /**
     * Class used to easily create states that execute one behavior and then automatically switch to a
     * specified state once that behavior completes, but with the option to interrupt the behavior early
     * and switch to another state before it completes.
     * @param behavior The behavior to execute.
     * @param onCompleteNextStateSupplier The supplier for what the next state should be once the behavior
     *                                    completes.
     * @param interruptingNextStateSupplier State supplier for handling cases where the state should switch
     *                                   BEFORE this state has completed, such as a manual override from the
     *                                   user to cancel an action.
     * @param additionalTelemetrySupplier A String supplier for additional telemetry that should be printed for this state
     */
    public InterruptableTaskState(Behavior behavior, Supplier<State> onCompleteNextStateSupplier, Supplier<State> interruptingNextStateSupplier, Supplier<String> additionalTelemetrySupplier) {
        this.behavior = behavior;
        this.onCompleteNextStateSupplier = onCompleteNextStateSupplier;
        this.interruptingNextStateSupplier = interruptingNextStateSupplier;
        this.additionalTelemetrySupplier = additionalTelemetrySupplier;
    }

    @Override
    public void enter() { behavior.enter(); }

    @Override
    public void update() { behavior.update(); }

    @Override
    public boolean isComplete() { return behavior.isComplete(); }

    @Override
    public State getNextState() {
        State interruptingNextState = interruptingNextStateSupplier.get();

        if (interruptingNextState != this) return interruptingNextState;
        if (behavior.isComplete()) return onCompleteNextStateSupplier.get();
        return this;
    }

    @Override
    public void exit() { behavior.exit(); }

    @Override
    public void processTelemetry(Telemetry telemetry, String prefix) {
        if (!additionalTelemetrySupplier.get().isEmpty()) {
            telemetry.addLine(prefix + additionalTelemetrySupplier.get());
            telemetry.addLine();
        }
        behavior.processTelemetry(telemetry, prefix);
    }

    @Override
    public void processSimpleTelemetry(Telemetry telemetry, String prefix) {
        if (!additionalTelemetrySupplier.get().isEmpty()) {
            telemetry.addLine(prefix + additionalTelemetrySupplier.get());
        }
        behavior.processSimpleTelemetry(telemetry, prefix);
    }

    /**
     * TaskStates use their root behavior's label as their own label.
     * @return The root behavior's label.
     */
    @Override
    public String getLabel() {
        return behavior.getLabel();
    }
}
