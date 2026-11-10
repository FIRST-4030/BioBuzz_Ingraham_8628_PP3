package org.firstinspires.ftc.teamcode.OpModes;

import org.firstinspires.ftc.generalUtilities.Blackboard;
import org.firstinspires.ftc.teamcode.Robot.RobotOpMode;

@com.qualcomm.robotcore.eventloop.opmode.TeleOp(name="Ivy TeleOp")
public class TeleOp extends RobotOpMode {

    @Override
    public void init() {
        super.init();

        robot.arm.closeGate();
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
        robot.arm.startIdleMode();
        robot.limelight.lookForBestAimingPipeline();
    }

    @Override
    public void loop() {
        switch (robot.arm.getArmMode()) {
            case IDLE:
            case COLLECTING:
//                limelight.lookForBestCollectingPipeline();
                robot.limelight.lookForBestAimingPipeline(); // TODO: Better default?
                robot.chassis.drive(-gamepad1.left_stick_y, gamepad1.left_stick_x, gamepad1.right_stick_x);
                break;
            case AIMING:
                robot.limelight.lookForBestAimingPipeline();
                if (robot.limelight.resultHasGoodTargets()) {
                    robot.chassis.lockedOnDrive(-gamepad1.left_stick_y, gamepad1.left_stick_x, robot.limelight);
                } else {
                    robot.chassis.drive(-gamepad1.left_stick_y, gamepad1.left_stick_x, gamepad1.right_stick_x);
                }
                break;
            default:
                robot.chassis.stopMotors();
                break;
        }

        super.loop();
    }
}
