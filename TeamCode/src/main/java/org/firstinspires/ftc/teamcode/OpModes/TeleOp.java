package org.firstinspires.ftc.teamcode.OpModes;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.IMU;

import org.firstinspires.ftc.generalUtilities.Blackboard;
import org.firstinspires.ftc.teamcode.Arm;
import org.firstinspires.ftc.teamcode.Chassis;
import org.firstinspires.ftc.teamcode.ControlHub;
import org.firstinspires.ftc.teamcode.Limelight;

@com.qualcomm.robotcore.eventloop.opmode.TeleOp(name = "TeleOp", group="Util")
public class TeleOp extends OpMode {
    ControlHub controlHub;
    Limelight limelight;
    Chassis chassis;
    Arm arm;

    @Override
    public void init() {
        controlHub = new ControlHub(hardwareMap);
        limelight = new Limelight(hardwareMap);
        chassis = new Chassis(hardwareMap);
        arm = new Arm(hardwareMap, limelight);
    }

    public void init_loop() {
        limelight.updateData(); // This line is vital, and should be called first in the loop
        controlHub.processBotIdentificationTelemetry(telemetry);
        Blackboard.initLoopProcess(telemetry, gamepad1);

        telemetry.update();
    }

    public void start() {
        limelight.lookForHives();
    }

    public void loop() {
        limelight.updateData(); // This line is vital, and should be called first in the loop
    }
}
