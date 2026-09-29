package org.firstinspires.ftc.teamcode.UtilOpModes;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.BuildConfig;
import org.firstinspires.ftc.teamcode.ControlHub;

import java.io.IOException;

/* DO NOT disable this opmode.
This opmode is for configuring bot identification per-robot using a text file that lives on the sd card.
This is useful, because the same code will use the correct Pedro Pathing constants for different
robots once their bot identification is set up.
 */
@TeleOp(name="Bot Identification Manager", group = "Util")
public class BotIdentificationOpMode extends OpMode {

    ControlHub controlHub;
    String operation= "";

    public void init() {
        controlHub = new ControlHub();
        telemetry.addData("Compiled on:", BuildConfig.COMPILATION_DATE);
        telemetry.update();
    }

    public void loop() {
        boolean completed;
        telemetry.addLine("Pick an operation:");
        telemetry.addLine("   A   - Create Bot Identification file");
        telemetry.addLine("   B   - Delete Bot Identification file");
        telemetry.addLine("   X   - Get ControlHub file name");
        telemetry.addLine("   Y   - Get Current bot identification");

        telemetry.addLine("   LB - Define bot as BIOBUZZ_DEMO");
        telemetry.addLine("   RB - Define bot as BIOBUZZ_COMPETITION");

        if (gamepad1.aWasReleased()) {
            try {
                completed = controlHub.createControlHubFile();
                if (completed) {
                    operation = "File Created";
                } else {
                    operation = "File FAILED to be created";
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        } else if (gamepad1.bWasReleased()) {
            completed = controlHub.deleteControlHubFile();
            if (completed) {
                operation = "File Deleted";
            } else {
                operation = "File FAILED to be deleted; May have already been deleted";
            }

        } else if (gamepad1.xWasReleased()) {
            operation = "File = ";
            operation = operation + controlHub.getControlHubFileName();

        } else if (gamepad1.yWasReleased()) {
            operation = "Bot = " + controlHub.getBotIdentification();
            if (controlHub.isFallBack()) {
                operation += " (Fallback)";
            }

            // BELOW:
            // CONTROLS FOR SETTING THE BOT IDENTIFICATION OF THE ROBOT
        } else if (gamepad1.leftBumperWasReleased()) {
            try {
                controlHub.initializeControlHub(ControlHub.BotIdentification.BIOBUZZ_DEMO);
                operation = "Bot = " + controlHub.getBotIdentification();

            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        } else if (gamepad1.rightBumperWasReleased()) {
            try {
                controlHub.initializeControlHub(ControlHub.BotIdentification.BIOBUZZ_COMPETITION);
                operation = "Bot = " + controlHub.getBotIdentification();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        telemetry.addLine("");
        telemetry.addData("Operation",operation);
        telemetry.update();
    }
}
