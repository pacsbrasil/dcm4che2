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
import java.rmi.RemoteException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.ejb.CreateException;
import javax.ejb.FinderException;

import org.dcm4che.data.Command;
import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.dict.Tags;
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
import org.dcm4chex.archive.util.HomeFactoryException;
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
	 * Returns the retrieve AET defined in this MBean.
	 * <p>
	 * This AET performs the move operation.
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
	 * This value is used for Move and media creation action (N-Action).
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
	 * Start listening to the JMS queue deined in <code>QUEUE</code>
	 * <p>
	 * This queue is used to receive media creation request from scheduler or web interface.
	 */
    protected void startService() throws Exception {
        super.startService();
    }

	/**
	 * Stop listening to the JMS queue deined in <code>QUEUE</code>
	 * 
	 */
    protected void stopService() throws Exception {
        super.stopService();
    }

    
    /**
     * Process the media creation request.
     * <DL>
     * <DD>1) Send media creation request to <code>mcMScpAET</code>. </DD>
     * <DD>2) Send N-Action command for media creation to <code>mcMScpAET</code>. </DD>
     * </DL>
     * <p>
     * 
	 * @param mediaDTO The MediaDTO object to process.
	 */
	private List getMWLList() {
    	ActiveAssociation assoc = null;
    	String iuid = null;
    	List list = new ArrayList();
    	try {
//get association for mwl find.    		
    		AEData aeData = new AECmd( this.getMwlScpAET() ).execute();
			assoc = openAssoc( aeData.getHostName(), aeData.getPort(), getMediaCreationAssocReq() );
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
            Dataset ds = getMediaCreationReqDS();
            Dimse mcRQ = aFact.newDimse(1, cmd, ds);
            FutureRSP rsp = assoc.invoke(mcRQ);
            Dimse dimse = rsp.get();
            List pending = rsp.listPending();
            Iterator iter = pending.iterator();
            while ( iter.hasNext() ) {
            	list.add( ( (Dimse) iter.next()).getDataset() );
            }
            list.add( dimse.getDataset() );
            
		} catch (Exception e) {
			log.error( "Cant get list working list! Reason: unexpected error", e);
			return list;
		} finally {
			if ( assoc != null )
				try {
					assoc.release( true );
				} catch (Exception e1) {
					log.error( "Cant release association for media creation request"+assoc.getAssociation(),e1);
				}
		}
    	return list;
	}

	/**
	 * Get the initialized media creation request dataset.
	 * 
	 * @return The Dataset for media creation request.
	 * 
	 * @throws CreateException
	 * @throws HomeFactoryException
	 * @throws FinderException
	 * @throws RemoteException
	 */
	private Dataset getMediaCreationReqDS( ) throws RemoteException, FinderException, HomeFactoryException, CreateException {
		Dataset ds = oFact.newDataset();
		ds.putCS(Tags.SpecificCharacterSet, "ISO_IR 100");

		return ds;
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
	private AAssociateRQ getMediaCreationAssocReq() {
    	AAssociateRQ mcAssocRQ = aFact.newAAssociateRQ();
    	mcAssocRQ.setCalledAET( this.getMwlScpAET() );
    	mcAssocRQ.setCallingAET( getCallingAET() );
    	mcAssocRQ.setMaxPDULength( maxPDUlen );
    	mcAssocRQ.addPresContext(aFact.newPresContext(1,
                UIDs.MediaCreationManagementSOPClass,
                new String[]{UIDs.forName("ExplicitVRLittleEndian"),
        					 UIDs.forName("ImplicitVRLittleEndian")} ));
    	return mcAssocRQ;
	}
	
}
