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
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.StringTokenizer;

import javax.ejb.FinderException;
import javax.management.Notification;
import javax.management.NotificationListener;

import org.dcm4che.dict.UIDs;
import org.dcm4cheri.util.StringUtils;
import org.dcm4chex.archive.config.RetryIntervalls;
import org.dcm4chex.archive.ejb.interfaces.FileDTO;
import org.dcm4chex.archive.ejb.interfaces.FileSystemDTO;
import org.dcm4chex.archive.ejb.interfaces.FileSystemMgt;
import org.dcm4chex.archive.ejb.interfaces.FileSystemMgtHome;
import org.dcm4chex.archive.util.EJBHomeFactory;

/**
 * @author gunter.zeilinger@tiani.com
 * @version $Revision$ $Date$
 * @since 12.09.2004
 *
 */
public class CompressionService extends TimerSupport {

    private long delayedCompressionInterval = 0L;
    private int disabledStartHour;
    private int disabledEndHour;
    
	private String compressAfter;
	private List aetCompressInfoList = new ArrayList();
	
	private int maxFiles;
	
	private static final String[] COMPRESSABLE_TRANSFER_SYNTAX = new String[]{UIDs.ImplicitVRLittleEndian, UIDs.ExplicitVRLittleEndian};
	
    private final NotificationListener delayedCompressionListener = 
        new NotificationListener(){
            public void handleNotification(Notification notif, Object handback) {
            	Calendar cal = Calendar.getInstance();
            	int hour = cal.get(Calendar.HOUR_OF_DAY);
            	if ( isDisabled( hour ) ) {
                	log.info("delayed compression cycle ignored in time between "+disabledStartHour+" and "+disabledEndHour+" !");
            	} else {
                	log.info("delayed compression cycle started");
                	try {
						startDelayedCompression();
					} catch (Exception e) {
						log.error("Delayed compression failed!",e);
					}
            	}
            }};

	private Integer delayedCompressionListenerID;
	private FileSystemMgt fileSystemMgt;
	
    public final String getDelayedCompressionInterval() {
        String s = RetryIntervalls.formatIntervalZeroAsNever(delayedCompressionInterval);
        if ( disabledEndHour != -1)
				s += "!"+disabledStartHour+"-"+disabledEndHour;
        return s;
    }
    

	public void setDelayedCompressionInterval(String interval) {
		long oldInterval = delayedCompressionInterval;
    	int pos = interval.indexOf('!');
    	if ( pos == -1 ) {
    		delayedCompressionInterval = RetryIntervalls.parseIntervalOrNever(interval);
    		disabledEndHour = -1;
    	} else {
    		delayedCompressionInterval = RetryIntervalls.parseIntervalOrNever(interval.substring(0,pos));
    		int pos1 = interval.indexOf('-', pos);
    		disabledStartHour = Integer.parseInt( interval.substring(pos+1,pos1));
    		disabledEndHour = Integer.parseInt( interval.substring(pos1+1) );
    	}
        if ( getState() == STARTED && oldInterval != delayedCompressionInterval ) {
            stopScheduler(delayedCompressionListenerID, delayedCompressionListener);
            delayedCompressionListenerID = startScheduler(delayedCompressionInterval, delayedCompressionListener);
        }
    }
    
    
    public final void setCompressAfter(String compressAfter) {
    	if ( this.compressAfter != null && this.compressAfter.equals( compressAfter ) ) return;
    	this.compressAfter = compressAfter;
    	this.aetCompressInfoList.clear();
    	if ( compressAfter == null || compressAfter.trim().length() < 1 ) return;
    	StringTokenizer st = new StringTokenizer( compressAfter, "," );
    	while ( st.hasMoreTokens() ) {
    		aetCompressInfoList.add( new AETCompressionInfo( st.nextToken() ) );
    	}
    	log.debug("setCompressAfter:"+aetCompressInfoList);
    }

