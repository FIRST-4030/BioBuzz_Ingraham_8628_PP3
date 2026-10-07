package org.firstinspires.ftc.teamcode.UtilOpModes;

import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.IMU;

import org.firstinspires.ftc.generalUtilities.Blackboard;
import org.firstinspires.ftc.teamcode.Arm;
import org.firstinspires.ftc.teamcode.Chassis;
import org.firstinspires.ftc.teamcode.ControlHub;
import org.firstinspires.ftc.teamcode.Limelight;

@Disabled
@TeleOp(name = "Arm Potentiometer Test", group="Util")
public class PotentiometerTestOp extends OpMode {

    ControlHub controlHub;

    IMU imu;
    Limelight limelight;
    Chassis chassis;
    Arm arm;

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

        if (gamepad1.left_bumper) {
            arm.unsafePowerArmHinge(-0.5);
        } else if (gamepad1.right_bumper) {
            arm.unsafePowerArmHinge(0.5);
        } else {
            arm.unsafePowerArmHinge(0);
        }

        handleTelemetry();
    }

    public void handleTelemetry() {
        telemetry.addLine("RB: Rotate upward, LB: Rotate downward");

        telemetry.addLine();
        telemetry.addData("Potentiometer voltage", arm.getPotentiometerVoltage());
        telemetry.addData("Angle", arm.getArmAngleDegrees());

        telemetry.addLine();
        telemetry.addLine("USE CAUTION! This opMode does not limit the rotation");
        telemetry.addLine("of the arm and you can damage the robot if not careful.");
        telemetry.addLine("Keep arm within 45 degrees to 180 degrees from the ground.");

        telemetry.update();
    }
}
