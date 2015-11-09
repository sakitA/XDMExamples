/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package xdmexamples.helper;

import javafx.beans.property.SimpleStringProperty;

/**
 *
 * @author sakit
 */
public class DataRegistry {
    
    private SimpleStringProperty   enable; // set enable/disable value
    private SimpleStringProperty    key;    // set key value
    private SimpleStringProperty    program;// set program value
    private SimpleStringProperty    data;   // set file value
    private String path;
    
    public DataRegistry(){}
    
    public DataRegistry(boolean enable, String key, String program, String data){
        setEnable(enable);
        this.key = new SimpleStringProperty(key);
        this.program = new SimpleStringProperty(program);
        this.data = new SimpleStringProperty(data);
    }

    public String getEnable() {
        return enable.get();
    }

    public void setEnable(boolean enable) {
        if(enable)
            this.enable = new SimpleStringProperty("İşləyir");
        else
            this.enable = new SimpleStringProperty("İşləmir");
    }

    public String getKey() {
        return key.get();
    }

    public void setKey(String key) {
        this.key.set(key);
    }

    public String getProgram() {
        return program.get();
    }

    public void setProgram(String program) {
        this.program.set(program);
    }

    public String getData() {
        return data.get();
    }

    public void setData(String data) {
        this.data.set(data);
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}