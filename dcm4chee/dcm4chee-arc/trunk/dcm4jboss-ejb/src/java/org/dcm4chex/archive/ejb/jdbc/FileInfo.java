/*
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
/* 
 * File: $Source$
 * Author: gunter
 * Date: 30.07.2003
 * Time: 16:33:21
 * CVS Revision: $Revision$
 * Last CVS Commit: $Date$
 * Author of last CVS Commit: $Author$
 */
package org.dcm4chex.archive.ejb.jdbc;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;

import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmDecodeParam;
import org.dcm4cheri.util.DatasetUtils;

/**
 * @author <a href="mailto:gunter@tiani.com">Gunter Zeilinger</a>
 *
 */
public class FileInfo
{
    public final byte[] patAttrs;
    public final byte[] studyAttrs;
    public final byte[] seriesAttrs;
    public final byte[] instAttrs;
    public final String sopIUID;
    public final String sopCUID;
    public final String retrieveAET;
    public final String uri;
    public final String fpath;
    public final String tsUID;
    public final String md5;
    public final long size;
    public final int status;
    public final String fsIUID;

    public FileInfo(
        byte[] patAttrs,
        byte[] studyAttrs,
        byte[] seriesAttrs,
        byte[] instAttrs,
        String sopIUID,
        String sopCUID,
        String retrieveAET,
        String uri,
        String fpath,
        String tsUID,
        String md5,
        long size,
        int status,
        String fsIUID)
    {
        this.patAttrs = patAttrs;
        this.studyAttrs = studyAttrs;
        this.seriesAttrs = seriesAttrs;
        this.instAttrs = instAttrs;
        this.sopIUID = sopIUID;
        this.sopCUID = sopCUID;
        this.retrieveAET = retrieveAET;
        this.uri = uri;
        this.fpath = fpath;
        this.tsUID = tsUID;
        this.md5 = md5;
        this.size = size;
        this.status = status;
        this.fsIUID = fsIUID;
    }

    public String toString()
    {
        return "FileInfo[iuid="
            + sopIUID
            + ", cuid="
            + sopCUID
            + ", aet="
            + retrieveAET
            + ", uri="
            + uri
            + ", fpath="
            + fpath
            + ", tsuid="
            + tsUID
            + ", status="
            + status;
    }

    public File toFile()
    {
        try
        {
            URI tmp = new URI(uri);
            String myHost = InetAddress.getLocalHost().getHostName();
            if (!"file".equalsIgnoreCase(tmp.getScheme())
                || !myHost.equalsIgnoreCase(tmp.getHost()))
            {
                throw new IllegalStateException("" + this);
            }
            return new File(new URI("file:" + tmp.getPath() + fpath));
        } catch (URISyntaxException e)
        {
            throw new IllegalStateException("" + this);
        } catch (IllegalArgumentException e)
        {
            throw new IllegalStateException("" + this);
        } catch (UnknownHostException e)
        {
            throw new RuntimeException(e);
        }
    }

    public Dataset getPatientAttrs() throws IOException
    {
        return DatasetUtils.fromByteArray(patAttrs, DcmDecodeParam.IVR_LE);
    }

    public Dataset getStudyAttrs() throws IOException
    {
        return DatasetUtils.fromByteArray(studyAttrs, DcmDecodeParam.IVR_LE);
    }

    public Dataset getSeriesAttrs() throws IOException
    {
        return DatasetUtils.fromByteArray(seriesAttrs, DcmDecodeParam.IVR_LE);
    }

    public Dataset getInstanceAttrs() throws IOException
    {
        return DatasetUtils.fromByteArray(instAttrs, DcmDecodeParam.IVR_LE);
    }

}
