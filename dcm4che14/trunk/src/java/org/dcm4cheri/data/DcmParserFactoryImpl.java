/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is part of dcm4che, an implementation of DICOM(TM) in
 * Java(TM), hosted at http://sourceforge.net/projects/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * TIANI Medgraph AG.
 * Portions created by the Initial Developer are Copyright (C) 2002-2005
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Gunter Zeilinger <gunter.zeilinger@tiani.com>
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 *
 * ***** END LICENSE BLOCK ***** */

package org.dcm4cheri.data;

import java.io.InputStream;
import java.lang.reflect.Constructor;

import javax.imageio.stream.ImageInputStream;

import org.apache.log4j.Logger;
import org.dcm4che.data.DcmParser;

/**
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0.0
 */
public final class DcmParserFactoryImpl
        extends org.dcm4che.data.DcmParserFactory {

    private static Logger log = Logger.getLogger(DcmParserFactoryImpl.class);
    
    private static final String FILTER_INPUT_STREAM = 
            "org.dcm4che.data.FilterInputStream";
    private static final String FILTER_IMAGE_INPUT_STREAM = 
            "org.dcm4che.data.FilterImageInputStream";

    private final Constructor filterInputStreamConstructor;
    private final Constructor filterImageInputStreamConstructor;
    
    /** Creates a new instance of DcmParserFactoryImpl */
    public DcmParserFactoryImpl() {
        filterInputStreamConstructor = 
                getConstructor(FILTER_INPUT_STREAM, InputStream.class);
        filterImageInputStreamConstructor =
                getConstructor(FILTER_IMAGE_INPUT_STREAM, ImageInputStream.class);
    }
    
    private static Constructor getConstructor(String key, Class initParamType) {
        try {
            String className = System.getProperty(key);
            if (className != null) {
                Class clazz = Class.forName(className);
                return clazz.getConstructor(new Class[] { initParamType });
            }
        } catch (Exception e) {
            log.warn("Failed to configure input stream filter", e);
        }
        return null;
    }

    public DcmParser newDcmParser(InputStream in) {
        if (filterInputStreamConstructor != null) {
            try {
                in = (InputStream) filterInputStreamConstructor
                        .newInstance(new Object[] { in });
            } catch (Exception e) {
                log.warn("Failed to initialize input stream filter", e);
            }
        }
        return new DcmParserImpl(in);
    }    

    public DcmParser newDcmParser(ImageInputStream in) {
        if (filterImageInputStreamConstructor != null) {
            try {
                in = (ImageInputStream) filterImageInputStreamConstructor
                        .newInstance(new Object[] { in });
            } catch (Exception e) {
                log.warn("Failed to initialize input stream filter", e);
            }
        }
        return new DcmParserImpl(in);
    }    

}
