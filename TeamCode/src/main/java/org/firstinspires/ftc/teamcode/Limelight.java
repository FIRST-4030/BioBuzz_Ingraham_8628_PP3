package org.firstinspires.ftc.teamcode;

import com.qualcomm.hardware.limelightvision.LLResult;
import com.qualcomm.hardware.limelightvision.LLResultTypes;
import com.qualcomm.hardware.limelightvision.LLStatus;
import com.qualcomm.hardware.limelightvision.Limelight3A;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.generalUtilities.Blackboard;

import java.util.ArrayList;
import java.util.List;

public class Limelight {

    public Limelight3A limelight;

    public enum HiveCell {
        RED_SCORING,
        RED_AUDIENCE,
        BLUE_SCORING,
        BLUE_AUDIENCE,
        NONE,
    }

    public final int RED_ALL_PIPELINE = 0;
    public final int BLUE_ALL_PIPELINE = 1;
    public final int RED_LEFT_CENTER_PIPELINE = 2;
    public final int RED_RIGHT_CENTER_PIPELINE = 3;
    public final int BLUE_LEFT_CENTER_PIPELINE = 4;
    public final int BLUE_RIGHT_CENTER_PIPELINE = 5;

    private final double X_ERROR_THRESHOLD = 10;
    private final double Y_ERROR_THRESHOLD = 10;

    // Used so that we only switch pipelines once the last requested pipeline was successfully
    // switched to
    private int lastRequestedPipeline = -1;
    private ElapsedTime timeSinceLastPipelineSwitch = new ElapsedTime();

    public Limelight(HardwareMap hardwareMap) {
        limelight = hardwareMap.get(Limelight3A.class, "limelight");
        limelight.setPollRateHz(100); // This sets how often we ask Limelight for data (100 times per second)
        limelight.start(); // This tells Limelight to start looking!
        switchToBestAimingPipeline();
    }

    public HiveCell getHiveCellFromTagID(int tagID) {
        switch (tagID) {
            case 30:
            case 31:
            case 32:
            case 33:
                return HiveCell.RED_SCORING;
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
                return HiveCell.BLUE_SCORING;
            default:
                return HiveCell.NONE;
        }
    }

    public double getXError() {
        LLResult result = limelight.getLatestResult();
        boolean resultIsValid = (result != null && result.isValid());

        if (resultIsValid) {
            return result.getTx();
        } else {
            return 0;
        }
    }

    public double getYError() {
        LLResult result = limelight.getLatestResult();
        boolean resultIsValid = (result != null && result.isValid());

        if (resultIsValid) {
            return result.getTy();
        } else {
            return 0;
        }
    }

    public boolean limelightPipelineIsCurrent() {
        if (getCurrentPipeline() == -1) return false;
        if (lastRequestedPipeline == -1) return true;
        // If 1.0 second has passed since the switch request, treat it as timed out to allow retry
        if (timeSinceLastPipelineSwitch.seconds() > 1.0) return true;
        return lastRequestedPipeline == getCurrentPipeline();
    }

