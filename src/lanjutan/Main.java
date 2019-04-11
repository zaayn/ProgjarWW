/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lanjutan;

import java.io.IOException;
import java.net.ServerSocket;

/**
 *
 * @author Laptop
 */
public class Main {
    private static MultiEchoServer server;
    
    public static void main(String[] args) throws IOException
    { 
        server = new MultiEchoServer();
        server.start();
    
    }
}
