package org.firstinspires.ftc.teamcode.pedro;

import com.pedropathing.api.PoseFactory;
import com.pedropathing.math.Pose;

public class Poses {
    private static final double FORWARD = 0;

    private static final PoseFactory p = PoseFactory.degrees();

    public static final Pose coolPose = p.of(0, 0, FORWARD);
    public static final Pose amazingPose = p.of(5, 0, FORWARD);
}
