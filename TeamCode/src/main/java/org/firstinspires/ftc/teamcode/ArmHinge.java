package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.hardware.AnalogInput;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.util.Range;

public class ArmHinge {
    public DcMotor motor;
    public AnalogInput armPotentiometer;

    private static final double MIN_ANGLE_DEG = 50.0;
    private static final double MAX_ANGLE_DEG = 140.0;

    public ArmHinge(HardwareMap hardwareMap) {
        motor = hardwareMap.get(DcMotor.class, "arm");
        motor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        armPotentiometer = hardwareMap.get(AnalogInput.class, "arm_potentiometer");
    }

    public void driveArmToAngle(double angleDegrees) {
        double currentArmAngleDegrees = this.getArmAngleDegrees();
        double power = calculateHingePowerForAngle(currentArmAngleDegrees, angleDegrees);

        motor.setPower(power);
    }

    public void stop() {
        motor.setPower(0);
    }

    private double calculateHingePowerForAngle(double currentArmAngleDegrees, double goalArmAngleDegrees) {
        double clippedGoalArmAngleDegrees = Range.clip(goalArmAngleDegrees, MIN_ANGLE_DEG, MAX_ANGLE_DEG);

        double error = clippedGoalArmAngleDegrees - currentArmAngleDegrees;
        double kP = 0.04;
        double kG = 0.5; // Tuning parameter for gravity compensation

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

}
