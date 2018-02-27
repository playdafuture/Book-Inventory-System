
import java.io.Serializable;

/**
 * Client to server messages
    //000: Connection
    0: disconnect

    //100: login management
    101 login, username, password
    102 new user, username, password
    103 reset password, newpassword

    //200: inventory searching
    201 search inventory: isbn
    202 search inventory: title
    205 search inventory: author
    203 isbn, title
    207 title, author
    206 isbn, author
    208 isbn, title, author

    //300: catalog management
    301 new catalog entry, isbn, title, author (null values allowed)
    302 modify catalog entry, isbn, title, author (null values allowed)

    //400: inventory management
    401 modify inventory: isbn, new quantity

    //500: user management
    501 promote to admin: username
    502 reset password: username
    503 delete user: username

 * @author Jinqiu Liu
 */
public class Message implements Serializable {
    int messageCode;
    String[] vars;
    String userName;
    
    public Message (int mc, String[] v) {
        messageCode = mc;
        vars = v;
    }
    
    public Message (int mc) {
        messageCode = mc;
        vars = new String[0];
    }
    
    public Message (int mc, String s0) {
        messageCode = mc;
        vars = new String[1];
        vars[0] = s0;
    }
    
    public Message (int mc, String s0, String s1) {
        messageCode = mc;
        vars = new String[2];
        vars[0] = s0;
        vars[1] = s1;
    }
    
    public Message (int mc, String s0, String s1, String s2) {
        messageCode = mc;
        vars = new String[3];
        vars[0] = s0;
        vars[1] = s1;
        vars[2] = s2;
    }
    
    public Message(String fromString) {
        String s = fromString;
        int idx = s.indexOf(" ");
        messageCode = Integer.parseInt(s.substring(0, idx));
        s = s.substring(idx + 1);
        idx = s.indexOf(" ");
        int size = Integer.parseInt(s.substring(0, idx));
        vars = new String[size];
        s = s.substring(idx + 1);
        for (int i = 0; i < size; i++) {
            idx = s.indexOf("[");
            int vl = Integer.parseInt(s.substring(0, idx));
            vars[i] = s.substring(idx + 1, idx + 1 + vl);
            s = s.substring(idx + 2 + vl);
        }
    }
    
    public void setUser(String userName) {
        this.userName = userName;
    }
    
    @Override
    public String toString() {
        String s = "";
        s += messageCode + " ";
        s += vars.length + " ";
        for (int i = 0; i < vars.length; i++) {
            s += vars[i].length() + "[";
            s += vars[i] + "]";
        }
        return s;
    }    
}
