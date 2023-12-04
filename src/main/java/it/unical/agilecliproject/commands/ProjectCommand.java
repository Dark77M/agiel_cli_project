package it.unical.agilecliproject.commands;


import it.unical.agilecliproject.utilities.Commons;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

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
}
