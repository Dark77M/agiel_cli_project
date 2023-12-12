package it.unical.agilecliproject;

import it.unical.agilecliproject.commands.ProjectCommand;
import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@SpringBootApplication
public class AgileCliProjectApplication {

    public static void main(String[] args) {
        SpringApplication.run(AgileCliProjectApplication.class, args);
    }
    @PostConstruct
    public void createDir(){
        try {
            // Specify the path for the directory you want to create
            String directoryPath = "log";

            Path directory = Paths.get(directoryPath);

            // Create the directory if it doesn't exist
            if (!Files.exists(directory)) {
                Files.createDirectories(directory);
            }
        } catch (Exception e) {
            // Handle exceptions appropriately
            System.err.println(e.getMessage());
        }
    }

    @PostConstruct
    public void lastActivity(){
        try {
            Path path = Paths.get("log/myApplication.log");
            List<String> context = Files.readAllLines(path);
            String lastLine = context.get(context.size()-1);
            ProjectCommand.eventName = lastLine.split(";")[2];
            ProjectCommand.activityName = lastLine.split(";")[3];
            if (ProjectCommand.eventName.equals("start"))
                ProjectCommand.isActivityStarted = true;
        }catch (Exception ex){
            System.err.println(ex.getMessage());
        }
    }

}
