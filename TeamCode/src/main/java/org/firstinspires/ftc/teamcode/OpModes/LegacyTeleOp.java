package org.firstinspires.ftc.teamcode.OpModes;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.IMU;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.generalUtilities.Blackboard;
import org.firstinspires.ftc.teamcode.Hardware.Chassis;
import org.firstinspires.ftc.teamcode.Hardware.LegacyArm;
import org.firstinspires.ftc.teamcode.Hardware.ControlHub;
import org.firstinspires.ftc.teamcode.Hardware.Limelight;

import java.util.Locale;

@com.qualcomm.robotcore.eventloop.opmode.TeleOp(name = "Legacy TeleOp")
public class LegacyTeleOp extends OpMode {

    ControlHub controlHub;

    IMU imu;
    Limelight limelight;
    Chassis chassis;
    LegacyArm legacyArm;

    ElapsedTime flowerQueueTimer = new ElapsedTime();
    boolean flowerQueued = false;
    double FLOWER_QUEUE_MS = 550;

    @Override
    public void init() {
        controlHub = new ControlHub(hardwareMap);

        imu = hardwareMap.get(IMU.class, "imu");
        limelight = new Limelight(hardwareMap);
        chassis = new Chassis(hardwareMap);
        legacyArm = new LegacyArm(hardwareMap, limelight);
        legacyArm.closeGate();

        imu.resetYaw();
    }

    public void init_loop() {
        limelight.updateData(); // This line is vital, and should be called first in the loop
        controlHub.processBotIdentificationTelemetry(telemetry);
        Blackboard.initLoopProcess(telemetry, gamepad1);
    }

    public void start() {
        legacyArm.startIdleMode();
        limelight.lookForBestAimingPipeline();
    }

    public void loop() {
        limelight.updateData(); // This line is vital, and should be called first in the loop
        handleModeSwitchingControls();
        handleFlowerControls();

        switch (legacyArm.getArmMode()) {
            case IDLE:
            case COLLECTING:
//                limelight.lookForBestCollectingPipeline();
                limelight.lookForBestAimingPipeline(); // TODO: Better default?
                chassis.directDrive(-gamepad1.left_stick_y, gamepad1.left_stick_x, gamepad1.right_stick_x);
                break;
            case AIMING:
                limelight.lookForBestAimingPipeline();
                if (limelight.resultHasGoodTargets()) {
                    chassis.lockedOnDrive(-gamepad1.left_stick_y, gamepad1.left_stick_x, limelight);
                } else {
                    chassis.directDrive(-gamepad1.left_stick_y, gamepad1.left_stick_x, gamepad1.right_stick_x);
                }
                break;
            default:
                chassis.stopMotors();
                break;
        }

        handleFlywheelAdjustment();
        legacyArm.update();
        handleTelemetry();
    }

    public void handleModeSwitchingControls() {
        if (gamepad1.rightBumperWasPressed()) {
            legacyArm.startCollectingMode();
        } else if (gamepad1.leftBumperWasPressed()) {
            legacyArm.startIdleMode();
        } else if (gamepad1.rightTriggerWasPressed()) {
            legacyArm.startAimingMode();
        }

        if (!flowerQueued) {
            legacyArm.ejectOverride = gamepad1.left_trigger > 0.5;
        }
    }

    public void handleFlowerControls() {
        if (gamepad1.startWasPressed()) {
            flowerQueueTimer.reset();
            flowerQueued = true;
            legacyArm.startIdleMode();
        }

        if (flowerQueued) {
            legacyArm.ejectOverride = true;

            if (flowerQueueTimer.milliseconds() > FLOWER_QUEUE_MS) {
                flowerQueued = false;
                legacyArm.startIdleMode();
            }
        }
    }

    public void handleTelemetry() {
        telemetry.addLine("LB: Idle, RB: Collect, RT: Aim + Shoot, LT: Eject");
        if (!(Blackboard.getAlliance() == Blackboard.Alliance.RED || Blackboard.getAlliance() == Blackboard.Alliance.BLUE)) {
            telemetry.addLine("WARNING: ALLIANCE UNSET! You should re-init!");
        } else {
            telemetry.addData("Alliance", Blackboard.getAlliance());
        }
        telemetry.addLine("-------");
        telemetry.addData("Mode", legacyArm.getArmMode());
        telemetry.addData("Current arm angle (degrees)", legacyArm.armHinge.getArmAngleDegrees());
        telemetry.addData("Target arm angle (degrees)", legacyArm.armHinge.getClippedTargetAngleDegrees());

        //telemetry.addData("Shooter Shot RPM", arm.getSHOOTER_SHOOTING_RPM());
        //telemetry.addData("Flipper Shot RPM", arm.getFLIPPER_SHOOTING_RPM());

        telemetry.addLine(String.format(Locale.US, "Shooter Shot target %.0f ,Current %.0f RPM",
                legacyArm.getShooter_shooting_rpm(), legacyArm.shooterFlywheel.getRPM()));
        telemetry.addData("Shooter at speed?", legacyArm.shooterFlywheel.atSpeed());
        telemetry.addLine(String.format(Locale.US, "Flipper Shot target %.0f ,Current %.0f RPM",
                legacyArm.getFlipper_shooting_rpm(), legacyArm.flipperFlywheel.getRPM()));
        telemetry.addData("Flipper at speed?", legacyArm.shooterFlywheel.atSpeed());

        telemetry.addData("At good height?", legacyArm.hasReachedAimingHeight());
        telemetry.addData("Ready to shoot?", legacyArm.readyToShoot());

        telemetry.addLine();

        telemetry.addData("Pipeline", limelight.getCurrentPipelineName());
//        telemetry.addData("MS since pipeline switch", limelight.getTimeSinceLastPipelineSwitch().milliseconds());

        telemetry.addLine();

        if (limelight.resultHasGoodTargets()) {
            telemetry.addData("On target", limelight.isOnTarget());
            //telemetry.addData("Tx", limelight.getXError());
            //telemetry.addData("Ty", limelight.getYError());
        } else {
            telemetry.addLine("No good targets visible");
        }

        telemetry.update();
    }

    public void handleFlywheelAdjustment() {
        if (gamepad1.dpadUpWasPressed()) {
            legacyArm.setShooter_shooting_rpm(legacyArm.getShooter_shooting_rpm()+10);
        } else if (gamepad1.dpadDownWasPressed()) {
            legacyArm.setShooter_shooting_rpm(legacyArm.getShooter_shooting_rpm()-10);
        } else if (gamepad1.yWasPressed()) {
            legacyArm.setFlipper_shooting_rpm(legacyArm.getFlipper_shooting_rpm()+10);
        } else if (gamepad1.aWasPressed()) {
            legacyArm.setFlipper_shooting_rpm(legacyArm.getFlipper_shooting_rpm()-10);
        }
    }
}
