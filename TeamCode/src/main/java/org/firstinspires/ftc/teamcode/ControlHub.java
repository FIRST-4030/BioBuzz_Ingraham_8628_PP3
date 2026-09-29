package org.firstinspires.ftc.teamcode;

import android.os.Environment;

import org.firstinspires.ftc.robotcore.external.Telemetry;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class ControlHub {
    public enum BotIdentification {
        DECODE,
        BIOBUZZ_COMPETITION,
        BIOBUZZ_DEMO,
        UNKNOWN,
        SANDBOX1,
        SANDBOX2,
        SANDBOX3
    }

    // This is the bot identification that will be used if the robot doesn't have a properly
    // configured ControlHub.txt.
    public static final BotIdentification defaultBotIdentification = BotIdentification.BIOBUZZ_COMPETITION;

    private final String fileName;

    public ControlHub() {
        String logFolder = Environment.getExternalStorageDirectory().getPath(); // /storage/emulated/0 also maps to /sdcard

        // filename used to be "/FIRST/Datalogs/ControlHub.txt" on older robots.
        // It's necessary to create a new ControlHub.txt with the bot identification
        // on older robots that used to store it here in order for them to work properly.
        fileName = logFolder + "/FIRST/ControlHub.txt";
    }

    public boolean createControlHubFile() throws IOException {
        File file = new File(fileName);

        file.createNewFile();
        return file.isFile();
    }

    public boolean deleteControlHubFile() {
        File file = new File(fileName);

        return file.delete();
    }

    public String getControlHubFileName() {
        return fileName;
    }

    public void initializeControlHub(BotIdentification botIdentification) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            writer.write(botIdentificationToString(botIdentification));
        }
    }

    public BotIdentification getBotIdentification() {
        String line = null;
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            line = reader.readLine();
        } catch (IOException e) {
            // Catches potential FileNotFoundException (which is an IOException)
            // and other I/O errors

//            System.err.println("An error occurred while reading the file: " + e.getMessage());

            line = botIdentificationToString(defaultBotIdentification);
        }

        return stringToBotIdentification(line);
    }

    /**
     * Returns whether the robot is resorting to the default bot identification because its
     * bot identification was incorrectly configured or is not present on the sd card.
     * @return Whether the robot is falling back to the default bot identification.
     */
    public boolean isFallBack() {
        boolean fallback = false;

        String line = null;
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            line = reader.readLine();
        } catch (IOException e) {
            fallback = true;
        }

        if (line == null) {
            fallback = true;
        }

        return fallback;
    }

    public String botIdentificationToString(BotIdentification botIdentification) {
        switch (botIdentification) {
            case DECODE:
                return "DECODE";
            case BIOBUZZ_COMPETITION:
                return "BIOBUZZ_COMPETITION";
            case BIOBUZZ_DEMO:
                return "BIOBUZZ_DEMO";
            case SANDBOX1:
                return "SANDBOX1";
            case SANDBOX2:
                return "SANDBOX2";
            case SANDBOX3:
                return "SANDBOX3";
            default:
                return "UNKNOWN";
        }
    }

    public BotIdentification stringToBotIdentification(String string) {
        if (string == null) {
            return defaultBotIdentification;
        }

        switch (string) {
            case "DECODE":
                return BotIdentification.DECODE;
            case "BIOBUZZ_COMPETITION":
                return BotIdentification.BIOBUZZ_COMPETITION;
            case "BIOBUZZ_DEMO":
                return BotIdentification.BIOBUZZ_DEMO;
            case "SANDBOX1":
                return BotIdentification.SANDBOX1;
            case "SANDBOX2":
                return BotIdentification.SANDBOX2;
            case "SANDBOX3":
                return BotIdentification.SANDBOX3;
            default:
                return BotIdentification.UNKNOWN;
        }
    }

//    public PedroConstants getRobotSpecificPedroConstants() {
//        switch (getBotIdentification()) {
//            case DECODE:
//                return new PedroConstantsDecodeBot();
//            case BIOBUZZ_COMPETITION:
//                return new PedroConstantsBioBuzzCompetitionBot();
//            case BIOBUZZ_DEMO:
//                return new PedroConstantsBioBuzzDemoBot();
//            default:
//                return new PedroConstantsBioBuzzCompetitionBot(); // Return competition constants
//        }
//    }

    public void processBotIdentificationTelemetry(Telemetry telemetry) {
        telemetry.addLine("--- BOT IDENTIFICATION ---");
        telemetry.addData("This robot will use the pedro constants for", this.botIdentificationToString(this.getBotIdentification()));
        if (this.isFallBack()) {
            telemetry.addLine("");
            telemetry.addLine("(WARNING: this is a fallback; bot identification is not configured or ControlHub.txt is " +
                    "not present on the sd card. The robot is resorting to the default bot: " +
                    this.botIdentificationToString(this.getBotIdentification()) +
                    ". Use the \"Bot Identification Manager\" OpMode to fix this.)");
        }
        telemetry.addLine();
    }

//    public Follower createFollower(HardwareMap hardwareMap) {
//        PedroConstants pedroConstants = getRobotSpecificPedroConstants();
//
//        return new FollowerBuilder(getRobotSpecificPedroConstants().getFollowerConstants(), hardwareMap)
//                .pathConstraints(pedroConstants.getPathConstraints())
//                .mecanumDrivetrain(pedroConstants.getDriveConstraints())
//                .pinpointLocalizer(pedroConstants.getLocalizerConstants())
//                .build();
//    }
}
