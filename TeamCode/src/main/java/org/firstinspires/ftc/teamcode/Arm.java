package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.hardware.AnalogInput;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.Range;

public class Arm {

    public DcMotor armHinge;
    public AnalogInput armPotentiometer;
    public Flywheel shooterFlywheel;
    public Flywheel flipperFlywheel;
    public Servo gateServo;
    Limelight limelight;

    private boolean reachedAimingHeight = false;
    private boolean reachedReadyToShoot = false;

    public boolean manualReadyToShoot = false; // Last resort
    // TODO: THIS IS TEMPORARY
    public double tempShootPower = 1;

    // 90 degrees is defined as parallel to the ground.
    private static final double COLLECTING_ARM_ANGLE_DEG = 30.0;
    private static final double AIMING_ARM_ANGLE_DEG = 120.0;

    private static final double MIN_ANGLE_DEG = 56.0;
    private static final double MAX_ANGLE_DEG = 140.0;

    private static final double FLIPPER_COLLECT_VELOCITY = 1; // TODO: What should this velocity be?
    private static final double FLIPPER_SHOOT_VELOCITY = 1; // TODO: What should this velocity be?

    private static final double SERVO_CLOSED_POSITION = 0.4;
    private static final double SERVO_OPEN_POSITION = 0.0;

    public enum ArmMode {
        IDLE,
        COLLECTING,
        AIMING
    }

    private ArmMode armMode = ArmMode.COLLECTING;

    public Arm(HardwareMap hardwareMap, Limelight limelight) {
        armHinge = hardwareMap.get(DcMotor.class, "arm");
        armHinge.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        armPotentiometer = hardwareMap.get(AnalogInput.class, "arm_potentiometer");

        // Both are using default controller values for now, but can be later changed with
        // MotorVelocityController.setControllerValues()
        flipperFlywheel = new Flywheel(hardwareMap, "flipper", DcMotorSimple.Direction.REVERSE);
        shooterFlywheel = new Flywheel(hardwareMap, "shooter", DcMotorSimple.Direction.REVERSE);
        gateServo = hardwareMap.get(Servo.class, "gate_servo");

        this.limelight = limelight;
    }

    public void update() {
        if (!isPotentiometerConnected()) {
            armHinge.setPower(0);
            shooterFlywheel.stop();
            flipperFlywheel.stop();
            return;
        }

        switch (armMode) {
            case IDLE:
                idleModeUpdate();
                break;
            case COLLECTING:
                collectingModeUpdate();
                break;
            case AIMING:
                aimingModeUpdate();
                break;
        }

        handleFlipperFlywheel();
        handleShootingFlywheel();
        handleShooterServo();

        flipperFlywheel.updatePower();
        shooterFlywheel.updatePower();
    }

    private void idleModeUpdate() {
        double currentArmAngleDegrees = this.getArmAngleDegrees();
        double power = calculateHingePowerForAngle(currentArmAngleDegrees, COLLECTING_ARM_ANGLE_DEG);

        armHinge.setPower(power);

        reachedAimingHeight = false;
        reachedReadyToShoot = false;
    }

    private void collectingModeUpdate() {
        double currentArmAngleDegrees = this.getArmAngleDegrees();
        double power = calculateHingePowerForAngle(currentArmAngleDegrees, COLLECTING_ARM_ANGLE_DEG);

        armHinge.setPower(power);

        reachedAimingHeight = false;
        reachedReadyToShoot = false;
    }

    private void aimingModeUpdate() {
        double currentArmAngleDegrees = this.getArmAngleDegrees();
        double goalArmAngleDegrees = AIMING_ARM_ANGLE_DEG;

        if (limelight != null && limelight.resultHasGoodTargets() && reachedAimingHeight) {
            double angleOffset = limelight.getYError();
            goalArmAngleDegrees = currentArmAngleDegrees + angleOffset;
        }

        double power = calculateHingePowerForAngle(currentArmAngleDegrees, goalArmAngleDegrees);
        armHinge.setPower(power);

        if (currentArmAngleDegrees > AIMING_ARM_ANGLE_DEG - 5) {
            reachedAimingHeight = true;
        }

        if (readyToShoot()) {
            reachedReadyToShoot = true;
        }
    }

    // --- ARM ---

    private double calculateHingePowerForAngle(double currentArmAngleDegrees, double goalArmAngleDegrees) {
        double clippedGoalArmAngleDegrees = Range.clip(goalArmAngleDegrees, MIN_ANGLE_DEG, MAX_ANGLE_DEG);

        // TODO: Want to try doing it this way too:
        double error = clippedGoalArmAngleDegrees - currentArmAngleDegrees;
        double kP = 0.08;
        double kG = 0.3; // Tuning parameter for gravity compensation

        // Angle is relative to ground (90° = horizontal)
        double feedForward = kG * Math.cos(Math.toRadians(currentArmAngleDegrees));
        double power = (error * kP) + feedForward;
        return Range.clip(power, -1.0, 1.0);
    }

    public double getArmAngleDegrees() {
        // TODO: Double check this equation is the correct one
        return 109.11623 * getPotentiometerVoltage() + 40.01092;
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

    public void collectingMode() {
        this.armMode = ArmMode.COLLECTING;
    }

    public void aimingMode() {
        this.armMode = ArmMode.AIMING;
    }

    public void idleMode() {
        this.armMode = ArmMode.IDLE;
    }

    // ONLY USE FOR DEBUGGING/CALIBRATION OPMODES!!
    // This does not limit the range of motion of the hinge
    public void unsafePowerArmHinge(double power) {
        armHinge.setPower(power);
    }

    // --- SHOOTER AND FLIPPER ---

    private void handleFlipperFlywheel() {
        switch (armMode) {
            case IDLE:
                flipperFlywheel.stop();
                break;
            case COLLECTING:
                flipperFlywheel.setTargetVelocity(FLIPPER_COLLECT_VELOCITY);
                break;
            case AIMING:
                if (reachedReadyToShoot) {
                    flipperFlywheel.setTargetVelocity(FLIPPER_SHOOT_VELOCITY);
                } else {
                    flipperFlywheel.stop();
                }
                break;
        }
    }

    private void handleShootingFlywheel() {
        if (armMode == ArmMode.AIMING) {
            shooterFlywheel.setTargetVelocity(tempShootPower);
        } else {
            shooterFlywheel.setTargetVelocity(0);
        }
    }

    private void handleShooterServo() {
        if (armMode != ArmMode.AIMING) {
            closeGate();
            return;
        }

        if (reachedReadyToShoot) {
            openGate();
        }
    }

    public boolean readyToShoot() {
        return (armMode == ArmMode.AIMING &&
                reachedAimingHeight &&
                limelight.isOnTarget() &&
                shooterFlywheel.atSpeed()) || manualReadyToShoot;
    }

    public boolean hasReachedReadyToShoot() {
        return reachedReadyToShoot;
    }

    private void closeGate() { gateServo.setPosition(
            SERVO_CLOSED_POSITION
    ); }

    private void openGate() { gateServo.setPosition(
            SERVO_OPEN_POSITION
    ); }
}
