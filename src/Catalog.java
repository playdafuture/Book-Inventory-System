
import java.io.Serializable;
import java.util.HashMap;

/**
 * A set of ISBN (primary key) to Book objects.
 * Serves as offline cached information when wanting detailed book info.
 * Use this in conjunction with Inventory.
 * @author Jinqiu Liu
 */
public class Catalog implements Serializable {
    private HashMap<String, Item> Catalog;
    public Catalog() {
        Catalog = new HashMap();
    }
    
    public void newEntry(Item newItem) {
        Catalog.put(newItem.getISBN(), newItem);
        if (Server.inventory.getListing(newItem.getISBN()) == null) {
            Server.inventory.newListing(newItem.getISBN());
        }
    }
    
    public Item getEntry(String ISBN) {
        return Catalog.get(ISBN);
    }
    
    public HashMap<String, Item> getEntryByTitle(String title) {
        HashMap<String, Item> results = new HashMap();
        for(String key: Catalog.keySet()) {
            if (Catalog.get(key).getTitle().contains(title)) {
                results.put(key, Catalog.get(key));
            }
        }
        return results;
    }
    
    public HashMap<String, Item> getEntryByAuthor(String author) {
        HashMap<String, Item> results = new HashMap();
        for(String key: Catalog.keySet()) {
            if (Catalog.get(key).getAuthorName().contains(author)) {
                results.put(key, Catalog.get(key));
            }
        }
        return results;
    }
    
    public String[] getISBNsByTitle(String title) {
        HashMap<String, Item> results = new HashMap();
        for(String key: Catalog.keySet()) {
            if (Catalog.get(key).getTitle().contains(title)) {
                results.put(key, Catalog.get(key));
            }
        }
        String[] ISBNs = new String[results.size()];
        int idx = 0;
        for (String key: results.keySet()) {
            ISBNs[idx] = key;
            idx++;
        }
        return ISBNs;
    }
    
    public String[] getISBNsByAuthor(String author) {
        HashMap<String, Item> results = new HashMap();
        for(String key: Catalog.keySet()) {
            if (Catalog.get(key).getAuthorName().contains(author)) {
                results.put(key, Catalog.get(key));
            }
        }
        String[] ISBNs = new String[results.size()];
        int idx = 0;
        for (String key: results.keySet()) {
            ISBNs[idx] = key;
            idx++;
        }
        return ISBNs;
    }
    
    public String[] getISBNsByTitleAndAuthor(String title, String author) {
        //first get results by title
        HashMap<String, Item> round1 = new HashMap();
        for(String key: Catalog.keySet()) {
            if (Catalog.get(key).getTitle().contains(title)) {
                round1.put(key, Catalog.get(key));
            }
        }
        //then filter by author
        HashMap<String, Item> results = new HashMap();
        for(String key: round1.keySet()) {
            if (round1.get(key).getAuthorName().contains(author)) {
                results.put(key, round1.get(key));
            }
        }
        //finally, compile results
        String[] ISBNs = new String[results.size()];
        int idx = 0;
        for (String key: results.keySet()) {
            ISBNs[idx] = key;
            idx++;
        }
        return ISBNs;
    }
}
