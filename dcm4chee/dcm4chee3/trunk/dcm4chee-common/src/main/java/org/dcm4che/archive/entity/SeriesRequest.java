/*
 * org.dcm4che.archive.entity.SeriesRequest.java
 * Created on May 27, 2007 by damien
 * Copyright 2007, QNH, Inc. info@qualitynighthawk.com, All rights reserved
 */
package org.dcm4che.archive.entity;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * org.dcm4che.archive.entity.SeriesRequest
 * 
 * @author <a href="mailto:damien.daddy@gmail.com">Damien Evans</a>
 */
@Entity
@Table(name = "series_req")
public class SeriesRequest extends EntityBase{

    private static final long serialVersionUID = 5657492869941909703L;

    @ManyToOne
    @JoinColumn(name = "series_fk")
    private Series series;
    
    /**
     * 
     */
    public SeriesRequest() {
    }

    public Series getSeries() {
        return series;
    }

    public void setSeries(Series series) {
        this.series = series;
    }
}