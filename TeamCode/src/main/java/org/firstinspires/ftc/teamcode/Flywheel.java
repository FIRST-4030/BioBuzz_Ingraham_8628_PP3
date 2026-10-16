package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;

public class Flywheel {
    DcMotorEx motor;

    // This class is mostly reworked from 8628's DECODE season "Shooter" class, and is intended as
    // a general-purpose flywheel motor controller for both our collector and shooter flywheels.

    // These should be tuned per-flywheel ideally; but just defaults to the values from DECODE
    public double KVelocity = 0.0243; // power multiplier for rotations per second
    // FeedBack term is Kp (proportional term)
    // Set Kp to zero when tuning the KVelocity term!!
    public double Kp = 0.3;

    private static final double COUNTS_PER_REV = 28 ;  // REV HD Hex 1:1 Motor Encoder
    private static final double errorMargin = 0.02;

    private double targetVelocity = 0;

    public Flywheel(HardwareMap hardwareMap, String name, DcMotorSimple.Direction dir) {
        motor = (DcMotorEx) hardwareMap.get(DcMotor.class, name);
        motor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        motor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);  // WITH OUT!
        motor.setDirection(dir);
    }

    public void updatePower() {
//        double currentVelocity = this.getVelocity();
//        double velocityError = targetVelocity - currentVelocity;
//        // CONTROLLER:  feedfoward = Kvelo + feedback = Kpos
//        double setPower = targetVelocity * KVelocity + velocityError * Kp;
//        motor.setPower(setPower);
        motor.setPower(targetVelocity);
    }

    public void setControllerValues(double Kp, double KVelocity) {
        this.Kp = Kp;
        this.KVelocity = KVelocity;
    }

    public void setPower(double power) {
        motor.setPower(power);
    }

    public void stop() {
        targetVelocity = 0;
        motor.setPower(0);
    }

    public void resetTargetVelocity() {
        targetVelocity = 0;
    }

    public void setTargetVelocity(double velocity) {
        this.targetVelocity = velocity;
    }

    public double getPower() {
        return motor.getPower();
    }

    public double getVelocity() {
        return motor.getVelocity(AngleUnit.DEGREES) / COUNTS_PER_REV;
    }

    public boolean atSpeed() {
        return (1 - errorMargin) * targetVelocity < this.getVelocity() &&
                this.getVelocity() < (1 + errorMargin) * targetVelocity;
    }
}
