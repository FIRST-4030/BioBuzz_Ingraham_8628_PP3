package org.firstinspires.ftc.teamcode;

import com.qualcomm.hardware.limelightvision.LLResult;
import com.qualcomm.hardware.limelightvision.LLResultTypes;
import com.qualcomm.hardware.limelightvision.Limelight3A;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.generalUtilities.Blackboard;
import org.firstinspires.ftc.robotcore.external.Telemetry;

import java.util.ArrayList;
import java.util.List;

public class Limelight {

    private Telemetry telemetry;
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
    public final int RED_LEFT_CENTER = 2;
    public final int RED_RIGHT_CENTER = 3;
    public final int BLUE_LEFT_CENTER = 4;
    public final int BLUE_RIGHT_CENTER = 5;

    public Limelight(Telemetry telemetry) {
        this.telemetry = telemetry;
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

    public double getAimingHorizontalError() {
        return getLatestResult().getTx();
    }

    public double getAimingVerticalError() {
        return getLatestResult().getTy();
    }

    public void switchToBestAimingPipeline(List<LLResultTypes.FiducialResult> fiducials) {
        int currentPipeline = getPipeline();
        int bestPipeline = currentPipeline;

        List<Integer> visibleTagIDs = new ArrayList<>();

        for (LLResultTypes.FiducialResult fiducial : fiducials) {
            visibleTagIDs.add(fiducial.getFiducialId());
        }

        // If for some reason we are aiming for the blue hives when we are on the red alliance,
        // or vice versa, switch the pipeline to the appropriate alliance!
        boolean wrongAlliance = false;
        switch (Blackboard.getAlliance()) {
            case BLUE:
                if (currentPipeline == RED_ALL_PIPELINE || currentPipeline == RED_LEFT_CENTER || currentPipeline == RED_RIGHT_CENTER) {
                    wrongAlliance = true;
                    limelight.pipelineSwitch(BLUE_ALL_PIPELINE);
                }
                break;
            case RED:
                if (currentPipeline == BLUE_ALL_PIPELINE || currentPipeline == BLUE_LEFT_CENTER || currentPipeline == BLUE_RIGHT_CENTER) {
                    wrongAlliance = true;
                    limelight.pipelineSwitch(RED_ALL_PIPELINE);
                }
                break;
        }
        if (wrongAlliance) return;

        switch (getPipeline()) {
            case RED_ALL_PIPELINE:
                if (visibleTagIDs.contains(31) || visibleTagIDs.contains(35)) {
                    bestPipeline = RED_LEFT_CENTER;
                }
                if (visibleTagIDs.contains(32) || visibleTagIDs.contains(36)) {
                    bestPipeline = RED_RIGHT_CENTER;
                }
                break;
            case BLUE_ALL_PIPELINE:
                if (visibleTagIDs.contains(39) || visibleTagIDs.contains(43)) {
                    bestPipeline = BLUE_LEFT_CENTER;
                }
                if (visibleTagIDs.contains(40) || visibleTagIDs.contains(44)) {
                    bestPipeline = BLUE_RIGHT_CENTER;
                }
                break;
            case RED_LEFT_CENTER:
                if (canSeeHiveCell(HiveCell.RED_SCORING, fiducials) && !visibleTagIDs.contains(31)) bestPipeline = RED_ALL_PIPELINE;
                if (canSeeHiveCell(HiveCell.RED_AUDIENCE, fiducials) && !visibleTagIDs.contains(35)) bestPipeline = RED_ALL_PIPELINE;
                break;
            case RED_RIGHT_CENTER:
                if (canSeeHiveCell(HiveCell.RED_SCORING, fiducials) && !visibleTagIDs.contains(32)) bestPipeline = RED_ALL_PIPELINE;
                if (canSeeHiveCell(HiveCell.RED_AUDIENCE, fiducials) && !visibleTagIDs.contains(36)) bestPipeline = RED_ALL_PIPELINE;
                break;
            case BLUE_LEFT_CENTER:
                if (canSeeHiveCell(HiveCell.BLUE_SCORING, fiducials) && !visibleTagIDs.contains(39)) bestPipeline = BLUE_ALL_PIPELINE;
                if (canSeeHiveCell(HiveCell.BLUE_AUDIENCE, fiducials) && !visibleTagIDs.contains(43)) bestPipeline = BLUE_ALL_PIPELINE;
                break;
            case BLUE_RIGHT_CENTER:
                if (canSeeHiveCell(HiveCell.BLUE_SCORING, fiducials) && !visibleTagIDs.contains(40)) bestPipeline = BLUE_ALL_PIPELINE;
                if (canSeeHiveCell(HiveCell.BLUE_AUDIENCE, fiducials) && !visibleTagIDs.contains(44)) bestPipeline = BLUE_ALL_PIPELINE;
                break;
        }

        if (bestPipeline != currentPipeline) limelight.pipelineSwitch(bestPipeline);
    }

    public boolean canSeeHiveCell(HiveCell hiveCell, List<LLResultTypes.FiducialResult> fiducials ) {
        boolean canSeeHiveCell = false;

        for (LLResultTypes.FiducialResult fiducial : fiducials) {
            if (getHiveCellFromTagID(fiducial.getFiducialId()) == hiveCell) {
                canSeeHiveCell = true;
                break;
            }
        }

        return canSeeHiveCell;
    }

    public boolean isOnTarget() {
        return false;
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

    public LLResult getLatestResult() {
        return limelight.getLatestResult();
    }
}