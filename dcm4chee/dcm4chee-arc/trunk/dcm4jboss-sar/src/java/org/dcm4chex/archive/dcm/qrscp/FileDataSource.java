/*
 * Copyright (c) 2002,2003 by TIANI MEDGRAPH AG
 * 
 * This file is part of dcm4che.
 * 
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation; either version 2 of the License, or (at your
 * option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */

package org.dcm4chex.archive.dcm.qrscp;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmDecodeParam;
import org.dcm4che.data.DcmEncodeParam;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.data.DcmParser;
import org.dcm4che.data.DcmParserFactory;
import org.dcm4che.data.FileFormat;
import org.dcm4che.dict.Tags;
import org.dcm4che.dict.VRs;
import org.dcm4che.net.DataSource;
import org.dcm4chex.archive.ejb.jdbc.FileInfo;

/**
 * @author Gunter.Zeilinger@tiani.com
 * @version $Revision$
 * @since 18.09.2003
 */
class FileDataSource implements DataSource {

    private static final DcmParserFactory parserFact =
        DcmParserFactory.getInstance();
    private static final DcmObjectFactory objFact =
        DcmObjectFactory.getInstance();

    private final QueryRetrieveScpService service;
    private final FileInfo fileInfo;
    private final byte[] buffer;

    public FileDataSource(
        QueryRetrieveScpService service,
        FileInfo fileInfo,
        byte[] buffer) {
        this.service = service;
        this.fileInfo = fileInfo;
        this.buffer = buffer;
    }

    public void writeTo(OutputStream out, String tsUID) throws IOException {
        DcmEncodeParam enc = DcmEncodeParam.valueOf(tsUID);
        File file = fileInfo.toFile();
        service.getLog().info("M-READ file:" + file);
        FileInputStream fis = new FileInputStream(file);
        BufferedInputStream bis = new BufferedInputStream(fis);
        try {
            DcmParser parser = parserFact.newDcmParser(bis);
            Dataset ds = objFact.newDataset();
            parser.setDcmHandler(ds.getDcmHandler());
            parser.parseDcmFile(FileFormat.DICOM_FILE, Tags.PixelData);
            updateAttrs(ds, fileInfo.patAttrs);
            updateAttrs(ds, fileInfo.studyAttrs);
            updateAttrs(ds, fileInfo.seriesAttrs);
            updateAttrs(ds, fileInfo.instAttrs);
            service.logDataset("Dataset:\n", ds);
            ds.writeDataset(out, enc);
            if (parser.getReadTag() != Tags.PixelData) return;            
            int len = parser.getReadLength();
            if (len == -1 && !enc.encapsulated) {
                decompress(ds, bis, out);
            } else {
                ds.writeHeader(out, enc, parser.getReadTag(), parser
                        .getReadVR(), len);
                if (len == -1) {
		            parser.parseHeader();
		            int itemlen;
	                while (parser.getReadTag() == Tags.Item) {
	                    itemlen = parser.getReadLength();
	                    ds.writeHeader(out, enc, Tags.Item, VRs.NONE, itemlen);
	                    copy(bis, out, itemlen, buffer);
	                    parser.parseHeader();
	                }
	                ds.writeHeader(out, enc, Tags.SeqDelimitationItem,
	                        VRs.NONE, 0);
	            } else {
	                copy(bis, out, len, buffer);
	            }
            }
            parser.parseDataset(parser.getDcmDecodeParam(), -1);
            ds.subSet(Tags.PixelData, -1).writeDataset(out, enc);
        } finally {
            try {
                bis.close();
            } catch (IOException ignore) {
            }
        }
    }

    private void decompress(Dataset ds, BufferedInputStream bis, OutputStream out) {
        throw new UnsupportedOperationException("decompression not yet implemented");        
    }

    private void copy(InputStream in, OutputStream out, int totLen,
            byte[] buffer) throws IOException {
        for (int len, toRead = totLen; toRead > 0; toRead -= len) {
            len = in.read(buffer, 0, Math.min(toRead, buffer.length));
            if (len == -1) { throw new EOFException(); }
            out.write(buffer, 0, len);
        }
    }

    /**
     * @param ds
     */
    private void updateAttrs(Dataset ds, byte[] attrs) throws IOException {
        ByteArrayInputStream bis = new ByteArrayInputStream(attrs);
        DcmParser parser = parserFact.newDcmParser(bis);
        parser.setDcmHandler(ds.getDcmHandler());
        parser.parseDataset(DcmDecodeParam.EVR_LE, -1);
        bis.close();
    }

}
