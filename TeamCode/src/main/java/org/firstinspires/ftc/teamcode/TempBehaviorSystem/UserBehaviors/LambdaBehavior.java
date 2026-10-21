package org.firstinspires.ftc.teamcode.TempBehaviorSystem.UserBehaviors;

import org.firstinspires.ftc.robotcore.external.Supplier;
import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.TempBehaviorSystem.Behavior;

/**
 * Used to create unique behaviors using lambdas instead of entire new behavior implementation
 * classes.
 * @author Edson James
 */
public class LambdaBehavior implements Behavior {

    private final String label;

    private final Runnable enterRunnable;
    private final Runnable updateRunnable;
    private final Supplier<Boolean> isCompleteSupplier;
    private final Runnable exitRunnable;
    private final Supplier<String> telemetrySupplier;

    /**
     * Used to create unique behaviors using lambdas instead of entire new behavior implementation
     * classes.
     * @param enterRunnable The Runnable that executes when the behavior enters.
     * @param updateRunnable The Runnable that executes each frame the behavior is active.
     * @param isCompleteSupplier Boolean Supplier for if the behavior is complete.
     * @param exitRunnable The Runnable that executes when the behavior exits.
     */
    public LambdaBehavior(
            Runnable enterRunnable,
            Runnable updateRunnable,
            Supplier<Boolean> isCompleteSupplier,
            Runnable exitRunnable
    ) {
        this(
                enterRunnable,
                updateRunnable,
                isCompleteSupplier,
                exitRunnable,
                () -> "",
                "Lambda Behavior"
        );
    }

    /**
     * Used to create unique behaviors using lambdas instead of entire new behavior implementation
     * classes.
     * @param enterRunnable The Runnable that executes when the behavior enters.
     * @param updateRunnable The Runnable that executes each frame the behavior is active.
     * @param isCompleteSupplier Boolean Supplier for if the behavior is complete.
     * @param exitRunnable The Runnable that executes when the behavior exits.
     * @param telemetrySupplier String Supplier for the telemetry output of the behavior
     */
    public LambdaBehavior(
            Runnable enterRunnable,
            Runnable updateRunnable,
            Supplier<Boolean> isCompleteSupplier,
            Runnable exitRunnable,
            Supplier<String> telemetrySupplier
    ) {
        this(
                enterRunnable,
                updateRunnable,
                isCompleteSupplier,
                exitRunnable,
                telemetrySupplier,
                "Lambda Behavior"
        );
    }

    /**
     * Used to create unique behaviors using lambdas instead of entire new behavior implementation
     * classes.
     * @param enterRunnable The Runnable that executes when the behavior enters.
     * @param updateRunnable The Runnable that executes each frame the behavior is active.
     * @param isCompleteSupplier Boolean Supplier for if the behavior is complete.
     * @param exitRunnable The Runnable that executes when the behavior exits.
     * @param telemetrySupplier String Supplier for the telemetry output of the behavior
     * @param label Label for this behavior
     */
    public LambdaBehavior(
            Runnable enterRunnable,
            Runnable updateRunnable,
            Supplier<Boolean> isCompleteSupplier,
            Runnable exitRunnable,
            Supplier<String> telemetrySupplier,
            String label
    ) {
        this.enterRunnable = enterRunnable;
        this.updateRunnable = updateRunnable;
        this.isCompleteSupplier = isCompleteSupplier;
        this.exitRunnable = exitRunnable;
        this.telemetrySupplier = telemetrySupplier;
        this.label = label;
    }

    @Override
    public void enter() {
        enterRunnable.run();
    }

    @Override
    public void update() {
        updateRunnable.run();
    }

    @Override
    public boolean isComplete() {
        return isCompleteSupplier.get();
    }

    @Override
    public void exit() {
        exitRunnable.run();
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public void processTelemetry(Telemetry telemetry, String prefix) {
        String telemetrySupplierValue = telemetrySupplier.get();

        if (!telemetrySupplierValue.isEmpty()) {
            telemetry.addLine(prefix + telemetrySupplierValue);
        }
    }
}
