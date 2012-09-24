package org.dcm4chee.web.war.tc;

import java.io.Serializable;

import org.dcm4chee.web.war.folder.delegate.WADODelegate;

/**
 * @author Bernhard Ableitinger <bernhard.ableitinger@agfa.com>
 * @version $Revision$ $Date$
 * @since May 04, 2011
 */
public class TCReferencedInstance implements Serializable {

    private TCReferencedSeries series;
    private String iuid;
    private String cuid;

    public TCReferencedInstance(TCReferencedSeries series, String iuid, String cuid) {
        this.iuid = iuid;
        this.cuid = cuid;
        this.series = series;
    }

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

    public String getInstanceUID() {
        return iuid;
    }

    public String getClassUID() {
        return cuid;
    }
    
    public boolean isImage() {
    	return isImage(cuid);
    }

    public static boolean isImage(String cuid) {
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