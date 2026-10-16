package org.firstinspires.ftc.teamcode.UtilOpModes;

import android.annotation.SuppressLint;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.Servo;

/**
 * Description of ServoTester.
 *
 * @author Dennis O'Brien
 * @date 11/13/2025
 */
@TeleOp(name = "ServoTester", group="Util")
public class ServoTester extends OpMode {
//    @Override
//    public void init() {
//
//    }
//
//    @Override
//    public void loop() {
//
//    }

    String DEVICE_NAME = "gate_servo";

    Servo   servo;
    double  position = 0.5;
    double increment = 0.05;

    @Override
    public void init() {
        servo = hardwareMap.get(Servo.class, DEVICE_NAME);
    }

    @SuppressLint("DefaultLocale")
    public void init_loop() {
        if (gamepad1.dpadUpWasReleased()) {
            position += increment;
        }

        if (gamepad1.dpadDownWasReleased()) {
            position -= increment;
        }

        servo.setPosition(0.5);

        telemetry.addLine(String.format("Servo will start at %.2f", position));
        telemetry.addLine("DPad Up: Increases servo start position");
        telemetry.addLine("DPad Down: Decreases servo start position");
        telemetry.update();
    }

    public void start() {
        servo.setPosition(position);
    }

    @SuppressLint("DefaultLocale")
    public void loop() {
        telemetry.addLine(String.format("DPad Left: Move servo down by %.2f",increment));
        telemetry.addLine(String.format("DPad Right: Move servo up by %.2f",increment));
        telemetry.addData("Servo Position", "%.2f", position);
        telemetry.update();

        if (gamepad1.dpadLeftWasReleased()) {
            position -= increment;
            position = Math.max(0., position);
            servo.setPosition(position);
        }

        if (gamepad1.dpadRightWasReleased()) {
            position += increment;
            position = Math.min(1., position);
            servo.setPosition(position);
        }
    }
}