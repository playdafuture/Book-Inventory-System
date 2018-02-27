
/**
 *
 * @author Future
 */
public class test {
    public static void main(String[] a) {
        URLProcess.rip("9780131428485");
        
    }
    
    public static void passedTests() {
        Inventory inv = new Inventory();
        System.out.println(inv.getSize());
        inv.newListing("9781929613212");
        System.out.println(inv.getSize());
        
        Item test = new Item("9781929613212");
        test.setTitle("Prometheus in Bondage, or, All the Girls I Should Have Kissed");
        test.setPublisher("Avid Press, LLC");

        test.setYearPublished(2000);
        System.out.println(test);
        
        Catalog c = new Catalog();
        c.newEntry(test);
        
        System.out.println(c.getEntry("9781929613212"));
        
        Item i = c.getEntry("9781929613212");
        
        System.out.println(c.getEntry("9781929613212"));
        
        Authentication auth = new Authentication();
        auth.addUser("admin", "admin", 0);
        auth.printUserInfo("admin");
        auth.printUserInfo("john");
        
        String[] s = {};
        Message m = new Message(0, s);
        Message m1 = new Message(m.toString());
        System.out.println(m1+"");
    }
}
