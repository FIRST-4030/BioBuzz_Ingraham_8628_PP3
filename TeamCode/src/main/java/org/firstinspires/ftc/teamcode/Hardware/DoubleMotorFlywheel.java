package org.firstinspires.ftc.teamcode.Hardware;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;

public class DoubleMotorFlywheel {
    DcMotorEx motor1;
    DcMotorEx motor2;

    // This class is mostly reworked from 8628's DECODE season "Shooter" class, and is intended as
    // a general-purpose flywheel motor controller for both our flipper and shooter flywheels.

    // These values should be tuned per-flywheel ideally; but just defaults to the values from DECODE
    public double Kp = 0.3; // Set Kp to zero when tuning the KVelocity term!!
    public double KVelocity = 0.0243; // power multiplier for rotations per second
    private double ticksPerRevolution = 28 ;  // REV HD Hex 1:1 Motor Encoder
    private double rpmTolerance = 50;
    private boolean holdZeroRPM = true;

    private double targetRPM = 0;

    public DoubleMotorFlywheel(HardwareMap hardwareMap, String name, String name2, DcMotorSimple.Direction dir1, DcMotorSimple.Direction dir2) {
        motor1 = (DcMotorEx) hardwareMap.get(DcMotor.class, name);
        motor1.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        motor1.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);  // WITH OUT!
        motor1.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
        motor1.setDirection(dir1);

        motor2 = (DcMotorEx) hardwareMap.get(DcMotor.class, name2);
        motor2.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        motor2.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);  // WITH OUT!
        motor2.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
        motor2.setDirection(dir2);
    }

    // Important to use!
    public void setControllerValues(double Kp, double KVelocity, double ticksPerRevolution, double rpmTolerance, boolean holdZeroRPM) {
        this.Kp = Kp;
        this.KVelocity = KVelocity;
        this.ticksPerRevolution = ticksPerRevolution;
        this.rpmTolerance = rpmTolerance;
        this.holdZeroRPM = holdZeroRPM;
    }

    public void updatePower() {
        if (targetRPM == 0 && !holdZeroRPM) {
            motor1.setPower(0);
            motor2.setPower(0);
            return;
        }

        double currentVelocity = this.getRPM();
        double rpmError = targetRPM - currentVelocity;
        double setPower = targetRPM * KVelocity + rpmError * Kp;
        motor1.setPower(setPower);
        motor2.setPower(setPower);
    }

    public void setPower(double power) {
        motor1.setPower(power);
    }

    public void stop() {
        targetRPM = 0;
        motor1.setPower(0);
        motor2.setPower(0);
    }

    public void setTargetRPM(double velocity) {
        this.targetRPM = velocity;
    }

    public double getTargetRPM() {
        return this.targetRPM;
    }

    public double getTicks() {
        return motor1.getCurrentPosition();
    }

    public double getPower() {
        return motor1.getPower();
    }

    public double getRPM() {
        return (motor1.getVelocity() / ticksPerRevolution) * 60;
    }

    public boolean atSpeed() {
        double currentVelocity = this.getRPM();
        double rpmError = targetRPM - currentVelocity;

        return Math.abs(rpmError) < rpmTolerance;
    }
}
