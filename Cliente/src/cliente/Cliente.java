/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cliente;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 *
 * @author jFluxie
 */
public class Cliente {
    
    private static Socket socket;
    
    private static BufferedReader br;
    
    private static DataOutputStream out;

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) throws IOException {
        
            connect();
            sendDesiredFile("x");
            while(true){
                
            
            }
		
	}
    
    public static boolean connect() throws IOException{
        
        final String host = "localhost";
        final int portNumber = 1978;
        socket = new Socket(host, portNumber);
        br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new DataOutputStream(socket.getOutputStream());
        String result=br.readLine();
        
        if(result.equalsIgnoreCase("CONNECTED")){
            System.out.println("yay");
            return true;
        }
        else{
            System.out.println("awww");
            socket=null;
            br=null;
            out=null;
            return false;
        }
        
    }
    
    public static void sendDesiredFile(String s){
        s="5mb.pdf";
        PrintWriter p = new PrintWriter(out, true);
        p.println("FILE;"+s);    
    }
}
