/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/
package org.dcm4chex.archive.ejb.entity;

import javax.ejb.CreateException;
import javax.ejb.EntityBean;
import javax.ejb.FinderException;
import javax.ejb.ObjectNotFoundException;
import javax.ejb.RemoveException;

import org.apache.log4j.Logger;
import org.dcm4che.data.Dataset;
import org.dcm4che.dict.Tags;
import org.dcm4chex.archive.ejb.interfaces.RefRequestLocal;
import org.dcm4chex.archive.ejb.interfaces.RefRequestLocalHome;

/**
 * @author gunter.zeilinger@tiani.com
 * @version Revision $Date$
 * @since 28.03.2005
 * 
 * @ejb.bean name="RefRequest" type="CMP" view-type="local"
 *           local-jndi-name="ejb/RefRequest" primkey-field="pk"
 * @ejb.persistence table-name="ref_request"
 * @ejb.transaction type="Required"
 * @jboss.entity-command name="hsqldb-fetch-key"
 * @ejb.finder signature="org.dcm4chex.archive.ejb.interfaces.RefRequestLocal findByRequestedProcedureID(java.lang.String id)"
 *             query="SELECT OBJECT(r) FROM RefRequest r WHERE r.requestedProcedureId = ?1"
 *             transaction-type="Supports"
 */

public abstract class RefRequestBean implements EntityBean {

    private static final Logger log = Logger.getLogger(RefRequestBean.class);

    /**
     * Auto-generated Primary Key
     * 
     * @ejb.interface-method
     * @ejb.pk-field
     * @ejb.persistence column-name="pk"
     * @jboss.persistence auto-increment="true"
     *  
     */
    public abstract Integer getPk();

    public abstract void setPk(Integer pk);
    
    /**
     * @ejb.interface-method
     * @ejb.persistence column-name="req_proc_id"
     */
    public abstract String getRequestedProcedureId();

    public abstract void setRequestedProcedureId(String id);

    /**
     * @ejb.interface-method
     * @ejb.persistence column-name="accession_no"
     */
    public abstract String getAccessionNumber();

    public abstract void setAccessionNumber(String no);

    /**
     * @ejb.interface-method
     */
    public String toString() {
        return "RefRequest[pk=" + getPk() 
                + ", rqprocid=" + getRequestedProcedureId()
                + ", accno=" + getAccessionNumber()
                + "]";
    }
    
    /**
     * @ejb.create-method
     */
    public Integer ejbCreate(String id, String accessionNo) throws CreateException {
        setRequestedProcedureId(id);
        setAccessionNumber(accessionNo);
        return null;
    }
    
    public void ejbPostCreate(String id, String accessionNo) throws CreateException {
        log.info("Created " + toString());
    }

    public void ejbRemove() throws RemoveException {
        log.info("Deleting " + toString());
    }

    
    public static RefRequestLocal valueOf(RefRequestLocalHome home, Dataset item)
            throws CreateException, FinderException {
        if (item == null) return null;

        final String id = item.getString(Tags.RequestedProcedureID);
        final String accNo = item.getString(Tags.AccessionNumber);
        try {
            return home.findByRequestedProcedureID(id);
        } catch (ObjectNotFoundException onf) {
            return home.create(id, accNo);
        }
    }
}
