
import java.io.Serializable;

/**
 * All detailed information about a book itself. 
 * Though this should be a sub-class of inventory, 
 * its significance should warrant a separate class file.
 * @author Jinqiu Liu
 */
public class Item implements Serializable {
    private final String ISBN;
    private String authorName;
    private String title;
    private int yearPublished;
    private String publisher;
    
    /**
     * Minimal constructor.
     * @param ISBN ISBN of the book.
     */
    public Item(String ISBN) {
        this.ISBN = ISBN;
    }
    
    public Item(String ISBN, String title) {
        this.ISBN = ISBN;
        this.title = title;
    }
    
    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public void setYearPublished(int yearPublished) {
        this.yearPublished = yearPublished;
    }
    
    public void setPublisher (String publisher) {
        this.publisher = publisher;
    }
    
    public String getISBN() {
        return ISBN;
    }
    
    public String getAuthorName() {
        return authorName;
    }
    
    public String getTitle() {
        return title;
    }
    
    public int getYearPublished() {
        return yearPublished;        
    }
    
    public String getPublisher () {
        return publisher;
    }
    
    public String[] toStringArray() {
        String[] args = new String[5];
        args[0] = ISBN;
        if (title != null) args[1] = title;
        else args[1] = "Unknown Title";
        if (authorName != null) args[2] = authorName;
        else args[2] = "Unknown Author";
        if (publisher != null) args[3] = publisher;
        else args[3] = "Unknown Publisher";
        if (yearPublished != 0) args[4] = yearPublished+"";
        else args[4] = "0";
        return args;
    }
    
    @Override
    public String toString() {
        if (title == null) {
            return "ISBN = " + ISBN;
        } else if (authorName == null) {
            return "<<" + title + ">> (ISBN = " + ISBN + ")";
        } else if (publisher == null) {
            String name = authorName;
            return "<<" + title + ">> by " + name + " (ISBN = " + ISBN + ")";
        } else if (yearPublished == 0) {
            String name = authorName;
            return "<<" + title + ">> by " + name + " "
                    + "(Published by " + publisher + ", ISBN = " + ISBN + ")";
        } else {
            String name = authorName;
            return "<<" + title + ">> by " + name + " "
                    + "(Published by " + publisher + ", " + yearPublished
                    + " ISBN = " + ISBN + ")";
        }
    }
}
