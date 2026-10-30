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
    private boolean reachedReadyToShoot = false;
    private final ElapsedTime timeSinceReachedReadyToShoot = new ElapsedTime();

    // TODO: THIS IS TEMPORARY
    public boolean ejectOverride = false;

    // 90 degrees is defined as parallel to the ground.
    public static final double COLLECTING_ARM_ANGLE_DEG = 50.0;
    public static final double AIMING_ARM_ANGLE_DEG = 120.0;

    private static final double SHOOTER_COLLECT_RPM = -1500;
    private static final double SHOOTER_SHOOTING_RPM = 2000;
    private static final double FLIPPER_COLLECT_RPM = 500; // TODO: What should this velocity be?
    private static final double FLIPPER_SHOOTING_RPM = 500; // TODO: What should this velocity be?

    private static final double SERVO_CLOSED_POSITION = 0.7;
    private static final double SERVO_OPEN_POSITION = 0.15;

    public enum ArmMode {
        IDLE,
        COLLECTING,
        AIMING
    }

    private ArmMode armMode = ArmMode.IDLE;

    public Arm(HardwareMap hardwareMap, Limelight limelight) {
        armHinge = new ArmHinge(hardwareMap);

        // Both are using default controller values for now, but can be later changed with
        // MotorVelocityController.setControllerValues()
        flipperFlywheel = new Flywheel(hardwareMap, "flipper", DcMotorSimple.Direction.REVERSE);
        flipperFlywheel.setControllerValues(0.1, 0.005, 753.2, 0.02, false);

        shooterFlywheel = new Flywheel(hardwareMap, "shooter", DcMotorSimple.Direction.REVERSE);
        shooterFlywheel.setControllerValues(0.007, 0.00046, 56, 0.02, false);

        gateServo = hardwareMap.get(Servo.class, "gate_servo");

        this.limelight = limelight;
    }

    public void update() {
        if (!armHinge.isPotentiometerConnected()) {
            armHinge.stop();
            shooterFlywheel.stop();
            flipperFlywheel.stop();
            closeGate();
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
                armHinge.driveArmToAngle(COLLECTING_ARM_ANGLE_DEG);
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

        armHinge.driveArmToAngle(goalArmAngleDegrees);

        if (currentArmAngleDegrees > AIMING_ARM_ANGLE_DEG - 10) {
            reachedAimingHeight = true;
        }

        if (readyToShoot()) {
            if (!reachedReadyToShoot) {
                timeSinceReachedReadyToShoot.reset();
            }
            reachedReadyToShoot = true;
        }
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
        reachedReadyToShoot = false;
    }

    public void startIdleMode() {
        this.armMode = ArmMode.IDLE;
        resetShot();
    }

    public boolean hasReachedAimingHeight() {
        return reachedAimingHeight;
    }

    // --- SHOOTER AND FLIPPER ---

    private void handleFlipperFlywheel() {
        if (ejectOverride) {
            if (armMode == ArmMode.AIMING) {
                flipperFlywheel.setTargetRPM(FLIPPER_COLLECT_RPM / 4);
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
                if (reachedReadyToShoot) {
                    flipperFlywheel.setTargetRPM(FLIPPER_SHOOTING_RPM);
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
            shooterFlywheel.setTargetRPM(SHOOTER_SHOOTING_RPM);
        } else {
            shooterFlywheel.setTargetRPM(0);
        }
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

        if (reachedReadyToShoot) {
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
                reachedAimingHeight &&
                limelight.isOnTarget() &&
                shooterFlywheel.atSpeed());
    }

    public void resetShot() {
        reachedReadyToShoot = false;
        closeGate();
    }
}
