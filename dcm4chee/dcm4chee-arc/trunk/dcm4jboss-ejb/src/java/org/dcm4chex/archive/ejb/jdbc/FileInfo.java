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
import java.net.URI;

/**
 * @author <a href="mailto:gunter@tiani.com">Gunter Zeilinger</a>
 *
 */
public class FileInfo {
    public final byte[] patAttrs;
    public final String sopIUID;
    public final String sopCUID;
    public final String host;
    public final String mnt;
    public final String fpath;
    public final String tsUID;
    public final String md5;
    public final long size;
    public final int status;
    public final String fsIUID;

    public FileInfo(
        byte[] patAttrs,
        String sopIUID,
        String sopCUID,
        String host,
        String mnt,
        String fpath,
        String tsUID,
        String md5,
        long size,
        int status,
        String fsIUID) {
        this.patAttrs = patAttrs;
        this.sopIUID = sopIUID;
        this.sopCUID = sopCUID;
        this.host = host;
        this.mnt = mnt;
        this.fpath = fpath;
        this.tsUID = tsUID;
        this.md5 = md5;
        this.size = size;
        this.status = status;
        this.fsIUID = fsIUID;
    }

    public File toFile() {
        try {
            return new File(new URI("file:" + mnt + "/" + fpath));
        } catch (Exception e) {
            throw new IllegalStateException("" + this);
        }

    }

}
