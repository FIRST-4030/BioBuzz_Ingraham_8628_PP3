package org.firstinspires.ftc.teamcode.TempBehaviorSystem.StateMachine;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.TempBehaviorSystem.Behavior;

import java.util.function.Supplier;

/**
 * Class used to easily create states that execute one behavior, while passing a state supplier
 * to the getNextState() method to easily define control flow without too much boilerplate.
 * @author Edson James
 * @see TaskState
 */
public class BaseState implements State {
    private final Behavior behavior;
    private final Supplier<State> nextStateSupplier;
    private final Supplier<String> additionalTelemetrySupplier;

    /**
     * Class used to easily create states that execute one behavior, while passing a state supplier
     * to the getNextState() method to easily define control flow without too much boilerplate.
     * @param behavior The behavior to execute.
     * @param nextStateSupplier The supplier for what the next state should be.
     */
    public BaseState(Behavior behavior, Supplier<State> nextStateSupplier) {
        this(behavior, nextStateSupplier, () -> (""));
    }

    /**
     * Class used to easily create states that execute one behavior, while passing a state supplier
     * to the getNextState() method to easily define control flow without too much boilerplate.
     * @param behavior The behavior to execute.
     * @param nextStateSupplier The supplier for what the next state should be.
     * @param additionalTelemetrySupplier A String supplier for additional telemetry that should be printed for this state
     */
    public BaseState(Behavior behavior, Supplier<State> nextStateSupplier, Supplier<String> additionalTelemetrySupplier) {
        this.behavior = behavior;
        this.nextStateSupplier = nextStateSupplier;
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
        return nextStateSupplier.get();
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
     * BaseStates use their root behavior's label as their own label.
     * @return The root behavior's label.
     */
    @Override
    public String getLabel() {
        return behavior.getLabel();
    }
}
