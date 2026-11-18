package org.firstinspires.ftc.teamcode.OpModes;

import static com.pedropathing.api.Paths.curve;
import static com.pedropathing.api.Paths.line;
import static com.pedropathing.ivy.commands.Commands.conditional;
import static com.pedropathing.ivy.commands.Commands.infinite;
import static com.pedropathing.ivy.commands.Commands.instant;
import static com.pedropathing.ivy.commands.Commands.waitMs;
import static com.pedropathing.ivy.commands.Commands.waitUntil;
import static com.pedropathing.ivy.groups.Groups.deadline;
import static com.pedropathing.ivy.groups.Groups.sequential;

import com.pedropathing.ivy.Command;

import org.firstinspires.ftc.generalUtilities.Blackboard;
import org.firstinspires.ftc.teamcode.Hardware.Limelight;
import org.firstinspires.ftc.teamcode.Robot.RobotOpMode;
import org.firstinspires.ftc.teamcode.pedro.Poses;

import java.util.Locale;

@com.qualcomm.robotcore.eventloop.opmode.Autonomous(name="Auto")
public class UprightHiveAuto extends RobotOpMode {

    enum HiveTipState {
        TIPPED_TOWARDS_GARDEN,
        TIPPED_AWAY_FROM_GARDEN,
    }

    HiveTipState hiveTipState = HiveTipState.TIPPED_TOWARDS_GARDEN;

    @Override
    public void init() {
        super.init();
    }

    @Override
    public void init_loop() {
        robot.controlHub.processBotIdentificationTelemetry(robot.telemetry);
        Blackboard.initLoopProcess(robot.telemetry, gamepad1);
        super.init_loop();
    }

    @Override
    public void start() {
        robot.chassis.follower.setPose(Poses.nearGardenStartingPose);

        sequential(
                goToHiveFromStart(),
                shoot(),
                collectGarden(),
                raiseArmToCheckHive(),
                goToHiveNearGarden(),
                checkHiveTip(),
//                conditional(
//                        () -> hiveTipState == HiveTipState.TIPPED_AWAY_FROM_GARDEN,
//
//                ),
                lowerArmAndStopCollecting()
        ).schedule();
    }

    @Override
    public void loop() {
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

        robot.telemetry.addData("Hive tip state", hiveTipState);
        robot.telemetry.addData("x", robot.chassis.follower.pose().x());
        robot.telemetry.addData("y", robot.chassis.follower.pose().y());

        robot.telemetry.addLine("-------");
        robot.telemetry.addData("Current arm angle (degrees)", robot.armHinge.getArmAngleDegrees());
        robot.telemetry.addData("Target arm angle (degrees)", robot.armHinge.getClippedTargetAngleDegrees());

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

    Command shoot() {
        return deadline(waitMs(2000), infiniteAimAndShoot());
    }

    Command goToHiveFromStart() {
        return robot.chassis.followPath(
                line(Poses.nearGardenStartingPose, Poses.hiveNearGarden)
                        .linear(Poses.nearGardenStartingPose, Poses.hiveNearGarden));
    }

    Command goToHiveNearGarden() {
        return robot.chassis.followPath(
                        line(Poses.gardenCollect, Poses.hiveNearGarden)
                                .linear(Poses.gardenCollect, Poses.hiveNearGarden));
    }

    Command goToHiveFarFromGarden() {
        return robot.chassis.followPath(
                line(Poses.gardenCollect, Poses.hiveNearGarden)
                        .linear(Poses.gardenCollect, Poses.hiveNearGarden));
    }

    Command goToHiveFarFromGardenFromGarden() {
        return robot.chassis.followPath(
                line(Poses.gardenCollect, Poses.hiveFarFromGarden)
                        .linear(Poses.gardenCollect, Poses.hiveFarFromGarden));
    }

    Command collectGarden() {
        return sequential(
                lowerArmAndStopCollecting(),
                robot.chassis.followPath(
                        curve(Poses.hiveNearGarden, Poses.controlPointToGarden, Poses.gardenSetup)
                                .constant(Poses.gardenSetup)),
                robot.chassis.followPath(
                        line(Poses.gardenSetup, Poses.gardenCollect)
                                .linear(Poses.gardenSetup, Poses.gardenCollect))
        );
    }

    Command lowerArmAndCollect() {
        return instant(() -> {
            robot.chassis.normalDrive();
            robot.armHinge.moveToCollectingPosition();
            robot.shooter.stop();
            robot.flipper.doCollectRPM();
            robot.gate.close();
        }).requiring(robot.chassis, robot.armHinge, robot.shooter, robot.flipper, robot.gate);
    }

    Command lowerArmAndStopCollecting() {
        return instant(() -> {
            robot.chassis.normalDrive();
            robot.armHinge.moveToCollectingPosition();
            robot.shooter.stop();
            robot.flipper.stop();
            robot.gate.close();
        }).requiring(robot.chassis, robot.armHinge, robot.shooter, robot.flipper, robot.gate);
    }

    Command ejectSequence() {
        return sequential(
                instant(() -> {
                    robot.chassis.normalDrive();
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
            robot.chassis.preciseDrive();
            robot.armHinge.moveToFlowerPosition();
            robot.shooter.stop();
            robot.flipper.stop();
            robot.gate.close();
        }).requiring(robot.chassis, robot.armHinge, robot.shooter, robot.flipper, robot.gate);
    }

    Command raiseArmToCheckHive() {
        return instant(() -> {
            robot.chassis.lockedOnDrive();
            robot.armHinge.lookAtHive();
            robot.shooter.stop();
            robot.flipper.stop();
            robot.gate.close();
            robot.limelight.switchToAllianceHivePipeline();
        }).requiring(robot.chassis, robot.armHinge, robot.shooter, robot.flipper, robot.gate);
    }

    Command checkHiveTip() {
        return instant(() -> {
            if (robot.limelight.canSeeHiveCell(Limelight.HiveCell.RED_AUDIENCE) ||
                    robot.limelight.canSeeHiveCell(Limelight.HiveCell.BLUE_SCORING)) {
                hiveTipState = HiveTipState.TIPPED_TOWARDS_GARDEN;
            } else if (robot.limelight.canSeeHiveCell(Limelight.HiveCell.BLUE_AUDIENCE) ||
                    robot.limelight.canSeeHiveCell(Limelight.HiveCell.RED_SCORING)) {
                hiveTipState = HiveTipState.TIPPED_AWAY_FROM_GARDEN;
            }
        });
    }

    Command raiseArmAndTakeAim() {
        return instant(() -> {
            robot.chassis.lockedOnDrive();
            robot.armHinge.lookAtHive();
            robot.shooter.doShootRPM();
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
