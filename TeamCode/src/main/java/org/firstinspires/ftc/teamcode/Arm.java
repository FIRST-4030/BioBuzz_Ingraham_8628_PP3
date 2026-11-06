package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;

public class Arm {

    public ArmHinge armHinge;
    public Flywheel shooterFlywheel;
    public Flywheel flipperFlywheel;
    public Servo gateServo;
    Limelight limelight;

    private boolean reachedAimingHeight = false;
    private final ElapsedTime timeSinceReachedReadyToShoot = new ElapsedTime();

    public enum ArmMode {
        IDLE,
        COLLECTING,
        AIMING
    }

    private ArmMode armMode = ArmMode.IDLE;

    // TODO: More elegant way to handle this?
    public boolean ejectOverride = false;

    // --- CONSTANTS ---

    // 90 degrees is defined as parallel to the ground.
    public static final double COLLECTING_ARM_ANGLE_DEG = 50.0;
    public static final double AIMING_ARM_ANGLE_DEG = 118.0;

    private static final double SERVO_CLOSED_POSITION = 0.75;
    private static final double SERVO_OPEN_POSITION = 0.15;

    private static final double SHOOTER_COLLECT_RPM = -1500;
    private static final double FLIPPER_COLLECT_RPM = 200; // TODO: What should this velocity be?

    // TODO: ALL MAX VELOCITIES ARE 6000 / GEAR RATIO (6000/26 = 230 RPM)
    private double shooter_shooting_rpm = 2100;
    private double flipper_shooting_rpm = 210;

    public void setShooter_shooting_rpm(double shooter_shooting_rpm) {
        this.shooter_shooting_rpm = shooter_shooting_rpm;
    }

    public double getShooter_shooting_rpm() {
        return shooter_shooting_rpm;
    }

    public void setFlipper_shooting_rpm(double flipper_shooting_rpm) {
        this.flipper_shooting_rpm = flipper_shooting_rpm;
    }

    public double getFlipper_shooting_rpm() {
        return flipper_shooting_rpm;
    }

    public Arm(HardwareMap hardwareMap, Limelight limelight) {
        armHinge = new ArmHinge(hardwareMap);

        // Both are using default controller values for now, but can be later changed with
        // MotorVelocityController.setControllerValues()
        flipperFlywheel = new Flywheel(hardwareMap, "flipper", DcMotorSimple.Direction.REVERSE);
        flipperFlywheel.setControllerValues(0.007, 0.005, 753.2, 100.0, false);

        shooterFlywheel = new Flywheel(hardwareMap, "shooter", DcMotorSimple.Direction.REVERSE);
        shooterFlywheel.setControllerValues(0.007, 0.00046, 56, 100.0, false);

        gateServo = hardwareMap.get(Servo.class, "gate_servo");

        this.limelight = limelight;
    }

    public void update() {
        if (!armHinge.isPotentiometerConnected()) {
            armHinge.stop();
            shooterFlywheel.stop();
            flipperFlywheel.stop();
            openGate();
            return;
        }

        handleArmHinge();
        handleFlipperFlywheel();
        handleShootingFlywheel();
        handleGateServo();

        flipperFlywheel.updatePower();
        shooterFlywheel.updatePower();
    }

    // --- ARM ---

    private void handleArmHinge() {
        switch (armMode) {
            case IDLE:
            case COLLECTING:
                armHinge.setTargetAngleDegrees(COLLECTING_ARM_ANGLE_DEG);
                armHinge.driveArmToTargetAngle();
                break;
            case AIMING:
                aimingModeArmHingeUpdate();
                break;
        }
    }

