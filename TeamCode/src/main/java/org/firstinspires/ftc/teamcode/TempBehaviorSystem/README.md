
# The Behavior System

The Behavior System is a little modular framework for Ingraham Robotics for organizing robot logic
into reusable, composable pieces called `behavior`s.

This README documents the core functionality in the framework with examples, but each class
also has documentation in their source code. Additionally, there are various demos/template
classes you can use:

- The [User Behavior Template](UserBehaviors/UserBehaviorTemplate.java) for instructions on making custom robot capabilities
using the framework
- The []

## The `Behavior` interface

Every [`Behavior`](Behavior.java) must implement the following methods:

- `enter()`: Called once when the behavior starts.
- `update()`: Called repeatedly during the main loop.
- `exit()`: Called once when the behavior completes or is interrupted.
- `isComplete()`: Defines an "end condition" for the behavior.

Almost every class you will work with in the Behavior System implements
`Behavior`. You can define new capabilities for the robot that implement `Behavior`, allowing
you to use your own custom robot functionality anywhere that a `Behavior` can be used.

### Note

Behaviors also must implement `processTelemetry()` (and optionally `processSimpleTelemetry`),
which is where information about what the behavior is actively doing is printed to the driver
station. Finally, behaviors have a `getLabel()` method used to attach a short descriptor/title
to a behavior, useful in telemetry and debugging. However, most behaviors make it optional
to specify a label during instantiation.

## `ParallelGroup` and `SequentialGroup`

This package includes two behaviors that are, themselves,
designed to work with lists of other behaviors. They are called
[`ParallelGroup`](ParallelGroup.java) and [`SequentialGroup`](SequentialGroup.java).

### `ParallelGroup`

A parallelGroup executes multiple `Behavior`s in a list at the same time, exiting each
of them as they complete.

For every parallelGroup you create, you can specify how it decides it is complete by
picking one of three strategies:

1. ALL: This parallelGroup is only complete once every behavior in the list
   is complete.
2. ANY: This parallelGroup is complete if any of the behaviors in the list are
   complete.
3. FIRST_IN_LIST: This parallelGroup is complete if the first behavior in the list
   is complete.

### `SequentialGroup`

A `SequentialGroup` is given a list of behaviors and executes them one at a time.

## `GroupBuilder`

Instances of ParallelGroup and SequentialGroup aren't designed to be created manually.
Instead, I encourage you to use the `GroupBuilder` which lets you fluently define
parallelGroups and sequentialGroups with a more readable syntax.

### Examples with one parallel/Sequential block

The GroupBuilder uses a builder pattern. You define parallel and sequential groups by
opening and closing blocks using `parallel()`, `sequential()`, and `end()` after calling
`GroupBuilder.create()`. The first block you open becomes the root of the final behavior.

Making a SequentialGroup could look like this:

```java
import org.firstinspires.ftc.teamcode.TempBehaviorSystem.GroupBuilder;
import org.firstinspires.ftc.teamcode.TempBehaviorSystem.UserBehaviors.WaitMS;

// (...)

Behavior myCoolBehavior = GroupBuilder.create()
        // Open a sequential block. Since this is the first block we open,
        // the whole behavior will be a SequentialGroup once built.
        .sequential("My cool behavior") // You can specify labels for blocks

        // Each behavior will run one at a time
        .add(new WaitMS(5000), "Wait 5 seconds") // You can set labels for behaviors
        .add(new WaitMS(2000), "Wait 2 seconds")
        .add(new WaitMS(3000), "Wait 3 seconds")
        .add(new WaitMS(1000), "Wait 1 second")

        .end() // Close the block
        .build(); // Build and return the final behavior
```

...and making a parallelGroup could look like this:

```java
import org.firstinspires.ftc.teamcode.TempBehaviorSystem.GroupBuilder;
import org.firstinspires.ftc.teamcode.TempBehaviorSystem.ParallelGroup;
import org.firstinspires.ftc.teamcode.TempBehaviorSystem.UserBehaviors.GamepadDrive;
import org.firstinspires.ftc.teamcode.TempBehaviorSystem.UserBehaviors.WaitMS;

// (...)

Behavior myAmazingBehavior = GroupBuilder.create()
        // Open a parallel block. This behavior will complete once the FIRST behavior
        // in the list completes.
        .parallel(ParallelGroup.CompletionCondition.FIRST_IN_LIST, "My amazing behavior")

        // Add a behavior inside the block
        .add(new WaitMS(5000), "Wait 5 seconds")

        // Both of these behaviors will be performed at the same time
        // during myAmazingBehavior's update loop.
        .add(new GamepadDrive(chassis, gamepad1))

        .end() // Close the block
        .build(); // Build and return the final behavior
```

### Example with nested parallel/Sequential blocks

GroupBuilder is most powerful when you nest these parallel and sequential blocks to define
complex logic.

```java
import org.firstinspires.ftc.teamcode.TempBehaviorSystem.GroupBuilder;
import org.firstinspires.ftc.teamcode.TempBehaviorSystem.ParallelGroup;
import org.firstinspires.ftc.teamcode.TempBehaviorSystem.UserBehaviors.GamepadDrive;
import org.firstinspires.ftc.teamcode.TempBehaviorSystem.UserBehaviors.WaitMS;

// (...)

Behavior myIncredibleBehavior = GroupBuilder.create()
        .sequential("My incredible behavior") // Open the first block
        .add(new WaitMS(2000), "Wait 2 seconds")

        // This second block will finish after the timer inside it completes:
        .parallel(ParallelGroup.CompletionCondition.ALL, "Drive for 2 seconds")
        //
        .add(new WaitMS(2000), "Wait 2 seconds")
        .add(new GamepadDrive(chassis, gamepad1))
        .end() // Close the second block

        .add(new WaitMS(3000), "Wait just 3 more seconds")

        .end() // Close the first block
        .build(); // Build everything as one behavior
```

## `State` and `StateMachine`

The `State` interface and `StateMachine` class are great for building opModes with
different "modes" or "states" the robot can be in, such as driving, shooting, or parking.

### The `State` interface
States are just behaviors with opinions about what the next active state should be. States
do this by adding one new method to `Behavior`: `getNextState()`.

You can make a new implementation of `State` manually in your opMode if you really need to,
but the majority of the time it's faster to create states using the classes
`BaseState` or `TaskState`. These classes are useful because they let you take an
existing `Behavior` and attach it to a `State` without unnecessary boilerplate.

---
