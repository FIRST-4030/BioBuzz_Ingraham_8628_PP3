package org.firstinspires.ftc.teamcode.UtilOpModes;

import android.annotation.SuppressLint;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.AnalogInput;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Servo;

@TeleOp(name = "Arm Tester", group="Util")
public class ArmTester extends OpMode {

    DcMotor arm;
    AnalogInput armPotentiometer;

    double goalAngle = 70;

    @Override
    public void init() {
        arm = hardwareMap.get(DcMotor.class, "arm");
        armPotentiometer = hardwareMap.get(AnalogInput.class, "arm_potentiometer");
    }

    public void init_loop() {
    }

    public void start() {
    }

    public void loop() {
//        if (gamepad1.right_bumper) {
//            arm.setPower(0.6);
//        } else if (gamepad1.left_bumper) {
//            arm.setPower(-0.6);
//        } else {
//            arm.setPower(0);
//        }

        double predictedAngle = 106.57316 * armPotentiometer.getVoltage() + 27.68994;

        double power = (goalAngle - predictedAngle) / 20;

        arm.setPower(power);

        if (gamepad1.left_bumper) {
            goalAngle = 70;
        }

        if (gamepad1.right_bumper) {
            goalAngle = 145;
        }

        telemetry.addData("Potentiometer voltage", armPotentiometer.getVoltage());
        telemetry.addData("Predicted angle", predictedAngle);
        telemetry.update();
    }
}