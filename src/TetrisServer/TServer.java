
package TetrisServer;

import java.net.*;
import java.io.*;
import java.util.LinkedList;
import java.util.Scanner;
//import java.util.Base64;

/**
 * Starts a new Tetris server and either accepts an address bind from the argument or from user input
 * After successful Server socket binding, wait for a socket to connect and accept them
 * Once a client connects, send them a short to acknowledge their connection, log their IP address with Log, and wait for their password to be sent
 * If the password is accepted, wait for the next client to connect, otherwise report them to IP block address and wait for a new connection
 * once two players connect, signal the players to start, then start the server threads
 * @author Joseph Gonzales
 */
public class TServer {
    
    private ServerSocket server;
    private static int s = 0;
    public boolean[] session = {false, false};
    
    TServer() {
        
    }
    
    public static void main(String[] args) throws IOException {
        try {
            InetAddress addr;
            if(args.length > 0) {
                System.exit(0);
                addr = InetAddress.getByName(args[0]);
            }
            else {
                System.out.print("Specify address to connect: ");
                Scanner scan = new Scanner(System.in);
                addr = InetAddress.getByName(scan.nextLine());
            }
            System.out.println("Binding to address...");
            ServerSocket sock = new ServerSocket(12300, 2, addr);//port, backlog, IP
            while (true) {
                Socket player1, player2;
                DataOutputStream outp1, outp2;
                System.out.println("Waiting for players...");
                
                player1 = sock.accept();//establishes connection to the first player
                outp1 = new DataOutputStream(player1.getOutputStream());
                outp1.writeShort(0);
                IPReport(player1);
                int i = new Log().checkIP(player1.getInetAddress().getHostAddress());
                if(i != 0) {
                    outp1.writeShort(1);
                    player1.close();
                    continue;// Restart if first client failed to connect
                }
                System.out.println("Player1 connected successfully\nWaiting for Player2");
                
                while(true) {
                    player2 = sock.accept();//establishes connection to the second player
                    outp2 = new DataOutputStream(player2.getOutputStream());
                    outp2.writeShort(0);

                    IPReport(player2);
                    //check to see if the IP address has failed to connect too many times

                    int j = new Log().checkIP(player2.getInetAddress().getHostAddress());

                    if(j != 0) {
                        outp2.writeShort(1);
                        player2.close();
                    }
                    else break;// Get out of loop if second client succeded to connect
                }
                System.out.println("Player2 has connected successfully\nInitiating game");
                
                outp1.writeShort(0);
                outp2.writeShort(0);
                TServer ts = new TServer();
                Tetris game = new Tetris(ts, player1, player2);
                
            }
        }
        catch(UnknownHostException e) {
            e.getStackTrace();
        }
        catch (IOException e) {
            e.getStackTrace();
        }
    } 
    
    /**
     * Takes the player socket and checks to see if the password they enter is correct
     * if it is, only log their IP to the log, otherwise log their IP to the log and blocked address log.
     * @param player 
     */
    private static void IPReport(Socket player) {
        try {
            DataInputStream inp = new DataInputStream(player.getInputStream());
            //read input stream for password
            if ("4herethe4blocksfall".equals(inp.readUTF())) {
                new Log().logIP(player.getInetAddress().getHostAddress(), 0);
            }
            //password login has failed, log the violating IP
            else {
                new Log().logIP(player.getInetAddress().getHostAddress(), 1);
            }   
        } catch (IOException ex) {
            ex.getStackTrace();
        }
    }
}

/**
 * A new Tetris instance that creates two threads to maintain the redirection of the I/O of both sockets
 * @author Joseph Gonzales
 */
class Tetris {
    
    public Socket sock1, sock2;
    public PlayOneToPlayTwo orpt;
    public PlayTwoToPlayOne irpt;
    public TServer ts;
    public short j = 0 , k = 0;
    
    /**
     * Default constructor
     */
    Tetris() {

    }
    
    /**
     * Takes in the sockets and sets up the threads that will manage the I/O of both clients
     * Then starts both threads
     * @param s
     * @param sock1
     * @param sock2 
     */
    Tetris(TServer s, Socket sock1, Socket sock2) {
        try {
            this.ts = s;
            this.sock1 = sock1;
            this.sock2 = sock2;
            
            this.orpt = new PlayOneToPlayTwo(this, new DataOutputStream(sock2.getOutputStream()), new DataInputStream(sock1.getInputStream()));
            this.irpt = new PlayTwoToPlayOne(this, new DataOutputStream(sock1.getOutputStream()), new DataInputStream(sock2.getInputStream()));
            startThread();
        } catch (IOException ex) {
            ex.getStackTrace();
        }
    }
    
    /**
     * Start the threads
     */
    private void startThread() {
        this.orpt.start();
        this.irpt.start();
    }
}

/**
 * Sends the data from the first player to the second player
 * Reads in the short from the first player then resends that short to the second player corresponding to the data thats going to be sent
 * If the first player sends a value greater than 1, notify the second player that the first player has lost or forfeited
 * If the second player sends a value greater than 1, then exit the loop immediately if the loop isn't already exited
 * @author Joseph Gonzales
 */
