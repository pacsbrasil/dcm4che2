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

package org.dcm4che.dict;

/**
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0.0
 */
public abstract class DictionaryFactory {

    public static DictionaryFactory getInstance() {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        String name = System.getProperty("dcm4che.dict.DictionaryFactory",
                "org.dcm4cheri.dict.DictionaryFactoryImpl");
        try {
            return (DictionaryFactory)loader.loadClass(name).newInstance();
        } catch (ClassNotFoundException ex) {
            throw new ConfigurationError("class not found: " + name, ex); 
        } catch (InstantiationException ex) {
            throw new ConfigurationError("could not instantiate: " + name, ex); 
        } catch (IllegalAccessException ex) {
            throw new ConfigurationError("could not instantiate: " + name, ex); 
        }
    }
    
    protected DictionaryFactory() {
    }
    
    public abstract TagDictionary newTagDictionary();

    public abstract TagDictionary getDefaultTagDictionary();

    public abstract UIDDictionary newUIDDictionary();

    public abstract UIDDictionary getDefaultUIDDictionary();

    static class ConfigurationError extends Error {
        ConfigurationError(String msg, Exception x) {
            super(msg,x);
        }
    }
}
