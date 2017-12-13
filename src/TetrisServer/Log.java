
package TetrisServer;

import java.io.*;
import java.util.Scanner;

/**
 * Logs an IP address to the standard log or a blocked log
 * Checks the blocked log to see if the IP shows up more than once
 * Returns 1 if the log has shown up more than 3 times
 * @author Joseph Gonzales
 */
class Log {
    private final String str = "logaddr.log";
    private final String str2 = "Blockaddr.log";
    private final File log, block;
    FileWriter out,outb;
    FileReader in;
    
    /**
     * Sets up logging files and creates new files if they don't exist
     * Sets up readers and writers for each file
     * @throws IOException 
     */
    Log() throws IOException {
        this.log = new File(str);
        this.block = new File(str2);
        if(!this.log.exists()) this.log.createNewFile();
        if(!this.block.exists()) this.block.createNewFile();
        this.out = new FileWriter(this.log);
        this.in = new FileReader(this.block);
        this.outb = new FileWriter(this.block);
    }
    
    /**
     * Takes the string hostAddress and the integer condition it is in
     * if the integer is greater than 0 then append the host address to the log and the blocked address log
     * otherwise log the host address to the log
     * @param hostAddress
     * @param i 
     */
    public void logIP(String hostAddress, int i) {
        try {
            if(i > 0) {
                out.append(hostAddress + "\n");
                outb.append(hostAddress + "\n");
                outb.close();
                out.close();
            }
            else {
                out.append(hostAddress + "\n");
                out.close();
                outb.close();

            }
        } catch(IOException e) {
            e.getStackTrace();
        }
    }
    
    /**
     * check the blocked address log to see if the host address parameter shows up more than 3 times
     * if it shows more than once, return 1, otherwise return 0
     * @param hostAddress
     * @return 
     */
    public int checkIP(String hostAddress) {
        Scanner scanner = new Scanner(this.in);
        int check = 0;
        while(scanner.hasNextLine()) {
            if(hostAddress.equals(scanner.nextLine())){
                if(check > 3) {
                    break;
                }
                check++;
            }
        }
        if(check > 3) return 1;
        else return 0;
    }
}
