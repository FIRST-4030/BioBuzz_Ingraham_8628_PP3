package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.hardware.AnalogInput;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.util.Range;

public class Arm {

    public DcMotor armHinge;
    public AnalogInput armPotentiometer;
//    public Flywheel shooterFlywheel;
//    public Flywheel collectorFlywheel;
    Limelight limelight;

    private boolean collecting = false;
    private boolean shooting = false;
    private boolean reachedAimingHeight = false;

    // 90 degrees is defined as parallel to the ground.
    private static final double COLLECTING_ARM_ANGLE_DEG = 70.0;
    private static final double AIMING_ARM_ANGLE_DEG = 130.0;

    private static final double MIN_ANGLE_DEG = 60.0;
    private static final double MAX_ANGLE_DEG = 180.0;

    private static final double COLLECTING_POWER = 20.0; // TODO: What should this velocity be?

    public enum ArmPositionMode {
        COLLECTING,
        AIMING
    }

    private ArmPositionMode mode = ArmPositionMode.COLLECTING;

    public Arm(HardwareMap hardwareMap, Limelight limelight) {
        armHinge = hardwareMap.get(DcMotor.class, "arm");
        armHinge.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        armPotentiometer = hardwareMap.get(AnalogInput.class, "arm_potentiometer");

        // Both are using default controller values for now, but can be later changed with
        // MotorVelocityController.setControllerValues()
//        shooterFlywheel = new Flywheel(hardwareMap, "shooter", DcMotorSimple.Direction.FORWARD);
//        collectorFlywheel = new Flywheel(hardwareMap, "collector", DcMotorSimple.Direction.FORWARD);

        this.limelight = limelight;
    }

    public void update() {
        if (!isPotentiometerConnected()) {
            armHinge.setPower(0);
//            shooterFlywheel.stop();
//            collectorFlywheel.stop();
            return;
        }

        switch (mode) {
            case COLLECTING:
                collectingModeUpdate();
                break;
            case AIMING:
                aimingModeUpdate();
                break;
        }

        handleShootingFlywheel();

//        shooterFlywheel.updatePower();
//        collectorFlywheel.updatePower();
    }

    private void collectingModeUpdate() {
        double currentArmAngleDegrees = this.getArmAngleDegrees();
        double power = calculateHingePowerForAngle(currentArmAngleDegrees, COLLECTING_ARM_ANGLE_DEG);

        armHinge.setPower(power);

        reachedAimingHeight = false;
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
    }

    // --- ARM ---

    private double calculateHingePowerForAngle(double currentArmAngleDegrees, double goalArmAngleDegrees) {
        double clippedGoalArmAngleDegrees = Range.clip(goalArmAngleDegrees, MIN_ANGLE_DEG, MAX_ANGLE_DEG);

        // TODO: Want to try doing it this way too:
        double error = clippedGoalArmAngleDegrees - currentArmAngleDegrees;
        double kP = 0.03;
        double kG = 0.15; // Tuning parameter for gravity compensation

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

    public ArmPositionMode getMode() {
        return mode;
    }

    public void setMode(ArmPositionMode mode) {
        this.mode = mode;
    }

    public void collectingMode() {
        this.mode = ArmPositionMode.COLLECTING;
    }

    public void aimingMode() {
        this.mode = ArmPositionMode.AIMING;
    }

    // ONLY USE FOR DEBUGGING/CALIBRATION OPMODES!!
    // This does not limit the range of motion of the hinge
    public void unsafePowerArmHinge(double power) {
        armHinge.setPower(power);
    }

    // --- SHOOTER AND COLLECTOR ---

    public void handleCollectingFlywheel() {
        if (shooting) {
//            collectorFlywheel.stop();
            return;
        }

        if (!collecting) {
//            collectorFlywheel.resetTargetVelocity();
            return;
        }

//        collectorFlywheel.setTargetVelocity(COLLECTING_POWER);
    }

    public void handleShootingFlywheel() {
        if (!shooting) {
//            shooterFlywheel.setTargetVelocity(0);
            return;
        }

//        shooterFlywheel.setTargetVelocity(20); // TODO: What should this velocity be?

//        boolean readyToShoot = (mode == ArmPositionMode.AIMING &&
//                limelight.isOnTarget() &&
//                shooterFlywheel.atSpeed());

        boolean readyToShoot = true;

        if (readyToShoot) {
            // Do servo stuff!
        }
    }

    public boolean isCollecting() {
        return collecting;
    }

    public boolean isShooting() {
        return shooting;
    }

    public void setCollecting(boolean collecting) {
        this.collecting = collecting;
    }

    public void startCollecting() { this.collecting = true; }
    public void stopCollecting() { this.collecting = false; }

    public void setShooting(boolean shooting) {
        this.shooting = shooting;
    }

    public void startShooting() { this.shooting = true; }
    public void stopShooting() { this.shooting = false; }
}
