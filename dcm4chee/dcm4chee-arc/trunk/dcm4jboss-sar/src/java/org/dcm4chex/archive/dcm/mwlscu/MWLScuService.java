/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/
package org.dcm4chex.archive.dcm.mwlscu;

import java.io.IOException;
import java.net.Socket;
import java.security.GeneralSecurityException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.dcm4che.data.Command;
import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.dict.UIDs;
import org.dcm4che.net.AAssociateAC;
import org.dcm4che.net.AAssociateRQ;
import org.dcm4che.net.ActiveAssociation;
import org.dcm4che.net.Association;
import org.dcm4che.net.AssociationFactory;
import org.dcm4che.net.Dimse;
import org.dcm4che.net.FutureRSP;
import org.dcm4che.net.PDU;
import org.dcm4chex.archive.ejb.jdbc.AECmd;
import org.dcm4chex.archive.ejb.jdbc.AEData;
import org.dcm4chex.archive.ejb.jdbc.MWLQueryCmd;
import org.jboss.system.ServiceMBeanSupport;

/**
 * @author franz.willer
 *
 * MBean to configure and service modality worklist managment issues.
 * <p>
 * 
 */
public class MWLScuService extends ServiceMBeanSupport {
	
	
	/** Holds the calling AET. */
	private String callingAET;

	/** Holds the AET of modality worklist service. */
	private String mwlSCPAET;

	
	/** Holds Association timeout in ms. */
	private int acTimeout;
	
	/** Holds DICOM message timeout in ms. */
	private int dimseTimeout;
	
	/** Holds max PDU length in bytes. */
	private int maxPDUlen = 16352;
	
	/** Holds socket close delay in ms. */
	private int soCloseDelay;
	
	/** DICOM priority. Used for move and media creation action. */
	private int priority = 0;

	private static final AssociationFactory aFact = AssociationFactory.getInstance();
    private static final DcmObjectFactory oFact = DcmObjectFactory.getInstance();
    
            
	/**
	 * Returns the calling AET defined in this MBean.
	 * 
	 * @return The calling AET.
	 */
	public String getCallingAET() {
		return callingAET;
	}
	
	/**
	 * Set the calling AET.
	 * 
	 * @param aet The calling AET to set.
	 */
	public void setCallingAET( String aet ) {
		callingAET = aet;
	}
	
	/**
	 * Returns the AET that holds the work list (Modality Work List SCP).
	 * 
	 * @return The retrieve AET.
	 */
	public String getMwlScpAET() {
		return mwlSCPAET;
	}
	
	/**
	 * Set the retrieve AET.
	 * 
	 * @param aet The retrieve AET to set.
	 */
	public void setMwlScpAET( String aet ) {
		mwlSCPAET = aet;
	}
	
	
	/**
	 * Returns the Association timeout in ms.
	 * 
	 * @return Returns the acTimeout.
	 */
	public int getAcTimeout() {
		return acTimeout;
	}
	/**
	 * Set the association timeout.
	 * 
	 * @param acTimeout The acTimeout in ms.
	 */
	public void setAcTimeout(int acTimeout) {
		this.acTimeout = acTimeout;
	}
	
	/**
	 * Returns the DICOM message timeout in ms.
	 * 
	 * @return Returns the dimseTimeout.
	 */
	public int getDimseTimeout() {
		return dimseTimeout;
	}
	
	/**
	 * Set the DICOM message timeout.
	 * 
	 * @param dimseTimeout The dimseTimeout in ms.
	 */
	public void setDimseTimeout(int dimseTimeout) {
		this.dimseTimeout = dimseTimeout;
	}
	
	/**
	 * Returns the socket close delay in ms.
	 * 
	 * @return Returns the soCloseDelay.
	 */
	public int getSoCloseDelay() {
		return soCloseDelay;
	}
	
	/**
	 * Set the socket close delay.
	 * 
	 * @param delay Socket close delay in ms.
	 */
	public void setSoCloseDelay( int delay ) {
		soCloseDelay = delay;
	}

	
	/**
	 * returns the max PDU length in bytes.
	 * 
	 * @return Returns the maxPDUlen.
	 */
	public int getMaxPDUlen() {
		return maxPDUlen;
	}
	
	/**
	 * Set the max PDU length.
	 * 
	 * @param maxPDUlen The maxPDUlen in bytes.
	 */
	public void setMaxPDUlen(int maxPDUlen) {
		this.maxPDUlen = maxPDUlen;
	}

	/**
	 * Returns the DICOM priority as int value.
	 * <p>
	 * This value is used for CFIND.
	 * 0..MED, 1..HIGH, 2..LOW
	 * 
	 * @return Returns the priority.
	 */
	public int getPriority() {
		return priority;
	}
	
	/**
	 * Set the DICOM priority.
	 * 
	 * @param priority The priority to set.
	 */
	public void setPriority(int priority) {
		if ( priority < 0 || priority > 2 ) priority = 0;
		this.priority = priority;
	}
	
