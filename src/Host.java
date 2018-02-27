
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 *
 * @author Future
 */
public class Host extends Thread {
    public static ServerSocket serverSocket;              //server socket to handle all incoming connections
    public Host() {   
        try {
            int port = ((int) ( 5000.0 * Math.random() )) + 5000;
            serverSocket = new ServerSocket(port, 20); //max connections == 20
            InetAddress serverAddress = InetAddress.getLocalHost();
            msg("Please use the following Server info:"
                    + "\n\tIP Address = " + serverAddress
                    + "\n\tPort = " + port);
        } catch (IOException ex) {
            msg("Connection ERROR, unable to establish a server");
        }
    }
    
    @Override
    public void run() {
        while (true) {
            try {
                Socket incomingConnection = serverSocket.accept();
                DataInputStream in = new DataInputStream(incomingConnection.getInputStream());
                DataOutputStream out = new DataOutputStream(incomingConnection.getOutputStream());         
                new Console(incomingConnection).start();
            } catch (IOException ex) {
                    msg("Unknown user failed to connect to server.");           
            }
        }
    }
    
    private void msg(String message) {
        System.out.println("[Server Host]\t" + message);
    }

}