class PlayOneToPlayTwo extends Thread {
    private final Tetris t;
    private final DataOutputStream out2;
    private final DataInputStream in1;
    
    /**
     * Initializes the tetris instance and I/O streams
     * @param t
     * @param out2
     * @param in1 
     */
    PlayOneToPlayTwo(Tetris t, DataOutputStream out2, DataInputStream in1) {
        this.t = t;
        this.out2 = out2;
        this.in1 = in1;
    }
    
    /**
     * Runnable thread method that monitors the short inputs of the first player and sends the second player data corresponding to that short value
     * if the value is greater than 2, send the second player a short that indicates a loss or forfeit
     * if the value is 1 then take the linked list object that the first player sent and resend it to the second player
     * default value takes the key event object that the first player sent and resend it to the second player
     * exit if the other thread set their j value to greater than 1
     */
    @Override
    public void run() {
        ObjectOutputStream o;
        ObjectInputStream i;
        boolean isRunning = true;
        
        try {
            o = new ObjectOutputStream(this.out2);
            i = new ObjectInputStream(this.in1);
            while(isRunning && (t.sock1.isConnected() && t.sock2.isConnected())) {
                t.k = in1.readShort();// set condition to send more tetrominoes if input from other thread requests it
                switch (t.k) {
                    case 3:
                        out2.writeShort(3);//notify the client1 of client2's forfeit
                        out2.flush();
                        break;
                    case 2:
                        out2.writeShort(2);//notify the client1 of client2's loss
                        out2.flush();
                        break;
                    case 1:
                        out2.writeShort(1);//notify the client to get ready to recieve a linked list
                        out2.flush();
                        o.writeObject(i.readObject());// send current tetromino stack
                        o.flush();
                        break;
                    default:
                        out2.writeShort(0);//notify the client to get ready to recieve a key event
                        out2.flush();
                        out2.writeInt(in1.readInt());//send user input
                        out2.flush();
                        break;
                }
                if(t.k > 1 || t.j > 1) break;
            }
            if(!t.sock1.isConnected()) {
                out2.writeShort(3);//notify the client1 of client2's forfeit due to connection loss
                out2.flush();
            }
        } 
        catch (IOException | ClassNotFoundException ex) {
            ex.getStackTrace();
        }
        System.out.println("Player disconnected");
    }
}

/**
 * Sends the data from the second player to the first player
 * Reads in the short from the second player then resends that short to the first player corresponding to the data thats going to be sent
 * If the second player sends a value greater than 1, notify the first player that the second player has lost or forfeited
 * If the first player sends a value greater than 1, then exit the loop immediately if the loop isn't already exited
 * @author Joseph Gonzales
 */
class PlayTwoToPlayOne extends Thread{
    private final Tetris t;
    private final DataInputStream in2;
    private final DataOutputStream out1;
    
    /**
     * Initializes the tetris instance and I/O streams
     * @param t
     * @param out1
     * @param in2 
     */
    PlayTwoToPlayOne(Tetris t, DataOutputStream out1, DataInputStream in2) {
        this.t = t;
        this.in2 = in2;
        this.out1 = out1;
    }
    /**
     * Runnable thread method that monitors the short inputs of the second player and sends the first player data corresponding to that short value
     * if the value is greater than 2, send the first player a short that indicates a loss or forfeit
     * if the value is 1 then take the linked list object that the second player sent and resend it to the first player
     * default value takes the key event object that the second player sent and resend it to the first player
     * exit if the other thread set their k value to greater than 1
     */
    @Override
    public void run() {
        ObjectOutputStream o;
        ObjectInputStream i;
        try {
            o = new ObjectOutputStream(this.out1);
            i = new ObjectInputStream(this.in2);
            while(true && (t.sock1.isConnected() && t.sock2.isConnected())) {
                t.j = in2.readShort();// set condition to send more tetrominoes if input from other thread requests it
                switch (t.j) {
                    case 3:
                        out1.writeShort(3);//notify the client2 of client1's forfeit
                        out1.flush();
                        break;
                    case 2:
                        out1.writeShort(2);//notify the client2 of  client1's loss
                        out1.flush();
                        break;
                    case 1:
                        out1.writeShort(1);//notify the client to get ready to recieve a linked list
                        out1.flush();
                        o.writeObject(i.readObject());// send current tetromino stack
                        o.flush();
                        break;
                    default:
                        out1.writeShort(0);//notify the client to get ready to recieve a key event
                        out1.flush();
                        out1.writeInt(in2.readInt());//send user input
                        out1.flush();
                        break;
                }
                if(t.k > 1 || t.j > 1) break;
            }
            
            if(!t.sock2.isConnected()) {
                out1.writeShort(3);//notify the client2 of client1's forfeit due to connection loss
                out1.flush();
            }
        } 
        catch (IOException | ClassNotFoundException ex) {
            ex.getStackTrace();
        }
        System.out.println("Player disconnected");
    }
}