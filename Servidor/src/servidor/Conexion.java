/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package servidor;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author jose felipe
 */
public class Conexion extends Thread {

    protected Socket socket;

    protected boolean refuse;

    protected BufferedReader brinp;

    protected DataOutputStream out;

    public Conexion(Socket clientSocket, boolean refuse) {
        this.socket = clientSocket;
        this.refuse = refuse;

        try {
            brinp = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            return;
        }
    }

    public void run() {

        if (!refuse) {

            try {
                socket.setSoTimeout(Servidor.TIMEOUT * 1000);
            } catch (SocketException ex) {
                Logger.getLogger(Conexion.class.getName()).log(Level.SEVERE, null, ex);
            }

            Servidor.incrementarConexiones();
            sendHello();
            while (true) {

                try {
                    String line;
                    System.out.println(line = brinp.readLine());
                    System.out.println("THE LINE IS " + line);
                    handleMessage(line);
                } catch (IOException e) {
                    try {
                        PrintWriter p = new PrintWriter(out, true);
                        p.println("TIMEOUT");
                        System.out.println("Timeout reached! Closing socket.");
                        socket.close();
                        Servidor.reducirConexiones();
                        return;
                    } catch (IOException ex) {
                        Logger.getLogger(Conexion.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        } else {
            try {
                //SEND MESSAGE REFUSING CONNECTION
                PrintWriter p = new PrintWriter(out, true);
                p.println("REFUSED");
                socket.close();
                Servidor.reducirConexiones();
                return;
            } catch (IOException ex) {
                Logger.getLogger(Conexion.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private void handleMessage(String s) {

        if (s.startsWith("FILE")) {
            System.out.println(s.split(";")[1]);

        }

    }

    private void sendHello() {
        PrintWriter p = new PrintWriter(out, true);
        p.println("CONNECTED");
    }

    private void sendOptions() {
        String options = "";

    }

    private void sendPacket() {

    }

    public static byte[] readFully(InputStream stream) throws IOException {
        byte[] buffer = new byte[8192];
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        int bytesRead;
        while ((bytesRead = stream.read(buffer)) != -1) {
            baos.write(buffer, 0, bytesRead);
        }
        return baos.toByteArray();
    }

    public static byte[] loadFile(String sourcePath) throws IOException {
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(sourcePath);
            return readFully(inputStream);
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
    }

}
