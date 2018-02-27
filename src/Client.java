
import java.awt.Point;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.Socket;
import java.util.Vector;
import javax.swing.JFileChooser;

/**
 * The main thread run on any client.
 * The client class will take in arguments to execute, 
 * and create various UI elements accordingly.
 * @author Jinqiu Liu
 */
public class Client {
    public static String hostIP;
    public static String hostPort;
    public static String username;
    public static String password;
    /**
     * 0- Normal/Unspecified;
     * 1- input only: just import;
     * 2- output only: just report;
     * 3- input and output: import then report.
     */
    public static int intentionCode;
    private static Settings settings;
    
    public static Socket connection;
    public static DataInputStream fromServer;
    public static DataOutputStream toServer;
    
    public static Point windowLocation;
    
    public static UI_Connect connect;
    public static UI_Login login;
    public static UI_Register register;
    public static UI_MainMenu mainMenu;
    public static UI_SearchInventory searchInventory;
    public static UI_SearchInventoryResults searchInventoryResults;
    public static UI_SearchUser searchUser;
    
    /**
     * Client's run method.
     * @param args
     */
    public static void main(String[] args) {
        parseArgs(args);        
        //connection
        boolean connectionResult = connectToHost(hostIP, hostPort);
        if (!connectionResult) { //connection arguments did not work, use UI
            initUI_Connect();
        } else {
            //login
            boolean loginResult = login(username, password);
            if (!loginResult) {
                initUI_Login();
            } else {
                initUI_MainMenu();
            }
        }
        //the login UI, if login successful, will call the main menu
    }
    
    public static boolean connectToHost(String ip, String port) {
        if (ip == null || port == null) {
            return false;
        }
        String hostIP = ip;
        int hostPort = Integer.parseInt(port);
        try {
            connection = new Socket(hostIP, hostPort);
            fromServer = new DataInputStream(connection.getInputStream());
            toServer = new DataOutputStream(connection.getOutputStream());
            return true;
        } catch (IOException ex) {
            return false;
        }
    }
    
    public static boolean autoLogin() {
        return login(username, password);
    }
    
    public static boolean login(String un, String pw) {
        String userName, passWord;
        if (un == null) {
            return false;
        } else {
            userName = un;
        }
        if (pw == null) {            
            return false;
        } else {            
            passWord = pw;
        }

        Message m = new Message(101, userName, passWord);
        out(m);
        Message sm = new Message(in());
        if (sm.messageCode == 1101) {
            return true;
        } else {
            //Authentication failed
            return false;
        }
    } 
    
    public static boolean login(String un, char[] pw) {
        String pwString = "";
        for (int i = 0; i < pw.length; i++) {
            pwString += pw[i];
        }
        return (login(un, pwString));
    }
    
    public static boolean register(String un, String pw) {
        Message m = new Message(102, un, pw);
        out(m);
        Message sm = new Message(in());
        if (sm.messageCode == 1103) {
            return true;
        } else {
            return false;
        }
    }
    
    public static boolean register(String un, char[] pw) {
        String pwString = "";
        for (int i = 0; i < pw.length; i++) {
            pwString += pw[i];
        }
        return (register(un, pwString));
    }
    
    public static void disconnect() {
        Message m = new Message(0);
        out(m);
        System.exit(0);
    }
        
    private static String in() {
        try {
            String msg = fromServer.readUTF();
            //System.out.println("in-"+msg);
            return msg;
        } catch (IOException ex) {
            System.out.println("Error getting message from server.");
        }
        return null;
    }
    
    private static void out(String msg) {
        try {
            //System.out.println("out-"+msg);
            toServer.writeUTF(msg);
        } catch (IOException ex) {
            System.out.println("Error seding message to server.");
        }
    }
    
    private static void out(Message m) {
        out(m.toString());
    }

