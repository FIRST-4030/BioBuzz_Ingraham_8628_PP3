package org.firstinspires.ftc.teamcode.OpModes;

import static com.pedropathing.ivy.commands.Commands.infinite;
import static com.pedropathing.ivy.commands.Commands.instant;
import static com.pedropathing.ivy.commands.Commands.waitUntil;
import static com.pedropathing.ivy.groups.Groups.sequential;

import com.pedropathing.ivy.Command;
import com.qualcomm.hardware.gobilda.GoBildaPinpointDriver;

import org.firstinspires.ftc.generalUtilities.Blackboard;
import org.firstinspires.ftc.teamcode.Hardware.Limelight;
import org.firstinspires.ftc.teamcode.Hardware.Shooter;
import org.firstinspires.ftc.teamcode.Robot.RobotOpMode;
import org.firstinspires.ftc.teamcode.pedro.Poses;

import java.util.Locale;

@com.qualcomm.robotcore.eventloop.opmode.TeleOp(name="TeleOp")
public class TeleOp extends RobotOpMode {

    GoBildaPinpointDriver pinpoint;

    boolean aiming = false;

    @Override
    public void init() {
        super.init();

        pinpoint = hardwareMap.get(GoBildaPinpointDriver.class, "pinpoint");
    }

    @Override
    public void init_loop() {
        robot.controlHub.processBotIdentificationTelemetry(robot.telemetry);
        Blackboard.initLoopProcess(robot.telemetry, gamepad1);
        super.init_loop();
    }

    @Override
    public void start() {
        super.start();
        robot.follower.setPose(Poses.nearGardenStartingPose);
        lowerArmAndStopCollecting().schedule();
    }

    @Override
    public void loop() {
        if (aiming && robot.limelight.resultHasGoodTargets()) {
            robot.lockedOntoHiveArcadeDrive(gamepad1);
        } else {
            robot.arcadeDrive(gamepad1);
        }

        if (gamepad1.rightTriggerWasPressed()) infiniteAimAndShoot().schedule();

        if (gamepad1.rightBumperWasPressed()) lowerArmAndCollect().schedule();

        if (gamepad1.leftBumperWasPressed()) lowerArmAndStopCollecting().schedule();

        if (gamepad1.leftTriggerWasPressed()) ejectSequence().schedule();

        if (gamepad1.dpadUpWasPressed()) setUpForFlower().schedule();

        if (gamepad1.dpadLeftWasPressed()) {
            Shooter.GENERAL_SHOOT_RPM -= 100;
        }

        if (gamepad1.dpadRightWasPressed()) {
            Shooter.GENERAL_SHOOT_RPM += 100;
        }

        handleTelemetry();
        super.loop();
    }

