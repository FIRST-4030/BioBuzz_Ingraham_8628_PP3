package org.firstinspires.ftc.teamcode.UtilOpModes;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.Arm;

@TeleOp(name = "Arm Tester", group="Util")
public class ArmTester extends OpMode {

    Arm arm;

    @Override
    public void init() {
        arm = new Arm(hardwareMap);
    }

    public void init_loop() {
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

        arm.update();

        telemetry.addData("Arm mode", arm.getArmMode());
        telemetry.addLine();
        telemetry.addData("Potentiometer voltage", arm.getPotentiometerVoltage());
        telemetry.addData("Current arm angle (degrees)", arm.getCurrentArmAngleDegrees());

        telemetry.update();
    }
}