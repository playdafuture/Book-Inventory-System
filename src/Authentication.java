
import java.io.Serializable;
import java.util.HashMap;


public class Authentication implements Serializable {
    class User implements Serializable {
        String username;
        int adminLevel; //0 is admin, 1 is user
        String salt;
        String hashword;
        
        public User(String username, String password, int level) {
            this.username = username;
            salt = randomSalt();
            hashword = hash(salt + password);
            adminLevel = level;
        }
        
        public int autenticate(String username, String password) {
            String tryHash = hash(salt+password);
            if (tryHash.equals(hashword)) {
                return adminLevel;
            } else {
                return -1;
            }
        }
        
        public String randomSalt() {
            String salt = "";
            for (int i = 0; i < 16; i++) {
                int r = (int) (Math.random() * 94);
                char c = (char) (' ' + r);
                salt += c;
            }
            return salt;
        }
        
        public String hash(String s) {
            String result = "";
            int [] vals = new int[16];
            for (int i = 0; i < s.length(); i++) {
                vals[i%16] += (s.charAt(i));
                vals[i%16] %= 16;
            }
            for (int i = 0; i < 16; i++) {
                if (vals[i] < 10) {
                    result += vals[i];
                } else {
                    char c = (char) ('a' + vals[i] - 10);
                    result += c;
                }
            }            
            return result;
        }
    }
    
    private HashMap<String, User> allUsers;
    
    public Authentication() {
        allUsers = new HashMap();
        allUsers.put("admin", new User("admin", "admin", 0));
        //default admin must be added
    }
    
    public void addUser(String un, String pw, int lv) {
        User u = new User(un, pw, lv);
        allUsers.put(un, u);
        System.out.println(un + " added");
    }
    
    public void delUser(String un) {
        allUsers.remove(un);
    }
    
    public int authUser(String un, String pw) {
        User u = allUsers.get(un);
        if (u != null) { //user exists
            return u.autenticate(un, pw);
        }
        return -1;
    }
    
    public boolean findUser(String un) {
        User u = allUsers.get(un);
        return u != null;
    }
    
    public int getUserLevel(String un) {
        User u = allUsers.get(un);
        if (u != null) {
            return u.adminLevel;
        } else {
            return -1;
        }
    }
    
    public void promoteUser(String un) {
        User u = allUsers.get(un);
        if (u != null) {
            u.adminLevel = 0;
        }
    }
    
    public void printUserInfo(String un) {
        User u = allUsers.get(un);
        if (u != null) {
            System.out.println(u.salt);
            System.out.println(u.hashword);
        } else {
            System.out.println("User does not exist");
        }
    }
}
