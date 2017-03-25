/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package servidor;

import static com.sun.xml.internal.ws.model.RuntimeModeler.PORT;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 *
 * @author jose felipe
 */
public class Servidor {
    
    //Puerto
    private static final int PORT = 1978;
    
    //Numero maximo de conexiones
    private static final int MAX_CONEXIONES=3;
    
    //Numero actual de conexiones
    private static int conexiones = 0;
    
    //Tamanio buffer
    private static final int BUFFER=10;
    
    //Tiempo en segundos de timeout
    public static int TIMEOUT=0;
    
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        ServerSocket serverSocket = null;
        Socket socket = null;

        try {
            serverSocket = new ServerSocket(PORT);
        } catch (IOException e) {
            e.printStackTrace();

        }
        while (true) {
            try {
                socket = serverSocket.accept();
            } catch (IOException e) {
                System.out.println("I/O error: " + e);
            }

            if(conexiones < MAX_CONEXIONES){
                new Conexion(socket, false).start();
            }  
            else{
                new Conexion(socket, true).start();
            }
        }
    }
    
     public static synchronized void incrementarConexiones() {
        conexiones++;
    }
     
     public static synchronized void reducirConexiones() {
        conexiones--;
    }
    
}
