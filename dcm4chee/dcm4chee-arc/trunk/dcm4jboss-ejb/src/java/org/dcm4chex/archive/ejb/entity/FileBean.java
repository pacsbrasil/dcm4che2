/* $Id$
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
package org.dcm4chex.archive.ejb.entity;

import javax.ejb.CreateException;
import javax.ejb.EntityBean;
import javax.ejb.RemoveException;

import org.apache.log4j.Logger;
import org.dcm4chex.archive.ejb.interfaces.InstanceLocal;

/**
 * @ejb.bean
 *  name="File"
 *  type="CMP"
 *  view-type="local"
 *  primkey-field="pk"
 *  local-jndi-name="ejb/File"
 * 
 * @ejb.transaction 
 *  type="Required"
 * 
 * @ejb.persistence
 *  table-name="file"
 * 
 * @jboss.entity-command
 *  name="postgresql-fetch-seq"
 *
 * @ejb.finder
 *  signature="java.util.Collection findAll()"
 *  query="SELECT OBJECT(a) FROM File AS a"
 *  transaction-type="Supports"
 *
 * @author <a href="mailto:gunter@tiani.com">Gunter Zeilinger</a>
 * @version $Revision$ $Date$
 *
 */
public abstract class FileBean implements EntityBean
{

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
     * Hostname.
     *
     * @ejb.interface-method
     * @ejb.persistence
     *  column-name="hostname"
     */
    public abstract String getHostName();
    public abstract void setHostName(String hostname);

    /**
     * Base Directory (mount point).
     *
     * @ejb.interface-method
     * @ejb.persistence
     *  column-name="basedir"
     */
    public abstract String getBaseDir();
    public abstract void setBaseDir(String basedir);

    /**
     * File ID (relative path to Base Directory).
     *
     * @ejb.interface-method
     * @ejb.persistence
     *  column-name="file_id"
     */
    public abstract String getFileId();
    public abstract void setFileId(String fileid);

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
    public byte[] getFileMd5()
    {
        char[] md5Hex = getFileMd5Field().toCharArray();
        byte[] md5 = new byte[16];
        for (int i = 0; i < md5.length; i++)
        {
            md5[i] =
                (byte) ((Character.digit(md5Hex[i >> 1], 16) << 4)
                    + Character.digit(md5Hex[(i >> 1) + 1], 16));
        }
        return md5;
    }

    public void setFileMd5(byte[] md5)
    {
        if (md5.length != 16)
        {
            throw new IllegalArgumentException("md5.length=" + md5.length);
        }
        char[] md5Hex = new char[32];
        for (int i = 0; i < md5.length; i++)
        {
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
    public abstract int getFileSize();
    public abstract void setFileSize(int size);

    /**
     * @ejb.relation
     *  name="instance-file"
     *  role-name="file-of-instance"
     *
     * @jboss:relation
     *  fk-column="instance_fk"
     *  related-pk-field="pk"
     * 
     * @ejb.interface-method view-type="local"
     */
    public abstract void setInstance(InstanceLocal inst);

    /**
     * @ejb.interface-method view-type="local"
     * 
     */
    public abstract InstanceLocal getInstance();

    /**
     * 
     * @ejb.interface-method
     */
    public String asString()
    {
        return prompt();
    }

    private String prompt()
    {
        return "File[pk="
            + getPk()
            + ", host="
            + getHostName()
            + ", dir="
            + getBaseDir()
            + ", id="
            + getFileId()
            + ", tsuid="
            + getFileTsuid()
            + ", inst->"
            + getInstance()
            + "]";
    }

    /**
     * Create file.
     *
     * @ejb.create-method
     */
    public Integer ejbCreate(
        String hostname,
        String basedir,
        String fileid,
        String tsuid,
        int size,
        byte[] md5,
        InstanceLocal instance)
        throws CreateException
    {
        setHostName(hostname);
        setBaseDir(basedir);
        setFileId(fileid);
        setFileTsuid(tsuid);
        setFileSize(size);
        setFileMd5(md5);
        return null;
    }

    public void ejbPostCreate(
        String hostname,
        String basedir,
        String fileid,
        String tsuid,
        int size,
        byte[] md5,
        InstanceLocal instance)
        throws CreateException
    {
        setInstance(instance);
        log.info("Created " + prompt());
    }

    public void ejbRemove() throws RemoveException
    {
        log.info("Deleting " + prompt());
    }
}
