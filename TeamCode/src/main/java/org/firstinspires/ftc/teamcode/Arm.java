package org.firstinspires.ftc.teamcode;

import com.pedropathing.ivy.Command;
import com.qualcomm.robotcore.hardware.AnalogInput;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.Range;

public class Arm {

    public DcMotor armHinge;
    public AnalogInput armPotentiometer;
    public Flywheel shooterFlywheel;
    public Flywheel flipperFlywheel;
    public Servo gateServo;
    Limelight limelight;

    private boolean reachedAimingHeight = false;
    private boolean reachedReadyToShoot = false;

    // TODO: THIS IS TEMPORARY
    public double tempShootPower = 0.75;
    public boolean ejectOverride = false;

    // 90 degrees is defined as parallel to the ground.
    private static final double COLLECTING_ARM_ANGLE_DEG = 50.0;
    private static final double AIMING_ARM_ANGLE_DEG = 120.0;

    private static final double MIN_ANGLE_DEG = 50.0;
    private static final double MAX_ANGLE_DEG = 140.0;

    private static final double FLIPPER_COLLECT_VELOCITY = 1; // TODO: What should this velocity be?
    private static final double FLIPPER_SHOOT_VELOCITY = 1; // TODO: What should this velocity be?

    private static final double SERVO_CLOSED_POSITION = 0.4;
    private static final double SERVO_OPEN_POSITION = 0.0;

    public Arm(HardwareMap hardwareMap, Limelight limelight) {
        armHinge = hardwareMap.get(DcMotor.class, "arm");
        armHinge.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        armPotentiometer = hardwareMap.get(AnalogInput.class, "arm_potentiometer");

        // Both are using default controller values for now, but can be later changed with
        // MotorVelocityController.setControllerValues()
        flipperFlywheel = new Flywheel(hardwareMap, "flipper", DcMotorSimple.Direction.REVERSE);
        shooterFlywheel = new Flywheel(hardwareMap, "shooter", DcMotorSimple.Direction.REVERSE);
        gateServo = hardwareMap.get(Servo.class, "gate_servo");

        this.limelight = limelight;
    }

    // --- ARM ---

    private double calculateHingePowerForAngle(double currentArmAngleDegrees, double goalArmAngleDegrees) {
        double clippedGoalArmAngleDegrees = Range.clip(goalArmAngleDegrees, MIN_ANGLE_DEG, MAX_ANGLE_DEG);

        double error = clippedGoalArmAngleDegrees - currentArmAngleDegrees;
        double kP = 0.07;
        double kG = 0.3; // Tuning parameter for gravity compensation

        // Angle is relative to ground (90° = horizontal)
        double feedForward = kG * Math.cos(Math.toRadians(currentArmAngleDegrees));
        double power = (error * kP) + feedForward;
        return Range.clip(power, -1.0, 1.0);
    }

    public double getArmAngleDegrees() {
        return 109.11623 * getPotentiometerVoltage() + 40.01092;
    }

    public boolean isPotentiometerConnected() {
        double v = getPotentiometerVoltage();
        return v > 0.05 && v < 3.25;
    }

    public double getPotentiometerVoltage() {
        return armPotentiometer.getVoltage();
    }

    // ONLY USE FOR DEBUGGING/CALIBRATION OPMODES!!
    // This does not limit the range of motion of the hinge
    public void unsafePowerArmHinge(double power) {
        armHinge.setPower(power);
    }

    // --- SHOOTER AND FLIPPER ---

    private void handleFlipperFlywheel() {
        if (ejectOverride) {
            flipperFlywheel.setTargetVelocity(-FLIPPER_COLLECT_VELOCITY);
            return;
        }
    }

    public boolean readyToShoot() {
        return (reachedAimingHeight &&
                limelight.isOnTarget() &&
                shooterFlywheel.atSpeed());
    }

    public void closeGate() { gateServo.setPosition(
            SERVO_CLOSED_POSITION
    ); }

    public void openGate() { gateServo.setPosition(
            SERVO_OPEN_POSITION
    ); }
}
