/*$Id$*/
/*****************************************************************************
 *                                                                           *
 *  Copyright (c) 2002 by TIANI MEDGRAPH AG <gunter.zeilinger@tiani.com>     *
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

package org.dcm4che.data;

import java.io.InputStream;
import javax.imageio.stream.ImageInputStream;

/**
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0.0
 */
public abstract class DcmParserFactory {

    public static DcmParserFactory getInstance() {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        String name = System.getProperty("dcm4che.data.DcmParserFactory",
                "org.dcm4cheri.data.DcmParserFactoryImpl");
        try {
            return (DcmParserFactory)loader.loadClass(name).newInstance();
        } catch (ClassNotFoundException ex) {
            throw new ConfigurationError("class not found: " + name, ex); 
        } catch (InstantiationException ex) {
            throw new ConfigurationError("could not instantiate: " + name, ex); 
        } catch (IllegalAccessException ex) {
            throw new ConfigurationError("could not instantiate: " + name, ex); 
        }
    }
    
    protected DcmParserFactory() {
    }
    
//    public abstract DcmParser newDcmParser();
    public abstract DcmParser newDcmParser(InputStream in);
    public abstract DcmParser newDcmParser(ImageInputStream in);

    static class ConfigurationError extends Error {
        ConfigurationError(String msg, Exception x) {
            super(msg,x);
        }
    }
}
