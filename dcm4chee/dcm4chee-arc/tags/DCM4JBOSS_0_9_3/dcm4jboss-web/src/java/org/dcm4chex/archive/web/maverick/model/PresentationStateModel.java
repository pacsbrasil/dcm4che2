/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/
package org.dcm4chex.archive.web.maverick.model;

import org.dcm4che.data.Dataset;
import org.dcm4che.dict.Tags;

/**
 * @author gunter.zeilinger@tiani.com
 * @version $Revision$ $Date$
 * @since 05.10.2004
 *
 */
public class PresentationStateModel extends InstanceModel {

    public PresentationStateModel(Dataset ds) {
        super(ds);
    }

    public final String getPresentationCreationDateTime() {
        return getDateTime(Tags.PresentationCreationDate,
                Tags.PresentationCreationTime);
    }

    public final String getPresentationCreatorName() {
        return ds.getString(Tags.PresentationCreatorName);
    }

    public final String getPresentationDescription() {
        return ds.getString(Tags.ContentDescription);
    }

    public final String getPresentationLabel() {
        return ds.getString(Tags.ContentLabel);
    }
}