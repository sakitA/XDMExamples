/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package xdmexamples.utility;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import javafx.application.Platform;
import javafx.scene.control.Tab;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;

/**
 *
 * @author sakit
 */
public class Client {
    
    private final static String host = "127.0.0.1";
    private final static int portNumber = 9000;
    private TextArea textArea;
    private TextField textField;
    private String clientName;
    private ObjectInputStream input;
    private ObjectOutputStream output;
    private Socket connection;
    private boolean stop;
    
    public Client(Tab tab){
        
        tab.setOnCloseRequest(e->{
            System.out.println("in tab close request");
            stopClient();
        });
        
        VBox vbox = (VBox)tab.getContent();
        
        textField = (TextField)vbox.getChildren().get(0);
        textField.setOnKeyPressed(e->{
            if(e.getCode()==KeyCode.ENTER){
                sendData(textField.getText());
                textField.setText("");
            }
        });
        
        textArea = (TextArea)vbox.getChildren().get(1);
        textArea.setEditable(false);
        
        clientName = tab.getText();
        stop = false;
    }
    
    public void runClient(){
        try {
            connectClient();
            getStreams();
            processConnection();
            closeClient();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private synchronized void displayMessage(String text) {
        Platform.runLater(()->{textArea.appendText(text+"\n");});
    }

    //step 1: connect to the client
    private void connectClient() throws IOException {
        displayMessage(clientName+" attempting connect to the server");
        connection = new Socket(host, portNumber);
        displayMessage(clientName+" connected to "+connection.getInetAddress());
    }

    //step 2: get input/output streams
    private void getStreams() throws IOException {
        output = new ObjectOutputStream(connection.getOutputStream());
        output.flush();
        
        input = new ObjectInputStream(connection.getInputStream());
    }

    //step 3: read from and write to socket
    private void processConnection() throws IOException{
        String message = null;
        while(!stop){
            try {
                message = (String)input.readObject();
                displayMessage(message);
            } catch (ClassNotFoundException ex) {
                ex.printStackTrace();
            } catch(EOFException eofe){                
            }
        }
    }
    
    //step 4: close onnection
    private synchronized void closeClient() throws IOException{
        if(output!=null)
            output.close();
        if(input!=null)
            input.close();
        if(connection.isConnected())
            connection.close();        
    }
    
    public synchronized void stopClient(){
        stop = true;
        sendData("@@@@@");
    }

    private synchronized void sendData(String text) {
        String message = null;
        try {            
            if(text.contains("~"))
                message = clientName+text;
            else{
                message = clientName+">"+text;
                displayMessage(message);
            }
            output.writeObject(message);
            output.flush();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}