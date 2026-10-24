package org.firstinspires.ftc.teamcode.pedro;

import com.pedropathing.api.PoseFactory;
import com.pedropathing.math.Pose;

public class Poses {
    private final double FORWARD = 90;

    private final PoseFactory p = PoseFactory.degrees();
    public Poses(){};

    public final Pose coolPose = p.of(0, 0, FORWARD);
    public final Pose amazingPose = p.of(12, 0, FORWARD + 90);
}
