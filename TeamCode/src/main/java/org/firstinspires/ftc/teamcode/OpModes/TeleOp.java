package org.firstinspires.ftc.teamcode.OpModes;

import org.firstinspires.ftc.generalUtilities.Blackboard;
import org.firstinspires.ftc.teamcode.Robot.RobotOpMode;

import java.util.Locale;

@com.qualcomm.robotcore.eventloop.opmode.TeleOp(name="Ivy TeleOp")
public class TeleOp extends RobotOpMode {

    boolean isAimingForShot = false;

    @Override
    public void init() {
        super.init();

        robot.imu.resetYaw();
    }

    @Override
    public void init_loop() {
        robot.limelight.updateData(); // This line is vital, and should be called first in the loop
        robot.controlHub.processBotIdentificationTelemetry(robot.telemetry);
        Blackboard.initLoopProcess(robot.telemetry, gamepad1);

        super.init_loop();
    }

    @Override
    public void start() {
        robot.limelight.lookForBestAimingPipeline();
    }

    @Override
    public void loop() {
        robot.chassis.drive(-gamepad1.left_stick_y, gamepad1.left_stick_x, gamepad1.right_stick_x, robot.limelight);

        if (gamepad1.rightTriggerWasPressed()) {
            robot.chassis.lockedOnDrive();
            robot.armHinge.lookAtHive();
            robot.shooter.doShootRPM();
            robot.flipper.stop();
            robot.gate.close();
            isAimingForShot = true;
        }

        if (gamepad1.rightBumperWasPressed()) {
            robot.chassis.normalDrive();
            robot.armHinge.moveToCollectingPosition();
            robot.shooter.stop();
            robot.flipper.doCollectRPM();
            robot.gate.close();
            isAimingForShot = false;
        }

        if (gamepad1.leftBumperWasPressed()) {
            robot.chassis.normalDrive();
            robot.armHinge.moveToCollectingPosition();
            robot.shooter.stop();
            robot.flipper.stop();
            robot.gate.close();
            isAimingForShot = false;
        }

        if (gamepad1.dpadUpWasPressed()) {
            robot.chassis.preciseDrive();
            robot.armHinge.moveToFlowerPosition();
            robot.shooter.stop();
            robot.flipper.stop();
            robot.gate.close();
            isAimingForShot = false;
        }

        if (isAimingForShot) {
            if (readyToShoot()) {
                robot.gate.open();
                robot.flipper.doShootRPM();
            } else {
                robot.gate.close();
                robot.flipper.stop();
            }
        } else {
            if (gamepad1.leftTriggerWasPressed()) {
                robot.flipper.doEjectRPM();
                robot.gate.open();
            }

            if (gamepad1.leftTriggerWasReleased()) {
                robot.flipper.doCollectRPM();
                robot.gate.close();
            }
        }

        handleTelemetry();

        super.loop();
    }

    public boolean readyToShoot() {
        return (robot.armHinge.isLookingForHive() &&
                robot.limelight.isOnTarget() &&
                robot.shooter.atSpeed());
    }

    public void handleTelemetry() {
        robot.telemetry.addLine("LB: Idle, RB: Collect, RT: Aim + Shoot, LT: Eject");
        if (!(Blackboard.getAlliance() == Blackboard.Alliance.RED || Blackboard.getAlliance() == Blackboard.Alliance.BLUE)) {
            robot.telemetry.addLine("WARNING: ALLIANCE UNSET! You should re-init!");
        } else {
            robot.telemetry.addData("Alliance", Blackboard.getAlliance());
        }
        robot.telemetry.addLine("-------");
        robot.telemetry.addData("Current arm angle (degrees)", robot.armHinge.getArmAngleDegrees());
        robot.telemetry.addData("Target arm angle (degrees)", robot.armHinge.getClippedTargetAngleDegrees());

        //robot.telemetry.addData("Shooter Shot RPM", arm.getSHOOTER_SHOOTING_RPM());
        //robot.telemetry.addData("Flipper Shot RPM", arm.getFLIPPER_SHOOTING_RPM());

        robot.telemetry.addLine(String.format(Locale.US, "Shooter Shot target %.0f ,Current %.0f RPM",
                robot.shooter.getTargetRPM(), robot.shooter.getRPM()));
        robot.telemetry.addData("Shooter at speed?", robot.shooter.atSpeed());
        robot.telemetry.addLine(String.format(Locale.US, "Flipper Shot target %.0f ,Current %.0f RPM",
                robot.flipper.getTargetRPM(), robot.flipper.getRPM()));
        robot.telemetry.addData("Flipper at speed?", robot.flipper.atSpeed());

        robot.telemetry.addData("Ready to shoot?", readyToShoot());

        robot.telemetry.addLine();

        robot.telemetry.addData("Pipeline", robot.limelight.getCurrentPipelineName());
//        robot.telemetry.addData("MS since pipeline switch", limelight.getTimeSinceLastPipelineSwitch().milliseconds());

        robot.telemetry.addLine();

        if (robot.limelight.resultHasGoodTargets()) {
            robot.telemetry.addData("On target", robot.limelight.isOnTarget());
            //robot.telemetry.addData("Tx", limelight.getXError());
            //robot.telemetry.addData("Ty", limelight.getYError());
        } else {
            robot.telemetry.addLine("No good targets visible");
        }

        robot.telemetry.update();
    }
}
