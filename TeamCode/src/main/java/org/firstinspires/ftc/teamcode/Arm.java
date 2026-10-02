package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.hardware.AnalogInput;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.HardwareMap;

public class Arm {

    DcMotor armHinge;
    AnalogInput armPotentiometer;
    Limelight limelight;

    // 90 degrees is defined as parallel to the ground.
    private final double COLLECTING_ARM_ANGLE_DEG = 60.0;
    private final double AIMING_ARM_ANGLE_DEG = 130.0;

    private double currentAimingAngleOffset = 0;

    public enum ArmMode {
        COLLECTING,
        AIMING
    }

    private ArmMode armMode = ArmMode.COLLECTING;

    public Arm(HardwareMap hardwareMap, Limelight limelight) {
        armHinge = hardwareMap.get(DcMotor.class, "arm");
        armPotentiometer = hardwareMap.get(AnalogInput.class, "arm_potentiometer");
        this.limelight = limelight;
    }

    public void update() {
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

        if (limelight.hasValidResult()) {
            currentAimingAngleOffset += limelight.getYError() * 0.5;

            if (AIMING_ARM_ANGLE_DEG + currentAimingAngleOffset > 170) {
                currentAimingAngleOffset = 170 - AIMING_ARM_ANGLE_DEG;
            }

            if (AIMING_ARM_ANGLE_DEG + currentAimingAngleOffset < 90) {
                currentAimingAngleOffset = 90 - AIMING_ARM_ANGLE_DEG;
            }
        } else {
            currentAimingAngleOffset *= 0.95;
        }

        double power = calculateHingePowerForAngle(currentArmAngleDegrees, AIMING_ARM_ANGLE_DEG + currentAimingAngleOffset);

        armHinge.setPower(power);
    }

    // Internal tools

    private double calculateHingePowerForAngle(double currentArmAngleDegrees, double goalArmAngleDegrees) {
        // TODO: Better feedforward + feedback equation for calculating motor power, not just a lerp
        return (goalArmAngleDegrees - currentArmAngleDegrees) / 20;
    }

    // Getters/setters

    public double getCurrentArmAngleDegrees() {
        return 106.57316 * armPotentiometer.getVoltage() + 27.68994;
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
