package org.firstinspires.ftc.teamcode.UtilOpModes;

import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.IMU;

import org.firstinspires.ftc.generalUtilities.Blackboard;
import org.firstinspires.ftc.teamcode.Hardware.Chassis;
import org.firstinspires.ftc.teamcode.Hardware.LegacyArm;
import org.firstinspires.ftc.teamcode.Hardware.ControlHub;
import org.firstinspires.ftc.teamcode.Hardware.Limelight;

@Disabled
@com.qualcomm.robotcore.eventloop.opmode.TeleOp(name = "Arm Test", group="Util")
public class ArmTest extends OpMode {

    ControlHub controlHub;

    IMU imu;
    Limelight limelight;
    Chassis chassis;
    LegacyArm legacyArm;

    double targetAngle = 100;

    @Override
    public void init() {
        controlHub = new ControlHub(hardwareMap);

        imu = hardwareMap.get(IMU.class, "imu");
        limelight = new Limelight(hardwareMap);
        chassis = new Chassis(hardwareMap);
        legacyArm = new LegacyArm(hardwareMap, limelight);
        legacyArm.closeGate();

        imu.resetYaw();
    }

    public void init_loop() {
        limelight.updateData(); // This line is vital, and should be called first in the loop
        controlHub.processBotIdentificationTelemetry(telemetry);
        Blackboard.initLoopProcess(telemetry, gamepad1);
    }

    public void start() {
        legacyArm.startIdleMode();
        limelight.lookForBestAimingPipeline();
    }

    public void loop() {
        limelight.updateData(); // This line is vital, and should be called first in the loop

        legacyArm.armHinge.setTargetAngleDegrees(targetAngle);
        legacyArm.armHinge.driveArmToTargetAngle();

//        if (gamepad1.dpadUpWasPressed()) {
//            targetAngle += 5;
//        } else if (gamepad1.dpadDownWasPressed()) {
//            targetAngle -= 5;
//        }
        targetAngle = gamepad1.left_stick_y * 60 + 90;

        if (gamepad1.aWasPressed()) {
            legacyArm.armHinge.kP -= 0.01;
        } else if (gamepad1.yWasPressed()) {
            legacyArm.armHinge.kP += 0.01;
        } else if (gamepad1.xWasPressed()) {
            legacyArm.armHinge.kG -= 0.01;
        } else if (gamepad1.bWasPressed()) {
            legacyArm.armHinge.kG += 0.01;
        }


        handleTelemetry();
    }

    public void handleTelemetry() {
        telemetry.addData("Current arm angle (50°-135° range)", legacyArm.armHinge.getArmAngleDegrees());
        telemetry.addData("Target angle", targetAngle);
        telemetry.addLine("  [DPAD-UP = higher, DPAD-DOWN = lower]");

        telemetry.addLine();
        telemetry.addData("kP", legacyArm.armHinge.kP);
        telemetry.addLine("[A = -0.01, Y = +0.01]");

        telemetry.addLine();
        telemetry.addData("kG", legacyArm.armHinge.kG);
        telemetry.addLine("[X = -0.01, B = +0.01]");

        telemetry.update();
    }
}
