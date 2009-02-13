package org.dcm4chee.xero.wado.multi;

import static org.dcm4chee.xero.wado.WadoParams.FRAME_NUMBER;
import static org.dcm4chee.xero.wado.WadoParams.OBJECT_UID;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4chee.xero.metadata.MetaData;
import org.dcm4chee.xero.metadata.filter.Filter;
import org.dcm4chee.xero.metadata.filter.FilterItem;
import org.dcm4chee.xero.metadata.servlet.ServletResponseItem;
import org.dcm4chee.xero.util.FilterCombineIterator;
import org.dcm4chee.xero.wado.RecodeDicom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This filter iterates over the available frames in an image, calling the next
 * filters for every frame. Every frame defaults to:
 * <ol>
 * <li>Every frame in the image, from 1 to n.</li>
 * <li>Every frame in the largest frame count bitmap overlay, if no images in
 * the frame.</li>
 * <li>Only those frames specified in a simpleFrameList or "frameNumber" if
 * simpleFrameList isn't specified.</li>
 * <li>If no overlays or frames exist or exist matching the frame list, then no
 * calls are made and null is returned.</li>
 * <li>If the DicomObject isn't found, then null is returned immediately.</li>
 * </ol>
 * 
 * @author bwallace
 */
public class FrameNumberItFilter implements Filter<Iterator<ServletResponseItem>> {
    private static final Logger log = LoggerFactory.getLogger(FrameNumberItFilter.class);

    /**
     * Calls the next filter with frameNumber set, and simpleFrameList un-set,
     * once for every frame that matches.
     */
    public Iterator<ServletResponseItem> filter(FilterItem<Iterator<ServletResponseItem>> filterItem, Map<String, Object> params) {
        String objectUID = (String) params.get(OBJECT_UID);
        if (objectUID == null)
            return null;
        DicomObject ds = dicomImageHeader.filter(null, params);
        if (ds == null)
            return null;
        List<Integer> frames = RecodeDicom.computeFrames(ds, params);
        if (frames == null)
            frames = allFrames(ds);
        if (frames.size() == 0)
            return null;
        // Optimization for the non multi-frames
        if (ds.getInt(Tag.NumberOfFrames, 1) == 1)
            return filterItem.callNextFilter(params);
        log.debug("Found multiple frames for " + objectUID);
        return new FrameIterator(frames, filterItem, params);
    }

    /** Gets a list containing all frame numbers */
    public static List<Integer> allFrames(DicomObject ds) {
        int start = 1;
        int end = ds.getInt(Tag.NumberOfFrames, 1);
        List<Integer> ret = new ArrayList<Integer>(end);
        for (int i = start; i <= end; i++) {
            ret.add(i);
        }
        return ret;
    }

    private Filter<DicomObject> dicomImageHeader;

    /** Gets the filter that returns the dicom object image header */
    public Filter<DicomObject> getDicomImageHeader() {
        return dicomImageHeader;
    }

    @MetaData(out = "${ref:dicomImageHeader}")
    public void setDicomImageHeader(Filter<DicomObject> dicomImageHeader) {
        this.dicomImageHeader = dicomImageHeader;
    }

    /** Iterate over the string UID's specified in the original parameters. */
    class FrameIterator extends FilterCombineIterator<Integer, ServletResponseItem> {

        public FrameIterator(List<Integer> frames, FilterItem<Iterator<ServletResponseItem>> filterItem, Map<String, Object> params) {
            super(frames, filterItem, params);
        }

        /** Sets the object UID to the current object UID */
        @Override
        protected void updateParams(Integer item, Map<String, Object> params) {
            params.put(FRAME_NUMBER, item);
            log.debug("updateParams for frame - on object UID {}", params.get(OBJECT_UID));
        }

        /** Restore the original UID */
        @Override
        protected void restoreParams(Integer item, Map<String, Object> params) {
            params.remove(FRAME_NUMBER);
        }

    }

}
