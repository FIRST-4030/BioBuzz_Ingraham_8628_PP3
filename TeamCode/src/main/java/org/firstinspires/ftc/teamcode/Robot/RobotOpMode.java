package org.firstinspires.ftc.teamcode.Robot;

import static com.pedropathing.ivy.Scheduler.schedule;

import com.pedropathing.ivy.Scheduler;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;

public abstract class RobotOpMode extends OpMode {
    protected Robot robot;

    @Override
    public void init() {
        Scheduler.reset();
        robot = new Robot(this);

        schedule(
                robot.limelight.periodic(),
                robot.armHinge.periodic(),
                robot.flipper.periodic(),
                robot.shooter.periodic()
        );
    }

    @Override
    public void init_loop() {
        Scheduler.execute();
        robot.telemetry.update();
    }

    @Override
    public void loop() {
        Scheduler.execute();
        robot.telemetry.update();
    }
}
