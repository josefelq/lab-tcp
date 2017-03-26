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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author jose quiroga
 */
public class Cliente {

    private Socket socket;

    private BufferedReader br;

    private DataOutputStream out;

    private ArrayList<Byte> fileData;

    private ArrayList options;

    public Cliente() {
        options = new ArrayList();
        fileData = new ArrayList<Byte>();
    }

    public void run() throws IOException {
        while (true) {
            String line;
            line = br.readLine();
            handleMessage(line);
        }
    }

    public void handleMessage(String s) throws IOException {

        System.out.println(s);

        if (s.equalsIgnoreCase("FINISHED")) {
            byte ar[] = readBytes();
            addDataToArray(ar);
            convertByteArrayToDoc(convertToByte(fileData));
            PrintWriter p = new PrintWriter(out, true);
            p.println("THANKS");
        } else if (s.startsWith("READY")) {
            PrintWriter p = new PrintWriter(out, true);
            p.println("OK");
            byte ar[] = readBytes();
            addDataToArray(ar);
        } else if (s.startsWith("OPTIONS")) {
            String[] f = s.split(";");
            for (int i = 1; i < f.length; i++) {
                options.add(f[i]);
            }
            System.out.println(options.toString());
        }

    }

    public byte[] convertToByte(ArrayList<Byte> list) {
        byte[] result = new byte[list.size()];
        for (int i = 0; i < list.size(); i++) {
            result[i] = (byte) list.get(i).byteValue();
        }
        return result;
    }

    public boolean connect() throws IOException {

        final String host = "localhost";
        final int portNumber = 1978;
        socket = new Socket(host, portNumber);
        br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new DataOutputStream(socket.getOutputStream());
        String result = br.readLine();

        if (result.equalsIgnoreCase("CONNECTED")) {
            System.out.println("llegamos aca");
            PrintWriter p = new PrintWriter(out, true);
            p.println("OPTIONS");
            return true;
        } else {
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
    }

    public void sendDesiredFile(String s) {
        PrintWriter p = new PrintWriter(out, true);
        p.println("FILE;" + s);
    }

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
}
