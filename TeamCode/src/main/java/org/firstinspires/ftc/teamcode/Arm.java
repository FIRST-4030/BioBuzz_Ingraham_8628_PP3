package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.hardware.AnalogInput;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.HardwareMap;

public class Arm {

    DcMotor armHinge;
    AnalogInput armPotentiometer;

    // 90 degrees is defined as parallel to the ground.
    private final double COLLECTING_ARM_ANGLE_DEG = 60.0;
    private final double AIMING_ARM_ANGLE_DEG = 130.0;

    public enum ArmMode {
        COLLECTING,
        AIMING
    }

    private ArmMode armMode = ArmMode.COLLECTING;

    public Arm(HardwareMap hardwareMap) {
        armHinge = hardwareMap.get(DcMotor.class, "arm");
        armPotentiometer = hardwareMap.get(AnalogInput.class, "arm_potentiometer");
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
        double power = calculateHingePowerForAngle(currentArmAngleDegrees, AIMING_ARM_ANGLE_DEG);

        /*
        * Eventually, I want to: first check if there are April tags for the hive visible. If so,
        * instead of rotating toward the AIMING_ARM_ANGLE_DEG, rotate towards the april tag we want
        * to aim for using the Limelight.
        *
        * For now, just rotating towards the preset aiming angle will do!
        * */

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
