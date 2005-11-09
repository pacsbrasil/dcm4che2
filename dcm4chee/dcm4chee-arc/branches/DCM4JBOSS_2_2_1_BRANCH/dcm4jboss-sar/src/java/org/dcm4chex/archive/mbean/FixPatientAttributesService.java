/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/
package org.dcm4chex.archive.mbean;

import java.rmi.RemoteException;

import javax.ejb.CreateException;
import javax.ejb.FinderException;

import org.apache.log4j.Logger;
import org.dcm4chex.archive.ejb.interfaces.FixPatientAttributes;
import org.dcm4chex.archive.ejb.interfaces.FixPatientAttributesHome;
import org.dcm4chex.archive.util.EJBHomeFactory;
import org.jboss.system.ServiceMBeanSupport;

/**
 * @author franz.willer@gwi-ag.com
 * @version $Revision$ $Date$
 * @since 35.03.2005
 *
 */
public class FixPatientAttributesService extends ServiceMBeanSupport {

     private int limitNumberOfRecordsPerTask;

    private static final Logger log = Logger.getLogger(FixPatientAttributesService.class);




    public int getLimitNumberOfRecordsPerTask() {
        return limitNumberOfRecordsPerTask;
    }

    public void setLimitNumberOfRecordsPerTask(int limit) {
        this.limitNumberOfRecordsPerTask = limit;
    }

  
    public int checkPatientAttributes() throws RemoteException, FinderException, CreateException {
    	return checkPatientAttributes(false);
    }
    public int repairPatientAttributes() throws RemoteException, FinderException, CreateException {
     	return checkPatientAttributes(true);
    }
    
    private int checkPatientAttributes(boolean doUpdate) throws RemoteException, FinderException {
    	FixPatientAttributes checker = newFixPatientAttributes();
    	int offset = 0, total = 0;
    	int[] fixed;
		do {
			fixed = checker.checkPatientAttributes(offset,limitNumberOfRecordsPerTask, doUpdate);
			total += fixed[0];
    		offset += limitNumberOfRecordsPerTask;
		} while (fixed[1] == limitNumberOfRecordsPerTask);
    	return total;
    }
    
    public int checkStudyAttributes() throws RemoteException, FinderException, CreateException {
    	return checkStudyAttributes(false);
    }
    public int repairStudyAttributes() throws RemoteException, FinderException, CreateException {
     	return checkStudyAttributes(true);
    }
    
    private int checkStudyAttributes(boolean doUpdate) throws RemoteException, FinderException {
    	FixPatientAttributes checker = newFixPatientAttributes();
    	int offset = 0, total = 0;
    	int[] fixed;
		do {
			fixed = checker.checkStudyAttributes(offset,limitNumberOfRecordsPerTask, doUpdate);
			total += fixed[0];
    		offset += limitNumberOfRecordsPerTask;
		} while (fixed[1] == limitNumberOfRecordsPerTask);
    	return total;
    }

    private FixPatientAttributes newFixPatientAttributes() {
        try {
        	FixPatientAttributesHome home = (FixPatientAttributesHome) EJBHomeFactory
                    .getFactory().lookup(FixPatientAttributesHome.class,
                    		FixPatientAttributesHome.JNDI_NAME);
            return home.create();
        } catch (Exception e) {
            throw new RuntimeException("Failed to access FixPatientAttributes EJB:",
                    e);
        }
    }
    
}