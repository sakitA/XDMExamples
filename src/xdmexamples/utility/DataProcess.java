/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package xdmexamples.utility;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;

/**
 *
 * @author sakit
 */
public class DataProcess implements Comparable{
    
    private SimpleStringProperty    pName;
    private SimpleIntegerProperty   pPid;
    private SimpleStringProperty    pSessionName;
    private SimpleIntegerProperty   pSession;
    private SimpleLongProperty      pMemUsage;
      
    public String getPName() {
        return pName.get();
    }

    public void setPName(String pName) {
        this.pName = new SimpleStringProperty(pName);
    }

    public int getPPid() {
        return pPid.get();
    }

    public void setPPid(int pPid) {
        this.pPid = new SimpleIntegerProperty(pPid);
    }

    public String getPSessionName() {
        return pSessionName.get();
    }

    public void setPSessionName(String pSessionName) {
        this.pSessionName = new SimpleStringProperty(pSessionName);
    }

    public int getPSession() {
        return pSession.get();
    }

    public void setPSession(int pSession) {
        this.pSession = new SimpleIntegerProperty(pSession);
    }

    public long getPMemUsage() {
        return pMemUsage.get();
    }

    public void setPMemUsage(long pMemUsage) {
        this.pMemUsage = new SimpleLongProperty(pMemUsage);
    }

    @Override
    public String toString() {
        return "DataProcess{" + "pName=" + pName + ", pPid=" + pPid + ", pSessionName=" + pSessionName + ", pSession=" + pSession + ", pMemUsage=" + pMemUsage + '}';
    }

    @Override
    public int compareTo(Object o) {
        if(((DataProcess)o).getPMemUsage() < this.pMemUsage.get())
            return -1;
        else if(((DataProcess)o).getPMemUsage() > this.pMemUsage.get())
            return 1;
        else
            return 0;
    }
}