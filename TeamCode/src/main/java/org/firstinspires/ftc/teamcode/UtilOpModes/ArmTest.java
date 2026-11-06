package org.firstinspires.ftc.teamcode.UtilOpModes;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.IMU;

import org.firstinspires.ftc.generalUtilities.Blackboard;
import org.firstinspires.ftc.teamcode.Arm;
import org.firstinspires.ftc.teamcode.Chassis;
import org.firstinspires.ftc.teamcode.ControlHub;
import org.firstinspires.ftc.teamcode.Limelight;

@com.qualcomm.robotcore.eventloop.opmode.TeleOp(name = "Arm Test", group="Util")
public class ArmTest extends OpMode {

    ControlHub controlHub;

    IMU imu;
    Limelight limelight;
    Chassis chassis;
    Arm arm;

    double targetAngle = 100;

    @Override
    public void init() {
        controlHub = new ControlHub(hardwareMap);

        imu = hardwareMap.get(IMU.class, "imu");
        limelight = new Limelight(hardwareMap);
        chassis = new Chassis(hardwareMap);
        arm = new Arm(hardwareMap, limelight);
        arm.closeGate();

        imu.resetYaw();
    }

    public void init_loop() {
        limelight.updateData(); // This line is vital, and should be called first in the loop
        controlHub.processBotIdentificationTelemetry(telemetry);
        Blackboard.initLoopProcess(telemetry, gamepad1);
    }

    public void start() {
        arm.startIdleMode();
        limelight.lookForBestAimingPipeline();
    }

    public void loop() {
        limelight.updateData(); // This line is vital, and should be called first in the loop

        arm.armHinge.setTargetAngleDegrees(targetAngle);
        arm.armHinge.driveArmToTargetAngle();

//        if (gamepad1.dpadUpWasPressed()) {
//            targetAngle += 5;
//        } else if (gamepad1.dpadDownWasPressed()) {
//            targetAngle -= 5;
//        }
        targetAngle = gamepad1.left_stick_y * 60 + 90;

        if (gamepad1.aWasPressed()) {
            arm.armHinge.kP -= 0.01;
        } else if (gamepad1.yWasPressed()) {
            arm.armHinge.kP += 0.01;
        } else if (gamepad1.xWasPressed()) {
            arm.armHinge.kG -= 0.01;
        } else if (gamepad1.bWasPressed()) {
            arm.armHinge.kG += 0.01;
        }


        handleTelemetry();
    }

    public void handleTelemetry() {
        telemetry.addData("Current arm angle (50°-135° range)", arm.armHinge.getArmAngleDegrees());
        telemetry.addData("Target angle", targetAngle);
        telemetry.addLine("  [DPAD-UP = higher, DPAD-DOWN = lower]");

        telemetry.addLine();
        telemetry.addData("kP", arm.armHinge.kP);
        telemetry.addLine("[A = -0.01, Y = +0.01]");

        telemetry.addLine();
        telemetry.addData("kG", arm.armHinge.kG);
        telemetry.addLine("[X = -0.01, B = +0.01]");

        telemetry.update();
    }
}