    public void switchToBestAimingPipeline() {
        LLResult result = limelight.getLatestResult();
        boolean resultIsValid = (result != null && result.isValid());

        if (!resultIsValid) return;
        if (!limelightPipelineIsCurrent()) return;

        int currentPipeline = getCurrentPipeline();
        int bestPipeline = currentPipeline;

        // If for some reason we are aiming for the blue hives when we are on the red alliance,
        // or vice versa, switch the pipeline to the appropriate alliance!
        boolean wrongAlliance = false;
        switch (Blackboard.getAlliance()) {
            case BLUE:
                if (currentPipeline == RED_ALL_PIPELINE || currentPipeline == RED_LEFT_CENTER_PIPELINE || currentPipeline == RED_RIGHT_CENTER_PIPELINE) {
                    wrongAlliance = true;
                    pipelineSwitch(BLUE_ALL_PIPELINE);
                }
                break;
            case RED:
                if (currentPipeline == BLUE_ALL_PIPELINE || currentPipeline == BLUE_LEFT_CENTER_PIPELINE || currentPipeline == BLUE_RIGHT_CENTER_PIPELINE) {
                    wrongAlliance = true;
                    pipelineSwitch(RED_ALL_PIPELINE);
                }
                break;
        }
        if (wrongAlliance) return;


        List<LLResultTypes.FiducialResult> fiducials = result.getFiducialResults();
        if (fiducials == null) return;

        List<Integer> visibleTagIDs = new ArrayList<>();
        for (LLResultTypes.FiducialResult fiducial : fiducials) {
            visibleTagIDs.add(fiducial.getFiducialId());
        }

        switch (currentPipeline) {
            case RED_ALL_PIPELINE:
                if (visibleTagIDs.contains(31) || visibleTagIDs.contains(35)) {
                    bestPipeline = RED_LEFT_CENTER_PIPELINE;
                } else if (visibleTagIDs.contains(32) || visibleTagIDs.contains(36)) {
                    bestPipeline = RED_RIGHT_CENTER_PIPELINE;
                }
                break;
            case BLUE_ALL_PIPELINE:
                if (visibleTagIDs.contains(39) || visibleTagIDs.contains(43)) {
                    bestPipeline = BLUE_LEFT_CENTER_PIPELINE;
                } else if (visibleTagIDs.contains(40) || visibleTagIDs.contains(44)) {
                    bestPipeline = BLUE_RIGHT_CENTER_PIPELINE;
                }
                break;
            case RED_LEFT_CENTER_PIPELINE:
                if (!visibleTagIDs.contains(31) && !visibleTagIDs.contains(35)) bestPipeline = RED_ALL_PIPELINE;
                break;
            case RED_RIGHT_CENTER_PIPELINE:
                if (!visibleTagIDs.contains(32) && !visibleTagIDs.contains(36)) bestPipeline = RED_ALL_PIPELINE;
                break;
            case BLUE_LEFT_CENTER_PIPELINE:
                if (!visibleTagIDs.contains(39) && !visibleTagIDs.contains(43)) bestPipeline = BLUE_ALL_PIPELINE;
                break;
            case BLUE_RIGHT_CENTER_PIPELINE:
                if (!visibleTagIDs.contains(40) && !visibleTagIDs.contains(44)) bestPipeline = BLUE_ALL_PIPELINE;
                break;
        }

        if (bestPipeline != currentPipeline) {
            pipelineSwitch(bestPipeline);
        }
    }

    public boolean canSeeHiveCell(HiveCell hiveCell, List<LLResultTypes.FiducialResult> fiducials ) {
        boolean canSeeHiveCell = false;

        if (fiducials == null) return false;

        for (LLResultTypes.FiducialResult fiducial : fiducials) {
            if (getHiveCellFromTagID(fiducial.getFiducialId()) == hiveCell) {
                canSeeHiveCell = true;
                break;
            }
        }

        return canSeeHiveCell;
    }

    public boolean isOnTarget() {
        LLResult result = limelight.getLatestResult();
        boolean resultIsValid = (result != null && result.isValid());

        if (resultIsValid) {
            return (Math.abs(result.getTx()) < X_ERROR_THRESHOLD && Math.abs(result.getTy()) < Y_ERROR_THRESHOLD);
        } else {
            return false;
        }
    }

    public boolean hasValidResult() {
        LLResult result = limelight.getLatestResult();
        return (result != null && result.isValid());
    }

    public int getCurrentPipeline() {
        LLStatus status = limelight.getStatus();
        if (status == null) return -1;
        return status.getPipelineIndex();
    }

    public void pipelineSwitch(int pipeline) {
        lastRequestedPipeline = pipeline;
        timeSinceLastPipelineSwitch.reset();
        limelight.pipelineSwitch(pipeline);
    }

    public LLResult getLatestResult() {
        return limelight.getLatestResult();
    }
}