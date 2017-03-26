/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package servidor;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.util.Arrays;
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

    private int currentPos;

    private byte[] fullFile;

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
                socket.setSendBufferSize(Servidor.BUFFER);
            } catch (SocketException ex) {
                Logger.getLogger(Conexion.class.getName()).log(Level.SEVERE, null, ex);
            }
            System.out.println("Se conecto uno!");
            Servidor.incrementarConexiones();
            sendConnected();
            while (true) {

                try {
                    String line;
                    line = brinp.readLine();
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

    private void handleMessage(String s) throws IOException {
        
        

        if (s.startsWith("FILE")) {
            currentPos = 0;
            fullFile = loadFile("./files/"+s.split(";")[1]);
            PrintWriter p = new PrintWriter(out, true);
            p.println("READY");
        } else if (s.equalsIgnoreCase("OK")) {
            System.out.println("sending files");
            PrintWriter p = new PrintWriter(out, true);
            if (sendBytes(fullFile)) {
                p.println("READY");
            } else {
                p.println("FINISHED");
            }
        } else if (s.equalsIgnoreCase("THANKS")) {
            currentPos = 0;
            fullFile = null;
        } else if (s.equalsIgnoreCase("OPTIONS")) {
            
            sendOptions();
        }
        else if(s.equalsIgnoreCase("DISCONNECT")){
            socket.close();
            Servidor.reducirConexiones();
            return;
        }

    }

    private void sendConnected() {
        PrintWriter p = new PrintWriter(out, true);
        p.println("CONNECTED");
    }

    private void sendOptions() {

        String options = "OPTIONS";

        File folder = new File("./files");
        File[] listOfFiles = folder.listFiles();

        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile()) {
                options += ";"+listOfFiles[i].getName();
            }
        }
        PrintWriter p = new PrintWriter(out, true);
        p.println(options);

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

    public void sendBytes(byte[] myByteArray, int start, int len) throws IOException {
        if (len < 0) {
            throw new IllegalArgumentException("Negative length not allowed");
        }
        if (start < 0 || start >= myByteArray.length) {
            throw new IndexOutOfBoundsException("Out of bounds: " + start);
        }

        out.writeInt(len);
        if (len > 0) {
            out.write(myByteArray, start, len);
            out.flush();
        }
    }

    public boolean sendBytes(byte[] myByteArray) throws IOException {
        if (currentPos + Servidor.FRAGMENTO < fullFile.length) {
            sendBytes(myByteArray, currentPos, currentPos + Servidor.FRAGMENTO);
            currentPos += Servidor.FRAGMENTO;

        } else {
            sendBytes(myByteArray, currentPos, fullFile.length);
            return true;
        }

        return false;

    }

    public String sendPacket() {
        String v = "";
        if (currentPos + Servidor.FRAGMENTO < fullFile.length) {
            byte[] subArray = Arrays.copyOfRange(fullFile, currentPos, currentPos + Servidor.FRAGMENTO);
            v = "PACKET;" + subArray.toString();
        } else if (currentPos + Servidor.FRAGMENTO >= fullFile.length && currentPos < fullFile.length) {
            byte[] subArray = Arrays.copyOfRange(fullFile, currentPos, fullFile.length);
            v = "PACKET;" + subArray.toString();
        } else {
            PrintWriter p = new PrintWriter(out, true);
            p.println("FINISHED");
        }
        currentPos += Servidor.FRAGMENTO;
        System.out.println(v);

        return v;

    }

}
