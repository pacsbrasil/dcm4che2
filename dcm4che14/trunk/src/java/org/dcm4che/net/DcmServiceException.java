/*****************************************************************************
 *                                                                           *
 *  Copyright (c) 2002 by TIANI MEDGRAPH AG                                  *
 *                                                                           *
 *  This file is part of dcm4che.                                            *
 *                                                                           *
 *  This library is free software; you can redistribute it and/or modify it  *
 *  under the terms of the GNU Lesser General Public License as published    *
 *  by the Free Software Foundation; either version 2 of the License, or     *
 *  (at your option) any later version.                                      *
 *                                                                           *
 *  This library is distributed in the hope that it will be useful, but      *
 *  WITHOUT ANY WARRANTY; without even the implied warranty of               *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU        *
 *  Lesser General Public License for more details.                          *
 *                                                                           *
 *  You should have received a copy of the GNU Lesser General Public         *
 *  License along with this library; if not, write to the Free Software      *
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA  *
 *                                                                           *
 *****************************************************************************/

package org.dcm4che.net;

import org.dcm4che.data.Command;
import org.dcm4che.dict.Tags;

/**
 * <description>
 *
 * @see <related>
 * @author  <a href="mailto:gunter@tiani.com">gunter zeilinger</a>
 * @version $Revision$ $Date$
 * @since July 28, 2002
 *
 * <p><b>Revisions:</b>
 *
 * <p><b>yyyymmdd author:</b>
 * <ul>
 * <li> explicit fix description (no line numbers but methods) go
 *            beyond the cvs commit message
 * </ul>
 */
public class DcmServiceException extends Exception {
    
    private final int status;
    private int errorID = -1;
    private int actionTypeID = -1;
    private int eventTypeID = -1;
    
    public DcmServiceException(int status) {
        this.status = status;
    }    
    
    public DcmServiceException(int status, String msg) {
        super(msg);
        this.status = status;
    }

    public DcmServiceException(int status, String msg, Throwable cause) {
        super(msg, cause);
        this.status = status;
    }

    public DcmServiceException(int status, Throwable cause) {
        super(cause);
        this.status = status;
    }
    
    public int getStatus() {
        return status;
    }

    public DcmServiceException setErrorID(int errorID) {
        this.errorID = errorID;
        return this;
    }

    public int getErrorID() {
        return errorID;
    }

    public DcmServiceException setEventTypeID(int eventTypeID) {
        this.eventTypeID = eventTypeID;
        return this;
    }

    public int getEventTypeID() {
        return eventTypeID;
    }

    public DcmServiceException setActionTypeID(int actionTypeID) {
        this.actionTypeID = actionTypeID;
        return this;
    }

    public int getActionTypeID() {
        return actionTypeID;
    }

    public void writeTo(Command cmd) {
        cmd.putUS(Tags.Status, status);
        String msg = getMessage();
        if (msg != null && msg.length() > 0) {
            cmd.putLO(Tags.ErrorComment,
                msg.length() > 64 ? msg.substring(0, 64) : msg);
        }
        if (errorID >= 0) {
            cmd.putUS(Tags.ErrorID, errorID);
        }
        if (actionTypeID >= 0) {
            cmd.putUS(Tags.ActionTypeID, actionTypeID);
        }
        if (eventTypeID >= 0) {
            cmd.putUS(Tags.EventTypeID, eventTypeID);
        }
    }
}
