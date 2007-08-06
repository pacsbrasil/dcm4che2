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
 * Gunter Zeilinger, Huetteldorferstr. 24/10, 1150 Vienna/Austria/Europe.
 * Portions created by the Initial Developer are Copyright (C) 2002-2005
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * See listed authors below.
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

package org.dcm4che2.imageio;

import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.stream.ImageInputStream;

import org.dcm4che2.data.Tag;
import org.dcm4che2.io.DicomInputStream;
import org.dcm4che2.util.TagUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.media.imageio.stream.SegmentedImageInputStream;
import com.sun.media.imageio.stream.StreamSegment;
import com.sun.media.imageio.stream.StreamSegmentMapper;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @version $Revision$ $Date$
 * @since Aug 6, 2007
 */
public class ItemParser implements StreamSegmentMapper {

    private static final Logger log = LoggerFactory.getLogger(ItemParser.class);

    private static final class Item {

        int offset;

        long startPos;

        int length;

        final int nextOffset() {
            return offset + length;
        }

        final long nextItemPos() {
            return startPos + length;
        }

        public String toString() {
            return "Item[off=" + offset + ", pos=" + startPos + ", len="
                    + length + "]";
        }
    }

    private final ArrayList items = new ArrayList();

    private final DicomInputStream dis;

    private final ImageInputStream iis;

    private boolean lastItemSeen = false;

    public ItemParser(DicomInputStream dis, ImageInputStream iis)
            throws IOException {
        this.dis = dis;
        this.iis = iis;
        // skip Basic Offset Table item.
        dis.readHeader();
        dis.skip(dis.valueLength());
        next();
    }

    public int getNumberOfDataFragments() {
        while (!lastItemSeen)
            next();
        return items.size();
    }

    public int getOffsetOfDataFragment(int index) {
        while (items.size() <= index)
            if (next() == null)
                throw new IndexOutOfBoundsException("index:" + index
                        + " >= size:" + items.size());
        return ((Item) items.get(index)).offset;
    }

    private Item next() {
        if (lastItemSeen)
            return null;
        try {
            if (!items.isEmpty())
                iis.seek(last().nextItemPos());
            dis.readHeader();
            if (log.isDebugEnabled())
                log.debug("Read " + TagUtils.toString(dis.tag()) + " #"
                        + dis.valueLength());
            if (dis.tag() == Tag.Item) {
                Item item = new Item();
                item.startPos = iis.getStreamPosition();
                item.length = dis.valueLength();
                if (!items.isEmpty())
                    item.offset = last().nextOffset();
                items.add(item);
                return item;
            }
        } catch (IOException e) {
            log.warn("i/o error reading next item:", e);
        }
        if (dis.tag() != Tag.SequenceDelimitationItem
                || dis.valueLength() != 0) {
            log.warn("expected (FFFE,E0DD) #0 but read "
                    + TagUtils.toString(dis.tag()) + " #" + dis.valueLength());
        }
        lastItemSeen = true;
        return null;
    }

    private Item last() {
        return (Item) items.get(items.size() - 1);
    }

    public StreamSegment getStreamSegment(long pos, int len) {
        StreamSegment retval = new StreamSegment();
        getStreamSegment(pos, len, retval);
        return retval;
    }

    public void getStreamSegment(long pos, int len, StreamSegment seg) {
        if (log.isDebugEnabled())
            log.debug("getStreamSegment(pos=" + pos + ", len=" + len + ")");
        Item item = last();
        while (item.nextOffset() <= pos) {
            if ((item = next()) == null) {
                seg.setSegmentLength(-1);
                return;
            }
        }
        int i = items.size() - 1;
        while (item.offset > pos)
            item = (Item) items.get(--i);
        seg.setStartPos(item.startPos + pos - item.offset);
        seg.setSegmentLength(Math.min((int) (item.offset + item.length - pos),
                len));
        if (log.isDebugEnabled())
            log.debug("return StreamSegment[start=" + seg.getStartPos()
                    + ", len=" + seg.getSegmentLength() + "]");
    }

    public long seekNextFrame(SegmentedImageInputStream siis)
            throws IOException {
        Item item = last();
        long pos = siis.getStreamPosition();
        int i = items.size() - 1;
        while (item.offset >= pos)
            item = (Item) items.get(--i);
        siis.seek(item.nextOffset());
        iis.seek(item.nextItemPos());
        return item.nextOffset();
    }

    public void seekFooter() throws IOException {
        iis.seek(last().nextItemPos());
        dis.readHeader();
    }
}
