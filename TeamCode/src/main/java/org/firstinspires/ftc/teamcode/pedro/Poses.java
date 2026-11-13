package org.firstinspires.ftc.teamcode.pedro;

import com.pedropathing.api.PoseFactory;
import com.pedropathing.math.Pose;

public class Poses {
    /*
    Coordinate system is like this:

    ---------------------------------------
    | (0, 141.5)           (141.5, 141.5) |
    |                                     |
    |           (70.75, 70.75)            |
    |                                     |
    | (0, 0)                   (141.5, 0) |
    ---------------------------------------
     */

    private static final PoseFactory p = PoseFactory.degrees();

    public static final Pose farFromParkStart = p.of(57.75, 8.75, 90);

    public static final Pose hiveFarFromPark = p.of(57.75, 22.75, 90);
    public static final Pose gardenSetup = p.of(35.75, 7.75, 180);
    public static final Pose gardenCollect = p.of(7.75, 7.75, 180);
}
