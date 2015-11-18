/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package xdmexamples.utility;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javafx.application.Platform;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;

/**
 *
 * @author sakit
 */
public class Server {

    private final TextArea textArea;
    private final TextField textField;
    private final int portNumber = 9000;
    private int maxClientCount;
    private ObjectOutputStream output;
    private ObjectInputStream input;
    private ServerSocket server;
    private Socket clientSocket;
    private final ExecutorService threadPool;

    private boolean running;
    private static int counter;

    public Server(TextArea area, TextField field, int maxClientCount) {
        counter = 0;
        this.maxClientCount = maxClientCount;
        threadPool = Executors.newFixedThreadPool(maxClientCount);
        textArea = area;
        area.setEditable(false);
        
        textField = field;
        textField.setOnKeyPressed(e->{
            if(e.getCode() == KeyCode.ENTER){
                sendData(textField.getText());
                textField.setText("");
            }
        });        
    }

    public boolean isServerRunnning() {
        return running;
    }

    //step 1: create a ServerSocket
    public void startServer() {
        try {
            server = new ServerSocket(portNumber);
            displayMessage("Server is running. There is no active client");
            displayMessage("Server is waiting a client");
            running = true;
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        run();
    }

    //step 2: wait for a connection
    private void run() {
        while (isServerRunnning()) {
            try {          
                displayMessage("Connected client count:"+counter);
                clientSocket = server.accept();
                ++counter;
            } catch (IOException e) {
                if (!isServerRunnning()) {
                    clientSocket = null;
                    --counter;
                    break;
                }
                throw new RuntimeException(
                        "Error accepting client connection", e);
            }
            threadPool.execute(()->{
                getStreams();
                processConnection();
            });
        }
        threadPool.shutdown();
    }
    
    //step 3: get input and output stream from Socket
    private void getStreams(){        
        try {
            output = new ObjectOutputStream(clientSocket.getOutputStream());
            output.flush();
            
            input = new ObjectInputStream(clientSocket.getInputStream());
        } catch (IOException ex) {
            ex.printStackTrace();
        }        
    }

    //step 4: process connection with client
    private void processConnection(){
        String message;
        boolean terminate = false;
        while(true){
            try {
                message = (String)input.readObject();
                terminate = message.contains("~");
                if(terminate){
                    displayMessage("\n"+message.substring(0,message.indexOf("~"))+" leave the server");
                    break;
                }
                else
                    displayMessage(message);
            } catch (IOException | ClassNotFoundException ex) {
                ex.printStackTrace();
            }
        }
    }
    
    //step 5: close the server
    public synchronized void stopServer() {
        running = false;
        if (!server.isClosed()) {
            try {
                if(output!=null)
                    output.close();
                if(input!=null)
                    input.close();
                if(clientSocket!=null)
                    clientSocket.close();
                if(server!=null){
                    server.close();
                    server = null;
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            displayMessage("Server is stopped");
        }
    }

    private synchronized void displayMessage(final String message) {
        Platform.runLater(()->{
            textArea.appendText(message+"\n");
        });        
    }
    
    private synchronized void sendData(String message){
        try {
            message = "Server>"+message;
            output.writeObject(message);    
            output.flush();
            displayMessage(message);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}