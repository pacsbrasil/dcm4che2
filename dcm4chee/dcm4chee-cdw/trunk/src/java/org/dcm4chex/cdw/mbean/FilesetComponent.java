/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/
package org.dcm4chex.cdw.mbean;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

/**
 * @author gunter.zeilinter@tiani.com
 * @version $Revision$ $Date$
 * @since 19.07.2004
 *
 */
final class FilesetComponent implements Comparable {

    private final String id;

    private final Comparable comparable;

    private FilesetComponent parent = null;

    private ArrayList childs = null;

    private long size = 0L;
    
    private String[] fileIDs;
    
    public FilesetComponent(String id, Comparable comparable, String[] fileIDs) {
        this.id = id;
        this.comparable = comparable;
        this.fileIDs = fileIDs;
    }

    public int compareTo(Object o) {
        if (comparable == null || ((FilesetComponent) o).comparable == null)
            return 0;
        return comparable.compareTo(((FilesetComponent) o).comparable);
    }

    public void addChild(FilesetComponent child) {
        if (child.parent != null)
                throw new IllegalStateException("Has already a parent: "
                        + child);
        if (childs == null) childs = new ArrayList();
        childs.add(child);
        incSize(child.size);
        child.parent = this;
    }

    public void removeChild(FilesetComponent child) {
        if (child.parent != this)
                throw new IllegalStateException("Not a child: " + child);
        childs.remove(child);
        incSize(-child.size);
        child.parent = null;
    }

    public void incSize(long delta) {
        this.size += delta;
        if (parent != null) parent.incSize(delta);
    }

    public FilesetComponent takeChilds(long maxSize) {
        FilesetComponent result = new FilesetComponent(id, comparable, fileIDs);
        Collections.sort(childs);
        while (!childs.isEmpty()) {
            FilesetComponent child = (FilesetComponent) childs.get(0);
            if (result.size + child.size > maxSize) break;
            removeChild(child);
            result.addChild(child);
        }
        return result;
    }

    public final long size() {
        return size;
    };
}
