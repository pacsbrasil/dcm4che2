/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is part of dcm4che, an implementation of DICOM(TM) in
 * Java(TM), available at http://sourceforge.net/projects/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * TIANI Medgraph AG.
 * Portions created by the Initial Developer are Copyright (C) 2003-2005
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Gunter Zeilinger <gunter.zeilinger@tiani.com>
 * Franz Willer <franz.willer@gwi-ag.com>
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 *
 * ***** END LICENSE BLOCK ***** */

package org.dcm4chex.archive.util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import javax.imageio.stream.FileImageInputStream;

import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmDecodeParam;
import org.dcm4che.data.DcmEncodeParam;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.data.DcmParser;
import org.dcm4che.data.DcmParserFactory;
import org.dcm4che.data.FileFormat;
import org.dcm4che.dict.Tags;
import org.dcm4che.dict.UIDs;
import org.dcm4che.dict.VRs;
import org.dcm4che.net.DataSource;
import org.dcm4che.util.BufferedOutputStream;
import org.dcm4chex.archive.codec.DecompressCmd;
import org.dcm4chex.archive.ejb.jdbc.FileInfo;
import org.jboss.logging.Logger;

/**
 * @author Gunter.Zeilinger@tiani.com
 * @version $Revision$
 * @since 18.09.2003
 */
public class FileDataSource implements DataSource {

    private final FileInfo fileInfo;
    private final byte[] buffer;
	private final Logger log;
	
	/** if true use Dataset.writeFile instead of writeDataset */
	private boolean writeFile = false;
	private boolean withoutPixeldata = false;

    // buffer == null => send no Pixeldata
    public FileDataSource(Logger logger, FileInfo fileInfo, byte[] buffer) {
    	if ( logger != null ) {
    		this.log = logger;
    	} else {
    		this.log = Logger.getLogger(FileDataSource.class);
    	}
        this.fileInfo = fileInfo;
        this.buffer = buffer;
    }
    
	public void setWithoutPixeldata(boolean withoutPixeldata) {
		this.withoutPixeldata = withoutPixeldata;
	}

	/**
	 * @return Returns the writeFile.
	 */
	public boolean isWriteFile() {
		return writeFile;
	}
	/**
	 * Set the write method (file or net).
	 * <p>
	 * If true, this datasource use writeFile instead of writeDataset. 
	 * Therefore the FileMetaInfo will be only written if writeFile is set to true explicitly!
	 * 
	 * @param writeFile The writeFile to set.
	 */
	public void setWriteFile(boolean writeFile) {
		this.writeFile = writeFile;
	}
    /**
     * 
     * @param out
     * @param tsUID
     * @param writeFile
     * @throws IOException
     */
    public void writeTo(OutputStream out, String tsUID) throws IOException {
        
        File file = FileUtils.toFile(fileInfo.basedir, fileInfo.fileID);
        log.info("M-READ file:" + file);
        FileImageInputStream fiis = new FileImageInputStream(file);
        try {
            DcmParser parser = DcmParserFactory.getInstance().newDcmParser(fiis);
            Dataset ds = DcmObjectFactory.getInstance().newDataset();
            parser.setDcmHandler(ds.getDcmHandler());
            parser.parseDcmFile(FileFormat.DICOM_FILE, Tags.PixelData);
            updateAttrs(ds, fileInfo.patAttrs);
            updateAttrs(ds, fileInfo.studyAttrs);
            updateAttrs(ds, fileInfo.seriesAttrs);
            updateAttrs(ds, fileInfo.instAttrs);
            String tsOrig = null;
            if ( writeFile && ds.getFileMetaInfo() != null ) {
            	tsOrig = ds.getFileMetaInfo().getTransferSyntaxUID();
            	if ( tsUID != null ) {
            		if ( tsUID.equals( UIDs.ExplicitVRLittleEndian) || ! tsUID.equals( tsOrig ) ) { //can only decompress here!
            			tsUID = UIDs.ExplicitVRLittleEndian;
            			ds.setFileMetaInfo( DcmObjectFactory.getInstance().newFileMetaInfo(ds, tsUID));
            		}
            	} else {
            		tsUID = tsOrig;
            	}
            }
            DcmEncodeParam enc = DcmEncodeParam.valueOf(tsUID);
        	BufferedOutputStream bos = new BufferedOutputStream(out, buffer);
        	try {
	            if (withoutPixeldata || parser.getReadTag() != Tags.PixelData) {
					log.debug("Dataset:\n");
					log.debug(ds);
					if ( writeFile )
						ds.writeFile(bos, enc);
					else
						ds.writeDataset(bos, enc);
	                return;                
	            }
	            int len = parser.getReadLength();
	            if (len == -1 && !enc.encapsulated) {
	                DecompressCmd cmd = new DecompressCmd(ds, tsOrig, parser);
	                len = cmd.getPixelDataLength();
					log.debug("Dataset:\n");
					log.debug(ds);
					if ( writeFile )
						ds.writeFile(bos, enc);
					else
						ds.writeDataset(bos, enc);
	                ds.writeHeader(bos, enc, Tags.PixelData, VRs.OW, (len+1)&~1);
	                try {
		                cmd.decompress(enc.byteOrder, bos);
					} catch (IOException e) {
					    throw e;
					} catch (Throwable e) {
					    throw new RuntimeException("Decompression failed:", e);
					}
					if ((len&1)!=0)
	                    bos.write(0);
	            } else {
					log.debug("Dataset:\n");
					log.debug(ds);
					if ( writeFile )
						ds.writeFile(bos, enc);
					else
						ds.writeDataset(bos, enc);
	                ds.writeHeader(bos, enc, Tags.PixelData, VRs.OB, len);
	                if (len == -1) {
			            parser.parseHeader();
			            int itemlen;
		                while (parser.getReadTag() == Tags.Item) {
		                    itemlen = parser.getReadLength();
		                    ds.writeHeader(bos, enc, Tags.Item, VRs.NONE, itemlen);
		                    bos.copyFrom(fiis, itemlen);
		                    parser.parseHeader();
		                }
		                ds.writeHeader(bos, enc, Tags.SeqDelimitationItem,
		                        VRs.NONE, 0);
		            } else {
		            	bos.copyFrom(fiis, len);
		            }
	            }
	            parser.parseDataset(parser.getDcmDecodeParam(), -1);
	            ds.subSet(Tags.PixelData, -1).writeDataset(bos, enc);
        	} finally {
        		bos.flush();
        	}
        } finally {
            try {
                fiis.close();
            } catch (IOException ignore) {
            }
        }
    }

    private void updateAttrs(Dataset ds, byte[] attrs) throws IOException {
        ByteArrayInputStream bis = new ByteArrayInputStream(attrs);
        DcmParser parser = DcmParserFactory.getInstance().newDcmParser(bis);
        parser.setDcmHandler(ds.getDcmHandler());
        parser.parseDataset(DcmDecodeParam.EVR_LE, -1);
        bis.close();
    }
}
