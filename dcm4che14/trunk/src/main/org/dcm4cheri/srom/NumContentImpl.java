/*$Id$*/
/*****************************************************************************
 *                                                                           *
 *  Copyright (c) 2001,2002 by TIANI MEDGRAPH AG <gunter.zeilinger@tiani.com>*
 *                                                                           *
 *  This file is part of dcm4che.                                            *
 *                                                                           *
 *  This library is free software; you can redistribute it and/or modify it  *
 *  under the terms of the GNU Lesser General Public License as published    *
 *  by the Free Software Foundation; either version 2 of the License, or     *
 *  (at your option) any later version.                                      *
 *                                                                           *
 *  This library is distributed in the hope that it will be useful, but      *
 *  WITHOUT ANY WARRANTY; without even the implied warranty of               *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU        *
 *  Lesser General Public License for more details.                          *
 *                                                                           *
 *  You should have received a copy of the GNU Lesser General Public         *
 *  License along with this library; if not, write to the Free Software      *
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA  *
 *                                                                           *
 *****************************************************************************/

package org.dcm4cheri.srom;

import org.dcm4che.data.Dataset;
import org.dcm4che.dict.Tags;
import org.dcm4che.srom.*;
import java.util.Date;

/**
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0
 */
class NumContentImpl extends NamedContentImpl implements NumContent {
    // Constants -----------------------------------------------------
    
    // Attributes ----------------------------------------------------
    private float value;
    private Code unit;

    // Constructors --------------------------------------------------
    NumContentImpl(KeyObject owner, Date obsDateTime, Template template,
            Code name, float value, Code unit) {
        super(owner, obsDateTime, template, checkNotNull(name));
        this.value = value;
        this.unit = checkNotNull(unit);
    }
    
    Content clone(KeyObject newOwner,  boolean inheritObsDateTime) {
        return new NumContentImpl(newOwner,
                getObservationDateTime(inheritObsDateTime),
                template, name, value, unit);
    }

    // Methodes --------------------------------------------------------
    public final void setName(Code newName) {
        this.name = checkNotNull(name);
    }
    
    public String toString() {
        return prompt().append(value).append(unit).toString();
    }

    public final ValueType getValueType() {
        return ValueType.NUM;
    }    
    
    public final float getValue() {
        return value;
    }

    public final Code getUnit() {
        return unit;
    }

    public final void setValue(float value) {
        this.value = value;
    }

    public final void setUnit(Code unit) {
        this.unit = checkNotNull(unit);
    }

    public void toDataset(Dataset ds) {
        super.toDataset(ds);
        Dataset mv = ds.setSQ(Tags.MeasuredValueSeq).addNewDataset();
        mv.setDS(Tags.NumericValue, value);
        unit.toDataset(mv.setSQ(Tags.MeasurementUnitsCodeSeq).addNewDataset());
    }
}
