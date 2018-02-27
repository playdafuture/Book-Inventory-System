import java.time.LocalDateTime;

/**
 * Timestamp uses the LocalDataTime object to store a local time, 
 * with millisecond precision.
 * @author Jinqiu Liu
 */
public class TimeStamp {
    LocalDateTime stamp;
    
    /**
     * Use the current system time and convert to timestamp object.
     */
    public TimeStamp() {
        stamp = LocalDateTime.now();        
    }
    
    @Override
    public String toString() {
        String s = ""+stamp;
        s = s.replace("-", "");
        s = s.replace(".", "");    
        s = s.replace(":", "");    
        return s;
    }
    
    /**
     * Outputs current time to console, in the format of
     * "YYYY-MM-DD HH:MM:SS:milli".
     */
    public void printTime() {
        System.out.println(stamp);
    }
}