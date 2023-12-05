package it.unical.agilecliproject.commands;


import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import it.unical.agilecliproject.utilities.Commons;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;

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
    @ShellMethod(key = "export",value = "export log files into csv format")
    public void exportData(
            @ShellOption(value = {"-f","--format"})String format,
            @ShellOption(value = {"-p","path"})String location){
        if (format == null || format.isEmpty()){
            System.out.println("Format is required");
            return;
        }
        if (location==null||location.isEmpty()){
            System.out.println("Location is required");
            return;
        }

        try {
            Path savePath = Paths.get(location);
            Path loadPath = Paths.get("C:\\Users\\F\\IdeaProjects\\AgileCLIProject\\my_logs.txt");
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
}
