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
 * Date: 09.07.2003
 * Time: 09:08:46
 * CVS Revision: $Revision$
 * Last CVS Commit: $Date$
 * Author of last CVS Commit: $Author$
 */
package org.dcm4chex.archive.ejb.entity;

import javax.ejb.CreateException;
import javax.ejb.EntityBean;
import javax.ejb.RemoveException;

import org.apache.log4j.Logger;
import org.dcm4chex.archive.ejb.interfaces.InstanceLocal;
import org.dcm4chex.archive.ejb.interfaces.MediaLocal;
import org.dcm4chex.archive.ejb.interfaces.NodeLocal;

/**
 * @ejb:bean
 *  name="File"
 *  type="CMP"
 *  view-type="local"
 *  primkey-field="pk"
 *  local-jndi-name="ejb/File"
 * 
 * @ejb:transaction 
 *  type="Required"
 * 
 * @ejb.persistence
 *  table-name="file"
 * 
 * @jboss.entity-command
 *  name="get-last-oid"
 *  class="org.jboss.ejb.plugins.cmp.jdbc.postgres.JDBCPostgresCreateCommand"
 *
 * @ejb.finder
 *  signature="java.util.Collection findAll()"
 *  query="SELECT OBJECT(a) FROM File AS a"
 *  transaction-type="Supports"
 *
 * @author <a href="mailto:gunter@tiani.com">Gunter Zeilinger</a>
 *
 */
public abstract class FileBean implements EntityBean {

    private static final Logger log = Logger.getLogger(FileBean.class);

    /**
     * Auto-generated Primary Key
     *
     * @ejb.interface-method
     * @ejb.pk-field
     * @ejb.persistence
     *  column-name="pk"
     * @jboss.persistence
     *  auto-increment="true"
     *
     */
    public abstract Integer getPk();

    public abstract void setPk(Integer pk);

    /**
     * File Path relative to mount point
     *
     * @ejb.interface-method
     * @ejb.persistence
     *  column-name="file_path"
     */
    public abstract String getFilePath();
    public abstract void setFilePath(String path);

    /**
     * Transfer Syntax UID
     *
     * @ejb.interface-method
     * @ejb.persistence
     *  column-name="file_tsuid"
     */
    public abstract String getFileTsuid();
    public abstract void setFileTsuid(String tsuid);

    /**
     * MD5 checksum as hex string
     *
     * @ejb.interface-method
     * @ejb.persistence
     *  column-name="file_md5"
     */
    public abstract String getFileMd5Field();
    public abstract void setFileMd5Field(String md5);

    /**
     * MD5 checksum in binary format
     *
     * @ejb.interface-method
     */
    public byte[] getFileMd5() {
        char[] md5Hex = getFileMd5Field().toCharArray();
        byte[] md5 = new byte[16];
        for (int i = 0; i < md5.length; i++) {
            md5[i] =
                (byte) ((Character.digit(md5Hex[i >> 1], 16) << 4)
                    + Character.digit(md5Hex[(i >> 1) + 1], 16));
        }
        return md5;
    }

    public void setFileMd5(byte[] md5) {
        if (md5.length != 16) {
            throw new IllegalArgumentException("md5.length=" + md5.length);
        }
        char[] md5Hex = new char[32];
        for (int i = 0; i < md5.length; i++) {
            md5Hex[i << 1] = Character.forDigit((md5[i] >> 4) & 0xf, 16);
            md5Hex[(i << 1) + 1] = Character.forDigit(md5[i] & 0xf, 16);
        }
        setFileMd5Field(new String(md5Hex));
    }

    /**
     * File Size
     *
     * @ejb.interface-method
     * @ejb.persistence
     *  column-name="file_size"
     */
    public abstract long getFileSize();
    public abstract void setFileSize(long size);

    /**
     * File status
     *
     * @ejb.interface-method
     * @ejb.persistence
     *  column-name="file_status"
     */
    public abstract int getFileStatus();

    /**
     * @ejb.interface-method
     */
    public abstract void setFileStatus(int status);

    /**
     * @ejb:relation
     *  name="file-node"
     *  role-name="file-at-node"
     *  target-ejb="Node"
     *  target-role-name="node-of-file"
     *  target-multiple="yes"
     *
     * @jboss:relation
     *  fk-column="node_fk"
     *  related-pk-field="pk"
     * 
     * @param node node where file is located
     */
    public abstract void setNode(NodeLocal node);

    /**
     * @ejb:interface-method view-type="local"
     * 
     * @return node where file is located
     */
    public abstract NodeLocal getNode();

    /**
     * @ejb:relation
     *  name="instance-file"
     *  role-name="file-of-instance"
     *
     * @jboss:relation
     *  fk-column="instance_fk"
     *  related-pk-field="pk"
     * 
     * @ejb:interface-method view-type="local"
     */
    public abstract void setInstance(InstanceLocal inst);

    /**
     * @ejb:interface-method view-type="local"
     * 
     */
    public abstract InstanceLocal getInstance();

    /**
     * @ejb:relation
     *  name="media-file"
     *  role-name="file-on-media"
     *
     * @jboss:relation
     *  fk-column="media_fk"
     *  related-pk-field="pk"
     * 
     * @ejb:interface-method view-type="local"
     */
    public abstract void setMedia(MediaLocal media);

    /**
     * @ejb:interface-method view-type="local"
     * 
     */
    public abstract MediaLocal getMedia();

    /**
     * 
     * @ejb.interface-method
     */
    public String asString() {
        return prompt();
    }

    private String prompt() {
        return "File[pk="
            + getPk()
            + ", node->"
            + getNode()
            + ", path="
            + getFilePath()
            + ", status="
            + getFileStatus()
            + ", tsuid="
            + getFileTsuid()
            + ", media->"
            + getMedia()
            + ", instance->"
            + getInstance()
            + "]";
    }

    /**
     * Create file.
     *
     * @ejb.create-method
     */
    public Integer ejbCreate(
        NodeLocal node,
        String path,
        String tsuid,
        long size,
        byte[] md5,
        InstanceLocal instance)
        throws CreateException {
        if (node == null) {
            throw new NullPointerException("node");
        }
        setFilePath(path);
        setFileTsuid(tsuid);
        setFileSize(size);
        setFileMd5(md5);
        return null;
    }

    public void ejbPostCreate(
        NodeLocal node,
        String path,
        String tsuid,
        long size,
        byte[] md5,
        InstanceLocal instance)
        throws CreateException {
        setNode(node);
        setInstance(instance);
        log.info("Created " + prompt());
    }

    public void ejbRemove() throws RemoveException {
        log.info("Deleting " + prompt());
    }
}
