package org.firstinspires.ftc.teamcode.pedro;

import com.pedropathing.api.PoseFactory;
import com.pedropathing.math.Pose;

public class Poses {
    /*
    All poses are defined as if we are on the RED alliance. When we are on the BLUE alliance,
    it's as if all of these positions are rotated 180° about the center of the field.

                    SCORING
    ---------------------------------------
    | (0, 141.5)           (141.5, 141.5) |
    |                                     |
    |           (70.75, 70.75)            |
    |                                     |
    | (0, 0)                   (141.5, 0) |
    ---------------------------------------
                    AUDIENCE
     */

    private static final PoseFactory p = PoseFactory.degrees();

    public static final Pose origin = p.of(8, 8, 90);

    public static final Pose nearGardenStartingPose = p.of(56, 6, 90);

    public static final Pose hiveNearGarden = p.of(57.75, 22.75, 90);
    public static final Pose hiveFarFromGarden = p.of(57.75, 118.75, 270);

    public static final Pose controlPointToGarden = p.of(38, 27, 130);
    public static final Pose gardenSetup = p.of(35.75, 8, 180);
    public static final Pose gardenCollect = p.of(8, 8, 180);
}
