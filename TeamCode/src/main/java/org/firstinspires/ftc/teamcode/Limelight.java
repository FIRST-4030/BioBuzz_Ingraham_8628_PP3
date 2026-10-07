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
    // Use internal results and status so we can update the data once each loop and use those
    // same results throughout the loop.
    // Be careful: changing values on the limelight won't result in a readable change until
    // the next loop at the earliest.
    private LLResult internalLatestResult;
    private LLStatus internalLatestStatus;

    public enum HiveCell {
        RED_SCORING,
        RED_AUDIENCE,
        BLUE_SCORING,
        BLUE_AUDIENCE,
        NONE,
    }

    // Pipeline ID constants
    public static final int RED_ALL_PIPELINE = 0;
    public static final int BLUE_ALL_PIPELINE = 1;
    public static final int RED_LEFT_CENTER_PIPELINE = 2;
    public static final int RED_RIGHT_CENTER_PIPELINE = 3;
    public static final int BLUE_LEFT_CENTER_PIPELINE = 4;
    public static final int BLUE_RIGHT_CENTER_PIPELINE = 5;
    public static final int POLLEN_PIPELINE = 6;

    // Thresholds for aiming
    private static final double X_ERROR_THRESHOLD = 1.5;
    private static final double Y_ERROR_THRESHOLD = 1.5;

    // Used so that we only switch pipelines once the last requested pipeline was successfully
    // switched to
    private int lastRequestedPipeline = -1;
    private final ElapsedTime timeSinceLastPipelineSwitch = new ElapsedTime();

    public Limelight(HardwareMap hardwareMap) {
        limelight = hardwareMap.get(Limelight3A.class, "limelight");
        limelight.setPollRateHz(100); // This sets how often we ask Limelight for data (100 times per second)
        limelight.start(); // This tells Limelight to start looking!
    }

    public void updateData() {
        internalLatestResult = limelight.getLatestResult();
        internalLatestStatus = limelight.getStatus();
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

    public String getCurrentPipelineName() {
        switch (getCurrentPipelineID()) {
            case 0:
                return "RED: all tags";
            case 1:
                return "BLUE: all tags";
            case 2:
                return "RED: aiming center-left";
            case 3:
                return "RED: aiming center-right";
            case 4:
                return "BLUE: aiming center-left";
            case 5:
                return "BLUE: aiming center-right";
            default:
                return "Unnamed pipeline";
        }
    }

    public Blackboard.Alliance getAllianceOfPipeline(int pipelineId) {
        switch (pipelineId) {
            case RED_ALL_PIPELINE:
            case RED_LEFT_CENTER_PIPELINE:
            case RED_RIGHT_CENTER_PIPELINE:
                return Blackboard.Alliance.RED;
            case BLUE_ALL_PIPELINE:
            case BLUE_LEFT_CENTER_PIPELINE:
            case BLUE_RIGHT_CENTER_PIPELINE:
                return Blackboard.Alliance.BLUE;
            default:
                return Blackboard.Alliance.UNKNOWN;
        }
    }

    public boolean limelightPipelineIsCurrent() {
        if (getCurrentPipelineID() == -1) return false;
        if (lastRequestedPipeline == -1) return true;
        // If 1.0 second has passed since the switch request, treat it as timed out to allow retry
        if (timeSinceLastPipelineSwitch.seconds() > 1.0) return true;
        return lastRequestedPipeline == getCurrentPipelineID();
    }

    public void lookForBestCollectingPipeline() {
        if (!limelightPipelineIsCurrent()) return;
        pipelineSwitch(POLLEN_PIPELINE);
    }

    public void lookForBestAimingPipeline() {
        if (!limelightPipelineIsCurrent()) return;

        int currentPipeline = getCurrentPipelineID();

        // --- FALLBACKS ---

        Blackboard.Alliance alliance = Blackboard.getAlliance();
        if (alliance == Blackboard.Alliance.BLUE && getAllianceOfPipeline(currentPipeline) != Blackboard.Alliance.BLUE) {
            pipelineSwitch(BLUE_ALL_PIPELINE);
            return;
        } else if (alliance == Blackboard.Alliance.RED && getAllianceOfPipeline(currentPipeline) != Blackboard.Alliance.RED) {
            pipelineSwitch(RED_ALL_PIPELINE);
            return;
        }

        if (!resultHasGoodTargets()) {
            if (currentPipeline == RED_LEFT_CENTER_PIPELINE || currentPipeline == RED_RIGHT_CENTER_PIPELINE) {
                pipelineSwitch(RED_ALL_PIPELINE);
            } else if (currentPipeline == BLUE_LEFT_CENTER_PIPELINE || currentPipeline == BLUE_RIGHT_CENTER_PIPELINE) {
                pipelineSwitch(BLUE_ALL_PIPELINE);
            }
            return;
        }

        // --- AIMING PIPELINES ---

        List<LLResultTypes.FiducialResult> fiducials = internalLatestResult.getFiducialResults();
        if (fiducials == null) return;

        List<Integer> visibleTagIDs = getTagIDsFromFiducials(fiducials);

        switch (currentPipeline) {
            case RED_ALL_PIPELINE:
                if (visibleTagIDs.contains(31) || visibleTagIDs.contains(35)) {
                    pipelineSwitch(RED_LEFT_CENTER_PIPELINE);
                } else if (visibleTagIDs.contains(32) || visibleTagIDs.contains(36)) {
                    pipelineSwitch(RED_RIGHT_CENTER_PIPELINE);
                }
                break;
            case BLUE_ALL_PIPELINE:
                if (visibleTagIDs.contains(39) || visibleTagIDs.contains(43)) {
                    pipelineSwitch(BLUE_LEFT_CENTER_PIPELINE);
                } else if (visibleTagIDs.contains(40) || visibleTagIDs.contains(44)) {
                    pipelineSwitch(BLUE_RIGHT_CENTER_PIPELINE);
                }
                break;
            case RED_LEFT_CENTER_PIPELINE:
                if (!visibleTagIDs.contains(31) && !visibleTagIDs.contains(35)) pipelineSwitch(RED_ALL_PIPELINE);
                break;
            case RED_RIGHT_CENTER_PIPELINE:
                if (!visibleTagIDs.contains(32) && !visibleTagIDs.contains(36)) pipelineSwitch(RED_ALL_PIPELINE);
                break;
            case BLUE_LEFT_CENTER_PIPELINE:
                if (!visibleTagIDs.contains(39) && !visibleTagIDs.contains(43)) pipelineSwitch(BLUE_ALL_PIPELINE);
                break;
            case BLUE_RIGHT_CENTER_PIPELINE:
                if (!visibleTagIDs.contains(40) && !visibleTagIDs.contains(44)) pipelineSwitch(BLUE_ALL_PIPELINE);
                break;
        }
    }

    public boolean canSeeHiveCell(HiveCell hiveCell) {
        boolean canSeeHiveCell = false;

        List<LLResultTypes.FiducialResult> fiducials = internalLatestResult.getFiducialResults();
        if (fiducials == null) return false;

        for (LLResultTypes.FiducialResult fiducial : fiducials) {
            if (getHiveCellFromTagID(fiducial.getFiducialId()) == hiveCell) {
                canSeeHiveCell = true;
                break;
            }
        }

        return canSeeHiveCell;
    }

    public List<Integer> getTagIDsFromFiducials(List<LLResultTypes.FiducialResult> fiducials) {
        List<Integer> visibleTagIDs = new ArrayList<>();
        for (LLResultTypes.FiducialResult fiducial : fiducials) {
            visibleTagIDs.add(fiducial.getFiducialId());
        }

        return visibleTagIDs;
    }

    public boolean resultHasGoodTargets() {
        return (internalLatestResult != null && internalLatestResult.isValid());
    }

    public boolean latestStatusIsValid() {
        return (internalLatestStatus != null);
    }

    public int getCurrentPipelineID() {
        if (!latestStatusIsValid()) return -1;
        return internalLatestStatus.getPipelineIndex();
    }

    public double getXError() {
        if (resultHasGoodTargets()) {
            return internalLatestResult.getTx();
        } else {
            return 0;
        }
    }

    public boolean isOnTarget() {
        if (resultHasGoodTargets()) {
            return (Math.abs(internalLatestResult.getTx()) < X_ERROR_THRESHOLD && Math.abs(internalLatestResult.getTy()) < Y_ERROR_THRESHOLD);
        } else {
            return false;
        }
    }

    public double getYError() {
        if (resultHasGoodTargets()) {
            return internalLatestResult.getTy();
        } else {
            return 0;
        }
    }

    public void pipelineSwitch(int pipeline) {
        lastRequestedPipeline = pipeline;
        timeSinceLastPipelineSwitch.reset();
        limelight.pipelineSwitch(pipeline);
    }

    public ElapsedTime getTimeSinceLastPipelineSwitch() {
        return timeSinceLastPipelineSwitch;
    }

    public LLResult getLatestResult() {
        return internalLatestResult;
    }
}
