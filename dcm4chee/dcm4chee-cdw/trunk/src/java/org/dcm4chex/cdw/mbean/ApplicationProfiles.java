/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/
package org.dcm4chex.cdw.mbean;

import java.util.HashMap;
import java.util.Properties;

/**
 * @author gunter.zeilinger@tiani.com
 * @version $Revision$ $Date$
 * @since 05.07.2004
 */
class ApplicationProfiles {
    Properties uriForProfile = new Properties();
    HashMap profiles = new HashMap();
    
    public ApplicationProfiles(MediaComposerService service) {
    }
    
    public ApplicationProfile getApplicationProfile(String name) {
        return (ApplicationProfile) profiles.get(name);
    }
}
