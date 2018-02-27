
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;

/**
 * Server class. Contains all the important aspects.
 * Server will link up database, authenticate, and spawn agent threads 
 * to clients and provide services according to their privilege level.
 * @author Jinqiu Liu
 */
public class Server {      
    
    static class DatabaseObject implements Serializable  {
        Inventory inventory;
        Catalog catalog;
        public DatabaseObject() {
            //nothing, really
        }        
    }
    public static Inventory inventory;              //inventory object
    public static Catalog catalog;                  //catalog object    
    public static File workingDirectory;            //path to all database files
    public static Log log;                          //log object
    public static Authentication authentication;    //authentication object
    public static Host host;
    private static final Scanner sc = new Scanner(System.in);
    private static String input = "";    
    
    public static void main(String[] args) {
        System.out.println("Server started!");
        setupEnvironment(args);
        host = new Host();
        host.start();
        System.out.println("Server is up and running. Type \"EXIT\" to shut down server");
        while (true) {
            input = sc.next();
            if (input.equals("EXIT")) {
                break;
            }
        }
        System.out.println("Starting saving sequence. Server will shut down automatically");
        save();
        System.out.println("All save process success, server will close now.");
        System.exit(0);
    }
    
    /**
     * Sets up working environment for the server.
     * The following aspects must be met: 1 or 2
     * 1. Working directory - Either specified, or deduced from 2.
     * 2. Database file - Either specified, or deduced from 1.
     * 3. Authentication file - deduced from 1.
     * 4. Database objects - deduced from 2. This includes catalog and inventory.
     * 5. Log file save location - deduced from 1.
     * @param args Can be empty or specify the working directory.
     */
    public static void setupEnvironment(String[] args) {
        //Step 1 xor 2.
        if (args.length > 0) {
            //args must contain path
            System.out.println("Directory argument found! Linking database file...");
            setWorkingDirectory(args[0]);
        } else {
            System.out.println("Directory argument not found!"
                + "\nChoose an option:"
                + "\n\t1. Specify a working directory and automatically find the latest database file"
                + "\n\t2. Specify an existing Database file and deduce the working directory"
                + "\n\t3. Specify a working directory and start a brand new databse"
                + "\n\t4. Specify a Database file and then a log file to re-construct a new Database (recovery)");
            input = sc.next();
            switch (input) {
                case "1":
                    jfcChooseDirectory();
                    setWorkingDirectory(workingDirectory.getAbsolutePath());
                    break;
                case "2":
                    jfcChooseFile();
                    break;
                case "3":
                    jfcChooseDirectory();
                    inventory = new Inventory();
                    catalog = new Catalog();
                    break;
                case "4":
                    setUpRecovery();
                    break;
            }
        }        
        //Step 3. Authentication file
        File authFile = new File(workingDirectory + "/users.info");
        if (authFile.exists()) {
            try {
                ObjectInputStream authin = new ObjectInputStream(new FileInputStream(authFile));
                authentication = (Authentication) authin.readObject();
                authin.close();
            } catch (Exception e) {
                System.out.println("Failed to load authentication info."
                        + "\n(C)reate new or (A)bort?");
                input = sc.next();
                if (input.equalsIgnoreCase("C")) {
                    authentication = new Authentication();
                } else {
                    System.exit(1);
                }
            }            
        } else {
            authentication = new Authentication();
        }
        //Step 4. Database objects
        //handled in step 1 xor 2
        
        //Step 5. Log file
        log = new Log(); 
    } 
    
    /**
     * Chooses a directory and sets it as the working directory.
     * This does NOT search for DB file or link them.
     */
    private static void jfcChooseDirectory() {
        JFileChooser jfc = new JFileChooser();
        jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        jfc.setApproveButtonText("Use this Directory");
        jfc.setApproveButtonToolTipText("Use the current directory");            
        int choice = jfc.showOpenDialog(null);
        if (choice == JFileChooser.APPROVE_OPTION) {
            workingDirectory = jfc.getSelectedFile();
        } else {
            workingDirectory = null;
            System.out.println("Working directory was not specified!");
        }
    }
    
    /**
     * Use file choose to get database file, 
     * links database objects, then deduce the working directory.
     */
    private static void jfcChooseFile() {        
        File databaseFile = null;
        JFileChooser jfc = new JFileChooser();
        jfc.setApproveButtonText("Use as Database File");
        int choice = jfc.showOpenDialog(null);            
        if (choice == JFileChooser.APPROVE_OPTION) {
            databaseFile = jfc.getSelectedFile();
        } else {
            System.out.println("Database file was not specified!");
            System.exit(1);
        }

        //load from file
        try {
            ObjectInputStream in = new ObjectInputStream(new FileInputStream(databaseFile));
            DatabaseObject dbobj = (DatabaseObject) in.readObject();
            in.close();
            inventory = dbobj.inventory;
            catalog = dbobj.catalog;
        } catch (Exception e) {
            System.out.println("Database File load error");
            System.exit(1);
        }        
        String filePath = databaseFile.getAbsolutePath();
        int idx = filePath.lastIndexOf("\\");
        filePath = filePath.substring(0, idx);
        workingDirectory = new File(filePath);
    }
    
