/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/
package org.dcm4chex.archive.mbean;

import java.io.File;
import java.rmi.RemoteException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import javax.ejb.FinderException;
import javax.management.Notification;
import javax.management.NotificationListener;

import org.dcm4che.dict.UIDs;
import org.dcm4cheri.util.StringUtils;
import org.dcm4chex.archive.codec.CodecCmd;
import org.dcm4chex.archive.codec.CompressCmd;
import org.dcm4chex.archive.codec.DecompressCmd;
import org.dcm4chex.archive.config.RetryIntervalls;
import org.dcm4chex.archive.ejb.interfaces.FileDTO;
import org.dcm4chex.archive.ejb.interfaces.FileSystemDTO;
import org.dcm4chex.archive.ejb.interfaces.FileSystemMgt;
import org.dcm4chex.archive.ejb.interfaces.FileSystemMgtHome;
import org.dcm4chex.archive.util.EJBHomeFactory;
import org.dcm4chex.archive.util.FileUtils;

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
	private int maxFiles;
	private boolean checkCompression;
    
	private List aetCompressInfoList = new ArrayList();
	
	
	private static final String[] COMPRESSABLE_TRANSFER_SYNTAX = new String[]{UIDs.ImplicitVRLittleEndian, UIDs.ExplicitVRLittleEndian};
	private static final String[] CODEC_NAMES = new String[]{"JPEG_LL", "JPEG_LSLL", "JPEG2000_LL"};
	private static final String[] COMPRESS_TRANSFER_SYNTAX = new String[]{UIDs.JPEGLossless, UIDs.JPEGLSLossless, UIDs.JPEG2000Lossless};
	
    private final NotificationListener delayedCompressionListener = 
        new NotificationListener(){
            public void handleNotification(Notification notif, Object handback) {
            	Calendar cal = Calendar.getInstance();
            	int hour = cal.get(Calendar.HOUR_OF_DAY);
            	if ( isDisabled( hour ) ) {
                	if ( log.isDebugEnabled() ) log.debug("delayed compression cycle ignored in time between "+disabledStartHour+" and "+disabledEndHour+" !");
            	} else {
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
    	this.aetCompressInfoList.clear();
    	if ( compressAfter == null || compressAfter.trim().length() < 1 ) return;
    	StringTokenizer st = new StringTokenizer( compressAfter, "," );
    	while ( st.hasMoreTokens() ) {
    		aetCompressInfoList.add( new AETCompressionInfo( st.nextToken() ) );
    	}
    }

    public final String getCompressAfter() {
    	StringBuffer sb = new StringBuffer();
    	Iterator iter = this.aetCompressInfoList.iterator();
    	if ( iter.hasNext() ) sb.append( ( (AETCompressionInfo) iter.next()).toString() );
    	while ( iter.hasNext() ) {
    		sb.append(",").append( ( (AETCompressionInfo) iter.next()).toString() );
    	}
        return sb.toString();
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
	 * @return Returns the checkCompression.
	 */
	public boolean isCheckCompression() {
		return checkCompression;
	}
	/**
	 * @param checkCompression The checkCompression to set.
	 */
	public void setCheckCompression(boolean checkCompression) {
		this.checkCompression = checkCompression;
	}
    public final int getMaxConcurrentCodec() {
        return CodecCmd.getMaxConcurrentCodec();
    }
    
    public final void setMaxConcurrentCodec(int maxConcurrentCodec) {
        CodecCmd.setMaxConcurrentCodec(maxConcurrentCodec);
    }
	

    /**
     * @throws FinderException
     * @throws RemoteException
	 * 
	 */
	public void startDelayedCompression() throws RemoteException, FinderException {
		log.info("Delayed compression started!");
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
					doCompress( files, info );
					limit -= files.length;
					if ( limit < 1 ) break;
				} else {
					log.info( "No files to compress for "+info+" on FS:"+fs[i] );
				}
			}
		
		}
	}

	/**
	 * @param files
	 * @param info
	 */
	private void doCompress(FileDTO[] files, AETCompressionInfo info) {
		if ( files.length < 1 ) return;
		File srcFile,destFile;
		String destPath;
		File tmpDir = FileUtils.toFile("tmp", "checks");// tmp directory in ServerHomeDir
		tmpDir.mkdir();
		File tmpFile;
		int[] ia = new int[1];
		byte[] md5;
		for ( int i = 0, len = files.length ; i < len ; i++ ) {
			srcFile = FileUtils.toFile(files[i].getDirectoryPath(), files[i].getFilePath());
			destFile = getDestFile( srcFile );
			if ( log.isDebugEnabled() ) log.debug( "Compress file "+srcFile+" to "+destFile+" with CODEC:"+info.getCodec()+"("+info.getTransferSyntax()+")" );
			try {
				md5 = CompressCmd.compressFile( srcFile, destFile, info.getTransferSyntax(), ia );
				boolean check = true;
				if ( checkCompression ) {
					tmpFile = File.createTempFile("check",null,tmpDir);
					tmpFile.deleteOnExit();
					byte[] dec_md5 = DecompressCmd.decompressFile( destFile, tmpFile, files[i].getFileTsuid(), ia[0]);
					if ( ! Arrays.equals( dec_md5, files[i].getFileMd5() ) ) {
						log.warn("File MD5 check failed for src file "+srcFile+"! Check pixel matrix now.");
						if ( ! FileUtils.equalsPixelData( srcFile, tmpFile) ) {
							check = false;
						}
					}
					if ( tmpFile.exists() ) tmpFile.delete();
				}
				if ( check ) {
					destPath = new File (files[i].getFilePath()).getParent()+File.separatorChar+destFile.getName();
					if ( log.isDebugEnabled() ) log.debug("replaceFile "+srcFile+" with "+destFile+" ! destPath:"+destPath);
					lookupFileSystemMgt().replaceFile( files[i].getPk(),
								destPath, info.getTransferSyntax(), (int)destFile.length(), md5 );
				} else {
					log.error("Pixel matrix of compressed file differs from original ("+srcFile+")! compressed file removed!");
					destFile.delete();
					lookupFileSystemMgt().setFileStatus( files[i].getPk(), FileDTO.FAILED_TO_CHECK );
				}
			} catch ( Exception x ) {
				log.error( "Can't compress file:"+srcFile, x );
				if ( destFile.exists() ) destFile.delete();
				try {
					lookupFileSystemMgt().setFileStatus( files[i].getPk(), FileDTO.FAILED_TO_COMPRESS );
				} catch ( Exception x1 ) {
					log.error("Failed to set FAILED_TO_COMPRESS for file "+srcFile );
				}
			}
		}
		
	}


	/**
	 * @param srcFile
	 * @return
	 */
	private File getDestFile(File src) {
		File path = src.getParentFile();
		long fnAsInt = Long.parseLong( src.getName(), 16 );
		File f = new File( path, Long.toHexString(++fnAsInt).toUpperCase());
		while ( f.exists() ) {
			f = new File( path, Long.toHexString(++fnAsInt).toUpperCase());
		}
		return f;
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
		int type;
		
		public AETCompressionInfo( String s ) {
			int skip = s.indexOf('=');
			int pos = s.indexOf('/');
			int pos1 = s.indexOf(']');
			if ( skip == -1 || pos == -1 || pos1 == -1 ) {
				throw new IllegalArgumentException("Wrong format! Use [aet=<AET>|<AET2>/<before>]<type>");
			}
			aets = StringUtils.split(s.substring(skip+1,pos),'|');
			before = RetryIntervalls.parseInterval( s.substring(pos+1,pos1));
			type = Arrays.asList(CODEC_NAMES).indexOf( s.substring(pos1+1).trim() );
			if ( type == -1 ) throw new IllegalArgumentException("Wrong CODEC name "+s.substring(pos1+1).trim()+"! Use JPEG_LL, JPEG_LSLL or JPEG2000_LL!");
		}

		/**
		 * @return
		 */
		public String getCodec() {
			return CODEC_NAMES[type];
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
		public String getTransferSyntax() {
			return COMPRESS_TRANSFER_SYNTAX[type];
		}
		
		public String toString() {
			return "[aet="+StringUtils.toString( aets, '|')+"/"+RetryIntervalls.formatInterval(before)+"]"+getCodec();
		}
	}
}