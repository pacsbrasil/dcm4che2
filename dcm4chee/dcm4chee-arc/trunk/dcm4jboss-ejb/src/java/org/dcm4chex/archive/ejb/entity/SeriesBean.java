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
import org.dcm4che.data.Dataset;
import org.dcm4che.dict.Tags;
import org.dcm4chex.archive.ejb.interfaces.StudyLocal;
import org.dcm4chex.archive.ejb.util.DatasetUtil;

/**

/**
 * @ejb:bean
 *  name="Series"
 *  type="CMP"
 *  view-type="local"
 *  primkey-field="pk"
 *  local-jndi-name="ejb/Series"
 * 
 * @ejb:transaction 
 *  type="Required"
 * 
 * @ejb.persistence
 *  table-name="series"
 * 
 * @jboss.entity-command
 *  name="get-last-oid"
 *  class="org.jboss.ejb.plugins.cmp.jdbc.postgres.JDBCPostgresCreateCommand"
 * 
 * @ejb.finder
 *  signature="Collection findAll()"
 *  query="SELECT OBJECT(a) FROM Series AS a"
 *  transaction-type="NotSupported"
 *
 * @ejb.finder
 *  signature="org.dcm4chex.archive.ejb.interfaces.SeriesLocal findBySeriesIuid(java.lang.String uid)"
 *  query="SELECT OBJECT(a) FROM Series AS a WHERE a.seriesIuid = ?1"
 *  transaction-type="NotSupported"
 *
 * @author <a href="mailto:gunter@tiani.com">Gunter Zeilinger</a>
 *
 */
public abstract class SeriesBean implements EntityBean {

    private static final String ATTRS_CFG = "series-attrs.cfg";

    private Logger log = Logger.getLogger(SeriesBean.class);
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
     * Series Instance UID
     *
     * @ejb.interface-method
     * @ejb.persistence
     *  column-name="series_iuid"
     */
    public abstract String getSeriesIuid();

    public abstract void setSeriesIuid(String uid);

    /**
     * Series Number
     *
     * @ejb.interface-method
     * @ejb.persistence
     *  column-name="series_no"
     */
    public abstract String getSeriesNumber();

    public abstract void setSeriesNumber(String no);
    
    /**
     * Modality
     *
     * @ejb.interface-method
     * @ejb.persistence
     *  column-name="modality"
     */
    public abstract String getModality();

    public abstract void setModality(String md);
    
    /**
     * PPS Start Datetime
     *
     * @ejb.interface-method
     * @ejb.persistence
     *  column-name="pps_start"
     */
    public abstract java.util.Date getPpsStartDateTime();

    public abstract void setPpsStartDateTime(java.util.Date datetime);
    
    /**
     * Encoded Series Dataset
     *
     * @ejb.persistence
     *  column-name="series_attrs"
     * 
     */
    public abstract byte[] getEncodedAttributes();

    public abstract void setEncodedAttributes(byte[] attr);

    /**
     * @ejb:relation
     *  name="study-series"
     *  role-name="series-of-study"
     *  cascade-delete="yes"
     *
     * @jboss:relation
     *  fk-column="study_fk"
     *  related-pk-field="pk"
     * 
     * @param study study of this series
     */
    public abstract void setStudy(StudyLocal study);
    
    /**
     * @ejb:interface-method view-type="local"
     * 
     * @return study of this series
     */
    public abstract StudyLocal getStudy();
    
    /**
     * @ejb:interface-method view-type="local"
     *
     * @param series all instances of this series
     */
    public abstract void setInstances(java.util.Collection series);

    /**
     * @ejb:interface-method view-type="local"
     * @ejb:relation
     *  name="series-instance"
     *  role-name="series-has-instance"
     *    
     * @return all instances of this series
     */
    public abstract java.util.Collection getInstances();

    /**
     * Create study.
     *
     * @ejb.create-method
     */
    public Integer ejbCreate(Dataset ds, StudyLocal study) throws CreateException {
        setSeriesIuid(ds.getString(Tags.SeriesInstanceUID));
        setSeriesNumber(ds.getString(Tags.SeriesNumber));
        setModality(ds.getString(Tags.Modality));
        setPpsStartDateTime(ds.getDateTime(Tags.PPSStartDate, Tags.PPSStartTime));
        setEncodedAttributes(DatasetUtil.toByteArray(ds.subSet(DatasetUtil.getFilter(ATTRS_CFG))));
        return null;
    }

    public void ejbPostCreate(Dataset ds, StudyLocal study) throws CreateException {
        setStudy(study);
        log.info("Created " + prompt());
    }

    public void ejbRemove() throws RemoveException {
        log.info("Deleting " + prompt());
    }
    
    /**
     * @ejb:interface-method
     */
    public Dataset getAttributes()
    {
        return DatasetUtil.fromByteArray(getEncodedAttributes());
    }

    /**
     * 
     * @ejb.interface-method
     */
    public String asString()
    {
        return prompt();
    }

    private String prompt() {
        return "Series[pk="
            + getPk()
            + ", uid="
            + getSeriesIuid()
            + ", study->"
            + getStudy()
            + "]";
    }
}
