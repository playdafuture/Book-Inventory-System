import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

/**
 * For each URL you are processing, obtain URLInfo. 
 * Output the name of the URL and all of its info about the URL to THE output file. 
 * The remaining processing will depend on the file ending that the URL points to. 
 */

/**
 * The URLProcess class contains methods that connects to a specified URL 
 * and downloads the resource to the local directory.
 * @author Jinqiu Liu
 */
public class URLProcess {
    static File inputFile;
    static File outputFile;
    static BufferedWriter outWriter;
    static String[] allArgs;
    
    /**
     * The idea is to get the bibliographic information all based on an ISBN.
     * @param ISBN The ISBN.
     * @return Book's full information.
     * String[] vars;
     * vars[0] = ISBN;
     * vars[1] = Title;
     * vars[2] = Author;
     * vars[3] = Publisher;
     * vars[4] = yearPublished;
     * 
     * or, null, if failed
     */
    public static String[] rip(String ISBN) {
        String[] vars = new String[5];
        try {
            URL searchPage = new URL("https://www.abebooks.com/servlet/SearchResults?isbn=" + ISBN);
            vars = processSearchPage(searchPage);
            vars[0] = ISBN;
        } catch (MalformedURLException ex) {
            Logger.getLogger(URLProcess.class.getName()).log(Level.SEVERE, null, ex);
        }
        return vars;
    }
   
    private static String[] processSearchPage(URL searchPage) {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(searchPage.openStream()));
            String line = in.readLine();
            boolean valid = false;
            while (line != null) {
                if (line.contains("listing_1")) {
                    line = line.substring(line.indexOf("href="));
                    line = line.substring(line.indexOf("\"") + 1);
                    line = line.substring(0, line.indexOf("\""));
                    valid = true;
                    break;
                }
                line = in.readLine();
            }         
            if (!valid) return null;
            URL bookPage = new URL("https://www.abebooks.com"+line);
            in.close();
            return processBookPage(bookPage);
        } catch (IOException ex) {
            Logger.getLogger(URLProcess.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;        
    }
    
    private static String[] processBookPage(URL bookPage) {
        String[] vars = new String[5];
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(bookPage.openStream()));
            String line = in.readLine();
            boolean[] checklist = {false, false, false, false};
            while (line != null) {
                if (line.contains("<span class=\"main-heading\">")) { //title
                    vars[1] = getTitle(line);
                    checklist[0] = true;
                }
                else if (line.contains("meta itemprop=\"author\"")) { //author                    
                    vars[2] = getContent(line);
                    checklist[1] = true;
                }
                else if (line.contains("meta itemprop=\"publisher\"")) { //publisher
                    vars[3] = getContent(line);
                    checklist[2] = true;
                }
                else if (line.contains("meta itemprop=\"datePublished\"")) { //date published
                    vars[4] = getContent(line);
                    checklist[3] = true;
                }                
                if (valid(checklist)) {
                    break;
                }
                line = in.readLine();
            }            
            return vars;
        } catch (IOException ex) {
            Logger.getLogger(URLProcess.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    private static boolean valid(boolean[] booleanArray) {
        for (int i = 0; i < booleanArray.length; i++) {
            if (booleanArray[i] == false) {
                return false;
            }
        }
        return true;
    }
    
    private static String getTitle(String line) {
        //<h1 id="book-title"><span class="main-heading">UML for Java(TM) Programmers (Robert C. Martin Series)</span></h1>
        String title = "";
        line = line.substring(line.indexOf("main-heading\">") + 14, line.indexOf("</span>"));
        title = line;
        return title;
    }

    private static String getContent(String line) {
        // <meta itemprop="author" content="Robert C. Martin" />
        String content = "";
        line = line.substring(line.indexOf("content=") + 9);
        line = line.substring(0, line.indexOf("\""));
        content = line;                
        return content;
    }

    
}
