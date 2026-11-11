package org.firstinspires.ftc.teamcode.Hardware;

import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;

public class Gate {

    public Servo servo;

    private static final double SERVO_CLOSED_POSITION = 0.75;
    private static final double SERVO_OPEN_POSITION = 0.15;

    public Gate(HardwareMap hardwareMap) {
        servo = hardwareMap.get(Servo.class, "gate_servo");
    }

    public void close() {
        servo.setPosition(SERVO_CLOSED_POSITION);
    }

    public void open() {
        servo.setPosition(SERVO_OPEN_POSITION);
    }
}
