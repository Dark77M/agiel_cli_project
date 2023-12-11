package it.unical.agilecliproject.commands;


import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import it.unical.agilecliproject.entities.ActivityAnalysis;
import it.unical.agilecliproject.utilities.Commons;
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
    boolean isActivityStarted =false;
    String activityName;

    @ShellMethod(key = "start",value = "start activity by using name")
    public void startActivity(@ShellOption(value = {"-n","--activity-name"})String name){
        try {
            if (isActivityStarted){
                System.out.println("Stop activity before start a new one!!");
                return;
            }
            String logInfo = String.format("%s;%s;%s;%s", Commons.getDate(),Commons.getTime(),"start",name);
            Commons.saveLog(logInfo);
            isActivityStarted = true;
            activityName = name;

        }catch (Exception ex){
            System.err.println(ex.getMessage());
            isActivityStarted = false;
            activityName = "";
        }
    }
    @ShellMethod(key = "stop", value = "stop tracing")
    public void stopActivity(){
        if(!isActivityStarted || activityName.isEmpty()){
            System.out.println("There is no running activity! Please start an activity first.");
            return;
        }
        String logInfo = String.format("%s;%s;%s;%s", Commons.getDate(),Commons.getTime(),"stop",activityName);
        Commons.saveLog(logInfo);
        isActivityStarted = false;
        activityName = "";
    }
    @ShellMethod(key = "export",value = "export log files into csv/json format")
    public void exportData(
            @ShellOption(value = {"-f","--format"})String format,
            @ShellOption(value = {"-s","--source"})String sourceLocation,
            @ShellOption(value = {"-d","--destination"})String destinationLocation){
        if (format == null || format.isEmpty()){
            System.out.println("Format is required");
            return;
        }
        if (sourceLocation==null||sourceLocation.isEmpty()||destinationLocation==null||destinationLocation.isEmpty()){
            System.out.println("Location is required");
            return;
        }

        try {
            Path savePath = Paths.get(destinationLocation);
            Path loadPath = Paths.get(sourceLocation);
            if (format.equals("csv") || format.equals("CSV")) {
                String header = "Date;Time;Event;Activity;";
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(header).append("\n");
                List<String> strings = Files.readAllLines(loadPath);
                for (String info : strings) {
                    if (info.startsWith("INFO")) {
                        String[] data = info.split(": ");
                        stringBuilder.append(data[1]).append("\n");
                    }
                }
                System.out.println(stringBuilder.toString());
                Files.write(savePath,stringBuilder.toString().getBytes(),
                        StandardOpenOption.CREATE_NEW,StandardOpenOption.TRUNCATE_EXISTING,StandardOpenOption.WRITE);


            }else if (format.equals("json") || format.equals("JSON")) {
                List<String> strings = Files.readAllLines(loadPath);
                JsonArray jsonArray = new JsonArray();
                for (String info : strings){
                    if (info.startsWith("INFO")){
                        String[] data = info.split(": ");
                        String[] jsonObjectData = data[1].split(";");

                        JsonObject object = new JsonObject();
                        object.addProperty("date",jsonObjectData[0].trim());
                        object.addProperty("time",jsonObjectData[1].trim());
                        object.addProperty("event",jsonObjectData[2].trim());
                        object.addProperty("activity",jsonObjectData[3].trim());

                        jsonArray.add(object);
                    }
                }
                Files.write(savePath,jsonArray.toString().getBytes(),
                        StandardOpenOption.CREATE_NEW,StandardOpenOption.TRUNCATE_EXISTING,StandardOpenOption.WRITE);
            }

        }
        catch(Exception ex){
                System.out.println(ex.getMessage());
            }


}
    @ShellMethod(key = "import", value = "import csv/json file for analysis")
    public void importData(
            @ShellOption(value = {"-f","--format"}) String format,
            @ShellOption(value = {"-p","--path"}) String path) {

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
        }
    }
    private void cumulativeTimeOnEachTask(ArrayList<ActivityAnalysis> activityAnalyses) {
        Map<String, Long> cumulativeTimeMap = new HashMap<>();

        try {
            activityAnalyses.forEach(activityAnalysis -> {
                if (!cumulativeTimeMap.containsKey(activityAnalysis.getActivityName())){
                    cumulativeTimeMap.put(activityAnalysis.getActivityName(),activityAnalysis.getDuration());
                }else{
                    long previousTime = cumulativeTimeMap.get(activityAnalysis.getActivityName());
                    previousTime+=activityAnalysis.getDuration();
                    cumulativeTimeMap.remove(activityAnalysis.getActivityName());
                    cumulativeTimeMap.put(activityAnalysis.getActivityName(), previousTime);
                }
            });

            System.out.println("==========================================");
            System.out.println("|== Cumulative time spent on each task ==|");
            System.out.println("==========================================");
            cumulativeTimeMap.forEach((key, value)->{
                System.out.printf("Activity: %s, Cumulative Time: %s%n",key,value);
               // System.out.println(LocalTime.MIN.plusSeconds(value));
            });
        }catch (Exception ex){
            System.err.println(ex.getMessage());
        }


    }
    private void averageTimeInWeekDays(ArrayList<ActivityAnalysis> activityAnalyses){
        System.out.println("===================================================");
        System.out.println("|== Average time spent working on each week day ==|");
        System.out.println("===================================================");

        for (String wd : Commons.WEEKDAY){
            long wdCount = activityAnalyses.stream().filter(activityAnalysis -> activityAnalysis.getWeekDays().equals(wd)).count();
            long wdSum = activityAnalyses.stream().filter(activityAnalysis -> activityAnalysis.getWeekDays().equals(wd))
                    .mapToLong(ActivityAnalysis::getDuration).sum();
            if(wdCount!=0) {
                long avg =  wdSum /wdCount;
//                Duration averageDuration = Duration.ofSeconds(avg);
//
//                long averageHours = averageDuration.toHours();
//                long averageMinutes = averageDuration.toMinutesPart();
//                long averageSecondsPart = averageDuration.toSecondsPart();

                System.out.printf("Week Day: %s, Average Time: %s%n\n",wd,avg);
            }
        }

    }

    private void averageTimePerDay(ArrayList<ActivityAnalysis> activityAnalyses){
        System.out.println("====================================");
        System.out.println("|== Average tracked time per day ==|");
        System.out.println("====================================");

        Stream<String> distinctDate = activityAnalyses.stream().map(ActivityAnalysis::getDate).distinct();
        distinctDate.forEach(date->{
            long count = activityAnalyses.stream().filter(data->data.getDate().equals(date)).count();
            long sum = activityAnalyses.stream().filter(data->data.getDate().equals(date)).mapToLong(ActivityAnalysis::getDuration).sum();
            double avg = (double)sum/count;

            System.out.printf("Date: %s, Average Tracked Time: %s%n\n",date,avg);
        });
    }

    private long getTimeDifference(String startTime, String stopTime){
        LocalTime sTime = LocalTime.parse(startTime);
        LocalTime eTime = LocalTime.parse(stopTime);

        Duration timeDifference = Duration.between(sTime, eTime);

        return timeDifference.getSeconds();
    }

    private String getWeekDay(String startDate, String stopDate){
        if(!startDate.equals(stopDate)){
            return "Date mismatch";
        }
        return LocalDate.parse(startDate, DateTimeFormatter.ofPattern("dd/MM/yyyy")).getDayOfWeek().name();


    }

}