	/**
	 * 
	 */
    protected void startService() throws Exception {
        super.startService();
    }

	/**
	 * 
	 */
    protected void stopService() throws Exception {
        super.stopService();
    }

    
    /**
     * Get a list of work list entries.
	 */
	public List findMWLEntries( Dataset searchDS ) {
		if ( "local".equalsIgnoreCase( this.getMwlScpAET() ) ) {
			return findMWLEntriesLocal( searchDS );
		} else {
			return findMWLEntriesFromAET( searchDS );
		}
	}
	/**
	 * @param searchDS
	 * @return
	 */
	private List findMWLEntriesLocal(Dataset searchDS) {
		List l = new ArrayList();
		MWLQueryCmd queryCmd = null;
		try {
			queryCmd = new MWLQueryCmd(searchDS);
			queryCmd.execute();
			while ( queryCmd.next() ) {
				l.add( queryCmd.getDataset() );
			}
		} catch (SQLException x) {
			log.error( "Exception in findMWLEntriesLocal! ", x);
		}
		if ( queryCmd != null ) queryCmd.close();
		return l;
	}

	private List findMWLEntriesFromAET( Dataset searchDS ) {
    	ActiveAssociation assoc = null;
    	String iuid = null;
    	List list = new ArrayList();
    	try {
//get association for mwl find.    		
    		AEData aeData = new AECmd( this.getMwlScpAET() ).execute();
			assoc = openAssoc( aeData.getHostName(), aeData.getPort(), getCFINDAssocReq() );
			if ( assoc == null ) {
				log.error( "Couldnt open association! AET:"+getMwlScpAET()+" host:"+aeData.getHostName()+":"+aeData.getPort() );
				return list;
			}
			Association as = assoc.getAssociation();
			if (as.getAcceptedTransferSyntaxUID(1) == null) {
            	log.error(getMwlScpAET()+" doesnt support CFIND request!", null );
				return list;
			}
//send media creation request.			
			Command cmd = oFact.newCommand();
            cmd.initCFindRQ(1, UIDs.ModalityWorklistInformationModelFIND, getPriority() );
            Dimse mcRQ = aFact.newDimse(1, cmd, searchDS);
            if ( log.isDebugEnabled() ) log.debug("make CFIND req:"+mcRQ);
            FutureRSP rsp = assoc.invoke(mcRQ);
            Dimse dimse = rsp.get();
            if ( log.isDebugEnabled() ) log.debug("CFIND resp:"+dimse);
            List pending = rsp.listPending();
            if ( log.isDebugEnabled() ) log.debug("CFIND pending:"+pending);
            Iterator iter = pending.iterator();
            while ( iter.hasNext() ) {
            	list.add( ( (Dimse) iter.next()).getDataset() );
            }
            list.add( dimse.getDataset() );
            
		} catch (Exception e) {
			log.error( "Cant get working list! Reason: unexpected error", e);
			return list;
		} finally {
			if ( assoc != null )
				try {
					assoc.release( true );
				} catch (Exception e1) {
					log.error( "Cant release association for CFIND modality working list"+assoc.getAssociation(),e1);
				}
		}
    	return list;
	}

	/**
	 * Open a DICOM association for given host, port and assocition request.
	 * 
	 * @param host		Host to create the association.
	 * @param port		Port number to create the association.
	 * @param assocRQ	The association request object.
	 * 
	 * @return	The Active association object.
	 * 
	 * @throws IOException
	 * @throws GeneralSecurityException
	 */
	private ActiveAssociation openAssoc( String host, int port, AAssociateRQ assocRQ ) throws IOException, GeneralSecurityException {
		Association assoc = aFact.newRequestor( new Socket( host, port ) );
		assoc.setAcTimeout(acTimeout);
		assoc.setDimseTimeout(dimseTimeout);
        assoc.setSoCloseDelay(soCloseDelay);
		PDU assocAC = assoc.connect(assocRQ);
		if (!(assocAC instanceof AAssociateAC)) { return null; }
		ActiveAssociation retval = aFact.newActiveAssociation(assoc, null);
		retval.start();
		return retval;
    }
  
    
    /**
     * Return the association request for media creation.
     * <p>
     * This association is used for sending media creation request and action command.
     * 
	 * @return Association for media creation.
	 */
	private AAssociateRQ getCFINDAssocReq() {
    	AAssociateRQ assocRQ = aFact.newAAssociateRQ();
    	assocRQ.setCalledAET( this.getMwlScpAET() );
    	assocRQ.setCallingAET( getCallingAET() );
    	assocRQ.setMaxPDULength( maxPDUlen );
    	assocRQ.addPresContext(aFact.newPresContext(1,
                UIDs.ModalityWorklistInformationModelFIND,
                new String[]{UIDs.forName("ExplicitVRLittleEndian"),
        					 UIDs.forName("ImplicitVRLittleEndian")} ));
    	return assocRQ;
	}
	
}