    private void aimingModeArmHingeUpdate() {
        double currentArmAngleDegrees = armHinge.getArmAngleDegrees();
        double goalArmAngleDegrees = AIMING_ARM_ANGLE_DEG;

        if (limelight != null && limelight.resultHasGoodTargets() && reachedAimingHeight) {
            double angleOffset = limelight.getYError();
            goalArmAngleDegrees = currentArmAngleDegrees + angleOffset;
        }

        armHinge.setTargetAngleDegrees(goalArmAngleDegrees);
        armHinge.driveArmToTargetAngle();

        if (currentArmAngleDegrees > AIMING_ARM_ANGLE_DEG - 10) {
            reachedAimingHeight = true;
        }

//        if (readyToShoot()) {
//            if (!reachedAimingHeight) {
//                timeSinceReachedReadyToShoot.reset();
//            }
//            reachedAimingHeight = true;
//        }
    }

    public ArmMode getArmMode() {
        return armMode;
    }

    public void startCollectingMode() {
        this.armMode = ArmMode.COLLECTING;
        resetShot();
    }

    public void startAimingMode() {
        this.armMode = ArmMode.AIMING;

        reachedAimingHeight = false;
//        reachedReadyToShoot = false;
    }

    public void startIdleMode() {
        this.armMode = ArmMode.IDLE;
        resetShot();
    }

    public boolean hasReachedAimingHeight() {
        return reachedAimingHeight;
    }

//    public boolean hasReachedReadyToShoot() {
//        return reachedReadyToShoot;
//    }

    // --- SHOOTER AND FLIPPER ---

    private void handleFlipperFlywheel() {
        if (ejectOverride) {
            if (armMode == ArmMode.AIMING) {
                flipperFlywheel.setTargetRPM(-FLIPPER_COLLECT_RPM / 6);
            } else {
                flipperFlywheel.setTargetRPM(-FLIPPER_COLLECT_RPM);
            }
            return;
        }

        switch (armMode) {
            case IDLE:
                flipperFlywheel.stop();
                break;
            case COLLECTING:
                flipperFlywheel.setTargetRPM(FLIPPER_COLLECT_RPM);
                break;
            case AIMING:
                if (readyToShoot()) {
                    flipperFlywheel.setTargetRPM(flipper_shooting_rpm);
                } else {
                    flipperFlywheel.stop();
                }
                break;
        }
    }

    private void handleShootingFlywheel() {
        if (armMode == ArmMode.COLLECTING) {
            shooterFlywheel.setTargetRPM(SHOOTER_COLLECT_RPM);
            return;
        }

        if (armMode == ArmMode.AIMING && !ejectOverride) {
            shooterFlywheel.setTargetRPM(shooter_shooting_rpm);
        } else {
            shooterFlywheel.setTargetRPM(0);
        }
    }

    private double calculateGoodShooterShootingRPM() {
        if (limelight.resultHasGoodTargets()) {
            return 2200; // TODO: some sort of default shooting rpm?
        }
        return limelight.getTa(); // TODO: Make actual equation for this calculation based on Ta
    }

    private double calculateGoodFlipperShootingRPM() {
        if (limelight.resultHasGoodTargets()) {
            return 2200; // TODO: some sort of default shooting rpm?
        }
        return limelight.getTa(); // TODO: Make actual equation for this calculation based on Ta
    }

    // --- SERVO ---

    private void handleGateServo() {
        if (ejectOverride) {
            openGate();
            return;
        }

        if (armMode != ArmMode.AIMING) {
            closeGate();
            return;
        }

        if (readyToShoot()) {
            openGate();
        } else {
            closeGate();
        }
    }

    public void closeGate() { gateServo.setPosition(
            SERVO_CLOSED_POSITION
    ); }

    public void openGate() { gateServo.setPosition(
            SERVO_OPEN_POSITION
    ); }

    public boolean readyToShoot() {
        return (armMode == ArmMode.AIMING &&
//                reachedAimingHeight &&
                limelight.isOnTarget() &&
                shooterFlywheel.atSpeed());
    }

    public void resetShot() {
//        reachedReadyToShoot = false;
        closeGate();
    }
}
