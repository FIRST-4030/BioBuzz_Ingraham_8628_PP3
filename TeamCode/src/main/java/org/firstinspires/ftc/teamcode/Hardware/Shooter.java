package org.firstinspires.ftc.teamcode.Hardware;

import static com.pedropathing.ivy.commands.Commands.infinite;

import com.pedropathing.ivy.Command;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.util.Range;

public class Shooter {

    public DoubleMotorFlywheel flywheels;

    public static final double COLLECT_RPM = -1500;
    public static double GENERAL_SHOOT_RPM = 2600;

    public static final double MAX_RPM = 2700;

    public Shooter(HardwareMap hardwareMap) {
        flywheels = new DoubleMotorFlywheel(hardwareMap, "shooter", "shooter_2", DcMotorSimple.Direction.REVERSE, DcMotorSimple.Direction.FORWARD);
        flywheels.setControllerValues(0.007, 0.00046, 56, 100.0, false);
    }

    public void doCollectRPM() {
        flywheels.setTargetRPM(COLLECT_RPM);
    }

    public void doGeneralShootRPM() {
        flywheels.setTargetRPM(GENERAL_SHOOT_RPM);
    }

    public void doDistanceSpecificShootRPM() {
        double calculatedRPM = 2500;
        double clippedRPM = Range.clip(calculatedRPM, -MAX_RPM, MAX_RPM);

        flywheels.setTargetRPM(clippedRPM);
    }

    public void stop() {
        flywheels.stop();
    }

    public boolean atSpeed() {
        return flywheels.atSpeed();
    }

    public double getRPM() {
        return flywheels.getRPM();
    }

    public double getTargetRPM() {
        return flywheels.getTargetRPM();
    }

    // --- Ivy ---

    public Command periodic() {
        return infinite(() -> {
            flywheels.updatePower();
        });
    }
}
