package org.dcm4che.image;

import java.nio.Buffer;

import org.dcm4che.data.Dataset;
import org.dcm4cheri.image.PixelDataFactoryImpl;
import org.dcm4cheri.image.PixelDataImpl;

/**
 * @author jforaci
 */
public abstract class PixelDataFactory
{
    public static PixelDataFactory newInstance()
    {
        return new PixelDataFactoryImpl();
    }
    
    public abstract PixelData newPixelData(Dataset ds);
    
    /*public PixelData getInstance(Dataset ds);
    
    public PixelData newInstance(Dataset ds, Buffer buff);*/
}