    public void handleTelemetry() {
        robot.telemetry.addLine("LB: Idle, RB: Collect, RT: Aim + Shoot, LT: Eject");
        if (!(Blackboard.getAlliance() == Blackboard.Alliance.RED || Blackboard.getAlliance() == Blackboard.Alliance.BLUE)) {
            robot.telemetry.addLine("WARNING: ALLIANCE UNSET! You should re-init!");
        } else {
            robot.telemetry.addData("Alliance", Blackboard.getAlliance());
        }

        robot.telemetry.addData("Can see red audience", robot.limelight.canSeeHiveCell(Limelight.HiveCell.RED_AUDIENCE));
        robot.telemetry.addData("Can see blue audience", robot.limelight.canSeeHiveCell(Limelight.HiveCell.BLUE_AUDIENCE));
        robot.telemetry.addData("Can see red scoring", robot.limelight.canSeeHiveCell(Limelight.HiveCell.RED_SCORING));
        robot.telemetry.addData("Can see blue scoring", robot.limelight.canSeeHiveCell(Limelight.HiveCell.BLUE_SCORING));
        robot.telemetry.addData("Distance to hive (CM)", robot.limelight.getDistanceCMFromHive());
        robot.telemetry.addData("Shooter rpm", Shooter.GENERAL_SHOOT_RPM);

        robot.telemetry.addLine("-------");
        robot.telemetry.addData("Follower x", robot.follower.pose().x());
        robot.telemetry.addData("Follower y", robot.follower.pose().y());
        robot.telemetry.addData("Follower heading", robot.follower.pose().heading());
        robot.telemetry.addLine();
        robot.telemetry.addData("Current arm angle (degrees)", robot.armHinge.getArmAngleDegrees());
        robot.telemetry.addData("Target arm angle (degrees)", robot.armHinge.getClippedTargetAngleDegrees());
        robot.telemetry.addData("Arm angle error", robot.armHinge.getErrorDegrees());

        robot.telemetry.addLine(String.format(Locale.US, "Shooter Shot target %.0f ,Current %.0f RPM",
                robot.shooter.getTargetRPM(), robot.shooter.getRPM()));
        robot.telemetry.addData("Shooter at speed?", robot.shooter.atSpeed());
        robot.telemetry.addLine(String.format(Locale.US, "Flipper Shot target %.0f ,Current %.0f RPM",
                robot.flipper.getTargetRPM(), robot.flipper.getRPM()));
        robot.telemetry.addData("Flipper at speed?", robot.flipper.atSpeed());

        robot.telemetry.addData("Ready to shoot?", robot.isReadyToShoot());

        robot.telemetry.addLine();
        robot.telemetry.addData("Pipeline", robot.limelight.getCurrentPipelineName());

        robot.telemetry.addLine();
        if (robot.limelight.resultHasGoodTargets()) {
            robot.telemetry.addData("On target", robot.limelight.isOnTarget());
        } else {
            robot.telemetry.addLine("No good targets visible");
        }
    }

    // --- Ivy ---

    Command lowerArmAndCollect() {
        return instant(() -> {
            aiming = false;
            robot.armHinge.moveToCollectingPosition();
            robot.shooter.stop();
            robot.flipper.doCollectRPM();
            robot.gate.close();
        }).requiring(robot.chassis, robot.armHinge, robot.shooter, robot.flipper, robot.gate);
    }

    Command lowerArmAndStopCollecting() {
        return instant(() -> {
            aiming = false;
            robot.armHinge.moveToCollectingPosition();
            robot.shooter.stop();
            robot.flipper.stop();
            robot.gate.close();
        }).requiring(robot.chassis, robot.armHinge, robot.shooter, robot.flipper, robot.gate);
    }

    Command ejectSequence() {
        return sequential(
                instant(() -> {
                    aiming = false;
                    robot.armHinge.moveToCollectingPosition();
                    robot.shooter.stop();
                    robot.flipper.stop();
                    robot.gate.close();
                }),
                waitUntil(robot.armHinge::isAtTargetAngle),
                instant(() -> {
                    robot.flipper.doEjectRPM();
                    robot.gate.open();
                })
        ).requiring(robot.chassis, robot.armHinge, robot.shooter, robot.flipper, robot.gate);
    }

    Command setUpForFlower() {
        return instant(() -> {
            aiming = false;
            robot.armHinge.moveToFlowerPosition();
            robot.shooter.stop();
            robot.flipper.stop();
            robot.gate.close();
        }).requiring(robot.chassis, robot.armHinge, robot.shooter, robot.flipper, robot.gate);
    }

    Command raiseArmAndTakeAim() {
        return instant(() -> {
            aiming = true;
            robot.armHinge.lookAtHive();
            robot.shooter.doGeneralShootRPM();
            robot.flipper.stop();
            robot.gate.close();
            robot.limelight.switchToAllianceHivePipeline();
        }).requiring(robot.chassis, robot.armHinge, robot.shooter, robot.flipper, robot.gate);
    }

    Command infiniteAimAndShoot() {
        return sequential(
                raiseArmAndTakeAim(),
                infinite(() -> {
                    if (robot.isReadyToShoot()) {
                        robot.gate.open();
                        robot.flipper.doShootRPM();
                    } else {
                        robot.gate.close();
                        robot.flipper.stop();
                    }
                })
        ).requiring(robot.chassis, robot.armHinge, robot.shooter, robot.flipper, robot.gate);
    }
}