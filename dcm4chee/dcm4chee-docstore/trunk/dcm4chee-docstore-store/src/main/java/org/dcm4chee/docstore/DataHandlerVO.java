package org.dcm4chee.docstore;

import javax.activation.DataHandler;

public class DataHandlerVO {
    private String uid;
    private DataHandler dh;
    
    public DataHandlerVO(String uid, DataHandler dh) {
        this.uid = uid;
        this.dh = dh;
    }

    public String getUid() {
        return uid;
    }

    public DataHandler getDataHandler() {
        return dh;
    }
}
