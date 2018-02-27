
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * The console acts as a user's avatar on the server and calls the functions. 
 * It also relays the message/results back to the client.
 * @author Jinqiu Liu
 */
public class Console extends Thread {
    Socket connection;
    DataInputStream input;
    DataOutputStream output;
    String userName = "Unknown User";
    int priviledgeLevel = -1; //-1 = not authenticated, 0 = admin, 1 = user
    
    public Console(String userName) {
        this.userName = userName;
    }
    
    public Console(Socket connection) {        
        setName(userName);
        this.connection = connection;
        try {
            input = new DataInputStream(connection.getInputStream());
            output = new DataOutputStream(connection.getOutputStream());
        } catch (IOException ex) {
            System.out.println("Connection error when creating console for " + userName);
        }
    }
    
    @Override
    public void run() {
        System.out.println(userName + " connected");
        while (true) {
            try {
                Message m = new Message(in());
                if (m.messageCode == 0) {
                    break;
                } else {
                    decode(m);
                }
            } catch (java.lang.NullPointerException e) {
                System.out.println("Failed to get message from " + userName);
                break;
            }            
        }
        System.out.println(userName + " disconnected");
    }
    
    private void decode(Message m) {
        int mc = m.messageCode;
        String[] v = m.vars;
        if (mc == 101) {
            authenticate(v[0], v[1]);
        }         
        else if (mc == 102) {
            newUser(v[0], v[1]);
        } 
        else if (mc == 201) {
            searchInventory(v);
        }
        else if (mc == 211) {
            batchSearch();
        }
        else if (mc == 301) {
            log(m);
            modCatalogEntry(v);
        }
        else if (mc == 308) {
            log(m);
            modCatalogEntryNR(v);
        }
        else if (mc == 309) { //end of batch import catalog
            out(new Message(1309));
        }
        else if (mc == 401) {
            log(m);
            modInventory(v[0], v[1]);
        }
        else if (mc == 411) {
            log(m);
            incInventory(v[0]);
        }
        else if (mc == 412) { //end of batch import inventory
            out(new Message(1412));
        }
        else if (mc == 501) {
            promoteUser(v[0]);
        }
        else if (mc == 503) {
            deleteUser(v[0]);
        }        
        else if (mc == 505) { //get this.priviledgelevel
            out(new Message(1505, ""+priviledgeLevel));
        }
        else if (mc == 506) {
            getUserInfo(v[0]);
        }
    }
    
    public void recover(Message m) {
        int mc = m.messageCode;
        String[] v = m.vars;
        if (mc == 301) {
            modCatalogEntryNR(v);
        }
        else if (mc == 308) {
            modCatalogEntryNR(v);
        }
        else if (mc == 401) {
            modInventoryNR(v[0], v[1]);
        }
        else if (mc == 411) {
            incInventory(v[0]);
        }
    }
    
    /**
     * Authenticate a user by checking the username and password.
     * Returns 1100 if login failed and 1101 if success.
     * @param username
     * @param password 
     */
    private void authenticate(String username, String password) {
        Message m;
        int loginResult = Server.authentication.authUser(username, password);
        priviledgeLevel = loginResult;        
        if (loginResult < 0) {
            //Failed
            m = new Message(1100);
        } else {
            //Success
            m = new Message(1101);
            this.userName = username;
            System.out.println(username + " authenticated");
        }
        out(m);
    }
    
    private void newUser(String newUN, String newPW) {
        Message m;
        if (Server.authentication.findUser(newUN)) {
            //user name already exist
            m = new Message(1102);
        } else {
            Server.authentication.addUser(newUN, newPW, 1); 
            //default user privilaege is not admin
            m = new Message(1103);
        }
        out(m);
    }
    
    private void getUserInfo(String un) {
        int ul = Server.authentication.getUserLevel(un);
        out(new Message(1506, ""+ul));
    }
    
    private void promoteUser(String un) {
        Server.authentication.promoteUser(un);        
    }
    
    private void deleteUser(String un) {
        Server.authentication.delUser(un);        
    }
    
