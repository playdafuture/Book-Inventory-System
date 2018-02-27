import java.io.File;
import javax.swing.JFileChooser;

/**
 * Settings for the system, such as input/output file, log file, etc.
 * @author Jinqiu Liu
 */
public class Settings {
    String[] args;
    File inputFile;
    File outputFile;
    /**
     * 0- Normal/Unspecified;
     * 1- input only: just import;
     * 2- output only: just report;
     * 3- input and output: import then report.
     */
    int routine;
    
    public Settings(String[] passedArgs) {
        routine = 0;
        args = passedArgs;                      //make args public within class
        for (int i = 0; i < args.length; i++) { //goes thru all the tokens
            if (args[i].charAt(0) == '-') {     //i-th position is a flag
                String flag = args[i];
                if (flag.length() == 2) {
                    if (flag.charAt(1) == 'i' || flag.charAt(1) == 'I') {
                        //input flag
                        routine += 1;
                        setInputFile(i+1);
                    } else if (flag.charAt(1) == 'o' || flag.charAt(1) == 'O') {
                        //output flag
                        routine += 2;
                        setOutputFile(i+1);                      
                    } else if (flag.equalsIgnoreCase("-u")
                            || flag.equalsIgnoreCase("-p")
                            || flag.equalsIgnoreCase("-ip")
                            || flag.equalsIgnoreCase("-port")) { //handled by outside
                        //Do nothing
                    } else {
                        //illegal flag
                        System.out.println("\"" + args[i] + "\" is not a supported flag.");
                    }
                } else {
                    //illegal flag
                    System.out.println("\"" + args[i] + "\" is not a supported flag.");
                }
            } // else, this token does not have the flag symbol, don't worry
        } // end of for-loop (for each token in argument)
    }
    
    private void setInputFile(int index) {
        try {
            String fileName = args[index];
            if (fileName.charAt(0) == '-') { //next token is also a flag
                throw new Exception();
            }
            inputFile = new File(fileName);            
        } catch (Exception e) {
            System.out.println("Failed to set input file, please use File Chooser to specify...");
            JFileChooser jfc = new JFileChooser();
            int choice = jfc.showOpenDialog(null);            
            if (choice == JFileChooser.APPROVE_OPTION) {
                inputFile = jfc.getSelectedFile();
            } else {
                inputFile = null;
                System.out.println("Input file was not specified!");
            }
        }
    }
    
    private void setOutputFile(int index) {
        try {
            String fileName = args[index];
            if (fileName.charAt(0) == '-') { //next token is also a flag
                throw new Exception();
            }
            outputFile = new File(fileName);            
        } catch (Exception e) {
            System.out.println("Failed to set output file, please use File Chooser to specify...");
            JFileChooser jfc = new JFileChooser();
            int choice = jfc.showOpenDialog(null);            
            if (choice == JFileChooser.APPROVE_OPTION) {
                outputFile = jfc.getSelectedFile();
            } else {
                outputFile = null;
                System.out.println("Output file was not specified!");
            }
        }
    }
    
    public int getIntentionCode() {
        return routine;
    }
    
    public void print() {
        System.out.println("Settings status: ");
        if (inputFile == null) {
            System.out.println("Input File = null");
        } else {
            System.out.println("Input File = " + inputFile);
        }
        if (outputFile == null) {
            System.out.println("Output File = null");
        } else {
            System.out.println("Output File = " + outputFile);
        }
    }
}
