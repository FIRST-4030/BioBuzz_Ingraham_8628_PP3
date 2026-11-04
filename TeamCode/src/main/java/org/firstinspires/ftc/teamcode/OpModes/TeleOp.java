package org.firstinspires.ftc.teamcode.OpModes;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.IMU;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.generalUtilities.Blackboard;
import org.firstinspires.ftc.teamcode.Arm;
import org.firstinspires.ftc.teamcode.Chassis;
import org.firstinspires.ftc.teamcode.ControlHub;
import org.firstinspires.ftc.teamcode.Limelight;

import java.util.Locale;

@com.qualcomm.robotcore.eventloop.opmode.TeleOp(name = "TeleOp", group="Util")
public class TeleOp extends OpMode {

    ControlHub controlHub;

    IMU imu;
    Limelight limelight;
    Chassis chassis;
    Arm arm;

    ElapsedTime timeSinceStartedShooting = new ElapsedTime();
    boolean wasShooting = false;

    @Override
    public void init() {
        controlHub = new ControlHub(hardwareMap);

        imu = hardwareMap.get(IMU.class, "imu");
        limelight = new Limelight(hardwareMap);
        chassis = new Chassis(hardwareMap);
        arm = new Arm(hardwareMap, limelight);
        arm.closeGate();

        imu.resetYaw();
    }

    public void init_loop() {
        limelight.updateData(); // This line is vital, and should be called first in the loop
        controlHub.processBotIdentificationTelemetry(telemetry);
        Blackboard.initLoopProcess(telemetry, gamepad1);
    }

    public void start() {
        arm.startIdleMode();
        limelight.lookForBestAimingPipeline();
    }

    public void loop() {
        limelight.updateData(); // This line is vital, and should be called first in the loop
        handleModeSwitchingControls();

        switch (arm.getArmMode()) {
            case IDLE:
            case COLLECTING:
                limelight.lookForBestCollectingPipeline();
                chassis.drive(-gamepad1.left_stick_y, gamepad1.left_stick_x, gamepad1.right_stick_x);
                break;
            case AIMING:
                limelight.lookForBestAimingPipeline();
                if (limelight.resultHasGoodTargets()) {
                    chassis.lockedOnDrive(-gamepad1.left_stick_y, gamepad1.left_stick_x, limelight);
                } else {
                    chassis.drive(-gamepad1.left_stick_y, gamepad1.left_stick_x, gamepad1.right_stick_x);
                }
                break;
            default:
                chassis.stopMotors();
                break;
        }

        handleFlywheelAdjustment();
        arm.update();
        handleTelemetry();
    }

    public void handleModeSwitchingControls() {
        if (gamepad1.rightBumperWasPressed()) {
            arm.startCollectingMode();
        } else if (gamepad1.leftBumperWasPressed()) {
            arm.startIdleMode();
        } else if (gamepad1.rightTriggerWasPressed()) {
            arm.startAimingMode();
        }

        if (!wasShooting && arm.hasReachedReadyToShoot()) {
            timeSinceStartedShooting.reset();
        }

        if (arm.getArmMode() == Arm.ArmMode.AIMING && arm.hasReachedReadyToShoot() && timeSinceStartedShooting.milliseconds() > 1000) {
            arm.startCollectingMode();
        }

        arm.ejectOverride = gamepad1.left_trigger > 0.5;

        wasShooting = arm.hasReachedReadyToShoot();
    }

    public void handleTelemetry() {
        telemetry.addLine("LB: Idle, RB: Collect, RT: Aim + Shoot, LT: Eject");
        if (!(Blackboard.getAlliance() == Blackboard.Alliance.RED || Blackboard.getAlliance() == Blackboard.Alliance.BLUE)) {
            telemetry.addLine("WARNING: ALLIANCE UNSET! You should re-init!");
        } else {
            telemetry.addData("Alliance", Blackboard.getAlliance());
        }
        telemetry.addLine("-------");
        telemetry.addData("Mode", arm.getArmMode());
        telemetry.addData("Current arm angle (degrees)", arm.armHinge.getArmAngleDegrees());

        //telemetry.addData("Shooter Shot RPM", arm.getSHOOTER_SHOOTING_RPM());
        //telemetry.addData("Flipper Shot RPM", arm.getFLIPPER_SHOOTING_RPM());

        telemetry.addLine(String.format(Locale.US, "Shooter Shot target %.0f ,Current %.0f RPM",
                arm.getShooter_shooting_rpm(),arm.shooterFlywheel.getRPM()));
        telemetry.addData("Shooter at speed?", arm.shooterFlywheel.atSpeed());
        telemetry.addLine(String.format(Locale.US, "Flipper Shot target %.0f ,Current %.0f RPM",
                arm.getFlipper_shooting_rpm(),arm.flipperFlywheel.getRPM()));
        telemetry.addData("Flipper at speed?", arm.shooterFlywheel.atSpeed());

        telemetry.addData("At good height?", arm.hasReachedAimingHeight());
        telemetry.addData("Ready to shoot?", arm.readyToShoot());

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
            arm.setShooter_shooting_rpm(arm.getShooter_shooting_rpm()+10);
        } else if (gamepad1.dpadDownWasPressed()) {
            arm.setShooter_shooting_rpm(arm.getShooter_shooting_rpm()-10);
        } else if (gamepad1.yWasPressed()) {
            arm.setFlipper_shooting_rpm(arm.getFlipper_shooting_rpm()+10);
        } else if (gamepad1.aWasPressed()) {
            arm.setFlipper_shooting_rpm(arm.getFlipper_shooting_rpm()-10);
        }
    }
}
