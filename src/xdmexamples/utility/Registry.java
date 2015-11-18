/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package xdmexamples.utility;

/**
 *
 * @author sakit
 */
public class Registry {
    
    private final String name;
    private final String type;
    private final String value;
    private String location;

    public Registry(String name, String type, String value) {
        this.name = name;
        this.type = type;
        this.value = value;
    }
    
    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getValue() {
        return value;
    }    
    
    public String getPath(){
        return location;
    }
    
    public void setPath(String location){
        this.location = location;
    }
}