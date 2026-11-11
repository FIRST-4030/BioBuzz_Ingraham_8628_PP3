package org.firstinspires.ftc.teamcode.Hardware;

import static com.pedropathing.ivy.commands.Commands.infinite;

import com.pedropathing.ivy.Command;
import com.qualcomm.robotcore.hardware.AnalogInput;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.util.Range;

public class ArmHinge {

    private DcMotor motor;
    private AnalogInput armPotentiometer;
    private Limelight limelight;

    public static final double MIN_ANGLE_DEG = 55.0;
    public static final double MAX_ANGLE_DEG = 125.0;  // TODO: Check angle at 29" arm height

    // 90 degrees is defined as parallel to the ground.
    public static final double COLLECTING_ARM_ANGLE_DEG = 50.0;
    public static final double AIMING_ARM_ANGLE_DEG = 118.0;
    public static final double FLOWER_ARM_ANGLE_DEG = 125.0;

    public double kP = 0.04;
    public double kG = 0.28;

    private double targetAngle = MIN_ANGLE_DEG;
    private boolean lookingForHive = false;

    public ArmHinge(HardwareMap hardwareMap, Limelight limelight) {
        motor = hardwareMap.get(DcMotor.class, "arm");
        motor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        armPotentiometer = hardwareMap.get(AnalogInput.class, "arm_potentiometer");
        this.limelight = limelight;
    }

    public void update() {
        if (!isPotentiometerConnected()) {
            stop();
            return;
        }

        if (lookingForHive) {
            setTargetAngleDegrees(calculateHiveFollowingTargetAngle());
        }

        driveArmToTargetAngle();
    }

    public void moveToCollectingPosition() {
        lookingForHive = false;
        targetAngle = COLLECTING_ARM_ANGLE_DEG;
    }

    public void moveToFlowerPosition() {
        lookingForHive = false;
        targetAngle = FLOWER_ARM_ANGLE_DEG;
    }

    public void lookAtHive() {
        lookingForHive = true;
    }

    public double getClippedTargetAngleDegrees() {
        return Range.clip(this.targetAngle, MIN_ANGLE_DEG, MAX_ANGLE_DEG);
    }

    public void driveArmToTargetAngle() {
        double currentArmAngleDegrees = this.getArmAngleDegrees();
        double power = calculateHingePowerForAngle(currentArmAngleDegrees, targetAngle);

        motor.setPower(power);
    }

    public void stop() {
        motor.setPower(0);
    }

    private double calculateHingePowerForAngle(double currentArmAngleDegrees, double goalArmAngleDegrees) {
        double clippedGoalArmAngleDegrees = Range.clip(goalArmAngleDegrees, MIN_ANGLE_DEG, MAX_ANGLE_DEG);

        double error = clippedGoalArmAngleDegrees - currentArmAngleDegrees;

        // Angle is relative to ground (90° = horizontal)
        double feedForward = kG * Math.cos(Math.toRadians(currentArmAngleDegrees - 90));
        double power = (error * kP) + feedForward;
        return Range.clip(power, -1.0, 1.0);
    }

    private double calculateHiveFollowingTargetAngle() {
        double currentArmAngleDegrees = getArmAngleDegrees();
        double targetAngleDegrees = AIMING_ARM_ANGLE_DEG;

        if (limelight != null && limelight.resultHasGoodTargets() && getArmAngleDegrees() > 100) { // TODO: > 100°? Is this okay?
            double angleOffset = limelight.getYError();
            targetAngleDegrees = currentArmAngleDegrees + angleOffset;
        }

        return targetAngleDegrees;
    }

    public double getArmAngleDegrees() {
        return 109.11623 * getPotentiometerVoltage() + 40.01092;
    }

    public boolean isLookingForHive() {
        return lookingForHive;
    }

    public void setTargetAngleDegrees(double targetAngle) {
        this.targetAngle = targetAngle;
    }

    public boolean isPotentiometerConnected() {
        double v = getPotentiometerVoltage();
        return v > 0.05 && v < 3.25;
    }

    public double getPotentiometerVoltage() {
        return armPotentiometer.getVoltage();
    }

    // --- Ivy ---

    public Command periodic() {
        return infinite(this::update);
    }
}
