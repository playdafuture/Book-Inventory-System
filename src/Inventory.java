


import java.io.Serializable;
import java.util.HashMap;

/**
 * Inventory consists of a set of listings, 
 * where each listing is essentially a pair of id and quantity.
 * @author Jinqiu Liu
 */
public class Inventory implements Serializable {
    
    public class Listing implements Serializable {
        private final String ISBN;
        private int quantity;
        
        public Listing(String ISBN) {
            this.ISBN = ISBN;
            this.quantity = 0;
        }
        
        public Listing(String ISBN, int quantity) {
            this.ISBN = ISBN;
            this.quantity = quantity;
        }
        
        public void modify(int newQuantity) {
            quantity = newQuantity;
        }
        
        public int add(int increment) {
            quantity += increment;
            return quantity;
        }
        
        public int sub(int decrement) {
            quantity -= decrement;
            return quantity;
        }
        
        public String getISBN() {
            return ISBN;
        }
        
        public int getQuantity() {
            return quantity;
        }        
    }
    
    private HashMap<String, Listing> allListings;
    
    public Inventory() {
        allListings = new HashMap();
    }
    
    /**
     * Adds a new listing into the inventory, where the quantity is 0.
     * @param ISBN 
     */
    public void newListing(String ISBN) {
        Listing newListing = new Listing(ISBN);
        allListings.put(ISBN, newListing);
        if (Server.catalog.getEntry(ISBN) == null) {
            //Catalog does not have this entry
            Item newItem = new Item(ISBN);
            String[] info = null;
            for (int i = 0; i < 3; i++) { //retry up to 3 times
                info = URLProcess.rip(ISBN);
                if (info != null) {
                    newItem.setTitle(info[1]);
                    newItem.setAuthorName(info[2]);
                    newItem.setPublisher(info[3]);
                    newItem.setYearPublished(Integer.parseInt(info[4]));
                    break;
                }
            }
            if (info == null) {
                newItem.setTitle("Unknown");
                newItem.setAuthorName("Unknown");
                newItem.setPublisher("Unknown");
                newItem.setYearPublished(0);
            }
            Server.catalog.newEntry(newItem);
        }
    }
    
    /**
     * Adds a new listing into the inventory, with the specified quantity.
     * @param ISBN
     * @param quantity 
     */
    public void newListing(String ISBN, int quantity) {
        Listing newListing = new Listing(ISBN, quantity);
        allListings.put(ISBN, newListing);
        if (Server.catalog.getEntry(ISBN) == null) {
            //Catalog does not have this entry
            Item newItem = new Item(ISBN);
            String[] info = null;
            for (int i = 0; i < 3; i++) { //retry up to 3 times
                info = URLProcess.rip(ISBN);
                if (info != null) {
                    newItem.setTitle(info[1]);
                    newItem.setAuthorName(info[2]);
                    newItem.setPublisher(info[3]);
                    newItem.setYearPublished(Integer.parseInt(info[4]));
                    break;
                }
            }
            if (info == null) {
                newItem.setTitle("Unknown");
                newItem.setAuthorName("Unknown");
                newItem.setPublisher("Unknown");
                newItem.setYearPublished(0);
            }
            Server.catalog.newEntry(newItem);
        }
    }
    
    public Listing getListing(String ISBN) {
        return allListings.get(ISBN);        
    }
    
    public Listing[] getAllListings() {
        Listing[] arrayAllListings = new Listing[allListings.size()];
        int index = 0;
        for (String key: allListings.keySet()) {
            arrayAllListings[index] = allListings.get(key);
            index++;
        }
        return arrayAllListings;
    }
    
    public int getQuantity(String ISBN) {
        Listing result = allListings.get(ISBN);
        if (result == null) {
            return Integer.MIN_VALUE;
        } else {
            return result.getQuantity();
        }
    }
    
    public int modifyListing(String ISBN, int newQuantity) {
        Listing result = allListings.get(ISBN);
        if (result == null) {
            return Integer.MIN_VALUE;
        } else {
            result.modify(newQuantity);
            return result.getQuantity();
        }
    }
    
    /**
     * Increment the quantity of a listing by 1.
     * If the listing does not exist, create a new one.
     * A catalog will be automatically created.
     * @param ISBN 
     */
    public void incListing(String ISBN) {
        if (getListing(ISBN) == null) {
            //not exist, create new
            newListing(ISBN, 1);
        } else {
            addToListing(ISBN, 1);
        }
    }
    
    public int addToListing(String ISBN, int increment) {
        Listing result = allListings.get(ISBN);
        if (result == null) {
            return Integer.MIN_VALUE;
        } else {
            result.add(increment);
            return result.getQuantity();
        }
    }
    
    public int subToListing(String ISBN, int increment) {
        Listing result = allListings.get(ISBN);
        if (result == null) {
            return Integer.MIN_VALUE;
        } else {
            result.sub(increment);
            return result.getQuantity();
        }
    }   
    
    public int getSize() {
        return allListings.size();
    }    
}
