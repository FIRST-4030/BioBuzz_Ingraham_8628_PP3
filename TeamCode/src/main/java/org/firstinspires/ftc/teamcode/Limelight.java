package org.firstinspires.ftc.teamcode;

import com.qualcomm.hardware.limelightvision.LLResult;
import com.qualcomm.hardware.limelightvision.LLResultTypes;
import com.qualcomm.hardware.limelightvision.Limelight3A;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.generalUtilities.Blackboard;
import org.firstinspires.ftc.robotcore.external.Telemetry;

import java.util.List;

public class Limelight {

    private Telemetry telemetry;
    public Limelight3A limelight;

    private static final double METERS_TO_INCHES = 39.3701;

    public enum HiveCell {
        RED_FAR,
        RED_AUDIENCE,
        BLUE_FAR,
        BLUE_AUDIENCE,
        NONE,
    }

    public final int RED_HIVES_PIPELINE = 0;
    public final int BLUE_HIVES_PIPELINE = 1;

    public Limelight(Telemetry telemetry) {
        this.telemetry = telemetry;
    }

    // On second thought, do we even need to know which cell we are aiming at?
    public HiveCell getHiveCellFromTagID(int tagID) {
        switch (tagID) {
            case 30:
            case 31:
            case 32:
            case 33:
                return HiveCell.RED_FAR;
            case 34:
            case 35:
            case 36:
            case 37:
                return HiveCell.RED_AUDIENCE;
            case 38:
            case 39:
            case 40:
            case 41:
                return HiveCell.BLUE_AUDIENCE;
            case 42:
            case 43:
            case 44:
            case 45:
                return HiveCell.BLUE_FAR;
            default:
                return HiveCell.NONE;
        }
    }

    public LLResultTypes.FiducialResult getBestHiveAprilTagOrNull() {
        LLResult latestResult = limelight.getLatestResult();
        List<LLResultTypes.FiducialResult> fiducials = latestResult.getFiducialResults();

        doAllianceAimingPipeline();

        LLResultTypes.FiducialResult closestAprilTag = null;
        double closestAprilTagArea = 0;
        for (LLResultTypes.FiducialResult fiducial : fiducials) {
            double targetArea = fiducial.getTargetArea();

            // TODO: For now, we can just pick the April tag closest to us. Might be worth thinking
            //  about better ways to pick where to aim within a hive cell.
            if (targetArea > closestAprilTagArea) {
                closestAprilTag = fiducial;
                closestAprilTagArea = targetArea;
            }
        }

        return closestAprilTag;
    }

    public void doAllianceAimingPipeline() {
        int pipelineToSwitchTo;
        switch (Blackboard.getAlliance()) {
            case RED:
                pipelineToSwitchTo = RED_HIVES_PIPELINE;
                break;
            case BLUE:
                pipelineToSwitchTo = BLUE_HIVES_PIPELINE;
                break;
            default:
                // TODO: Should we really aim for red by default? Maybe we should think of specific
                //  fallback functionality in case the alliance is still unknown at game time.
                pipelineToSwitchTo = RED_HIVES_PIPELINE;
                break;
        }

        if (getPipeline() != pipelineToSwitchTo) {
            limelight.pipelineSwitch(pipelineToSwitchTo);
        }
    }

    public void init(HardwareMap hardwareMap) {
        limelight = hardwareMap.get(Limelight3A.class, "limelight");
        limelight.setPollRateHz(100); // This sets how often we ask Limelight for data (100 times per second)
        limelight.start(); // This tells Limelight to start looking!
    }

    public boolean hasResults() {
        LLResult result = limelight.getLatestResult();
        return (result != null && result.isValid());
    }

    public int getPipeline() { return limelight.getStatus().getPipelineIndex(); }
}