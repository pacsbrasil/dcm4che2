package org.dcm4cheri.image;

import org.dcm4che.data.Dataset;
import org.dcm4che.image.PixelData;
import org.dcm4che.image.PixelDataFactory;

/**
 * @author jforaci
 */
public class PixelDataFactoryImpl extends PixelDataFactory
{
    public PixelData newPixelData(Dataset ds)
    {
        return new PixelDataImpl(ds);
    }
}
