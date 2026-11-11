package org.firstinspires.ftc.teamcode.Hardware;

import static com.pedropathing.ivy.commands.Commands.infinite;

import com.pedropathing.ivy.Command;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;

public class Flipper {

    public Flywheel flywheel;

    public static final double COLLECT_RPM = 1500;
    public static final double SHOOT_RPM = 2600;
    public static final double EJECT_RPM = -1500;
    public static final double FLOWER_RPM = -250;

    public Flipper(HardwareMap hardwareMap) {
        flywheel = new Flywheel(hardwareMap, "flipper", DcMotorSimple.Direction.REVERSE);
        flywheel.setControllerValues(0.007, 0.005, 753.2, 100.0, false);
    }

    public void doCollectRPM() {
        flywheel.setTargetRPM(COLLECT_RPM);
    }

    public void doShootRPM() {
        flywheel.setTargetRPM(SHOOT_RPM);
    }

    public void doEjectRPM() {
        flywheel.setTargetRPM(EJECT_RPM);
    }

    public void doFlowerRPM() {
        flywheel.setTargetRPM(FLOWER_RPM);
    }

    public void stop() {
        flywheel.stop();
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
        });
    }
}
