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
import javax.ejb.EJBException;
import javax.ejb.EntityBean;
import javax.ejb.EntityContext;
import javax.ejb.FinderException;
import javax.ejb.ObjectNotFoundException;
import javax.ejb.RemoveException;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.log4j.Logger;
import org.dcm4chex.archive.ejb.interfaces.DirectoryLocalHome;
import org.dcm4chex.archive.ejb.interfaces.InstanceLocal;
import org.dcm4chex.archive.ejb.interfaces.DirectoryLocal;

/**
 * @author <a href="mailto:gunter@tiani.com">Gunter Zeilinger</a>
 * @version $Revision$ $Date$
 *
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
 * @ejb.ejb-ref
 *  ejb-name="Directory" 
 *  view-type="local"
 *  ref-name="ejb/Directory"
 *
 */
public abstract class FileBean implements EntityBean
{

    private static final Logger log = Logger.getLogger(FileBean.class);

    private DirectoryLocalHome dirHome;

    public void setEntityContext(EntityContext ctx)
    {
        Context jndiCtx = null;
        try
        {
            jndiCtx = new InitialContext();
            dirHome =
                (DirectoryLocalHome) jndiCtx.lookup(
                    "java:comp/env/ejb/Directory");
        } catch (NamingException e)
        {
            throw new EJBException(e);
        } finally
        {
            if (jndiCtx != null)
            {
                try
                {
                    jndiCtx.close();
                } catch (NamingException ignore)
                {}
            }
        }
    }

    public void unsetEntityContext()
    {
        dirHome = null;
    }

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
     * File Path (relative path to Directory).
     *
     * @ejb.interface-method
     * @ejb.persistence
     *  column-name="filepath"
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
     * @ejb.relation
     *  name="directory-file"
     *  role-name="file-in-directory"
     *  target-ejb="Directory"
     *  target-role-name="directory-of-file"
     *  target-multiple="yes"
     *
     * @jboss:relation
     *  fk-column="directory_fk"
     *  related-pk-field="pk"
     * 
     * @param dir Directory of File
     */
    public abstract void setDirectory(DirectoryLocal dir);

    /**
     * @ejb.interface-method view-type="local"
     * 
     * @return Directory of File
     */
    public abstract DirectoryLocal getDirectory();
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
            + ", filepath="
            + getFilePath()
            + ", tsuid="
            + getFileTsuid()
            + ", inst->"
            + getInstance()
            + ", dir->"
            + getDirectory()
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
        String path,
        String tsuid,
        int size,
        byte[] md5,
        InstanceLocal instance)
        throws CreateException
    {
        setFilePath(path);
        setFileTsuid(tsuid);
        setFileSize(size);
        setFileMd5(md5);
        return null;
    }

    public void ejbPostCreate(
        String hostname,
        String basedir,
        String path,
        String tsuid,
        int size,
        byte[] md5,
        InstanceLocal instance)
        throws CreateException
    {
        setInstance(instance);
        setDirectory(getOrCreateDirectory(hostname, basedir));
        log.info("Created " + prompt());
    }

    private DirectoryLocal getOrCreateDirectory(
        String hostname,
        String basedir)
        throws CreateException
    {
        try
        {
            return dirHome.findByHostNameAndDirectoryPath(hostname, basedir);
        } catch (ObjectNotFoundException onfe)
        {
            return dirHome.create(hostname, basedir);
        } catch (FinderException e)
        {
            throw new EJBException(e);
        }

    }

    public void ejbRemove() throws RemoveException
    {
        log.info("Deleting " + prompt());
    }
}
