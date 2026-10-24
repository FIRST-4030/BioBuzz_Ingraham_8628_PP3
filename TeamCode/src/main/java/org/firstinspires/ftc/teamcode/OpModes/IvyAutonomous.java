package org.firstinspires.ftc.teamcode.OpModes;

import static com.pedropathing.api.Paths.line;
import static com.pedropathing.ivy.Scheduler.schedule;
import static com.pedropathing.ivy.commands.Commands.instant;
import static com.pedropathing.ivy.commands.Commands.waitMs;
import static com.pedropathing.ivy.commands.Commands.waitUntil;
import static com.pedropathing.ivy.groups.Groups.deadline;
import static com.pedropathing.ivy.groups.Groups.sequential;
import static com.pedropathing.ivy.pedro.PedroCommands.follow;

import com.pedropathing.follower.Follower;
import com.pedropathing.ivy.Command;
import com.pedropathing.ivy.Scheduler;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.IMU;

import org.firstinspires.ftc.generalUtilities.Blackboard;
import org.firstinspires.ftc.teamcode.Arm;
import org.firstinspires.ftc.teamcode.Chassis;
import org.firstinspires.ftc.teamcode.ControlHub;
import org.firstinspires.ftc.teamcode.Limelight;
import org.firstinspires.ftc.teamcode.pedro.Poses;

@Disabled
@com.qualcomm.robotcore.eventloop.opmode.TeleOp(name = "TeleOp", group="Util")
public class IvyAutonomous extends OpMode {

    ControlHub controlHub;

    IMU imu;
    Limelight limelight;
    Chassis chassis;
    Arm arm;
    Follower follower;
    Poses poses;

    enum StartingPosition {
        FACING_TIPPED_HIVE,
        FACING_UPRIGHT_HIVE
    }

    StartingPosition startingPosition = StartingPosition.FACING_TIPPED_HIVE;

    Command aimAndShoot = sequential(
            instant(() -> {
                arm.aimingMode();
                arm.resetShot();
            }),
            waitUntil(() -> arm.readyToShoot()),
            waitMs(500)
    );

    Command parkFromFar = sequential(
            follow(follower, line(poses.coolPose, poses.amazingPose).linear(poses.coolPose, poses.amazingPose))
    );

    Command parkFromNear = sequential(
            follow(follower, line(poses.amazingPose, poses.coolPose).linear(poses.amazingPose, poses.coolPose))
    );

    Command uprightHiveSequence = sequential(
            aimAndShoot,
            parkFromFar
    );

    Command tippedHiveSequence = sequential(
            parkFromNear
    );

    @Override
    public void init() {
        Scheduler.reset();

        controlHub = new ControlHub(hardwareMap);

        imu = hardwareMap.get(IMU.class, "imu");
        limelight = new Limelight(hardwareMap);
        chassis = new Chassis(hardwareMap);
        arm = new Arm(hardwareMap, limelight);
        follower = controlHub.getRobotSpecificPedroConstants();

        arm.closeGate();
        imu.resetYaw();
    }

    public void init_loop() {
        limelight.updateData(); // This line is vital, and should be called first in the loop

        controlHub.processBotIdentificationTelemetry(telemetry);
        telemetry.addLine("RB + Y: HIVE UPRIGHT");
        telemetry.addLine("RB + A: HIVE DOWN");
        telemetry.addData("Start position:", startingPosition);
        telemetry.addLine();
        Blackboard.initLoopProcess(telemetry, gamepad1);

        if (gamepad1.right_bumper && gamepad1.yWasPressed()) {
            startingPosition = StartingPosition.FACING_UPRIGHT_HIVE;
        } else if (gamepad1.right_bumper && gamepad1.aWasPressed()) {
            startingPosition = StartingPosition.FACING_TIPPED_HIVE;
        }

        telemetry.update();
    }

    public void start() {
        arm.idleMode();
        limelight.lookForBestAimingPipeline();

        if (startingPosition == StartingPosition.FACING_UPRIGHT_HIVE) {
            schedule(uprightHiveSequence);
        } else {
            schedule(tippedHiveSequence);
        }
    }

    public void loop() {
        limelight.updateData(); // This line is vital, and should be called first in the loop

        Scheduler.execute();

        arm.update();
        handleTelemetry();
    }

    public void handleTelemetry() {
        telemetry.addData("Shooter velocity", arm.tempShootPower);
        if (!(Blackboard.getAlliance() == Blackboard.Alliance.RED || Blackboard.getAlliance() == Blackboard.Alliance.BLUE)) {
            telemetry.addLine("WARNING: ALLIANCE UNSET! You should re-init!");
        } else {
            telemetry.addData("Alliance", Blackboard.getAlliance());
        }
        telemetry.addLine("-------");
        telemetry.addData("Mode", arm.getArmMode());
        telemetry.addData("Current arm angle (degrees)", arm.getArmAngleDegrees());
        telemetry.addData("At good height?", arm.hasReachedAimingHeight());
        telemetry.addData("Ready to shoot?", arm.readyToShoot());

        telemetry.addLine();

        telemetry.addData("Pipeline", limelight.getCurrentPipelineName());
        telemetry.addData("MS since pipeline switch", limelight.getTimeSinceLastPipelineSwitch().milliseconds());

        telemetry.addLine();

        if (limelight.resultHasGoodTargets()) {
            telemetry.addData("On target", limelight.isOnTarget());
            telemetry.addData("Tx", limelight.getXError());
            telemetry.addData("Ty", limelight.getYError());
        } else {
            telemetry.addLine("No good targets visible");
        }

        telemetry.update();
    }
}
