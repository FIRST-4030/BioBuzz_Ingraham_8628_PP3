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

    IMU imu;
    Limelight limelight;
    Chassis chassis;
    Arm arm;

    @Override
    public void init() {
        controlHub = new ControlHub(hardwareMap);

        imu = hardwareMap.get(IMU.class, "imu");
        limelight = new Limelight(hardwareMap);
        chassis = new Chassis(hardwareMap);
        arm = new Arm(hardwareMap, limelight);

        imu.resetYaw();
    }

    public void init_loop() {
        limelight.updateData(); // This line is vital, and should be called first in the loop
        controlHub.processBotIdentificationTelemetry(telemetry);
        Blackboard.initLoopProcess(telemetry, gamepad1);
    }

    public void start() {
        limelight.lookForHives();
    }

    public void loop() {
        limelight.updateData(); // This line is vital, and should be called first in the loop
        handleModeSwitchingControls();

        switch (arm.getArmMode()) {
            case IDLE:
            case COLLECTING:
                limelight.lookForPollen();
                chassis.drive(-gamepad1.left_stick_y, gamepad1.left_stick_x, gamepad1.right_stick_x);
                break;
            case AIMING:
                limelight.lookForHives();
                if (limelight.resultHasGoodTargets()) {
                    chassis.lockedOnDrive(-gamepad1.left_stick_y, gamepad1.left_stick_x, limelight);
                } else {
                    chassis.drive(-gamepad1.left_stick_y, gamepad1.left_stick_x, gamepad1.right_stick_x);
                }
                break;
            default:
                chassis.stopMotors();
                break;
        }

        arm.update();
        handleTelemetry();
    }

    public void handleModeSwitchingControls() {
        if (gamepad1.rightBumperWasPressed()) {
            arm.collectingMode();
        } else if (gamepad1.leftBumperWasPressed()) {
            arm.idleMode();
        } else if (gamepad1.rightTriggerWasPressed()) {
            arm.aimingMode();
        }

        if (gamepad1.dpadUpWasPressed()) {
            arm.tempShootPower += 0.05;
        } else if (gamepad1.dpadDownWasPressed()) {
            arm.tempShootPower -= 0.05;
        }

        arm.ejectOverride = gamepad1.left_trigger > 0.5;
    }

    public void handleTelemetry() {
        telemetry.addLine("LB: Idle, RB: Collect, RT: Aim + Shoot, LB: Eject");
        telemetry.addLine("Dpad: Up: Faster shooter, DOWN: Slower shooter");
        telemetry.addData("Shooter velocity", arm.tempShootPower);
        if (!(Blackboard.getAlliance() == Blackboard.Alliance.RED || Blackboard.getAlliance() == Blackboard.Alliance.BLUE)) {
            telemetry.addLine("WARNING: ALLIANCE UNSET! You should re-init!");
        } else {
            telemetry.addData("Alliance", Blackboard.getAlliance());
        }
        telemetry.addLine("-------");
        telemetry.addData("Mode", arm.getArmMode());
        telemetry.addData("Current arm angle (degrees)", arm.getArmAngleDegrees());
        telemetry.addData("At good height?", arm.hasReachedAimingHeight());
        telemetry.addData("Ready to shoot?", arm.readyToShoot());

        telemetry.addLine();

        telemetry.addData("Pipeline", limelight.getCurrentPipelineName());
        telemetry.addData("MS since pipeline switch", limelight.getTimeSinceLastPipelineSwitch().milliseconds());

        telemetry.addLine();

        if (limelight.resultHasGoodTargets()) {
            telemetry.addData("On target", limelight.isOnTarget());
            telemetry.addData("Tx", limelight.getXError());
            telemetry.addData("Ty", limelight.getYError());
        } else {
            telemetry.addLine("No good targets visible");
        }

        telemetry.update();
    }
}
