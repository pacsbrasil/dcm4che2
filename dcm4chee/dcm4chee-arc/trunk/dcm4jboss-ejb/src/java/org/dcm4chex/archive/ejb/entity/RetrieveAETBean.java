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

import org.apache.log4j.Logger;

/**
 * @ejb.bean name="RetrieveAET" type="CMP" view-type="local"
 * 	primkey-field="pk" local-jndi-name="ejb/RetrieveAET"
 * 
 * @ejb.transaction type="Required"
 * 
 * @ejb.persistence table-name="retrieve_aet"
 * 
 * @jboss.entity-command name="hsqldb-fetch-key"
 * 
 * @ejb.finder
 *  signature="org.dcm4chex.archive.ejb.interface.RetrieveAETLocal findByAET(java.lang.String aet)"
 *  query="SELECT OBJECT(a) FROM RetrieveAET AS a WHERE a.aet = ?1"
 *  transaction-type="Supports"
 *
 * @author gunter.zeilinger@tiani.com
 * @version $Revision$ $Date$
 * @since 26.11.2004
 */
public abstract class RetrieveAETBean implements EntityBean {

    private static final Logger log = Logger.getLogger(RetrieveAETBean.class);
    
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
     * @ejb.persistence column-name="aet"
     */
    public abstract String getAET();

    public abstract void setAET(String aet);

    /**
     * @ejb.interface-method
     * @ejb.persistence column-name="availability"
     */
    public abstract int getAvailability();

    /**
     * @ejb.interface-method
     */
    public abstract void setAvailability(int availability);

    /**
     * @ejb.create-method
     */
    public Integer ejbCreate(String aet, int availability) throws CreateException {
        setAET(aet);
        setAvailability(availability);
        return null;
    }

    public void ejbPostCreate(String iuid, String id) throws CreateException {
        log.info("Created " + this);
    }

    public String asString() {
        return "RetrieveAET[aet=" + getAET() + "availability=" + getAvailability()
                + "]";
    }
}
