package org.firstinspires.ftc.teamcode.UtilOpModes;

import com.qualcomm.hardware.limelightvision.LLResultTypes;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.IMU;

import org.firstinspires.ftc.generalUtilities.Blackboard;
import org.firstinspires.ftc.teamcode.Arm;
import org.firstinspires.ftc.teamcode.Chassis;
import org.firstinspires.ftc.teamcode.ControlHub;
import org.firstinspires.ftc.teamcode.Limelight;

import java.util.List;

@TeleOp(name = "Debug TeleOp", group="Util")
public class DebugTeleOp extends OpMode {

    ControlHub controlHub;

    IMU imu;
    Limelight limelight;
    Chassis chassis;
    Arm arm;

    enum DriveControlMode {
        COLLECTING,
        AIMING,
    }

    DriveControlMode driveControlMode = DriveControlMode.COLLECTING;

    @Override
    public void init() {
        controlHub = new ControlHub();

        imu = hardwareMap.get(IMU.class, "imu");
        limelight = new Limelight(hardwareMap);
        chassis = new Chassis(hardwareMap);
        arm = new Arm(hardwareMap, limelight);
    }

    public void init_loop() {
        limelight.updateData(); // This line is vital, and should be called first in the loop
        controlHub.processBotIdentificationTelemetry(telemetry);
        Blackboard.initLoopProcess(telemetry, gamepad1);
    }

    public void start() {
        arm.collectingPositionMode();
        limelight.lookForBestAimingPipeline();
    }

    public void loop() {
        limelight.updateData(); // This line is vital, and should be called first in the loop
        handleModeSwitchingControls();

        switch (driveControlMode) {
            case COLLECTING:
//                limelight.lookForBestCollectingPipeline();
                chassis.drive(gamepad1.left_stick_y, -gamepad1.left_stick_x, gamepad1.right_stick_x);
                break;
            case AIMING:
                limelight.lookForBestAimingPipeline();
                chassis.lockedOnDrive(gamepad1.left_stick_y, -gamepad1.left_stick_x, gamepad1, limelight);
                break;
            default:
                chassis.stopMotors();
                break;
        }

        arm.update();
        handleTelemetry();
    }

    public void handleModeSwitchingControls() {
        if (gamepad1.leftBumperWasPressed()) {
            driveControlMode = DriveControlMode.COLLECTING;
            arm.collectingPositionMode();
        } else if (gamepad1.rightBumperWasPressed()) {
            driveControlMode = DriveControlMode.AIMING;
            arm.aimingPositionMode();
        }
    }

    public void handleTelemetry() {
        telemetry.addLine("LB: Aiming mode, RB: Collecting mode");
        telemetry.addLine("-------");
        telemetry.addData("Arm mode", arm.getArmPositionMode());
        telemetry.addData("Driving mode", driveControlMode);
        telemetry.addData("Current arm angle (degrees)", arm.getArmAngleDegrees());

        telemetry.addLine();

        telemetry.addData("Alliance", Blackboard.getAlliance());
        telemetry.addData("Pipeline", limelight.getCurrentPipelineName());
        telemetry.addData("MS since pipeline switch", limelight.getTimeSinceLastPipelineSwitch().milliseconds());

        telemetry.addLine();

        if (limelight.resultHasGoodTargets()) {
            List<LLResultTypes.FiducialResult> fiducials = limelight.getLatestResult().getFiducialResults();
            telemetry.addData("On target", limelight.isOnTarget());
            telemetry.addData("Tx", limelight.getXError());
            telemetry.addData("Ty", limelight.getYError());
        } else {
            telemetry.addLine("No good targets visible");
        }

        telemetry.update();
    }
}
