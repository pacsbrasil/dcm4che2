/* $Id$
 * Copyright (c) 2004 by TIANI MEDGRAPH AG
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
package com.tiani.prnscp.print;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

/**
 * @author gunter.zeilinter@tiani.com
 * @version $Revision$ $Date$
 * @since 25.04.2004
 *
 */
class IconLoader {

    private static IconLoader instance = new IconLoader();

    private static class Entry {

        final long timestamp;

        final BufferedImage bi;

        Entry(File file) throws IOException {
            timestamp = file.lastModified();
            bi = ImageIO.read(file);
        }
    }

    private Map map = Collections.synchronizedMap(new HashMap());

    public static IconLoader getInstance() {
        return instance;
    }

    private IconLoader() {
    }

    public BufferedImage load(File file) throws IOException {
        Entry entry = (Entry) map.get(file);
        if (entry == null || entry.timestamp < file.lastModified()) {
            entry = new Entry(file);
            map.put(file, entry);
        }
        return entry.bi;
    }
}
