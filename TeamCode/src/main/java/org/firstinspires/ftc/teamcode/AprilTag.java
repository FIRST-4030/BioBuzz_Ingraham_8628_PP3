/* Copyright (c) 2023 FIRST. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted (subject to the limitations in the disclaimer below) provided that
 * the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list
 * of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice, this
 * list of conditions and the following disclaimer in the documentation and/or
 * other materials provided with the distribution.
 *
 * Neither the name of FIRST nor the names of its contributors may be used to endorse or
 * promote products derived from this software without specific prior written permission.
 *
 * NO EXPRESS OR IMPLIED LICENSES TO ANY PARTY'S PATENT RIGHTS ARE GRANTED BY THIS
 * LICENSE. THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.firstinspires.ftc.teamcode;

import android.annotation.SuppressLint;

import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.robotcore.external.hardware.camera.BuiltinCameraDirection;
import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.vision.VisionPortal;
import org.firstinspires.ftc.vision.apriltag.AprilTagDetection;
import org.firstinspires.ftc.vision.apriltag.AprilTagProcessor;

import java.util.List;

/**
 * Documentation:
 * ---------------------------------------------------------------
 * closeAprilTag -- Closes vision portal
 * getBearing -- Returns bearing of selected goal tag
 * getColor -- Gets string (red/green) depending on detection of the goal id
 * getObeliskBearing -- Returns current Obelisk bearing
 * getObeliskRange -- Returns current Obelisk range
 * getYaw -- Returns yaw of selected goal tag
 * initAprilTag -- Builds AprilTag processor
 * runInLoop -- Updates the bearing to goal April Tag
 * scanField -- Scans field for April Tags, sets boolean for visible side (red/blue) and obelisk orientation
 * setGoalTagId -- Manually sets the goal April Tag id
 */
@Deprecated
public class AprilTag {

    private static final boolean USE_WEBCAM = true;  // true = webcam, false = phone

    /**
     * Variables to store the position and orientation of the camera on the robot. Setting these
     * values requires a definition of the axes of the camera and robot:
     * <p>
     * Camera axes:
     * Origin location: Center of the lens
     * Axes orientation: +x right, +y down, +z forward (from camera's perspective)
     * <p>
     * Robot axes (this is typical, but you can define this however you want):
     * Origin location: Center of the robot at field height
     * Axes orientation: +x right, +y forward, +z upward
     * <p>
     * Position:
     * If all values are zero (no translation), that implies the camera is at the center of the
     * robot. Suppose your camera is positioned 5 inches to the left, 7 inches forward, and 12
     * inches above the ground - you would need to set the position to (-5, 7, 12).
     * <p>
     * Orientation:
     * If all values are zero (no rotation), that implies the camera is pointing straight up. In
     * most cases, you'll need to set the pitch to -90 degrees (rotation about the x-axis), meaning
     * the camera is horizontal. Use a yaw of 0 if the camera is pointing forwards, +90 degrees if
     * it's pointing straight left, -90 degrees for straight right, etc. You can also set the roll
     * to +/-90 degrees if it's vertical, or 180 degrees if it's upside-down.
     */

    private AprilTagProcessor aprilTag;
    private VisionPortal visionPortal;

    private int goalTagId;

    public double distanceToGoal;
    private double bearing;
    private double yaw;
    private double goalBearingBlue, goalBearingRed;
    private double obeliskBearing;
    private double obeliskRange;

    private boolean blueSide;
    private boolean redSide;
    private boolean PPG,PGP,GPP;
    private boolean targetInView = false;

    List<AprilTagDetection> currentDetections;

    @SuppressLint("DefaultLocale")
    public void scanField(Telemetry telemetry){

        currentDetections = aprilTag.getDetections();

        if(!currentDetections.isEmpty()) {
            for (AprilTagDetection detection : currentDetections) {
//                if (detection.metadata != null) {
//                    if (detection.id == 20){
//                        blueSide = true;
//                        redSide = false;
//                        goalBearingBlue = detection.ftcPose.bearing;
//                        distanceToGoal = detection.ftcPose.range;
//                        goalTagId = detection.id;
//                    }
//                    if (detection.id == 24){
//                        blueSide = false;
//                        redSide = true;
//                        goalBearingRed = detection.ftcPose.bearing;
//                        distanceToGoal = detection.ftcPose.range;
//                        goalTagId = detection.id;
//                    }
//                    if (detection.id == 22){ //PGP
//                        PPG = false;
//                        GPP = false;
//                        PGP = true;
//                        obeliskRange = detection.ftcPose.range;
//                        obeliskBearing = detection.ftcPose.bearing;
//                    }
//                    else if (detection.id == 23){ //PPG
//                        PPG = true;
//                        GPP = false;
//                        PGP = false;
//                        obeliskRange = detection.ftcPose.range;
//                        obeliskBearing = detection.ftcPose.bearing;
//                    }
//                    else if (detection.id == 21){ //GPP
//                        PPG = false;
//                        GPP = true;
//                        PGP = false;
//                        obeliskRange = detection.ftcPose.range;
//                        obeliskBearing = detection.ftcPose.bearing;
//                    }

                 //}
            }

            telemetry.addLine(String.format("# AprilTags Detected: %d\n", currentDetections.size()));
            telemetry.addLine(String.format("GPP=%b, PGP=%b, PPG=%b\n", GPP, PGP, PPG));
            if (blueSide) {
                bearing = goalBearingBlue;
                telemetry.addLine(String.format("Blue  Goal:  Bearing=%6.2f", goalBearingBlue));
            }
            if (redSide) {
                bearing = goalBearingRed;
                telemetry.addLine(String.format("Red  Goal:  Bearing=%6.2f", goalBearingRed));
            }

        } else {
            telemetry.addLine("No tags");
            bearing = 999;
        }
    }

