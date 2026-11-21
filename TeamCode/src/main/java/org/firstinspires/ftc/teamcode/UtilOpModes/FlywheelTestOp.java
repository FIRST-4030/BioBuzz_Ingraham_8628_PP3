package org.firstinspires.ftc.teamcode.UtilOpModes;

import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.IMU;

import org.firstinspires.ftc.generalUtilities.Blackboard;
import org.firstinspires.ftc.teamcode.Hardware.Chassis;
import org.firstinspires.ftc.teamcode.Hardware.ControlHub;
import org.firstinspires.ftc.teamcode.Hardware.Flywheel;
import org.firstinspires.ftc.teamcode.Hardware.Limelight;

@TeleOp(name = "Flywheel TestOp", group="Util")
public class FlywheelTestOp extends OpMode {

    ControlHub controlHub;

    IMU imu;
    Limelight limelight;
    Chassis chassis;
    Flywheel shooterFlywheel; // TODO: Wrong flywheel
    Flywheel shooterFlywheel2; // TODO: Wrong flywheel

    double targetVelocity = 0;
    double kVelocity = 0.000341;
    double kP = 0;

    boolean leftStickMode = false;

    @Override
    public void init() {
        controlHub = new ControlHub(hardwareMap);

        imu = hardwareMap.get(IMU.class, "imu");
        limelight = new Limelight(hardwareMap);
        chassis = new Chassis(hardwareMap, controlHub);
        shooterFlywheel = new Flywheel(hardwareMap, "shooter", DcMotorSimple.Direction.REVERSE);
        shooterFlywheel2 = new Flywheel(hardwareMap, "shooter_2", DcMotorSimple.Direction.FORWARD);
    }

    public void init_loop() {
        limelight.updateData(); // This line is vital, and should be called first in the loop
        controlHub.processBotIdentificationTelemetry(telemetry);
        Blackboard.initLoopProcess(telemetry, gamepad1);
    }

    public void start() {
        limelight.switchToAllianceHivePipeline();
        shooterFlywheel.stop();
        shooterFlywheel2.stop();
    }

    public void loop() {
        limelight.updateData(); // This line is vital, and should be called first in the loop
        shooterFlywheel.setTargetRPM(targetVelocity);
        shooterFlywheel2.setTargetRPM(targetVelocity);

        shooterFlywheel.setControllerValues(kP, kVelocity, 56, 50, true);
        shooterFlywheel2.setControllerValues(kP, kVelocity, 56, 50, true);

        shooterFlywheel.updatePower();
        shooterFlywheel2.updatePower();

        if (gamepad1.dpadUpWasPressed()) {
            leftStickMode = false;
            targetVelocity += 100;
        } else if (gamepad1.dpadDownWasPressed()) {
            leftStickMode = false;
            targetVelocity -= 100;
        } else if (gamepad1.dpadRightWasPressed()) {
            kP += 0.0001;
//            kVelocity += 0.000001;
        } else if (gamepad1.dpadLeftWasPressed()) {
            kP -= 0.0001;
//            kVelocity -= 0.000001;
        }

        if (gamepad1.left_stick_x != 0) {
            leftStickMode = true;
        }

        if (leftStickMode) {
            targetVelocity = gamepad1.left_stick_x * 2700;
        }

        handleTelemetry();
    }

    public void handleTelemetry() {
        telemetry.addLine("DPAD-UP: More velo, DPAD-DOWN: Less velo");
        telemetry.addLine("DPAD-RIGHT: More k-p, DPAD-LEFT: Less k-p");

        telemetry.addLine();
        telemetry.addData("Target velocity", targetVelocity);
        telemetry.addData("Actual velocity", shooterFlywheel.getRPM());
        telemetry.addData("K-velocity", "%.8f", kVelocity);
        telemetry.addData("K-P", kP);
        telemetry.addData("At speed?", shooterFlywheel.atSpeed());

        telemetry.addData("Ticks", shooterFlywheel.getTicks());

        telemetry.update();
    }
}
