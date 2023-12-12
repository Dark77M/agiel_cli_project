package it.unical.agilecliproject.commands;


import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import it.unical.agilecliproject.entities.ActivityAnalysis;
import it.unical.agilecliproject.utilities.Commons;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@ShellComponent
public class ProjectCommand {
    public static boolean isActivityStarted = false;
    public static String eventName;
    public static String activityName;

    private final ApplicationContext applicationContext;

    @Autowired
    public ProjectCommand(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @ShellMethod(key = "start", value = "start activity by using name")
    public void startActivity(@ShellOption(value = {"-n", "--activity-name"}) String name) {
        try {
            if (isActivityStarted) {
                System.out.println("Stop activity before start a new one!!");
                closeShell();
                return;
            }
            String logInfo = String.format("%s;%s;%s;%s\n", Commons.getDate(), Commons.getTime(), "start", name);
            Commons.saveLog(logInfo);
            isActivityStarted = true;
            eventName = "start";
            activityName = name;

        } catch (Exception ex) {
            System.err.println(ex.getMessage());
            isActivityStarted = false;
            activityName = "";
            activityName = "";
        } finally {
            closeShell();
        }
    }

    @ShellMethod(key = "stop", value = "stop tracing")
    public void stopActivity() {
        if (!isActivityStarted || activityName.isEmpty()) {
            System.out.println("There is no running activity! Please start an activity first.");
            closeShell();
            return;
        }
        String logInfo = String.format("%s;%s;%s;%s\n", Commons.getDate(), Commons.getTime(), "stop", activityName);
        Commons.saveLog(logInfo);
        isActivityStarted = false;
        eventName  = "";
        activityName = "";
        closeShell();

    }

    @ShellMethod(key = "export", value = "export log files into csv/json format")
    public void exportData(
            @ShellOption(value = {"-f", "--format"}) String format,
            //      @ShellOption(value = {"-s","--source"})String sourceLocation,
            @ShellOption(value = {"-d", "--destination"}) String destination) {
        if (!eventName.equals("stop")) {
            System.err.println("You need to stop current activity before the export.");
            closeShell();
            return;
        }
        if (format == null || format.isEmpty()) {
            System.err.println("Format is required!!");
            return;
        }
        if (destination == null || destination.isEmpty()) {
            System.err.println("Paths are required!!");
            return;
        }

        // check file format and -f both are same
        String destinationFileFormat = "";
        if (format.equals("csv") || format.equals("CSV")) {
            destinationFileFormat = destination.substring(destination.length() - 3, destination.length());
        } else if (format.equals("json") || format.equals("JSON")) {
            destinationFileFormat = destination.substring(destination.length() - 4, destination.length());
        }

        if (!format.equals(destinationFileFormat)) {
            System.err.println("Wrong file format!!!");
            return;
        }


        try {
            Path savePath = Paths.get(destination);
            Path loadPath = Paths.get("log/myApplication.log");
            if (format.equals("csv") || format.equals("CSV")) {
                String header = "Date;Time;Event;Activity;";
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(header).append("\n");
                List<String> strings = Files.readAllLines(loadPath);
                for (String info : strings) {
                    String[] data = info.split(";");

                    stringBuilder.append(data[0]).append(";").append(data[1]).append(";").append(data[2]).append(";").append(data[3])
                            .append("\n");
                }
                System.out.println(stringBuilder.toString());
                Files.write(savePath, stringBuilder.toString().getBytes(),
                        StandardOpenOption.CREATE_NEW, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);


            } else if (format.equals("json") || format.equals("JSON")) {
                List<String> strings = Files.readAllLines(loadPath);
                JsonArray jsonArray = new JsonArray();
                for (String info : strings) {
                     String[] data = info.split(";");
                        // String[] jsonObjectData = data[1].split(";");

                        JsonObject object = new JsonObject();
                        object.addProperty("date", data[0].trim());
                        object.addProperty("time", data[1].trim());
                        object.addProperty("event", data[2].trim());
                        object.addProperty("activity", data[3].trim());

                        jsonArray.add(object);
                }
                System.out.println(jsonArray);
                Files.write(savePath, jsonArray.toString().getBytes(),
                        StandardOpenOption.CREATE_NEW, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
            }

        } catch (Exception ex) {
            System.err.println(ex.getMessage());
        } finally {
            closeShell();
        }


    }

    @ShellMethod(key = "import", value = "import csv/json file for analysis")
    public void importData(
            @ShellOption(value = {"-f", "--format"}) String format,
            @ShellOption(value = {"-p", "--path"}) String path) {

        try {
            ArrayList<ActivityAnalysis> activityAnalyses = new ArrayList<>();
            String startTime = null, stopTime = null, startDate = null, stopDate = null;
            Path inputFilePath = Paths.get(path);
            if (format.equals("csv") || format.equals("CSV")) {
                List<String> fileContent = Files.readAllLines(inputFilePath);
                for (String content : fileContent) {
                    if (!content.startsWith("Date")) {
                        String[] data = content.split(";");


                        String date = data[0];
                        String time = data[1];
                        String event = data[2];
                        String activity = data[3];

                        if (event.equals("start")) {
                            startTime = time;
                            startDate = date;
                        } else if (event.equals("stop")) {
                            stopTime = time;
                            stopDate = date;
                        }
                        if (event.equals("stop") && startTime != null && startDate != null && stopTime != null && stopDate != null) {
                            activityAnalyses.add(new ActivityAnalysis(
                                    activity,
                                    getTimeDifference(startTime, stopTime),
                                    startDate,
                                    getWeekDay(startDate, stopDate)
                            ));

                            startDate = stopDate = startTime = stopTime = null;
                        }
                    }

                }
            } else if (format.equals("json") || format.equals("JSON")) {
                JsonArray jsonArray = JsonParser.parseString(Files.readString(inputFilePath)).getAsJsonArray();
                for (int index = 0; index < jsonArray.size(); index++) {
                    JsonObject object = jsonArray.get(index).getAsJsonObject();

                    String date = object.get("date").getAsString();
                    String time = object.get("time").getAsString();
                    String event = object.get("event").getAsString();
                    String activity = object.get("activity").getAsString();

                    if (event.equals("start")) {
                        startTime = time;
                        startDate = date;
                    } else if (event.equals("stop")) {
                        stopTime = time;
                        stopDate = date;
                    }

                    if (event.equals("stop") && startTime != null && startDate != null && stopTime != null && stopDate != null) {
                        activityAnalyses.add(new ActivityAnalysis(
                                activity,
                                getTimeDifference(startTime, stopTime),
                                startDate,
                                getWeekDay(startDate, stopDate)
                        ));

                        startDate = stopDate = startTime = stopTime = null;
                    }
                }
            }
            //activityAnalyses.forEach(activityAnalysis -> System.out.println(activityAnalysis.getActivityName()+", "+activityAnalysis.getWeekDays()+", "+activityAnalysis.getDuration()+", "+activityAnalysis.getDate()));

            // first: Cumulative time spent on each task
            cumulativeTimeOnEachTask(activityAnalyses);

            // second: Average time spent working on each week day
            averageTimeInWeekDays(activityAnalyses);

            // Third: Average tracked time per day
            averageTimePerDay(activityAnalyses);

        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }finally {
            closeShell();
        }
    }

    private void cumulativeTimeOnEachTask(ArrayList<ActivityAnalysis> activityAnalyses) {
        Map<String, Long> cumulativeTimeMap = new HashMap<>();

        try {
            activityAnalyses.forEach(activityAnalysis -> {
                if (!cumulativeTimeMap.containsKey(activityAnalysis.getActivityName())) {
                    cumulativeTimeMap.put(activityAnalysis.getActivityName(), activityAnalysis.getDuration());
                } else {
                    long previousTime = cumulativeTimeMap.get(activityAnalysis.getActivityName());
                    previousTime += activityAnalysis.getDuration();
                    cumulativeTimeMap.remove(activityAnalysis.getActivityName());
                    cumulativeTimeMap.put(activityAnalysis.getActivityName(), previousTime);
                }
            });

            System.out.println("==========================================");
            System.out.println("|== Cumulative time spent on each task ==|");
            System.out.println("==========================================");
            cumulativeTimeMap.forEach((key, value) -> {
                System.out.printf("Activity: %s, Cumulative Time: %s%n", key, value);
                // System.out.println(LocalTime.MIN.plusSeconds(value));
            });
        } catch (Exception ex) {
            System.err.println(ex.getMessage());
        }


    }

    private void averageTimeInWeekDays(ArrayList<ActivityAnalysis> activityAnalyses) {
        System.out.println("===================================================");
        System.out.println("|== Average time spent working on each week day ==|");
        System.out.println("===================================================");

        for (String wd : Commons.WEEKDAY) {
            long wdCount = activityAnalyses.stream().filter(activityAnalysis -> activityAnalysis.getWeekDays().equals(wd)).count();
            long wdSum = activityAnalyses.stream().filter(activityAnalysis -> activityAnalysis.getWeekDays().equals(wd))
                    .mapToLong(ActivityAnalysis::getDuration).sum();
            if (wdCount != 0) {
                long avg = wdSum / wdCount;
//                Duration averageDuration = Duration.ofSeconds(avg);
//
//                long averageHours = averageDuration.toHours();
//                long averageMinutes = averageDuration.toMinutesPart();
//                long averageSecondsPart = averageDuration.toSecondsPart();

                System.out.printf("Week Day: %s, Average Time: %s%n\n", wd, avg);
            }
        }

    }

    private void averageTimePerDay(ArrayList<ActivityAnalysis> activityAnalyses) {
        System.out.println("====================================");
        System.out.println("|== Average tracked time per day ==|");
        System.out.println("====================================");

        Stream<String> distinctDate = activityAnalyses.stream().map(ActivityAnalysis::getDate).distinct();
        distinctDate.forEach(date -> {
            long count = activityAnalyses.stream().filter(data -> data.getDate().equals(date)).count();
            long sum = activityAnalyses.stream().filter(data -> data.getDate().equals(date)).mapToLong(ActivityAnalysis::getDuration).sum();
            double avg = (double) sum / count;

            System.out.printf("Date: %s, Average Tracked Time: %s%n\n", date, avg);
        });
    }

    private long getTimeDifference(String startTime, String stopTime) {
        LocalTime sTime = LocalTime.parse(startTime);
        LocalTime eTime = LocalTime.parse(stopTime);

        Duration timeDifference = Duration.between(sTime, eTime);

        return timeDifference.getSeconds();
    }

    private String getWeekDay(String startDate, String stopDate) {
        if (!startDate.equals(stopDate)) {
            return "Date mismatch";
        }
        return LocalDate.parse(startDate, DateTimeFormatter.ofPattern("dd/MM/yyyy")).getDayOfWeek().name();


    }

    private void closeShell() {
        try {
            System.exit(SpringApplication.exit(this.applicationContext, new ExitCodeGenerator() {
                @Override
                public int getExitCode() {
                    return 0;
                }
            }));
        } catch (Exception ex) {
            System.err.println(ex.getMessage());
        }

    }

}