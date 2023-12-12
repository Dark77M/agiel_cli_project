package it.unical.agilecliproject.utilities;

import it.unical.agilecliproject.commands.ProjectCommand;
import it.unical.agilecliproject.conf.LoggingConf;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class Commons {
    static Logger log =Logger.getLogger(ProjectCommand.class.getName());
    //static FileHandler fileHandler;

    public static final String[] WEEKDAY = {"MONDAY","TUESDAY","WEDNESDAY","THURSDAY","FRIDAY"};
    public static String getDate(){
        try {
            LocalDate currentDate = LocalDate.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            return currentDate.format(formatter);
        }catch (Exception ex){
            System.err.println(ex.getMessage());
        }

        return null;
    }
    public static String getTime(){
        try {
            LocalTime currentTime = LocalTime.now();
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
            return currentTime.format(timeFormatter);
        }catch (Exception ex){
            System.err.println(ex.getMessage());
        }

        return null;
    }
    public static void saveLog(String info){
        try {
            String filePattern = "log/myApplication.log";
            LoggingConf conf = new LoggingConf(filePattern);
            log.addHandler(conf);
            log.info(info);
            conf.close();
        }catch (Exception ex){
            System.err.println(ex.getMessage());
        }
    }


}
