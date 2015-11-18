/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package xdmexamples.utility;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author sakit
 */
public class Server {
    
    private final String hostname = "127.0.0.1";
    private final int portNumber = 9090;
    private ServerSocket server;
    private ExecutorService threadPool;
    
    private boolean running;
    
    public boolean isServerRunnning(){
        return running;
    }
    
    public synchronized void startServer(){
        running = true;
    }
    
    public synchronized void stopServer(){
        running = false;
        if(!server.isClosed()){
            try {
                server.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }    
    }
}
