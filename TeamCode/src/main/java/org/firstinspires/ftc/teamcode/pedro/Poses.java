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
    public static final Pose farFromGardenStartingPose = p.of(58, 133, 270);

    public static final Pose hiveNearGarden = p.of(58, 22.75, 90);
    public static final Pose hiveFarFromGarden = p.of(58, 118, 270);

    public static final Pose controlPointToGarden = p.of(6, 40, 270);
    public static final Pose gardenCollect = p.of(6, 13, 270);

    public static final Pose nearGardenParkSetup = p.of(25, 35, 90);
    public static final Pose nearGardenPark = p.of(12, 96, 90);

    public static final Pose farFromGardenParkSetup = p.of(25, 35, 90);
    public static final Pose farFromGardenPark = p.of(12, 112, 0);

    public static final Pose nearGardenFlowerSetup = p.of(50, 123, 113);
    public static final Pose nearGardenFlowerSetupControlPoint = p.of(40, 120, 113);
    public static final Pose nearGardenFlower1 = p.of(47.7, 123.7, 115);
    public static final Pose nearGardenFlower2 = p.of(42.8, 124.2, 113);
}