    public static void searchInventory(String[] args) {
        out(new Message(201, args));
        Message sm = new Message(in());
        if (sm.messageCode == 1200) {
            //no results
            initUI_SearchInventoryResults();
        } else {
            //1 or more results
            Vector display = new Vector();
            Vector detail = new Vector();
            Vector quantity = new Vector();
            while (sm.messageCode == 1201) {
                String s = sm.vars[1] + "x " + sm.vars[0];
                display.add(s);
                Message nest = new Message(sm.vars[2]);
                detail.add(nest.vars);
                quantity.add(Integer.parseInt(sm.vars[1]));
                sm = new Message(in());
            } //end of results
            initUI_SearchInventoryResults(display, detail, quantity);
        }
    }

    public static void updateInventory(String ISBN, int newQuant) {
        out(new Message(401, ISBN, newQuant+""));
        Message sm = new Message(in());
        if (sm.messageCode == 1401) {
            initUI_popMessage("Update Success!");
        } else {
            initUI_popMessage("Unexpected Server response!");
        }
    }
    
    public static void batchUpdateInventory() {
        File catalogFile = null;
        JFileChooser jfc = new JFileChooser();
        jfc.setApproveButtonText("Use as Inventory File");
        int choice = jfc.showOpenDialog(null);            
        if (choice == JFileChooser.APPROVE_OPTION) {
            catalogFile = jfc.getSelectedFile();
        } else {
            System.out.println("File was not specified!");
            return;
        }
        try {
            BufferedReader br = new BufferedReader(new FileReader(catalogFile));
            String line = "-";
            while (true) {
                line = br.readLine();
                if (line == null) break;
                if (line.length() == 0) {
                    line = br.readLine();
                }
                if (line == null) break;
                
                out(new Message(411, line));    
            }
            out(new Message(412));
            Message sm = new Message(in());
            if (sm.messageCode == 1412) {
                initUI_popMessage("Batch Operation Successfully Completed!");
            } else {
                initUI_popMessage("Unexpected Server Response, redo operation");
            }            
        } catch (Exception e) {
            System.out.println("File I/O Error while performing batch operation!");
            initUI_popMessage("File I/O Error while performing batch operation!");
        }
    }
    
    public static void batchUpdateInventory(File f) {
        File catalogFile = f;
        try {
            BufferedReader br = new BufferedReader(new FileReader(catalogFile));
            String line = "-";
            while (true) {
                line = br.readLine();
                if (line == null) break;
                if (line.length() == 0) {
                    line = br.readLine();
                }
                if (line == null) break;
                
                out(new Message(411, line));    
            }
            out(new Message(412));
            Message sm = new Message(in());
            if (sm.messageCode == 1412) {
                initUI_popMessage("Batch Operation Successfully Completed!");
            } else {
                initUI_popMessage("Unexpected Server Response, redo operation");
            }            
        } catch (Exception e) {
            System.out.println("File I/O Error while performing batch operation!");
            initUI_popMessage("File I/O Error while performing batch operation!");
        }
    }
    
    public static void autoGet(UI_UpdateCatalog ui, String ISBN) {
        String[] vars = URLProcess.rip(ISBN);
        if (containsNull(vars)) {
            initUI_popMessage("Unable to automatically fill");
        } else {
            ui.fillCatalogInfo(vars);
        }
    }
    
    private static boolean containsNull(String[] s) {
        if (s == null) {
            return true;
        } else {
            for (int i = 0; i < s.length; i++) {
                if (s[i] == null) {
                    return true;
                }
            }
        }
        return false;
    }

    public static void updateCatalog(String[] args) {
        Message m = new Message(301, args);
        out(m);
        Message sm = new Message(in());
        if (sm.messageCode == 1301) {
            initUI_popMessage("New Catalog Item Created");
        } else {
            initUI_popMessage("Existing Catalog Item Modified");
        }
    }
    
