package org.firstinspires.ftc.teamcode.OpModes;

import static com.pedropathing.api.Paths.curve;
import static com.pedropathing.api.Paths.line;
import static com.pedropathing.ivy.commands.Commands.infinite;
import static com.pedropathing.ivy.commands.Commands.instant;
import static com.pedropathing.ivy.commands.Commands.waitMs;
import static com.pedropathing.ivy.commands.Commands.waitUntil;
import static com.pedropathing.ivy.groups.Groups.deadline;
import static com.pedropathing.ivy.groups.Groups.sequential;
import static com.pedropathing.ivy.pedro.PedroCommands.follow;

import com.pedropathing.ivy.Command;

import org.firstinspires.ftc.generalUtilities.Blackboard;
import org.firstinspires.ftc.teamcode.Hardware.Limelight;
import org.firstinspires.ftc.teamcode.Robot.RobotOpMode;
import org.firstinspires.ftc.teamcode.pedro.Poses;

import java.util.Locale;

@com.qualcomm.robotcore.eventloop.opmode.Autonomous(name="Far From Garden Full Auto")
public class FarFromGardenFullAuto extends RobotOpMode {

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
        super.start();
        robot.follower.setPose(Poses.farFromGardenStartingPose);

        sequential(
                sequential(
                        goToHiveFromStart(),
                        raiseArmToCheckHive(),
                        waitUntilSeesHiveFarFromGarden(),
                        waitMs(500),
                        shoot(),

                        lowerArmAndStopCollecting(),
                        shoveOutFlowerPollenHaphazardly(),
                        lowerArmAndStopCollecting(),
                        waitUntil(() -> false) // Just sit and wait
                ).until(() -> getRuntime() > 23),
                lowerArmAndStopCollecting(),
                parkFromFarFromGarden()
        ).schedule();
    }

    @Override
    public void loop() {
        super.loop();
        handleTelemetry();
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
        robot.telemetry.addData("x", robot.follower.pose().x());
        robot.telemetry.addData("y", robot.follower.pose().y());
        robot.telemetry.addData("heading", robot.follower.pose().heading());

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

        robot.telemetry.update();
    }

    // --- Ivy ---

    Command waitUntilSeesHiveFarFromGarden() {
        return waitUntil(() ->
            robot.limelight.canSeeHiveCell(Limelight.HiveCell.BLUE_AUDIENCE) ||
                    robot.limelight.canSeeHiveCell(Limelight.HiveCell.RED_SCORING)
        );
    }

    Command parkFromNearGarden() {
        return follow(
                robot.follower,
                line(Poses.nearGardenParkSetup, Poses.nearGardenPark)
                        .linear(Poses.nearGardenParkSetup, Poses.nearGardenPark));
    }

    Command parkFromFarFromGarden() {
        return follow(
                robot.follower,
                line(Poses.farFromGardenParkSetup, Poses.farFromGardenPark)
                        .linear(Poses.farFromGardenParkSetup, Poses.farFromGardenPark));
    }

    Command shoot() {
        return deadline(waitMs(3000), infiniteAimAndShoot());
    }

    Command goToHiveFromStart() {
        return follow(
                robot.follower,
                line(Poses.farFromGardenStartingPose, Poses.hiveFarFromGarden)
                        .linear(Poses.farFromGardenStartingPose, Poses.hiveFarFromGarden));
    }

    Command collectGarden() {
        return sequential(
                lowerArmAndCollect(),
                follow(
                        robot.follower,
                        curve(Poses.hiveNearGarden, Poses.controlPointToGarden, Poses.gardenCollect)
                                .constant(Poses.gardenCollect)),
                waitMs(1000)
        );
    }

    Command shoveOutFlowerPollenHaphazardly() {
        return sequential(
                raiseArmForFlower(),
                follow(
                        robot.follower,
                        line(Poses.hiveFarFromGarden, Poses.nearGardenFlowerSetup)
                                .linear(Poses.hiveFarFromGarden, Poses.nearGardenFlowerSetup)
                ),
                doOneLoopOfFlowerShoving(),
                doOneLoopOfFlowerShoving(),
                doOneLoopOfFlowerShoving(),
                doOneLoopOfFlowerShoving(),
                follow(
                        robot.follower,
                        line(Poses.nearGardenFlowerSetup, Poses.hiveFarFromGarden)
                                .linear(Poses.nearGardenFlowerSetup, Poses.hiveFarFromGarden)
                )
        );
    }

    Command doOneLoopOfFlowerShoving() {
        return sequential(
                        follow(
                                robot.follower,
                                line(Poses.nearGardenFlowerSetup, Poses.nearGardenFlower1)
                                        .linear(Poses.nearGardenFlowerSetup, Poses.nearGardenFlower1)
                        ),
                        follow(
                                robot.follower,
                                line(Poses.nearGardenFlower1, Poses.nearGardenFlower2)
                                        .linear(Poses.nearGardenFlower1, Poses.nearGardenFlower2)
                        ),
                        follow(
                                robot.follower,
                                curve(Poses.nearGardenFlower2, Poses.nearGardenFlowerSetupControlPoint, Poses.nearGardenFlowerSetup)
                                        .linear(Poses.nearGardenFlower2, Poses.nearGardenFlowerSetup)
                        )
                );
    }

    Command lowerArmAndCollect() {
        return instant(() -> {
            robot.armHinge.moveToCollectingPosition();
            robot.shooter.stop();
            robot.flipper.doCollectRPM();
            robot.gate.close();
        }).requiring(robot.chassis, robot.armHinge, robot.shooter, robot.flipper, robot.gate);
    }

    Command lowerArmAndStopCollecting() {
        return instant(() -> {
            robot.armHinge.moveToCollectingPosition();
            robot.shooter.stop();
            robot.flipper.stop();
            robot.gate.close();
        }).requiring(robot.chassis, robot.armHinge, robot.shooter, robot.flipper, robot.gate);
    }

    Command ejectSequence() {
        return sequential(
                instant(() -> {
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
            robot.armHinge.moveToFlowerPosition();
            robot.shooter.stop();
            robot.flipper.stop();
            robot.gate.close();
        }).requiring(robot.chassis, robot.armHinge, robot.shooter, robot.flipper, robot.gate);
    }

    Command raiseArmToCheckHive() {
        return instant(() -> {
            robot.armHinge.lookAtHive();
            robot.shooter.stop();
            robot.flipper.stop();
            robot.gate.close();
            robot.limelight.switchToAllianceHivePipeline();
        }).requiring(robot.chassis, robot.armHinge, robot.shooter, robot.flipper, robot.gate);
    }

    Command raiseArmForFlower() {
        return instant(() -> {
            robot.armHinge.moveToFlowerPosition();
            robot.shooter.stop();
            robot.flipper.stop();
            robot.gate.close();
            robot.limelight.switchToAllianceHivePipeline();
        }).requiring(robot.chassis, robot.armHinge, robot.shooter, robot.flipper, robot.gate);
    }

    Command raiseArmAndTakeAim() {
        return instant(() -> {
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
                    robot.lockOntoHiveWhileStill();
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
