package org.firstinspires.ftc.teamcode.Hardware;

import static com.pedropathing.ivy.commands.Commands.infinite;

import com.pedropathing.ivy.Command;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;

public class Shooter {

    public Flywheel flywheel;
    public Flywheel flywheel2;

    public static final double COLLECT_RPM = -1500;
    public static final double SHOOT_RPM = 2600;

    public Shooter(HardwareMap hardwareMap) {
        flywheel = new Flywheel(hardwareMap, "shooter", DcMotorSimple.Direction.REVERSE);
        flywheel.setControllerValues(0.007, 0.00046, 56, 100.0, false);

        flywheel2 = new Flywheel(hardwareMap, "shooter_2", DcMotorSimple.Direction.FORWARD);
        flywheel2.setControllerValues(0.007, 0.00046, 56, 100.0, false);
    }

    public void doCollectRPM() {
        flywheel.setTargetRPM(COLLECT_RPM);
        flywheel2.setTargetRPM(COLLECT_RPM);
    }

    public void doShootRPM() {
        flywheel.setTargetRPM(SHOOT_RPM);
        flywheel2.setTargetRPM(SHOOT_RPM);
    }

    public void stop() {
        flywheel.stop();
        flywheel2.stop();
    }

    public boolean atSpeed() {
        return flywheel.atSpeed();
    }

    public double getRPM() {
        return flywheel.getRPM();
    }

    public double getTargetRPM() {
        return flywheel.getTargetRPM();
    }

    // --- Ivy ---

    public Command periodic() {
        return infinite(() -> {
            flywheel.updatePower();
            flywheel2.updatePower();
        });
    }
}