    public static void batchUpdateCatalog() {
        File catalogFile = null;
        JFileChooser jfc = new JFileChooser();
        jfc.setApproveButtonText("Use as Catalog File");
        int choice = jfc.showOpenDialog(null);            
        if (choice == JFileChooser.APPROVE_OPTION) {
            catalogFile = jfc.getSelectedFile();
        } else {
            System.out.println("File was not specified!");
            return;
        }
        try {
            BufferedReader br = new BufferedReader(new FileReader(catalogFile));
            String line = "-";
            while (true) {
                line = br.readLine();
                if (line == null) break;
                if (line.length() == 0) {
                    line = br.readLine();
                }
                if (line == null) break;
                String ISBN = line;
                String Title = br.readLine();
                String Author = br.readLine();
                String Publisher = br.readLine();
                String PubYear = br.readLine();
                
                String[] args = {ISBN, Title, Author, Publisher, PubYear};
                
                out(new Message(308, args));
            }
            out(new Message(309));
            Message sm = new Message(in());
            if (sm.messageCode == 1309) {
                initUI_popMessage("Batch Operation Successfully Completed!");
            } else {
                initUI_popMessage("Unexpected Server Response, redo operation");
            }            
        } catch (Exception e) {
            System.out.println("File I/O Error while performing batch operation!");
            initUI_popMessage("File I/O Error while performing batch operation!");
        }        
    }
    
    public static void batchExport() {
        File exportDirectory;
        JFileChooser jfc = new JFileChooser();
        jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        jfc.setApproveButtonText("Use this Directory");
        jfc.setApproveButtonToolTipText("Create Inventory Report in this directory");            
        int choice = jfc.showOpenDialog(null);
        if (choice == JFileChooser.APPROVE_OPTION) {
            exportDirectory = jfc.getSelectedFile();
        } else {
            exportDirectory = null;
            System.out.println("Working directory was not specified!");
        }
        
        File f = new File(exportDirectory.getAbsolutePath() + "/InventoryReport" + new TimeStamp() + ".txt");
        
        try {
            f.createNewFile();
            BufferedWriter bw = new BufferedWriter(new FileWriter(f));
            
            out(new Message(211, ""));
            Message sm = new Message(in());
            if (sm.messageCode == 1200) {
                //no results
                bw.append("No results");
            } else {
                //1 or more results
                while (sm.messageCode == 1201) {
                    bw.append(sm.vars[1] + "x " + sm.vars[0]);                    
                    bw.newLine();                    
                    sm = new Message(in());
                } //end of results                
            }
            bw.close();
        } catch (IOException ex) {
            System.out.println("File I/O Exception while exporting");
        }        
    }
    
    public static void batchExport(File f) {
        if (f.isDirectory()) {
            f = new File(f.getAbsolutePath() + "/InventoryReport" + new TimeStamp() + ".txt");
        }
        
        try {
            f.createNewFile();
            BufferedWriter bw = new BufferedWriter(new FileWriter(f));
            
            out(new Message(211, ""));
            Message sm = new Message(in());
            if (sm.messageCode == 1200) {
                //no results
                bw.append("No results");
            } else {
                //1 or more results
                while (sm.messageCode == 1201) {
                    bw.append(sm.vars[1] + "x " + sm.vars[0]);                    
                    bw.newLine();                    
                    sm = new Message(in());
                } //end of results                
            }
            bw.close();
        } catch (IOException ex) {
            System.out.println("File I/O Exception while exporting");
        }
    }

    /**
     * Checks the current user's admin/priviledge level with the server.
     * @return 0 = admin, 1 = regular user, -1 = error/non-existing user.
     */
    public static int checkUserLevel() {
        out(new Message(505));
        Message sm = new Message(in());
        return Integer.parseInt(sm.vars[0]);
    }
    
    public static void promoteUser(String un) {
        out(new Message(501, un));
    }
    
    public static void deleteUser(String un) {
        out(new Message(503, un));
    }    
    
