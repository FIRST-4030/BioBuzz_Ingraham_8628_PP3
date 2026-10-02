package org.firstinspires.ftc.teamcode.UtilOpModes;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.IMU;

import org.firstinspires.ftc.generalUtilities.Blackboard;
import org.firstinspires.ftc.teamcode.Arm;
import org.firstinspires.ftc.teamcode.Chassis;
import org.firstinspires.ftc.teamcode.ControlHub;
import org.firstinspires.ftc.teamcode.Limelight;

@TeleOp(name = "Debug TeleOp", group="Util")
public class DebugTeleOp extends OpMode {

    ControlHub controlHub;

    IMU imu;
    Limelight limelight;
    Chassis chassis;
    Arm arm;

    enum DriveControlMode {
        MANUAL,
        AUTOMATIC,
    }

    DriveControlMode driveControlMode = DriveControlMode.MANUAL;

    @Override
    public void init() {
        controlHub = new ControlHub();

        imu = hardwareMap.get(IMU.class, "imu");
        limelight = new Limelight(telemetry);
        limelight.init(hardwareMap);
        chassis = new Chassis(hardwareMap);
        arm = new Arm(hardwareMap, limelight);
    }

    public void init_loop() {
        controlHub.processBotIdentificationTelemetry(telemetry);
        Blackboard.initLoopProcess(telemetry, gamepad1);
    }

    public void start() {
        arm.activateCollectingMode();
        limelight.switchToBestAimingPipeline();
    }

    public void loop() {
        handleModeSwitchingControls();

        switch (driveControlMode) {
            case MANUAL:
                chassis.drive(gamepad1.left_stick_y, -gamepad1.left_stick_x, gamepad1.right_stick_x);
                break;
            case AUTOMATIC:
                // TODO: Better PID equation for this instead of just a lerp
                chassis.drive(gamepad1.left_stick_y, -gamepad1.left_stick_x, limelight.getXError() / 35);
                break;
        }

        arm.update();
        handleTelemetry();
    }

    public void handleModeSwitchingControls() {
        if (gamepad1.rightBumperWasPressed()) {
            arm.activateAimingMode();
        } else if (gamepad1.leftBumperWasPressed()) {
            arm.activateCollectingMode();
        }

        if (gamepad1.bWasPressed()) {
            driveControlMode = DriveControlMode.MANUAL;
        } else if (gamepad1.aWasPressed()) {
            driveControlMode = DriveControlMode.AUTOMATIC;
        }

    }

    public void handleTelemetry() {
        // TODO: Tx telemetry

        telemetry.addLine();

        telemetry.addData("Arm mode", arm.getArmMode());
        telemetry.addData("Driving mode", driveControlMode);
        telemetry.addLine();
        telemetry.addData("Potentiometer voltage", arm.getPotentiometerVoltage());
        telemetry.addData("Current arm angle (degrees)", arm.getCurrentArmAngleDegrees());

        telemetry.update();
    }
}
