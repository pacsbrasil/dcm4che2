/*******************************************************************************
 * Copyright (c) 2010 Nicolas Roduit.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse  License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Nicolas Roduit - initial API and implementation
 ******************************************************************************/
package org.weasis.core.api.media.data;

import java.net.URI;
import java.util.Hashtable;

public interface Codec {

    /**
     * @return the codec name (must be unique)
     */
    String getCodecName();

    /**
     * @return the MIME types that can be read by the codec
     */
    String[] getReaderMIMETypes();

    /**
     * @return the list of file extensions supported the reader
     */
    String[] getReaderExtensions();

    /**
     * @return the MIME types that can be write by the codec
     */
    String[] getWriterMIMETypes();

    /**
     * @return the list of file extensions supported the writer
     */
    String[] getWriterExtensions();

    MediaReader getMediaIO(URI media, String mimeType, Hashtable<String, Object> properties);

    /**
     * @param mimeType
     * @return true if the codec supports the MIME type
     */
    boolean isMimeTypeSupported(String mimeType);

}