    private static void parseArgs(String[] args) {
        if (args.length > 0) {
            try {
                for (int i = 0; i < args.length; i++) {
                    if (args[i].equalsIgnoreCase("-ip")) {
                        hostIP = args[i+1];
                    } else if (args[i].equalsIgnoreCase("-port")) {
                        hostPort = args[i+1];
                    } else if (args[i].equalsIgnoreCase("-u")) {
                        username = args[i+1];
                    } else if (args[i].equalsIgnoreCase("-p")) {
                        password = args[i+1];
                    }
                } 
            } catch (Exception e) {
                System.out.println("Invalid arguments format!");      
                hostIP = null;
                hostPort = null;
                username = null;
                password = null;
            }
        }
        settings = new Settings(args);
        intentionCode = settings.getIntentionCode();
    }
    
    //Pre-Main Menu ---------------------------------------------------------
    
    public static void initUI_Connect() {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                connect = new UI_Connect();
                if (windowLocation != null) connect.setLocation(windowLocation);
                connect.setVisible(true);
            }
        });
    }
    
    public static void initUI_Login() {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                login = new UI_Login();
                if (windowLocation != null) login.setLocation(windowLocation);
                login.setVisible(true);
            }
        });
    }
    
    public static void hideUI_Login() {
        login.setVisible(false);
    }
    
    public static void showUI_Login() {
        if (windowLocation != null) login.setLocation(windowLocation);
        login.setVisible(true);
    }
    
    public static void initUI_Register() {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                register = new UI_Register();
                if (windowLocation != null) register.setLocation(windowLocation);
                register.setVisible(true);
            }
        });
    }
    
    public static void hideUI_Register() {
        register.setVisible(false);
    }
    
    public static void showUI_Register() {
        if (windowLocation != null) register.setLocation(windowLocation);
        register.setVisible(true);
    }
    
    // Main Menu ------------------------------------------------------------
    
    public static void initUI_MainMenu() {
        if (intentionCode == 1) { //import only
            if (settings.inputFile == null) {
                System.out.println("Input file not linked properly");
                System.exit(2);
            }
            batchUpdateInventory(settings.inputFile);
            System.out.println("Batch import complete");
            System.exit(0);
        } else if (intentionCode == 2) { //export only
            if (settings.outputFile == null) {
                System.out.println("Output file not linked properly");
                System.exit(2);
            }
            batchExport(settings.outputFile);
            System.out.println("Batch export complete");
            System.exit(0);
        } else if (intentionCode == 3) { //import and export
            if (settings.inputFile == null) {
                System.out.println("Input file not linked properly");
                System.exit(2);
            }
            if (settings.outputFile == null) {
                System.out.println("Output file not linked properly");
                System.exit(2);
            }                    
            batchUpdateInventory(settings.inputFile);
            System.out.println("Batch import complete");
            batchExport(settings.outputFile);
            System.out.println("Batch export complete");
            System.exit(0);
        } else {
            java.awt.EventQueue.invokeLater(new Runnable() {
                public void run() {
                    mainMenu = new UI_MainMenu();
                    if (windowLocation != null) mainMenu.setLocation(windowLocation);
                    mainMenu.setVisible(true);
                }
            });
        }
    }
    
    public static void showUI_MainMenu() {
        if (windowLocation != null) mainMenu.setLocation(windowLocation);
        mainMenu.setVisible(true);
    }
    
    // Inventory -------------------------------------------------------------
    
    public static void initUI_SearchInventory() {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                searchInventory = new UI_SearchInventory();
                if (windowLocation != null) searchInventory.setLocation(windowLocation);
                searchInventory.setVisible(true);
            }
        });
    }
    
    public static void showUI_SearchInventory() {
        if (windowLocation != null) searchInventory.setLocation(windowLocation);
        searchInventory.setVisible(true);
    }
    
    public static void hideUI_SearchInventory() {
        searchInventory.setVisible(false);
    }
    
    public static void initUI_SearchInventoryResults() {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                searchInventoryResults = new UI_SearchInventoryResults();
                Vector v = new Vector();
                v.add("No results");
                searchInventoryResults.setDisplayList(v);
                if (windowLocation != null) searchInventoryResults.setLocation(windowLocation);    
                searchInventoryResults.setVisible(true);
            }
        });
    }
    
    public static void initUI_SearchInventoryResults(Vector display, Vector details, Vector quantity) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                searchInventoryResults = new UI_SearchInventoryResults();
                searchInventoryResults.setDisplayList(display);
                searchInventoryResults.setDetailedInfo(details, quantity);
                if (windowLocation != null) searchInventoryResults.setLocation(windowLocation);      
                searchInventoryResults.setVisible(true);
            }
        });
    }
    
    public static void showUI_SearchInventoryResults() {
        searchInventoryResults.setVisible(true);
    }
    
    public static void initUI_ItemDetail(String[] cataInfo, int qInfo) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                UI_ItemDetail id = new UI_ItemDetail();
                id.setData(cataInfo, qInfo);
                if (windowLocation != null) id.setLocation(windowLocation);      
                id.setVisible(true);
            }
        });
    }
    
    // Update Catalog ------------------------------------------------------
    
    public static void initUI_UpdateCatalog(String[] args) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                UI_UpdateCatalog uc = new UI_UpdateCatalog();
                if (windowLocation != null) uc.setLocation(windowLocation);
                if (args == null) {
                    uc.clearStockInfo();
                } else if (args.length == 5) { //catalog info only
                    uc.fillCatalogInfo(args);
                    uc.clearStockInfo();
                } else if (args.length == 6) { //catalog info and stock info
                    uc.fillCatalogInfo(args);
                    uc.fillStockInfo(args);
                }
                uc.setVisible(true);
            }
        });        
    }
    
    public static void initUI_UpdateInventory(String[] args) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                UI_UpdateInventory ui = new UI_UpdateInventory();
                if (windowLocation != null) ui.setLocation(windowLocation);
                if (args == null) {
                    ui.clearStockInfo();
                } else if (args.length == 5) { //catalog info only
                    ui.fillCatalogInfo(args);
                    ui.clearStockInfo();
                } else if (args.length == 6) { //catalog info and stock info
                    ui.fillCatalogInfo(args);
                    ui.fillStockInfo(args);
                }
                ui.setVisible(true);
            }
        });   
    }
    
    // User Management -----------------------------------------------------
    
    public static void initUI_SearchUser() {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                searchUser = new UI_SearchUser();   
                if (windowLocation != null) searchUser.setLocation(windowLocation);
                searchUser.setVisible(true);
            }
        });
    }
    
    public static void showUI_SearchUser() {
        if (windowLocation != null) searchUser.setLocation(windowLocation);
        searchUser.setVisible(true);
    }
    
    public static void hideUI_SearchUser() {
        searchUser.setVisible(false);
    }
    
    public static void initUI_SearchUserResults(String un) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                UI_SearchUserResults sur = new UI_SearchUserResults();   
                out(new Message(506, un));
                Message sm = new Message(in());
                String[] results = {un, sm.vars[0]};
                if (sm.vars[0].equals("1")) {
                    results[1] = "Normal User";
                } else if (sm.vars[0].equals("0")) {
                    results[1] = "Admin User";
                } else {
                    results[1] = "User does not exist";
                }
                sur.displayInfo(results);
                if (windowLocation != null) sur.setLocation(windowLocation);
                sur.setVisible(true);
            }
        });        
    }
    
    public static void initUI_popMessage(String message) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                UI_ServerMessage pop = new UI_ServerMessage();
                pop.setMessage(message);
                if (windowLocation != null) pop.setLocation(windowLocation);
                pop.setVisible(true);
            }
        });
    }    
    
}