    /**
     * Given a directory, search the latest db file.
     * If file is found, link object.
     * If no file is found, create blank database file.
     * @param dir Directory String.
     */
    private static void setWorkingDirectory(String dir) {
        workingDirectory = new File(dir);
        File[] allDBFiles = workingDirectory.listFiles();
        for (int i = allDBFiles.length - 1; i >= 0; i--) {
            String fileName = allDBFiles[i].getName();
            if (fileName.startsWith("Database") && fileName.endsWith(".db")) {
                try {
                    ObjectInputStream in = new ObjectInputStream(new FileInputStream(allDBFiles[i]));
                    DatabaseObject dbobj = (DatabaseObject) in.readObject();
                    in.close();
                    inventory = dbobj.inventory;
                    catalog = dbobj.catalog;
                    return;
                } catch (Exception e) {
                    System.out.println("Error trying to set up working directory to " + dir);
                }
                break;
            }
        }
        System.out.println("No database file found! Creating new...");
        inventory = new Inventory();
        catalog = new Catalog();
    }
    
    private static void setUpRecovery() {
        DatabaseObject dbo = null;
        File logFile = null;
        
        
        JFileChooser jfc = new JFileChooser();
        jfc.setApproveButtonText("Use as Database File");
        int choice = jfc.showOpenDialog(null);            
        if (choice == JFileChooser.APPROVE_OPTION) {
            File dbf = jfc.getSelectedFile();            
            try {
                ObjectInputStream in;
                in = new ObjectInputStream(new FileInputStream(dbf));
                dbo = (DatabaseObject) in.readObject();
                
                String filePath = dbf.getAbsolutePath();
                int idx = filePath.lastIndexOf("\\");
                filePath = filePath.substring(0, idx);
                workingDirectory = new File(filePath);
            } catch (Exception ex) {
                System.out.println("Error while reading and converting Database File");
                System.exit(2);
            }            
        } else {
            System.out.println("Database file was not specified!");
            System.exit(1);
        }      
        
        
        jfc = new JFileChooser();
        jfc.setApproveButtonText("Use as Log File");
        choice = jfc.showOpenDialog(null);            
        if (choice == JFileChooser.APPROVE_OPTION) {
            logFile = jfc.getSelectedFile();
        } else {
            System.out.println("Log file was not specified!");
            System.exit(1);
        }
        
        recovery(dbo, logFile);
    }

    private static void save() {
        String timeStamp = new TimeStamp()+"";
        DatabaseObject dbobj = new DatabaseObject();
        dbobj.catalog = catalog;
        dbobj.inventory = inventory;
        String databaseFileName = "Database." + timeStamp + ".db";
        File databaseFile = new File(workingDirectory + "/" + databaseFileName);
        try {
            databaseFile.createNewFile();
            ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(databaseFile));
            out.writeObject(dbobj);
            out.close();
        } catch (IOException e) {
            System.out.println("Critical Error! File save failed!");
            System.out.println(e.getMessage());
            System.out.println(e.getLocalizedMessage());
            System.exit(2);
        }        
        System.out.println("File save success!");
        log.close();
        System.out.println("Log save success!");
        File authFile = new File(workingDirectory + "/users.info");
        try {
            ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(authFile));
            out.writeObject(authentication);
            out.close();
        } catch (IOException e) {
            System.out.println("Critical Error! File save failed!");
            System.out.println(e.getMessage());
            System.out.println(e.getLocalizedMessage());
            System.exit(2);
        }
        System.out.println("Authentication save success!");
    }
    
    private static void recovery(DatabaseObject dbo, File logFile) {
        catalog = dbo.catalog;
        inventory = dbo.inventory;
        try {
            BufferedReader br = new BufferedReader(new FileReader(logFile));
            Console recoverConsole = new Console("Recovery");            
            String line = br.readLine();            
            while (line != null) {
                if (line.isEmpty()) {
                    line = br.readLine();
                    continue;
                }
                Message m = new Message(line);
                recoverConsole.recover(m);
                line = br.readLine();
            }
        } catch (Exception ex) {
            System.out.println("Recovery error!");
            return;
        }
        System.out.println("Recovery complete! EXIT to save");        
    }
}