    public void initAprilTag(HardwareMap hardwareMap) {

        aprilTag = new AprilTagProcessor.Builder()

            //.setDrawAxes(false)
            .setDrawCubeProjection(true)
            //.setDrawTagOutline(true)
            //.setTagFamily(AprilTagProcessor.TagFamily.TAG_36h11)
            //.setTagLibrary(AprilTagGameDatabase.getCenterStageTagLibrary())
            .setOutputUnits(DistanceUnit.INCH, AngleUnit.DEGREES)
//            .setCameraPose(cameraPosition, cameraOrientation)

            // == CAMERA CALIBRATION ==
            // If you do not manually specify calibration parameters, the SDK will attempt
            // to load a predefined calibration for your camera.
            //.setLensIntrinsics(578.272, 578.272, 402.145, 221.506)
            // ... these parameters are fx, fy, cx, cy.

            .build();

        // Adjust Image Decimation to trade-off detection-range for detection-rate.
        // eg: Some typical detection data using a Logitech C920 WebCam
        // Decimation = 1 ..  Detect 2" Tag from 10 feet away at 10 Frames per second
        // Decimation = 2 ..  Detect 2" Tag from 6  feet away at 22 Frames per second
        // Decimation = 3 ..  Detect 2" Tag from 4  feet away at 30 Frames Per Second (default)
        // Decimation = 3 ..  Detect 5" Tag from 10 feet away at 30 Frames Per Second (default)
        // Note: Decimation can be changed on-the-fly to adapt during a match.
        //aprilTag.setDecimation(3);

        VisionPortal.Builder builder = new VisionPortal.Builder();

        if (USE_WEBCAM) {
            builder.setCamera(hardwareMap.get(WebcamName.class, "Webcam 1"));
        } else {
            builder.setCamera(BuiltinCameraDirection.BACK);
        }

        // Choose a camera resolution. Not all cameras support all resolutions.
        //builder.setCameraResolution(new Size(640, 480));

        // Enable the RC preview (LiveView).  Set "false" to omit camera monitoring.
        //builder.enableLiveView(true);

        // Set the stream format; MJPEG uses less bandwidth than default YUY2.
        //builder.setStreamFormat(VisionPortal.StreamFormat.YUY2);

        // Choose whether or not LiveView stops if no processors are enabled.
        // If set "true", monitor shows solid orange screen if no processors enabled.
        // If set "false", monitor shows camera view without annotations.
        //builder.setAutoStopLiveView(false);

        // Set and enable the processor.
        builder.addProcessor(aprilTag);

        // Build the Vision Portal, using the above settings.
        visionPortal = builder.build();

        // Disable or re-enable the aprilTag processor at any time.
        //visionPortal.setProcessorEnabled(aprilTag, true);

    }

    public void closeAprilTag(){
        visionPortal.close();
    }
    /**
     * Add telemetry about AprilTag detections.
     */
    @SuppressLint("DefaultLocale")
    public boolean runInLoop(Telemetry telemetry, boolean display) {

        currentDetections = aprilTag.getDetections();
        if (display) {
            telemetry.addData("# AprilTags Detected", currentDetections.size());
        }

        if(!currentDetections.isEmpty()) {
            for (AprilTagDetection detection : currentDetections) {
//                if (detection.metadata != null) {
//                    if (detection.id == goalTagId) {
//                        targetInView = true;
//                        distanceToGoal = detection.ftcPose.y;
//                        bearing = detection.ftcPose.bearing;
//                        yaw = detection.ftcPose.yaw;
//                        if (display) {
//                            telemetry.addLine(String.format("\n==== (ID %d) %s", detection.id, detection.metadata.name));
//                            telemetry.addLine(String.format("XYZ %6.1f %6.1f %6.1f  (inch)", detection.ftcPose.x, detection.ftcPose.y, detection.ftcPose.z));
//                            telemetry.addLine(String.format("PRY %6.1f %6.1f %6.1f  (deg)", detection.ftcPose.pitch, detection.ftcPose.roll, detection.ftcPose.yaw));
//                            telemetry.addLine(String.format("RBE %6.1f %6.1f %6.1f  (inch, deg, deg)", detection.ftcPose.range, detection.ftcPose.bearing, detection.ftcPose.elevation));
//                        }
//                    } else {
//                        targetInView = false;
//                        if (display) {
//                            telemetry.addLine(String.format("\n==== (ID %d) Unknown", detection.id));
//                            telemetry.addLine(String.format("Center %6.0f %6.0f   (pixels)", detection.center.x, detection.center.y));
//                        }
//                    }
//                }
                if (display) { telemetry.update(); }
            }
        }
        else {
            bearing = 999;
        }
        return targetInView;
    }

    public double getBearing() { return bearing; }
    public double getYaw() { return yaw; }
    public double getObeliskBearing() { return obeliskBearing; }
    public double getObeliskRange() { return obeliskRange; }
    public double getGoalTagId() { return goalTagId; }

    public void setGoalTagID(int value) { goalTagId = value; }
}