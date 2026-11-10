package org.firstinspires.ftc.teamcode.Hardware;

import com.qualcomm.robotcore.hardware.AnalogInput;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.util.Range;

public class ArmHinge {
    public DcMotor motor;
    public AnalogInput armPotentiometer;

    public static final double MIN_ANGLE_DEG = 55.0;
    public static final double MAX_ANGLE_DEG = 125.0;  // TODO: Check angle at 29" arm height

    public double kP = 0.04;
    public double kG = 0.28;

    public double targetAngle = 90;

    public ArmHinge(HardwareMap hardwareMap) {
        motor = hardwareMap.get(DcMotor.class, "arm");
        motor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        armPotentiometer = hardwareMap.get(AnalogInput.class, "arm_potentiometer");
    }

    public void setTargetAngleDegrees(double targetAngle) {
        this.targetAngle = targetAngle;
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

}
