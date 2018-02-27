
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Log file manager.
 * Log files provide a history of what changes were made to the database.
 * The log file follows the following format: (each line represents 1 operation)
 * username, operation, database, variables...
 * for example, admin, add, inventory, 20, 123456789012
 *      means that admin has added 20 to inventory where isbn = 123456789012
 * different operations will have different variables, 
 * and discussed in details in the comments, in the console's logging section.
 * @author Jinqiu Liu
 */
public class Log {
    File logFile;
    BufferedWriter out;
    
    public Log() {
        String timestamp = new TimeStamp()+"";
        String logFileName = "Log."+timestamp+".txt";
        logFile = new File(Server.workingDirectory + "/" + logFileName);
        try {
            logFile.createNewFile();
            out = new BufferedWriter(new FileWriter(logFile, true));
        } catch (IOException ex) {
            System.out.println("Unable to create log file!");            
        }
    }
    
    /**
     * Appends a new line to the log file.     * 
     * @param m See Message.
     */
    public void add(Message m) {
        try {
            out.append(m + " performed by " + m.userName + " at " + new TimeStamp() +  "\n");
            out.flush();            
        } catch (IOException ex) {
            System.out.println("IO Exception when logging file!");
        }
    }
    
    /**
     * Call this when ready to exit and close the file output writer.
     */
    public void close() {
        try {
            out.close();
        } catch (IOException ex) {
            System.out.println("IO Exception when closing logging file!");
        }
    }
}
