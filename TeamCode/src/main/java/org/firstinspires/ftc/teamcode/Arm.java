package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.hardware.AnalogInput;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.Range;

public class Arm {

    DcMotor armHinge;
    AnalogInput armPotentiometer;
    Limelight limelight;

    // 90 degrees is defined as parallel to the ground.
    private final double COLLECTING_ARM_ANGLE_DEG = 60.0;
    private final double AIMING_ARM_ANGLE_DEG = 130.0;

    private ElapsedTime deltaTimer = new ElapsedTime();
    private double lastTime = 0.0;

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
        double currentTime = deltaTimer.seconds();
        double deltaTime = currentTime - lastTime;

        switch (armMode) {
            case COLLECTING:
                collectingModeUpdate();
                break;
            case AIMING:
                aimingModeUpdate();
                break;
        }

        lastTime = currentTime;
    }

    private void collectingModeUpdate() {
        double currentArmAngleDegrees = this.getCurrentArmAngleDegrees();
        double power = calculateHingePowerForAngle(currentArmAngleDegrees, COLLECTING_ARM_ANGLE_DEG);

        armHinge.setPower(power);
    }

    private void aimingModeUpdate() {
        double currentArmAngleDegrees = this.getCurrentArmAngleDegrees();

        double angleOffset = 0;
//        if (limelight.hasValidResult()) {
//            // TODO: Does this even work?????????
//            angleOffset = limelight.getYError() + (currentArmAngleDegrees - AIMING_ARM_ANGLE_DEG);
//        }

        double goalArmAngleDegrees = AIMING_ARM_ANGLE_DEG + angleOffset;
        double power = calculateHingePowerForAngle(currentArmAngleDegrees, goalArmAngleDegrees);
        armHinge.setPower(power);
    }

    // Internal tools

    private double calculateHingePowerForAngle(double currentArmAngleDegrees, double goalArmAngleDegrees) {
        // TODO: Better feedforward + feedback equation for calculating motor power, not just a lerp
        return Range.clip((goalArmAngleDegrees - currentArmAngleDegrees) / 20, -1.0, 1.0);
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