    /**
     * Search the inventory based on the arguments.
     * @param args 
     * 201: Search Inventory: ISBN, Title, Author
     * 		If ISBN is present [use ISBN] else [use Title and/or Author]
     *          Null Values Allowed.
     *          [Response: 1200 or 1201,1201,1201,...,1209]
     */
    private void searchInventory(String[] args) {
        if (!args[0].isEmpty()) {
            //search by ISBN only
            if (Server.inventory.getListing(args[0]) == null) {
                //no such listing
                out(new Message(1200));
                return;
            } else {
                returnISBNResult(args[0]);        //sub-routine, returns 1201     
                out(new Message(1209)); //end of results
            }     
        } else {
            String[] ISBNs = null;
            if (!args[1].isEmpty() && !args[2].isEmpty()) {
                //use both
                ISBNs = Server.catalog.getISBNsByTitleAndAuthor(args[1], args[2]);
            } else if (args[2].isEmpty()) {
                //use args1
                ISBNs = Server.catalog.getISBNsByTitle(args[1]);
            } else {
                //use args2
                ISBNs = Server.catalog.getISBNsByAuthor(args[2]);
            }
            if (ISBNs == null || ISBNs.length == 0) {
                out(new Message(1200));
                return;
            } else {
                for (int i = 0; i < ISBNs.length; i++) {
                    returnISBNResult(ISBNs[i]);
                }
                out(new Message(1209)); //end of results
            }
        }        
    }
    
    private void returnISBNResult(String ISBN) {
        String[] returnArgs = new String[3];
        returnArgs[0] = Server.catalog.getEntry(ISBN).toString();
        returnArgs[1] = ""+Server.inventory.getQuantity(ISBN);
        Message nestedMessage = new Message(1202, Server.catalog.getEntry(ISBN).toStringArray());
        returnArgs[2] = nestedMessage+"";
        out(new Message(1201, returnArgs));
    }
    
    /**
     * Gets all listing in the database.
     * Returns 1200 if server is empty or 1201 for each entry and 1209 to end.
     */
    private void batchSearch() {
        Inventory.Listing[] all = Server.inventory.getAllListings();
        if (all.length == 0) {
            out(new Message(1200));
        } else {
            for (int i = 0; i < all.length; i++) {
                returnISBNResult(all[i].getISBN());
            }
            out(new Message(1209));
        }        
    }
   
    private void modCatalogEntry(String[] v) {
        Item i = Server.catalog.getEntry(v[0]);
        if (i != null) {
            if (v[1] != null) i.setTitle(v[1]);
            if (v[2] != null) i.setAuthorName(v[2]);
            if (v[3] != null) i.setPublisher(v[3]);
            if (v[4] != null) i.setYearPublished(Integer.parseInt(v[4]));
            out(new Message(1302));
        } else {
            Item newItem = new Item(v[0]);
            if (v[1] != null) newItem.setTitle(v[1]);
            if (v[2] != null) newItem.setAuthorName(v[2]);
            if (v[3] != null) newItem.setPublisher(v[3]);
            if (v[4] != null) newItem.setYearPublished(Integer.parseInt(v[4]));
            Server.catalog.newEntry(newItem);
            out(new Message(1301));
        }        
    }    
    
    private void modCatalogEntryNR(String[] v) {
        Item i = Server.catalog.getEntry(v[0]);
        if (i != null) {
            if (v[1] != null) i.setTitle(v[1]);
            if (v[2] != null) i.setAuthorName(v[2]);
            if (v[3] != null) i.setPublisher(v[3]);
            if (v[4] != null) i.setYearPublished(Integer.parseInt(v[4]));
        } else {
            Item newItem = new Item(v[0]);
            if (v[1] != null) newItem.setTitle(v[1]);
            if (v[2] != null) newItem.setAuthorName(v[2]);
            if (v[3] != null) newItem.setPublisher(v[3]);
            if (v[4] != null) newItem.setYearPublished(Integer.parseInt(v[4]));
            Server.catalog.newEntry(newItem);
        }        
    }    
    
    private void modInventory(String ISBN, String newQuant) {
        Server.inventory.modifyListing(ISBN, Integer.parseInt(newQuant));
        out(new Message(1401));
    }
    
    private void modInventoryNR(String ISBN, String newQuant) {
        Server.inventory.modifyListing(ISBN, Integer.parseInt(newQuant));
    }
    
    private void incInventory(String ISBN) {
        Server.inventory.incListing(ISBN);
    }
    
    private void out(String message) {
        try {
            //System.out.println("out-"+message);
            output.writeUTF(message);            
        } catch (IOException ex) {
            System.out.println("Connection error when sending message to user " + userName);
        }
    }
    
    private void out(Message m) {
        out("" + m);        
    }
    
    private String in() {
        try {
            String s = input.readUTF();
            //System.out.println("in-"+s);
            return s;
        } catch (IOException ex) {
            System.out.println("Connection error when receiving message from user " + userName);
        }
        return null;
    }

    private void log(Message m) {
        m.userName = this.userName;
        Server.log.add(m);
    }
    
}
