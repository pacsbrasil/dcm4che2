/* $Id$
 * Copyright (c) 2002,2003 by TIANI MEDGRAPH AG
 *
 * This file is part of dcm4che.
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package org.dcm4chex.archive.hl7;

import ca.uhn.hl7v2.app.Application;
import ca.uhn.hl7v2.app.ApplicationException;
import ca.uhn.hl7v2.app.DefaultApplication;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.Segment;
import ca.uhn.hl7v2.model.v231.datatype.CX;
import ca.uhn.hl7v2.model.v231.datatype.XPN;
import ca.uhn.hl7v2.model.v231.group.ADT_A40_PIDPD1MRGPV1;
import ca.uhn.hl7v2.model.v231.message.ADT_A40;
import ca.uhn.hl7v2.model.v231.segment.MRG;
import ca.uhn.hl7v2.model.v231.segment.PID;

/**
 * @author gunter.zeilinger@tiani.com
 * @version $Revision$ $Date$
 * @since 08.03.2004
 * 
 * @jmx.mbean extends="org.dcm4chex.archive.hl7.HL7AcceptServiceMBean"
 */
public class HL7MergePatientService
    extends HL7AcceptService
    implements org.dcm4chex.archive.hl7.HL7MergePatientServiceMBean {

    private final Application handler = new DefaultApplication() {
        public Message processMessage(Message in) throws ApplicationException {
            Message out = null;
            try {
                ADT_A40 a40 = (ADT_A40)in;
                ADT_A40_PIDPD1MRGPV1 pid_mrg = a40.getADT_A40_PIDPD1MRGPV1();
                PID pid = pid_mrg.getPID();
                CX pid0 = pid.getPatientIdentifierList(0);
                TS pid.getDateTimeOfBirth()
                XPN pn0 = pid.getPatientName(0);
                MRG mrg = pid_mrg.getMRG();
                CX pid1 = mrg.getPriorPatientIdentifierList(0);
                XPN pn2 = mrg.getPriorPatientName(0);
                
                
                //get default ACK
                out = makeACK((Segment) in.get("MSH"));
            } catch (Exception e) {
                throw new ApplicationException(
                    "Couldn't create response message: " + e.getMessage());
            }
            return out;
        }
    };
    
    protected Application getApplication() {
        return handler;
    }

}
