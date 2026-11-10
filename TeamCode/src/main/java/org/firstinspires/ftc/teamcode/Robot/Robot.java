package org.firstinspires.ftc.teamcode.Robot;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.IMU;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.Hardware.Arm;
import org.firstinspires.ftc.teamcode.Hardware.Chassis;
import org.firstinspires.ftc.teamcode.Hardware.ControlHub;
import org.firstinspires.ftc.teamcode.Hardware.Limelight;

public class Robot {
    public final HardwareMap hardwareMap;
    public final Telemetry telemetry;

    public final ControlHub controlHub;
    public final IMU imu;
    public final Limelight limelight;
    public final Chassis chassis;
    public final Arm arm;

    public Robot(OpMode opMode) {
        hardwareMap = opMode.hardwareMap;
        telemetry = opMode.telemetry;

        controlHub = new ControlHub(hardwareMap);
        imu = hardwareMap.get(IMU.class, "imu");
        limelight = new Limelight(hardwareMap);
        chassis = new Chassis(hardwareMap);
        arm = new Arm(hardwareMap, limelight);
    }
}
