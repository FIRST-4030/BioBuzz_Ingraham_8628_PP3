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
public class TaskState implements State {
    private final Behavior behavior;
    private final Supplier<State> onCompleteNextStateSupplier;
    private final Supplier<String> additionalTelemetrySupplier;

    /**
     * Class used to easily create states that execute one behavior and then automatically switch to a
     * specified state once that behavior completes, without too much boilerplate.
     * @param behavior The behavior to execute.
     * @param onCompleteNextStateSupplier The supplier for what the next state should be once the behavior
     *                                    completes.
     */
    public TaskState(Behavior behavior, Supplier<State> onCompleteNextStateSupplier) {
        this(behavior, onCompleteNextStateSupplier, () -> (""));
    }

    /**
     * Class used to easily create states that execute one behavior and then automatically switch to a
     * specified state once that behavior completes, without too much boilerplate.
     * @param behavior The behavior to execute.
     * @param onCompleteNextStateSupplier The supplier for what the next state should be once the behavior
     *                                    completes.
     * @param additionalTelemetrySupplier A String supplier for additional telemetry that should be printed for this state
     */
    public TaskState(Behavior behavior, Supplier<State> onCompleteNextStateSupplier, Supplier<String> additionalTelemetrySupplier) {
        this.behavior = behavior;
        this.onCompleteNextStateSupplier = onCompleteNextStateSupplier;
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
