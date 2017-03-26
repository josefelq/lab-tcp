/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cliente;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author jose quiroga
 */
public class Cliente extends Thread{

    private Socket socket;

    private BufferedReader br;

    private DataOutputStream out;

    private ArrayList options;
    
    private int currentPos;
    
    private int numPackets;
    
    private long totalBytes;
    
    private String repositorio;
    
    private int fragmento;
    
    private FileOutputStream fos;
    
    private boolean pausado;

    public Cliente(){
        pausado=false;
        repositorio="C:\\Users\\Jos\\Music\\example";
        options = new ArrayList();
    }
    
    public void run(){
        while(true){
        }
    }

    public boolean connect() throws IOException {

        final String host = "localhost";
        final int portNumber = 1978;
        socket = new Socket(host, portNumber);
        br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new DataOutputStream(socket.getOutputStream());
        String result = br.readLine();

        if (result.equalsIgnoreCase("CONNECTED")) {
            System.out.println("CONNECTED");
            PrintWriter p = new PrintWriter(out, true);
            p.println("OPTIONS");
            String line;
            line = br.readLine();
            String[] f = line.split(";");
            for (int i = 1; i < f.length; i++) {
                options.add(f[i]);
            }
            System.out.println(options.toString());
            return true;
        } else {
            System.out.println("REFUSED");
            socket.close();
            br.close();
            out.close();
            return false;
        }

    }

    public void disconnect() throws IOException {
        PrintWriter p = new PrintWriter(out, true);
        p.println("DISCONNECT");
        socket.close();
        br.close();
        out.close();
        System.out.println("Disconnected");
        
    }

    public void sendDesiredFile(String s) throws IOException {
        currentPos=0;
        PrintWriter p = new PrintWriter(out, true);
        p.println("FILE;" + s);
        String pack = br.readLine();
        String bit = br.readLine();
        String frag = br.readLine();
        numPackets = Integer.parseInt(pack.split(";")[1]);
	totalBytes = Long.parseLong(bit.split(";")[1]);
        fragmento=Integer.parseInt(frag.split(";")[1]);	
        File f = new File(repositorio+"\\"+s);
	f.createNewFile();
	fos = new FileOutputStream(f);
    }
    
    public void download() throws IOException{
        
        while(currentPos<numPackets && !pausado){
        PrintWriter p = new PrintWriter(out, true);
        p.println("SEND");
        DataInputStream dis = new DataInputStream(socket.getInputStream());
        int offset = currentPos*fragmento;
        int length = (int) Math.min(fragmento, totalBytes-offset);
	byte[] data = new byte[length];
	dis.read(data, 0, length);
	fos.write(data);
	fos.flush();
	System.out.println("PACKET "+(currentPos+1)+": "+data.toString());
	currentPos++;
        }
        
   
    }
    
    public void pause(){
        pausado=!pausado;
    
    }
    

    /*
    public byte[] readBytes() throws IOException {
        DataInputStream in = new DataInputStream(socket.getInputStream());
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte buffer[] = new byte[1024];
        for (int s; (s = in.read(buffer)) != -1;) {
            baos.write(buffer, 0, s);
        }
        return baos.toByteArray();
    }

    public void convertByteArrayToDoc(byte[] b) {
        OutputStream out;
        try {
            out = new FileOutputStream("C:\\Users\\Jos\\Music\\example\\test2.txt");
            out.write(b);
            out.close();
            System.out.println("write success");
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public void addDataToArray(byte[] a) {
        for (int i = 0; i < a.length; i++) {
            fileData.add(a[i]);
        }

    }
    
        public byte[] convertToByte(ArrayList<Byte> list) {
        byte[] result = new byte[list.size()];
        for (int i = 0; i < list.size(); i++) {
            result[i] = (byte) list.get(i).byteValue();
        }
        return result;
    }
     */
}
