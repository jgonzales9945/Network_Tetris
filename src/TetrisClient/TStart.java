package TetrisClient;

import java.io.*;
import java.net.*;
import javax.swing.JOptionPane;

/**
 * Sets up the network connection by asking the user to input the desired IP.
 * It checks to see if this client is authorized to start the game, other wise the client will exit.
 * Once authorized, the client will wait until the server says there is two clients ready to play.
 * The client will then initiate the game.
 * @author tritonium
 */
public class TStart {

    private static final String PSW ="4herethe4blocksfall";
    public static void main(String[] args) {
        try {
            String ip;
            Socket client;
            if(args.length > 0) {
                ip = args[0];
            }
            else {
                ip = JOptionPane.showInputDialog(null, "Enter an IP address to connect to\n","Network Connect");//ask user for IP they want to connect to
            }
            //InetAddress addr = InetAddress.getByName(ip);//get name or IP from network
            if(/*addr*/ip != null) {
                //System.out.println("connecting...");
                client=new Socket(/*addr*/ip,12300);//connect to the specified address at 1235 port
            }            
            else {
                JOptionPane.showMessageDialog(null,"Address could not be used, defaulting to local host", "Invalid Address",JOptionPane.INFORMATION_MESSAGE);
                client = new Socket("127.0.0.1",12300);//connect to local host
            }
            System.out.println("connected");
            //set up I/O data streams, then send the password
            DataInputStream inp = new DataInputStream(client.getInputStream());
            DataOutputStream outp = new DataOutputStream(client.getOutputStream());
            short conf1 = inp.readShort();
            //JOptionPane.showMessageDialog(null,"Connected to the server\nWaiting for Opponent...");
            outp.writeUTF(PSW);//send pasword, need to convert
            short conf2 = inp.readShort();
            //check the server confirmation response
            //if zero, continue to initialize, otherwise quit
            if(conf2 == 0) {
                //JOptionPane.showMessageDialog(null, "Opponent has connected\n now starting");
                TetrisC game = new TetrisC(client, inp, outp);//start game and let the game control the network streams
            }
            else {
                JOptionPane.showMessageDialog(null, "IP Confirmation failiure\nNow exiting.","IP Check Failiure",JOptionPane.ERROR_MESSAGE);
                System.exit(-1);
            }
        }
        catch(UnknownHostException e) {
            System.out.println("Socket error");
            e.getStackTrace();
        }
        catch ( IOException e){
            System.out.println("An I/O Exception Occurred!");
            e.getStackTrace();
        }
    } 
}
