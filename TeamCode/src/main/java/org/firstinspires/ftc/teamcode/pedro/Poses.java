package org.firstinspires.ftc.teamcode.pedro;

import com.pedropathing.api.PoseFactory;
import com.pedropathing.math.Pose;

public class Poses {
    private final double FORWARD = 90;
    //constructor and other important stuff
    private final PoseFactory poseFactory = PoseFactory.degrees();
    public Poses(){}

    //test auto poses
    public final Pose testStart = poseFactory.of(24, 24, 0);
    public final Pose corner1 = poseFactory.of(48, 24, 0);
    public final Pose corner2 = poseFactory.of(48, 48, 0);
    public final Pose corner3 = poseFactory.of(24, 48, 0);



    //other test poses
    public final Pose testStart2 = poseFactory.of(56, 8, 0);
    public final Pose testMid = poseFactory.of(18, 63, 0);
    public final Pose testControlPoint = poseFactory.of(14,26, 0);



    //Audience side auto positions
    public final Pose audienceStartPos = poseFactory.of(63, 5, FORWARD);
    public final Pose audienceShootPos = poseFactory.of(56, 20, FORWARD);

    public final Pose audienceCollectContPose = poseFactory.of(35, 10 , FORWARD);
    public final Pose audienceCollectPos = poseFactory.of(13, 10, FORWARD + 90);

    public final Pose audienceParkContPose = poseFactory.of(20, 49 , FORWARD);
    public final Pose audienceParkPos = poseFactory.of(9, 94, FORWARD - 90);

    //worst case
    public final Pose parkNoHeading = poseFactory.of(9, 94, FORWARD);
    public final Pose zero = poseFactory.of(0,0, FORWARD);

    //scoring side
    public final Pose scoringBlueStartPos = poseFactory.of(70, 5, FORWARD);
    public final Pose secondLocPos = poseFactory.of(70, 20, FORWARD);
    public final Pose scoringBlueShootPos = poseFactory.of(130, 20, FORWARD);

    public final Pose scoringBlueCollectContPose = poseFactory.of(144-35, 144-10 , FORWARD);
    public final Pose scoringBlueCollectPos = poseFactory.of(144-13, 144-10, FORWARD + 90);

    public final Pose scoringParkContPose = poseFactory.of(144-20, 144-49 , FORWARD);
    public final Pose scoringParkPos = poseFactory.of(144-9, 144-94, FORWARD - 90);



    //scoring blue


}
