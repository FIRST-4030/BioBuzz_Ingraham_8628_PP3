package org.firstinspires.ftc.teamcode.TempBehaviorSystem.UserBehaviors;

import com.qualcomm.robotcore.hardware.Gamepad;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.TempBehaviorSystem.Behavior;
import org.firstinspires.ftc.teamcode.Chassis;

/**
 * Behavior for robot-centric driving using a gamepad. Never completes.
 * @author Edson James
 */
public class GamepadDrive implements Behavior {
    private final Chassis chassis;
    private final Gamepad gamepad;
    private final String label;

    /**
     * Behavior for robot-centric driving using a gamepad. Never completes.
     * @param chassis The chassis instance to use for driving.
     * @param gamepad The gamepad to control driving with.
     */
    public GamepadDrive(Chassis chassis, Gamepad gamepad) {
        this(chassis, gamepad, "Gamepad Drive");
    }

    /**
     * Behavior for robot-centric driving using a gamepad. Never completes.
     * @param chassis The chassis instance to use for driving.
     * @param gamepad The gamepad to control driving with.
     * @param label The label for this behavior.
     */
    public GamepadDrive(Chassis chassis, Gamepad gamepad, String label) {
        this.chassis = chassis;
        this.gamepad = gamepad;
        this.label = label;
    }

    /**
     * Turns braking on for the driving motors.
     */
    @Override
    public void enter() {
        chassis.resetZeroPowerBehavior();
    }

    /**
     * Drives the robot using the gamepad's left stick and right stick
     */
    @Override
    public void update() {
        chassis.drive(-gamepad.left_stick_y, gamepad.left_stick_x, gamepad.right_stick_x);
    }

    /**
     * Always returns false; this Behavior does not have a "completed" state.
     * @return false.
     */
    @Override
    public boolean isComplete() {
        return false;
    }

    /**
     * Stops driving motors.
     */
    @Override
    public void exit() {
        chassis.stopMotors();
    }

    @Override
    public void processTelemetry(Telemetry telemetry, String prefix) {
        telemetry.addLine(prefix + "(Gamepad driving...)");
    }

    @Override
    public String getLabel() {
        return label;
    }
}
