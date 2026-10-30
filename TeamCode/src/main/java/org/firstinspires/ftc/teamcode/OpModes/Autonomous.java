package org.firstinspires.ftc.teamcode.OpModes;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.IMU;

import org.firstinspires.ftc.generalUtilities.Blackboard;
import org.firstinspires.ftc.teamcode.Arm;
import org.firstinspires.ftc.teamcode.Chassis;
import org.firstinspires.ftc.teamcode.ControlHub;
import org.firstinspires.ftc.teamcode.Limelight;
import org.firstinspires.ftc.teamcode.TempBehaviorSystem.Behavior;
import org.firstinspires.ftc.teamcode.TempBehaviorSystem.GroupBuilder;
import org.firstinspires.ftc.teamcode.TempBehaviorSystem.ParallelGroup;
import org.firstinspires.ftc.teamcode.TempBehaviorSystem.StateMachine.State;
import org.firstinspires.ftc.teamcode.TempBehaviorSystem.StateMachine.StateMachine;
import org.firstinspires.ftc.teamcode.TempBehaviorSystem.StateMachine.TaskState;
import org.firstinspires.ftc.teamcode.TempBehaviorSystem.UserBehaviors.InstantBehavior;
import org.firstinspires.ftc.teamcode.TempBehaviorSystem.UserBehaviors.UpdatingBehavior;
import org.firstinspires.ftc.teamcode.TempBehaviorSystem.UserBehaviors.WaitMS;
import org.firstinspires.ftc.teamcode.TempBehaviorSystem.UserBehaviors.WaitUntil;

@com.qualcomm.robotcore.eventloop.opmode.Autonomous(name = "Autonomous", group="Util")
public class Autonomous extends OpMode {

    ControlHub controlHub;

    IMU imu;
    Limelight limelight;
    Chassis chassis;
    Arm arm;

    StateMachine mainStateMachine;
    State shootAndParkState, forwardASmidgeState;

    Behavior shootGameObjects, activateIdleMode, park, driveForwardASmidge;

    enum StartingPosition {
        FACING_TIPPED_HIVE,
        FACING_UPRIGHT_HIVE
    }

    StartingPosition startingPosition = StartingPosition.FACING_TIPPED_HIVE;

    @Override
    public void init() {
        controlHub = new ControlHub(hardwareMap);

        imu = hardwareMap.get(IMU.class, "imu");
        limelight = new Limelight(hardwareMap);
        chassis = new Chassis(hardwareMap);
        arm = new Arm(hardwareMap, limelight);
        arm.closeGate();

        shootGameObjects = GroupBuilder.create()
                .sequential("Prepare for shot")
                    .add(new InstantBehavior(() -> {
                        arm.startAimingMode();
                        arm.resetShot();
                    }))
                    .add(new WaitUntil(() -> arm.readyToShoot()))
                    .add(new WaitMS(1200))
                .end()
                .build();

        activateIdleMode = new InstantBehavior(() -> {
            arm.startIdleMode();
        }, "Activate idle mode");

        park = GroupBuilder.create()
                .sequential("Park")
                    .parallel(ParallelGroup.CompletionCondition.FIRST_IN_LIST)
                        .add(new WaitMS(1500))
                        .add(new UpdatingBehavior(() -> {
                            chassis.drive(1, -1, 0);
                        }))
                    .end()
                    .parallel(ParallelGroup.CompletionCondition.FIRST_IN_LIST)
                        .add(new WaitMS(1100))
                        .add(new UpdatingBehavior(() -> {
                            chassis.drive(1, 0, 0);
                        }))
                    .end()
                    .add(new InstantBehavior(() -> chassis.stopMotors()))
                    .add(new WaitMS(200))
                    .parallel(ParallelGroup.CompletionCondition.FIRST_IN_LIST)
                        .add(new WaitMS(150))
                        .add(new UpdatingBehavior(() -> {
                            chassis.drive(0, -1, 0);
                        }))
                    .end()
                    .add(new InstantBehavior(() -> chassis.stopMotors()))
                .end()
                .build();

        driveForwardASmidge = GroupBuilder.create()
                .sequential()
                    .parallel(ParallelGroup.CompletionCondition.FIRST_IN_LIST)
                        .add(new WaitMS(400))
                        .add(new UpdatingBehavior(() -> {
                            chassis.drive(1, 0, 0);
                        }))
                    .end()
                    .add(new InstantBehavior(() -> chassis.stopMotors()))
                    .add(new WaitMS(200))
                    .parallel(ParallelGroup.CompletionCondition.FIRST_IN_LIST)
                        .add(new WaitMS(1150))
                        .add(new UpdatingBehavior(() -> {
                            chassis.drive(0, 1, 0);
                        }))
                    .end()
                    .add(new InstantBehavior(() -> chassis.stopMotors()))
                .end()
                .build();


        mainStateMachine = new StateMachine("Main");

        shootAndParkState = new TaskState(
                GroupBuilder.create()
                        .sequential("Autonomous sequence")
                            .add(shootGameObjects)
                            .add(activateIdleMode)
                            .add(park)
                        .end()
                        .build(),
                () -> null
        );

        forwardASmidgeState = new TaskState(
                driveForwardASmidge,
                () -> null
        );
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
        arm.startCollectingMode();
        limelight.lookForBestAimingPipeline();

        if (startingPosition == StartingPosition.FACING_TIPPED_HIVE) {
            mainStateMachine.setInitialState(forwardASmidgeState);
            arm.startIdleMode();
        } else {
            mainStateMachine.setInitialState(shootAndParkState);
        }
        mainStateMachine.enter();
    }

    public void loop() {
        limelight.updateData(); // This line is vital, and should be called first in the loop
        mainStateMachine.update();
        arm.update();

        handleTelemetry();
    }

    public void handleTelemetry() {
        mainStateMachine.processTelemetry(telemetry, "");

        telemetry.addLine();

        if (limelight.resultHasGoodTargets()) {
            telemetry.addData("On target", limelight.isOnTarget());
            telemetry.addData("Tx", limelight.getXError());
            telemetry.addData("Ty", limelight.getYError());
            telemetry.addData("Ta", limelight.getTa());
        } else {
            telemetry.addLine("No good targets visible");
        }

        telemetry.update();
    }
}
