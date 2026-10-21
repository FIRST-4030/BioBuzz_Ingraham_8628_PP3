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
    State mainState;

    Behavior shootGameObjects, activateIdleMode, driveForwardASmidge;

    @Override
    public void init() {
        controlHub = new ControlHub();

        imu = hardwareMap.get(IMU.class, "imu");
        limelight = new Limelight(hardwareMap);
        chassis = new Chassis(hardwareMap);
        arm = new Arm(hardwareMap, limelight);

        shootGameObjects = GroupBuilder.create()
                .sequential("Prepare for shot")
                    .add(new InstantBehavior(() -> {
                        arm.aimingMode();
                        arm.resetShot();
                    }))
                    .add(new WaitUntil(() -> arm.readyToShoot()))
                    .add(new WaitMS(500))
                .end()
                .build();

        activateIdleMode = new InstantBehavior(() -> {
            arm.idleMode();
        }, "Activate idle mode");

        driveForwardASmidge = GroupBuilder.create()
                .sequential("Drive forward a smidge")
                    .parallel(ParallelGroup.CompletionCondition.FIRST_IN_LIST)
                        .add(new WaitMS(250))
                        .add(new UpdatingBehavior(() -> {
                            chassis.drive(-1, 0, 0);
                        }))
                    .end()
                    .parallel(ParallelGroup.CompletionCondition.FIRST_IN_LIST)
                        .add(new WaitMS(1250))
                        .add(new UpdatingBehavior(() -> {
                            chassis.drive(0, -1, 0);
                        }))
                    .end()
                    .add(new InstantBehavior(() -> chassis.stopMotors()))
                .end()
                .build();



        mainStateMachine = new StateMachine("Main");

        mainState = new TaskState(
                GroupBuilder.create()
                        .sequential("Autonomous sequence")
                            .add(shootGameObjects)
                            .add(activateIdleMode)
                            .add(driveForwardASmidge)
                        .end()
                        .build(),
                () -> null
        );
    }

    public void init_loop() {
        limelight.updateData(); // This line is vital, and should be called first in the loop
        controlHub.processBotIdentificationTelemetry(telemetry);
        Blackboard.initLoopProcess(telemetry, gamepad1);
    }

    public void start() {
        arm.collectingMode();
        limelight.lookForBestAimingPipeline();

        mainStateMachine.setInitialState(mainState);
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
