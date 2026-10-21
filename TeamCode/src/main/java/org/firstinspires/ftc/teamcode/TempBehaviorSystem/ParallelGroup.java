package org.firstinspires.ftc.teamcode.TempBehaviorSystem;

import org.firstinspires.ftc.robotcore.external.Telemetry;

import java.util.List;

/**
 * A behavior which runs every behavior in a list on every update call. The behaviors are executed in the order
 * that they appear in the list. The completion condition strategy of parallelGroup can be specified:
 * ALL for when every behavior is complete, ANY for when any behavior is complete, and FIRST_IN_LIST for when the
 * first behavior in the list completes. The GroupBuilder uses ALL by default when creating parallelGroups.
 * @author Edson James
 * @see SequentialGroup
 */
public class ParallelGroup implements Behavior {
    public enum CompletionCondition {
        ALL,
        ANY,
        FIRST_IN_LIST,
        NEVER,
    }

    private final CompletionCondition completionCondition;
    private final List<Behavior> behaviors;
    private final String label;
    private final boolean[] completionCache;
    private final boolean[] exitedCache;

    public ParallelGroup(CompletionCondition completionCondition, List<Behavior> behaviors, String label) {
        this.completionCondition = completionCondition;
        this.behaviors = behaviors;
        this.label = label;
        this.completionCache = new boolean[behaviors.size()];
        this.exitedCache = new boolean[behaviors.size()];
    }

    /**
     * Enters all behaviors in the list.
     */
    @Override
    public void enter() {
        for (int i = 0; i < behaviors.size(); i++) {
            Behavior behavior = behaviors.get(i);
            behavior.enter();
            completionCache[i] = behavior.isComplete();
            exitedCache[i] = false;

            if (completionCache[i]) {
                behavior.exit();
                exitedCache[i] = true;
            }
        }
    }

    /**
     * Updates all behaviors in the list that are not completed.
     */
    @Override
    public void update() {
        for (int i = 0; i < behaviors.size(); i++) {
            Behavior behavior = behaviors.get(i);
            if (!completionCache[i]) {
                behavior.update();
                completionCache[i] = behavior.isComplete();

                if (completionCache[i]) {
                    behavior.exit();
                    exitedCache[i] = true;
                }
            }
        }
    }

    /**
     * Returns whether the parallel group is complete based on the completionCondition strategy:
     * ALL, ANY, or FIRST.
     * @return Whether the parallel group is complete
     */
    @Override
    public boolean isComplete() {
        switch (this.completionCondition) {
            case ALL:
                return areAllBehaviorsCompleted();
            case ANY:
                return areAnyBehaviorsCompleted();
            case FIRST_IN_LIST:
                return isFirstCompleted();
            case NEVER:
                return false;
            default:
                return false;
        }
    }

    /**
     * Exits every behavior in the list that hasn't already exited.
     */
    @Override
    public void exit() {
        for (int i = 0; i < behaviors.size(); i++) {
            if (!exitedCache[i]) {
                behaviors.get(i).exit();
                exitedCache[i] = true;
            }
        }
    }

    public boolean areAllBehaviorsCompleted() {
        for (boolean complete : completionCache) {
            if (!complete) {
                return false;
            }
        }

        return true;
    }

    public boolean areAnyBehaviorsCompleted() {
        for (boolean complete : completionCache) {
            if (complete) {
                return true;
            }
        }

        return false;
    }

    public boolean isFirstCompleted() {
        if (completionCache.length == 0) {
            return true;
        }

        return completionCache[0];
    }

    @Override
    public void processTelemetry(Telemetry telemetry, String prefix) {
        int currentMS = (int) (System.currentTimeMillis());

        if (completionCondition == CompletionCondition.NEVER) {
            telemetry.addLine(prefix + "(Parallel: NEVER ends)");
        } else {
            telemetry.addLine(prefix + "(Parallel: Ends once " + completionCondition + " have completed)");
        }
        for (int i = 0; i < behaviors.size(); i++) {
            Behavior behavior = behaviors.get(i);
            // There's some crazy jazz here that just makes the arrows look pretty lol
            String listPrefix = "--> ";
            if (currentMS % 500 > 250) {
                listPrefix = "--->";
            }
            if (completionCache[i]) {
                listPrefix = "✔   ";
            }
            telemetry.addLine(prefix + listPrefix + behavior.getLabel());

            if (!completionCache[i]) {
                behavior.processTelemetry(telemetry, prefix + "    ");
            }
        }
    }

    @Override
    public void processSimpleTelemetry(Telemetry telemetry, String prefix) {
        for (int i = 0; i < behaviors.size(); i++) {
            if (!completionCache[i]) {
                behaviors.get(i).processSimpleTelemetry(telemetry, prefix);
            }
        }
    }

    @Override
    public String getLabel() {
//        return "PAR: " + label;
        return label;
    }
}