    public final String getCompressAfter() {
        return compressAfter;
    }
    
	/**
	 * @return Returns the maxFiles.
	 */
	public int getMaxFiles() {
		return maxFiles;
	}
	/**
	 * @param maxFiles The maxFiles to set.
	 */
	public void setMaxFiles(int maxFiles) {
		this.maxFiles = maxFiles;
	}

    /**
     * @throws FinderException
     * @throws RemoteException
	 * 
	 */
	protected void startDelayedCompression() throws RemoteException, FinderException {

		Timestamp before; 
		FileSystemDTO[] fs = lookupFileSystemMgt().getAllFileSystems();
		AETCompressionInfo info;
		FileDTO[] files;
		int limit = maxFiles;
		for ( int i = 0 ; i < fs.length && limit > 0; i++) {
			for ( int j = 0, len = aetCompressInfoList.size(); j < len ; j++ ) {
				info = (AETCompressionInfo) aetCompressInfoList.get(j);
				before = new Timestamp( System.currentTimeMillis() - info.getBefore() );
				files = lookupFileSystemMgt().findToCompress( COMPRESSABLE_TRANSFER_SYNTAX, info.getAETs(), fs[i].getDirectoryPath(), before, limit);
				if ( files != null ) {
					log.info( "Compress files for "+info+" on FS:"+fs[i]+" nrOfFiles:"+files.length );
					//TODO Do compression here!
					limit -= files.length;
					if ( limit < 1 ) break;
				} else {
					log.info( "No files to compress for "+info+" on FS:"+fs[i] );
				}
			}
		
		}
	}

	/**
	 * @param hour
	 * @return
	 */
	protected boolean isDisabled(int hour) {
		if ( disabledEndHour >= disabledStartHour) {
			return hour >= disabledStartHour && hour <= disabledEndHour;
		} else {
			return ! ( hour > disabledEndHour && hour < disabledStartHour );
		}
	}

    protected void startService() throws Exception {
         super.startService();
         delayedCompressionListenerID = startScheduler(delayedCompressionInterval,
                 delayedCompressionListener);
    }
    
    protected void stopService() throws Exception {
        stopScheduler(delayedCompressionListenerID, delayedCompressionListener);
        super.stopService();
    }
    
    private FileSystemMgt lookupFileSystemMgt() {
    	if ( fileSystemMgt != null ) return fileSystemMgt;
        try {
            FileSystemMgtHome home = (FileSystemMgtHome) EJBHomeFactory
                    .getFactory().lookup(FileSystemMgtHome.class,
                            FileSystemMgtHome.JNDI_NAME);
            fileSystemMgt = home.create();
            return fileSystemMgt;
        } catch (Exception e) {
            throw new RuntimeException("Failed to access File System Mgt EJB:",
                    e);
        }
    }

    
	public class AETCompressionInfo {
		String[] aets;
		long before;
		String type;
		
		public AETCompressionInfo( String s ) {
			int skip = s.indexOf('=');
			int pos = s.indexOf('/');
			int pos1 = s.indexOf(']');
			if ( skip == -1 || pos == -1 || pos1 == -1 ) {
				throw new IllegalArgumentException("Wrong format! Use [aet=<AET>|<AET2>/<before>]<type>");
			}
			aets = StringUtils.split(s.substring(skip+1,pos),'|');
			before = RetryIntervalls.parseInterval( s.substring(pos+1,pos1));
			type = s.substring(pos1+1);
			log.debug("new AETCompressionInfo for:"+s.substring(skip+1,pos));
		}

		/**
		 * @return
		 */
		public String[] getAETs() {
			return aets;
		}
		/**
		 * @return Returns the before.
		 */
		public long getBefore() { 
			return before;
		}
		/**
		 * @return Returns the type.
		 */
		public String getType() {
			return type;
		}
		
		public String toString() {
			return "[aet="+StringUtils.toString( aets, '|')+"/"+before+"]"+type;
		}
	}
}