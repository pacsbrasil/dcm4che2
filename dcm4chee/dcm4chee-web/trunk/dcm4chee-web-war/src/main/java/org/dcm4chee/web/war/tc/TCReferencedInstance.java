package org.dcm4chee.web.war.tc;

import org.dcm4chee.web.war.folder.delegate.WADODelegate;
import org.dcm4chee.web.war.tc.imageview.TCImageViewImage;

/**
 * @author Bernhard Ableitinger <bernhard.ableitinger@agfa.com>
 * @version $Revision$ $Date$
 * @since May 04, 2011
 */
public class TCReferencedInstance implements TCImageViewImage {

    private static final long serialVersionUID = 1L;

    private TCReferencedSeries series;
    private String iuid;
    private String cuid;

    public TCReferencedInstance(TCReferencedSeries series, String iuid, String cuid) {
        this.iuid = iuid;
        this.cuid = cuid;
        this.series = series;
    }

    @Override
    public TCReferencedSeries getSeries()
    {
        return series;
    }
    
    public String getStudyUID() {
        return series.getStudy().getStudyUID();
    }
    
    public String getSeriesUID() {
        return series.getSeriesUID();
    }

    @Override
    public String getInstanceUID() {
        return iuid;
    }

    @Override
    public String getClassUID() {
        return cuid;
    }

    public boolean isImage() {
        return WADODelegate.IMAGE == WADODelegate.getInstance()
                .getRenderType(cuid);
    }
    
    @Override
    public boolean equals(Object o)
    {
        if (o instanceof TCReferencedInstance)
        {
            TCReferencedInstance ref = (TCReferencedInstance)o;
            return getStudyUID().equals(ref.getStudyUID()) &&
                getSeriesUID().equals(ref.getSeriesUID()) &&
                getInstanceUID().equals(ref.getInstanceUID());
        }
        return super.equals(o);
    }
}