package org.firstinspires.ftc.teamcode.TempBehaviorSystem;

import java.util.ArrayList;
import java.util.List;

/**
 * A fluent API for building nested Behaviors.
 * Handles Sequential and Parallel groups.
 * @author Edson James
 */
public class GroupBuilder {
    private final List<Behavior> behaviors = new ArrayList<>();
    private final GroupBuilder parent;
    private final Type type;
    private ParallelGroup.CompletionCondition parallelCondition;
    private String label;

    private enum Type { ROOT, SEQUENTIAL, PARALLEL }

    private GroupBuilder(GroupBuilder parent, Type type) {
        this.parent = parent;
        this.type = type;
    }

    /**
     * Starts a new GroupBuilder at the root.
     * @return A new GroupBuilder
     */
    public static GroupBuilder create() {
        return new GroupBuilder(null, Type.ROOT);
    }

    /**
     * Sets the label for the current behavior block.
     * @param label The label to set
     * @return This GroupBuilder
     */
    public GroupBuilder label(String label) {
        this.label = label;
        return this;
    }

    /**
     * Starts a sequential group block.
     * @return A new GroupBuilder for the sequential block
     */
    public GroupBuilder sequential() {
        return new GroupBuilder(this, Type.SEQUENTIAL);
    }

    /**
     * Starts a sequential group block with a label.
     * @param label The label for the sequential block
     * @return A new GroupBuilder for the sequential block
     */
    public GroupBuilder sequential(String label) {
        return sequential().label(label);
    }

    /**
     * Starts a parallel group block with the default CompletionCondition (ALL).
     * @return A new GroupBuilder for the parallel block
     */
    public GroupBuilder parallel() {
        return parallel(ParallelGroup.CompletionCondition.ALL);
    }

    /**
     * Starts a parallel group block with a label and default CompletionCondition (ALL).
     * @param label The label for the parallel block
     * @return A new GroupBuilder for the parallel block
     */
    public GroupBuilder parallel(String label) {
        return parallel().label(label);
    }

    /**
     * Starts a parallel group block with a specific CompletionCondition.
     * @param condition The condition for the parallel block to complete
     * @return A new GroupBuilder for the parallel block
     */
    public GroupBuilder parallel(ParallelGroup.CompletionCondition condition) {
        GroupBuilder child = new GroupBuilder(this, Type.PARALLEL);
        child.parallelCondition = condition;
        return child;
    }

    /**
     * Starts a parallel group block with a specific CompletionCondition and label.
     * @param condition The condition for the parallel block to complete
     * @param label The label for the parallel block
     * @return A new GroupBuilder for the parallel block
     */
    public GroupBuilder parallel(ParallelGroup.CompletionCondition condition, String label) {
        return parallel(condition).label(label);
    }

    /**
     * Adds a behavior to the current block.
     * @param behavior The behavior to add
     * @return This GroupBuilder
     */
    public GroupBuilder add(Behavior behavior) {
        behaviors.add(behavior);
        return this;
    }

    /**
     * Ends the current sequential or parallel block and adds it to the parent block.
     * @return The parent GroupBuilder
     * @throws IllegalStateException if called on the root builder
     */
    public GroupBuilder end() {
        if (parent == null) {
            throw new IllegalStateException("GroupBuilder cannot call end() on root builder. Use build() instead.");
        }
        parent.add(this.internalBuild());
        return parent;
    }

    /**
     * Finalizes the build and returns the resulting behavior.
     * If multiple behaviors were added at the root level, they are wrapped in a SequentialGroup.
     * @return The built Behavior
     */
    public Behavior build() {
        if (parent != null) {
            throw new IllegalStateException("GroupBuilder has unclosed blocks. Call end() before build().");
        }
        return internalBuild();
    }

    private Behavior internalBuild() {
        String finalLabel = (label != null) ? label : (type == Type.ROOT ? "Root Sequence" : type.name());

        if (type == Type.PARALLEL) {
            return new ParallelGroup(parallelCondition, behaviors, finalLabel);
        } else if (type == Type.SEQUENTIAL) {
            return new SequentialGroup(behaviors, finalLabel);
        } else {
            // Root type
            if (behaviors.size() == 1 && label == null) {
                return behaviors.get(0);
            } else {
                return new SequentialGroup(behaviors, finalLabel);
            }
        }
    }
}
