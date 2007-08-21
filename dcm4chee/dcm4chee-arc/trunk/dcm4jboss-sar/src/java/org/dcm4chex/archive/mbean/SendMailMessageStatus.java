/*
 * org.dcm4chex.archive.mbean.SendMailDelivered.java
 * Created on Aug 19, 2007 by jfalk
 * Copyright 2007, QNH, Inc. info@qualitynighthawk.com, All rights reserved
 */
package org.dcm4chex.archive.mbean;

import java.io.Serializable;
import java.util.Map;

/**
 * Message containing a SendMail send properties map and the status of the send.
 * 
 *  org.dcm4chex.archive.mbean.SendMailMessageStatus
 * 
 * @author <a href="mailto:jfalkmu@gmail.com">jfalk</a>
 */
public class SendMailMessageStatus implements Serializable{

    private static final long serialVersionUID = 4529711564783113836L;

    private boolean success = true;

    // SendMail properties
    private Map mailProperties;

    public SendMailMessageStatus(Map mailProperties) {
        this.mailProperties = mailProperties;
    }

    public SendMailMessageStatus(boolean status, Map mailProperties) {
        this.success = status;
        this.mailProperties = mailProperties;
    }

    public Map getMailProperties() {
        return mailProperties;
    }

    public void setMailProperties(Map mailProperties) {
        this.mailProperties = mailProperties;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}