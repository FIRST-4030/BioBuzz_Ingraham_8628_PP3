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

    Chassis chassis;
    Arm arm;

    ControlHub controlHub;
    IMU imu;
    Limelight limelight;

    @Override
    public void init() {
        chassis = new Chassis(hardwareMap);
        arm = new Arm(hardwareMap);

        controlHub = new ControlHub();
        imu = hardwareMap.get(IMU.class, "imu");

        limelight = new Limelight(telemetry);
        limelight.init(hardwareMap);
    }

    public void init_loop() {
        controlHub.processBotIdentificationTelemetry(telemetry);
        Blackboard.initLoopProcess(telemetry, gamepad1);
    }

    public void start() {
        arm.activateCollectingMode();
    }

    public void loop() {
        if (gamepad1.rightBumperWasPressed()) {
            arm.activateAimingMode();
        } else if (gamepad1.leftBumperWasPressed()) {
            arm.activateCollectingMode();
        }

        chassis.drive(gamepad1.left_stick_y, gamepad1.left_stick_x, gamepad1.right_stick_x);
        arm.update();

        telemetry.addData("Best April Tag Tx", limelight.getBestHiveAprilTagOrNull().getTargetXPixels());
        telemetry.addLine();

        telemetry.addData("Arm mode", arm.getArmMode());
        telemetry.addLine();
        telemetry.addData("Potentiometer voltage", arm.getPotentiometerVoltage());
        telemetry.addData("Current arm angle (degrees)", arm.getCurrentArmAngleDegrees());

        telemetry.update();
    }
}
