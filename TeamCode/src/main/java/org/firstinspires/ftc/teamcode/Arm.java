package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.hardware.AnalogInput;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.util.Range;

public class Arm {

    DcMotor armHinge;
    AnalogInput armPotentiometer;
    Limelight limelight;

    // 90 degrees is defined as parallel to the ground.
    private final double COLLECTING_ARM_ANGLE_DEG = 60.0;
    private final double AIMING_ARM_ANGLE_DEG = 130.0;

    private final double MIN_ANGLE_DEG = 60.0;
    private final double MAX_ANGLE_DEG = 180.0;

    public enum ArmMode {
        COLLECTING,
        AIMING
    }

    private ArmMode armMode = ArmMode.COLLECTING;

    public Arm(HardwareMap hardwareMap, Limelight limelight) {
        armHinge = hardwareMap.get(DcMotor.class, "arm");
        armHinge.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        armPotentiometer = hardwareMap.get(AnalogInput.class, "arm_potentiometer");
        this.limelight = limelight;
    }

    public void update() {
        if (!isPotentiometerConnected()) {
            armHinge.setPower(0);
            return;
        }

        switch (armMode) {
            case COLLECTING:
                collectingModeUpdate();
                break;
            case AIMING:
                aimingModeUpdate();
                break;
        }
    }

    private void collectingModeUpdate() {
        double currentArmAngleDegrees = this.getCurrentArmAngleDegrees();
        double power = calculateHingePowerForAngle(currentArmAngleDegrees, COLLECTING_ARM_ANGLE_DEG);

        armHinge.setPower(power);
    }

    private void aimingModeUpdate() {
        double currentArmAngleDegrees = this.getCurrentArmAngleDegrees();
        double goalArmAngleDegrees = AIMING_ARM_ANGLE_DEG;

        double angleOffset = 0;

        if (limelight != null && limelight.latestResultIsValid()) {
            angleOffset = limelight.getYError();
            goalArmAngleDegrees = currentArmAngleDegrees + angleOffset;
        }

        double power = calculateHingePowerForAngle(currentArmAngleDegrees, goalArmAngleDegrees);
        armHinge.setPower(power);
    }

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

    public double getCurrentArmAngleDegrees() {
        return 106.57316 * getPotentiometerVoltage() + 27.68994;
    }

    public boolean isPotentiometerConnected() {
        double v = getPotentiometerVoltage();
        return v > 0.05 && v < 3.25;
    }

    public double getPotentiometerVoltage() {
        return armPotentiometer.getVoltage();
    }

    public ArmMode getArmMode() {
        return armMode;
    }

    public void setArmMode(ArmMode armMode) {
        this.armMode = armMode;
    }

    public void activateCollectingMode() {
        this.armMode = ArmMode.COLLECTING;
    }

    public void activateAimingMode() {
        this.armMode = ArmMode.AIMING;
    }
}
