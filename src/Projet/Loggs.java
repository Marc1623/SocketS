package Projet;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class Loggs {

    public String logName;
    public Logger loggerCliSer;
    private FileHandler fh;

    void startLog(){

        // Initiate
        loggerCliSer = Logger.getLogger("log");

        //set the log level
        loggerCliSer.setLevel(Level.INFO);

        try
        {
            //Get the currentMonth for save log per month
            Calendar currentDate = Calendar.getInstance();
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM");
            String monthNow = formatter.format(currentDate.getTime());

            //Create a ServerFile and its log
            fh = new FileHandler("Loggs"+monthNow+".log", true);
            loggerCliSer.addHandler(fh);

            // Use a custom Formater
            LoggsFormatter myFormatter = new LoggsFormatter();
            fh.setFormatter(myFormatter);
        }
        catch (RuntimeException ex)
        {
            loggerCliSer.log(Level.SEVERE, "Runtime exception thrown", ex);
            ex.printStackTrace();
        }
        catch (IOException ex)
        {
            loggerCliSer.log(Level.SEVERE, "IO Exception thrown", ex);
            ex.printStackTrace();
        }
    }

    private static class ServLoggerHolder {
        private final static Loggs instance = new Loggs();
    }


    public static Loggs getInstance() {
        return ServLoggerHolder.instance;
    }
}


class LoggsFormatter extends Formatter {
    public LoggsFormatter() {
        super();
    }


    public String format(LogRecord record) {

        // Create a StringBuffer to contain the formatted record
        StringBuffer sb = new StringBuffer();

        // Get the date from the LogRecord and add it to the buffer
        Date date = new Date(record.getMillis());
        sb.append(date.toString());
        sb.append(";");
        sb.append(record.getSourceClassName());
        sb.append(";");

        // Get the level name and add it to the buffer
        sb.append(record.getLevel().getName());
        sb.append(";");
        sb.append(formatMessage(record));
        sb.append("\r\n");
        return sb.toString();
    }
}
