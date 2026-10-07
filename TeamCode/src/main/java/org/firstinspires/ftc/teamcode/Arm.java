package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.hardware.AnalogInput;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.util.Range;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;

public class Arm {

    DcMotor armHinge;
    DcMotor collectorWheel;
    DcMotor shooterWheel;
    AnalogInput armPotentiometer;
    Limelight limelight;

    // 90 degrees is defined as parallel to the ground.
    private static final double COLLECTING_ARM_ANGLE_DEG = 70.0;
    private static final double AIMING_ARM_ANGLE_DEG = 130.0;

    private static final double MIN_ANGLE_DEG = 60.0;
    private static final double MAX_ANGLE_DEG = 180.0;

    public enum ArmPositionMode {
        COLLECTING,
        AIMING
    }

    private ArmPositionMode armPositionMode = ArmPositionMode.COLLECTING;

    public Arm(HardwareMap hardwareMap, Limelight limelight) {
        armHinge = hardwareMap.get(DcMotor.class, "arm");
        armHinge.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

//        collectorWheel = hardwareMap.get(DcMotor.class, "collector");
//        shooterWheel = hardwareMap.get(DcMotor.class, "shooter");

        armPotentiometer = hardwareMap.get(AnalogInput.class, "arm_potentiometer");

        this.limelight = limelight;
    }

    public void update() {
        if (!isPotentiometerConnected()) {
            armHinge.setPower(0);
            return;
        }

        switch (armPositionMode) {
            case COLLECTING:
                collectingModeUpdate();
                break;
            case AIMING:
                aimingModeUpdate();
                break;
        }
    }

    private void collectingModeUpdate() {
        double currentArmAngleDegrees = this.getArmAngleDegrees();
        double power = calculateHingePowerForAngle(currentArmAngleDegrees, COLLECTING_ARM_ANGLE_DEG);

        armHinge.setPower(power);
    }

    private void aimingModeUpdate() {
        double currentArmAngleDegrees = this.getArmAngleDegrees();
        double goalArmAngleDegrees = AIMING_ARM_ANGLE_DEG;

        if (limelight != null && limelight.resultHasGoodTargets()) {
            double angleOffset = limelight.getYError();
            goalArmAngleDegrees = currentArmAngleDegrees + angleOffset;
        }

        double power = calculateHingePowerForAngle(currentArmAngleDegrees, goalArmAngleDegrees);
        armHinge.setPower(power);
    }

    // --- Arm ---

    private double calculateHingePowerForAngle(double currentArmAngleDegrees, double goalArmAngleDegrees) {
        double clippedGoalArmAngleDegrees = Range.clip(goalArmAngleDegrees, MIN_ANGLE_DEG, MAX_ANGLE_DEG);

        return Range.clip((clippedGoalArmAngleDegrees - currentArmAngleDegrees) / 20, -1.0, 1.0);

        // TODO: Want to try doing it this way too:
//        double error = clippedGoalArmAngleDegrees - currentArmAngleDegrees;
//        double kP = 0.03;
//        double kG = 0.15; // Tuning parameter for gravity compensation
//
//        // Angle is relative to ground (90° = horizontal)
//        double feedForward = kG * Math.cos(Math.toRadians(currentArmAngleDegrees));
//        double power = (error * kP) + feedForward;
//        return Range.clip(power, -1.0, 1.0);
    }

    public double getArmAngleDegrees() {
        // TODO: This equation is wrong now!!!
        return 109.11623 * getPotentiometerVoltage() + 40.01092;
    }

    public boolean isPotentiometerConnected() {
        double v = getPotentiometerVoltage();
        return v > 0.05 && v < 3.25;
    }

    public double getPotentiometerVoltage() {
        return armPotentiometer.getVoltage();
    }

    public ArmPositionMode getArmPositionMode() {
        return armPositionMode;
    }

    public void setArmPositionMode(ArmPositionMode armPositionMode) {
        this.armPositionMode = armPositionMode;
    }

    public void collectingPositionMode() {
        this.armPositionMode = ArmPositionMode.COLLECTING;
    }

    public void aimingPositionMode() {
        this.armPositionMode = ArmPositionMode.AIMING;
    }

    // ONLY USE FOR DEBUGGING/CALIBRATION OPMODES!!
    public void unsafePowerArmHinge(double power) {
        armHinge.setPower(power);
    }

    // --- Collector and shooter ---

    public void shootingUpdate() {
        double shooterPowerToReach = 1;

        // Look into how this was done in the old shooter class!
    }
}
