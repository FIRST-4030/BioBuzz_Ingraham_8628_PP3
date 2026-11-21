package org.firstinspires.ftc.teamcode.Robot;

import static com.pedropathing.ivy.pedro.PedroCommands.hold;

import com.pedropathing.follower.Follower;
import com.pedropathing.follower.ManualDrive;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.IMU;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.Hardware.ArmHinge;
import org.firstinspires.ftc.teamcode.Hardware.Chassis;
import org.firstinspires.ftc.teamcode.Hardware.ControlHub;
import org.firstinspires.ftc.teamcode.Hardware.Flipper;
import org.firstinspires.ftc.teamcode.Hardware.Gate;
import org.firstinspires.ftc.teamcode.Hardware.Limelight;
import org.firstinspires.ftc.teamcode.Hardware.Shooter;

public class Robot {
    public final HardwareMap hardwareMap;
    public final Telemetry telemetry;

    public final ControlHub controlHub;
    public final IMU imu;
    public final Limelight limelight;
    public final Chassis chassis;
    public final Follower follower;

    public final Shooter shooter;
    public final Flipper flipper;
    public final Gate gate;
    public final ArmHinge armHinge;

    public Robot(OpMode opMode) {
        hardwareMap = opMode.hardwareMap;
        telemetry = opMode.telemetry;

        controlHub = new ControlHub(hardwareMap);
        imu = hardwareMap.get(IMU.class, "imu");
        limelight = new Limelight(hardwareMap);
        chassis = new Chassis(hardwareMap, controlHub);
        follower = controlHub.createRobotSpecificFollower();

        shooter = new Shooter(hardwareMap);
        flipper = new Flipper(hardwareMap);
        gate = new Gate(hardwareMap);
        armHinge = new ArmHinge(hardwareMap, limelight);
    }

    public Boolean isReadyToShoot() {
        return armHinge.isLookingForHive() &&
                limelight.isOnTarget() &&
                shooter.atSpeed();
    }

    public void arcadeDrive(Gamepad gamepad) {
        ManualDrive.driveOrHold(follower,
                -gamepad.left_stick_y, -gamepad.left_stick_x, -gamepad.right_stick_x);
    }

    public void lockedOntoHiveArcadeDrive(Gamepad gamepad) {
        if (limelight.resultHasGoodTargets()) {
            ManualDrive.driveOrHold(follower,
                    -gamepad.left_stick_y, -gamepad.left_stick_x, limelight.getXError() * -0.03);
        } else {
            arcadeDrive(gamepad);
        }
    }

    public void lockOntoHiveWhileStill() {
        if (limelight.resultHasGoodTargets()) {
            ManualDrive.driveOrHold(follower,
                    0, 0, limelight.getXError() * 0.03);
        } else {
            ManualDrive.driveOrHold(follower, 0, 0, 0);
        }
    }
}
