package org.firstinspires.ftc.teamcode.Robot;

import static com.pedropathing.ivy.commands.Commands.infinite;
import static com.pedropathing.ivy.commands.Commands.instant;
import static com.pedropathing.ivy.commands.Commands.waitMs;
import static com.pedropathing.ivy.commands.Commands.waitUntil;
import static com.pedropathing.ivy.groups.Groups.deadline;
import static com.pedropathing.ivy.groups.Groups.sequential;
import static com.pedropathing.ivy.pedro.PedroCommands.follow;
import static com.pedropathing.ivy.pedro.PedroCommands.hold;

import com.pedropathing.ivy.Command;
import com.pedropathing.math.Pose;
import com.pedropathing.paths.Path;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
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
}
