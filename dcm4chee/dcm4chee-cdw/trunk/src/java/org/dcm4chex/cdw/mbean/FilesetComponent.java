/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/
package org.dcm4chex.cdw.mbean;

import java.util.ArrayList;
import java.util.Collections;

import org.dcm4cheri.util.StringUtils;
import org.dcm4chex.cdw.common.ExecutionStatusInfo;
import org.dcm4chex.cdw.common.MediaCreationException;
import org.jboss.logging.Logger;

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
    
    public String toString() {
        return "FilesetComponent[" + StringUtils.toString(fileIDs, '/')
        	+ ", size=" + size + ", cmp=" + comparable + "]";
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

    private void moveChilds(FilesetComponent dest, long maxSize, Logger log) {
        while (!childs.isEmpty()) {
            FilesetComponent child = (FilesetComponent) childs.get(0);
            if (dest.size + child.size > maxSize) break;
            log.info("move " + child);
            removeChild(child);
            dest.addChild(child);
        }
    }
    
    public void split(long freeSizeFirst, long freeSizeOther, ArrayList result, Logger log)
		throws MediaCreationException {
        log.info("split " + this);
        Collections.sort(childs);
        while (!childs.isEmpty()) {
			FilesetComponent dest = newFilesetComponent();
			moveChilds(dest, result.isEmpty() ? freeSizeFirst : freeSizeOther, log);
			if (dest.childs == null) {
				FilesetComponent child = (FilesetComponent) childs.get(0);
				if (child.childs == null)
					throw new MediaCreationException(
					        ExecutionStatusInfo.INST_OVERSIZED,
                            "Instance size exceeds Media Capacity");
				child.split(freeSizeFirst, freeSizeOther, result, log);				
			} else
				result.add(dest);				
		}		
    }

    private FilesetComponent newFilesetComponent() {
        FilesetComponent result = new FilesetComponent(id, comparable, fileIDs);
		if (parent != null)
			parent.newFilesetComponent().addChild(result); 
        return result;
    }

    public final long size() {
        return size;
    }

	public final FilesetComponent root() {
		return parent != null ? parent.root() : this; 
	}

	public final ArrayList childs() {
		return childs; 
	}

	public final String id() {
		return id; 
	}

	public final String[] fileIDs() {
		return fileIDs; 
	}
}
