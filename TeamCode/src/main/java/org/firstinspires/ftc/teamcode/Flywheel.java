package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;

public class Flywheel {
    DcMotorEx motor;

    // This class is mostly reworked from 8628's DECODE season "Shooter" class, and is intended as
    // a general-purpose flywheel motor controller for both our flipper and shooter flywheels.

    // These values should be tuned per-flywheel ideally; but just defaults to the values from DECODE
    public double Kp = 0.3; // Set Kp to zero when tuning the KVelocity term!!
    public double KVelocity = 0.0243; // power multiplier for rotations per second
    private double ticksPerRevolution = 28 ;  // REV HD Hex 1:1 Motor Encoder
    private double atSpeedMarginOfError = 50;
    private boolean holdZeroRPM = true;

    private double targetRPM = 0;

    public Flywheel(HardwareMap hardwareMap, String name, DcMotorSimple.Direction dir) {
        motor = (DcMotorEx) hardwareMap.get(DcMotor.class, name);
        motor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        motor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);  // WITH OUT!
        motor.setDirection(dir);
    }

    // Important to use!
    public void setControllerValues(double Kp, double KVelocity, double ticksPerRevolution, double atSpeedMarginOfError, boolean holdZeroRPM) {
        this.Kp = Kp;
        this.KVelocity = KVelocity;
        this.ticksPerRevolution = ticksPerRevolution;
        this.atSpeedMarginOfError = atSpeedMarginOfError;
        this.holdZeroRPM = holdZeroRPM;
    }

    public void updatePower() {
        if (targetRPM == 0 && !holdZeroRPM) {
            motor.setPower(0);
            return;
        }

        double currentVelocity = this.getRPM();
        double rpmError = targetRPM - currentVelocity;
        // CONTROLLER:  feedfoward = Kvelo + feedback = Kpos
        double setPower = targetRPM * KVelocity + rpmError * Kp;
        motor.setPower(setPower);
    }

    public void setPower(double power) {
        motor.setPower(power);
    }

    public void stop() {
        targetRPM = 0;
        motor.setPower(0);
    }

    public void resetTargetRPM() {
        targetRPM = 0;
    }

    public void setTargetRPM(double velocity) {
        this.targetRPM = velocity;
    }

    public double getTicks() {
        return motor.getCurrentPosition();
    }

    public double getPower() {
        return motor.getPower();
    }

    public double getRPM() {
        return (motor.getVelocity() / ticksPerRevolution) * 60;
    }

    public boolean atSpeed() {
        double currentVelocity = this.getRPM();
        double rpmError = targetRPM - currentVelocity;

        return Math.abs(rpmError) < atSpeedMarginOfError;
    }
}
