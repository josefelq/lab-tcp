/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cliente;

import java.io.IOException;

/**
 *
 * @author jFluxie
 */
public class Main {
    
    public static void main(String[] args) throws IOException{
        Cliente c=new Cliente();
        c.start();
        c.connect();
        c.sendDesiredFile("5mb.pdf");
        c.download();
        c.disconnect();
    }
    
}
