package it.unical.agilecliproject.conf;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;

public class LoggingConf extends FileHandler {

    public LoggingConf(String pattern) throws IOException, SecurityException {
        super(pattern,true);
        setFormatter(new CustomFormat());
        setLevel(Level.INFO);
    }
}
