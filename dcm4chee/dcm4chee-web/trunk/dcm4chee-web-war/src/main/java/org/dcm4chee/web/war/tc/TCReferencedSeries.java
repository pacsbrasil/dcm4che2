package org.dcm4chee.web.war.tc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.dcm4che2.data.DicomObject;
import org.dcm4chee.archive.entity.Series;
import org.dcm4chee.archive.util.JNDIUtils;
import org.dcm4chee.web.dao.tc.TCQueryLocal;
import org.dcm4chee.web.war.tc.imageview.TCImageViewSeries;

/**
 * @author Bernhard Ableitinger <bernhard.ableitinger@agfa.com>
 * @version $Revision$ $Date$
 * @since May 04, 2011
 */
public class TCReferencedSeries implements TCImageViewSeries {
    private static final long serialVersionUID = 1L;
    
    private TCReferencedStudy study;
    private String suid;
    private List<TCReferencedInstance> instances;
    private List<TCReferencedInstance> notImages;
    private DicomObject dataset;
    private boolean dbQueryDone;
    
    public TCReferencedSeries(String suid, TCReferencedStudy study)
    {
        this.suid = suid;
        this.study = study;
        this.dbQueryDone = false;
        this.instances = new ArrayList<TCReferencedInstance>();
    }
    @Override
    public String getSeriesUID()
    {
        return suid;
    }
    @Override
    public TCReferencedStudy getStudy()
    {
        return study;
    }
    public int getInstanceCount()
    {
        return instances.size();
    }
    @Override
    public int getImageCount()
    {
        return instances.size()-(notImages!=null?notImages.size():0);
    }
    @Override
    public String getSeriesValue(int tag)
    {
        if (!dbQueryDone)
        {
            try
            {
                TCQueryLocal dao = (TCQueryLocal) JNDIUtils
                    .lookup(TCQueryLocal.JNDI_NAME);
                Series series = dao.findSeriesByUID(suid);
                if (series!=null)
                {
                    dataset = series.getAttributes(true);
                }
            }
            finally
            {
                dbQueryDone = true;
            }
        }
        
        if (dataset!=null)
        {
            return dataset.getString(tag);
        }
        
        return null;
    }
    @SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
    public List<TCReferencedImage> getImages()
    {
        if (notImages==null)
        {
            return (List)getInstances();
        }
        else
        {
            List<TCReferencedImage> images = new ArrayList<TCReferencedImage>((List)instances);
            images.removeAll(notImages);
            return images;
        }
    }
    public List<TCReferencedInstance> getInstances()
    {
        return Collections.unmodifiableList(instances);
    }
    public void addInstance(TCReferencedInstance instance)
    {
        if (!instances.contains(instance))
        {
            instances.add(instance);
            
            if (!instance.isImage())
            {
                if (notImages==null)
                {
                    notImages = new ArrayList<TCReferencedInstance>(5);
                }
                if (!notImages.contains(instance))
                {
                    notImages.add(instance);
                }
            }
        }
    }
    public void removeInstance(TCReferencedInstance instance)
    {
    	if (instances!=null)
    	{
    		instances.remove(instance);
    		if (notImages!=null)
    		{
    			notImages.remove(instance);
    			if (notImages.isEmpty())
    			{
    				notImages = null;
    			}
    		}
    	}
    }
}