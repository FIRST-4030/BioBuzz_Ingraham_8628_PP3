package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.util.Range;

import org.firstinspires.ftc.robotcore.external.Telemetry;

public class Chassis {

    //    private double maxPower = 1.0;
    private double maxSpeed = 1.0;  // make this slower for outreaches

    public DcMotor frontLeftDrive, frontRightDrive, backLeftDrive, backRightDrive;

    public Chassis(HardwareMap hardwareMap) {
        frontLeftDrive = hardwareMap.get(DcMotor.class, "leftFront");
        frontRightDrive = hardwareMap.get(DcMotor.class, "rightFront");
        backLeftDrive = hardwareMap.get(DcMotor.class, "leftBack");
        backRightDrive = hardwareMap.get(DcMotor.class, "rightBack");

        // We set the left motors in reverse which is needed for drive trains where the left
        // motors are opposite to the right ones.
        frontLeftDrive.setDirection(DcMotor.Direction.REVERSE);
        frontRightDrive.setDirection(DcMotor.Direction.FORWARD);
        backLeftDrive.setDirection(DcMotor.Direction.REVERSE);
        backRightDrive.setDirection(DcMotor.Direction.FORWARD);

        // This uses RUN_USING_ENCODER to be more accurate.   If you don't have the encoder
        // wires, you should remove these
        frontLeftDrive.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        frontRightDrive.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        backLeftDrive.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        backRightDrive.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);

        frontLeftDrive.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        frontRightDrive.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        backLeftDrive.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        backRightDrive.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
    }

    public void resetZeroPowerBehavior() {
        frontLeftDrive.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        frontRightDrive.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        backLeftDrive.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        backRightDrive.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
    }

    // Thanks to FTC16072 for sharing this code!!
    public void drive(double forward, double right, double rotate) {
        // This calculates the power needed for each wheel based on the amount of forward,
        // strafe right, and rotate
        double frontLeftPower = forward + right + rotate;
        double frontRightPower = forward - right - rotate;
        double backRightPower = forward + right - rotate;
        double backLeftPower = forward - right + rotate;

        // This is needed to make sure we don't pass > 1.0 to any wheel
        // It allows us to keep all of the motors in proportion to what they should
        // be and not get clipped
//        maxPower = Math.max(maxPower, Math.abs(frontLeftPower));
//        maxPower = Math.max(maxPower, Math.abs(frontRightPower));
//        maxPower = Math.max(maxPower, Math.abs(backRightPower));
//        maxPower = Math.max(maxPower, Math.abs(backLeftPower));

        // We multiply by maxSpeed so that it can be set lower for outreaches
        // When a young child is driving the robot, we may not want to allow full
        // speed.
        frontLeftDrive.setPower(maxSpeed * frontLeftPower);
        frontRightDrive.setPower(maxSpeed * frontRightPower);
        backLeftDrive.setPower(maxSpeed * backLeftPower);
        backRightDrive.setPower(maxSpeed * backRightPower);
//        frontLeftDrive.setPower(frontLeftPower);
//        frontRightDrive.setPower(frontRightPower);
//        backLeftDrive.setPower(backLeftPower);
//        backRightDrive.setPower(backRightPower);
    }

    public void lockedOnDrive(double forward, double right, Gamepad gamepad, Limelight limelight) {
        // TODO: Better PID equation for this instead of just a lerp
        this.drive(gamepad.left_stick_y, -gamepad.left_stick_x, limelight.getXError() / 35);
    }

    public void chasingDrive(double forward, double right, Gamepad gamepad, Limelight limelight) {
        if (limelight.resultHasGoodTargets()) {
            double weightedTargetAreaPercentage = Range.clip(limelight.getLatestResult().getTa() * 2, 0, 1);
            this.drive(-1 + weightedTargetAreaPercentage, limelight.getXError() / 20, limelight.getXError() / 35);
        }
        this.drive(0, 0, 0);
    }

//    public void setMaxPower(double maxPower) {
//        this.maxPower = maxPower;
//    }

    public void setMaxSpeed(double maxSpeed) {
        this.maxSpeed = maxSpeed;
    }

    public void stopMotors() {
        frontLeftDrive.setPower(0);
        frontRightDrive.setPower(0);
        backLeftDrive.setPower(0);
        backRightDrive.setPower(0);
    }

    public void moveAllMotors(double frontleftpower, double frontrightpower, double backleftpower, double backrightpower) {
        frontLeftDrive.setPower(frontleftpower);
        frontRightDrive.setPower(frontrightpower);
        backLeftDrive.setPower(backleftpower);
        backRightDrive.setPower(backrightpower);
    }

    public void getMotorSpeed(Telemetry telemetry) {
        telemetry.addData("fleft", frontLeftDrive.getPower());
        telemetry.addData("fright", frontRightDrive.getPower());
        telemetry.addData("lback", backLeftDrive.getPower());
        telemetry.addData("lRIGHT", backRightDrive.getPower());
    }
}
