/*
 *  Copyright (c) 2002 by TIANI MEDGRAPH AG                                  *
 *                                                                           *
 *  This file is part of dcm4che.                                            *
 *                                                                           *
 *  This library is free software; you can redistribute it and/or modify it  *
 *  under the terms of the GNU Lesser General Public License as published    *
 *  by the Free Software Foundation; either version 2 of the License, or     *
 *  (at your option) any later version.                                      *
 *                                                                           *
 *  This library is distributed in the hope that it will be useful, but      *
 *  WITHOUT ANY WARRANTY; without even the implied warranty of               *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU        *
 *  Lesser General Public License for more details.                          *
 *                                                                           *
 *  You should have received a copy of the GNU Lesser General Public         *
 *  License along with this library; if not, write to the Free Software      *
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA  *
 *                                                                           *
 */
package com.tiani.prnscp.print;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmElement;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.data.FileFormat;
import org.dcm4che.data.FileMetaInfo;
import org.dcm4che.dict.Tags;
import org.dcm4che.dict.UIDs;
import org.dcm4che.util.UIDGenerator;

/**
 *  Description of the Class
 *
 * @author     <a href="mailto:gunter@tiani.com">gunter zeilinger</a>
 * @since     April 1, 2003
 * @version    $Revision$
 */
public class PLutBuilder
{

    private final static HashMap DICOM_SHAPES = new HashMap(3);
    static {
        DICOM_SHAPES.put("IDENTITY", "IDENTITY");
        DICOM_SHAPES.put("LIN_OD", "LIN OD");
    }
    private final static String LUT_FILE_EXT = ".lut";

    private final DcmObjectFactory dof = DcmObjectFactory.getInstance();

    private String shape;
    private float center = 0.5f;
    private float slope = 0.f;
    private float gamma = 1.f;
    private int length = 4096;
    private int bits = 8;
    private String uid;
    private String explanation;


    /**Constructor for the PLutBuilder object */
    public PLutBuilder() { }


    /**
     *Constructor for the PLutBuilder object
     *
     * @param  configInfo  Description of the Parameter
     * @param  plutDir     Description of the Parameter
     */
    public PLutBuilder(String configInfo, String plutDir)
    {
        shape = get("shape=", configInfo);
        if (shape != null && !DICOM_SHAPES.containsKey(shape)) {
            if (!toFile(plutDir, shape).isFile()) {
                throw new IllegalArgumentException("shape:" + shape);
            }
        }
        String c,s,g;
        if ((c = get("center=", configInfo)) != null) {
            setCenter(Float.parseFloat(c));
        }
        if ((s = get("slope=", configInfo)) != null) {
            setSlope(Float.parseFloat(s));
        }
        if ((g = get("gamma=", configInfo)) != null) {
            setGamma(Float.parseFloat(g));
        }
        if (shape == null && c == null && s == null && g == null) {
            throw new IllegalArgumentException(configInfo);
        }
    }


    private static File toFile(String dir, String shape)
    {
        return new File(dir, shape + LUT_FILE_EXT);
    }


    private static String get(String key, String s)
    {
        int pos = s.indexOf(key);
        if (pos == -1) {
            return null;
        }
        pos += key.length();
        int end = s.indexOf(',', pos);
        return end == -1
                 ? s.substring(pos)
                 : s.substring(pos, end);
    }


    /**
     *  Description of the Method
     *
     * @return    Description of the Return Value
     */
    public String toString()
    {
        return shape != null
                 ? "shape=" + shape
                 : "center=" + center
                 + ",slope=" + slope
                 + ",gamma=" + gamma
                 + ",length=" + length
                 + ",bits=" + bits;
    }


    /**
     *  Gets the shape attribute of the PLut object
     *
     * @return    The shape value
     */
    public String getShape()
    {
        return shape;
    }


    /**
     *  Sets the shape attribute of the PLut object
     *
     * @param  shape  The new shape value
     */
    public void setShape(String shape)
    {
        this.shape = shape;
    }


    /**
     *  Gets the center attribute of the PLut object
     *
     * @return    The center value
     */
    public double getCenter()
    {
        return center;
    }


    /**
     *  Sets the center attribute of the PLut object
     *
     * @param  center  The new center value
     */
    public void setCenter(float center)
    {
        if (center < 0. || center > 1.) {
            throw new IllegalArgumentException("center:" + center);
        }
        this.center = center;
    }


    /**
     *  Gets the slope attribute of the PLut object
     *
     * @return    The slope value
     */
    public float getSlope()
    {
        return slope;
    }


    /**
     *  Sets the slope attribute of the PLut object
     *
     * @param  slope  The new slope value
     */
    public void setSlope(float slope)
    {
        if (slope < 0.f || slope > 10.f) {
            throw new IllegalArgumentException("slope:" + slope);
        }
        this.slope = slope;
    }


    /**
     *  Gets the gamma attribute of the PLut object
     *
     * @return    The gamma value
     */
    public float getGamma()
    {
        return gamma;
    }


    /**
     *  Sets the gamma attribute of the PLut object
     *
     * @param  gamma  The new gamma value
     */
    public void setGamma(float gamma)
    {
        if (gamma < 0.1f || gamma > 10.f) {
            throw new IllegalArgumentException("gamma:" + gamma);
        }
        this.gamma = gamma;
    }


    /**
     *  Gets the length attribute of the PLut object
     *
     * @return    The length value
     */
    public int getLength()
    {
        return length;
    }


    /**
     *  Sets the length attribute of the PLut object
     *
     * @param  length  The new length value
     */
    public void setLength(int length)
    {
        if (length < 256 || length > 4096) {
            throw new IllegalArgumentException("length:" + length);
        }
        this.length = length;
    }


    /**
     *  Gets the bits attribute of the PLut object
     *
     * @return    The bits value
     */
    public int getBits()
    {
        return bits;
    }


    /**
     *  Sets the bits attribute of the PLut object
     *
     * @param  bits  The new bits value
     */
    public void setBits(int bits)
    {
        if (bits < 8 || bits > 16) {
            throw new IllegalArgumentException("bits:" + bits);
        }
        this.bits = bits;
    }


    /**
     *  Gets the uid attribute of the PLut object
     *
     * @return    The uid value
     */
    public String getUID()
    {
        return uid;
    }


    /**
     *  Sets the uid attribute of the PLut object
     *
     * @param  uid  The new uid value
     */
    public void setUID(String uid)
    {
        this.uid = uid;
    }


    /**
     *  Gets the explanation attribute of the PLut object
     *
     * @return    The explanation value
     */
    public String getExplanation()
    {
        return explanation;
    }


    /**
     *  Sets the explanation attribute of the PLut object
     *
     * @param  explanation  The new explanation value
     */
    public void setExplanation(String explanation)
    {
        this.explanation = explanation;
    }


    /**
     *  Description of the Method
     *
     * @return    Description of the Return Value
     */
    public int[] create()
    {
        //slope == 1/standard derivation
        final double k = -slope * slope / 2.;
        final double dx = 2. / length;
        final double invG = 1 / gamma;
        double x = -center * 2;
        double dlut[] = new double[length];
        double sum = 0.;
        for (int i = 1; i < length; i++, x += dx) {
            sum += Math.exp(k * x * x) * dx;
            dlut[i] = Math.pow(sum, invG);
        }
        //normalize dlut into presentation lut
        final double f = ((1 << bits) - 1) / dlut[length - 1];
        int[] plut = new int[length];
        for (int i = 0; i < length; i++) {
            plut[i] = (int) Math.round(f * dlut[i]);
        }
        return plut;
    }


    /**
     *  Description of the Method
     *
     * @param  plut  Description of the Parameter
     * @return       Description of the Return Value
     */
    public Dataset toDataset(int[] plut)
    {
        if (shape != null) {
            throw new IllegalStateException("shape=" + shape);
        }
        Dataset dsLut = dof.newDataset();
        DcmElement sq = dsLut.putSQ(Tags.PresentationLUTSeq);
        Dataset item = sq.addNewItem();
        item.putUS(Tags.LUTDescriptor, new int[]{plut.length, 0, bits});
        item.putLO(Tags.LUTExplanation,
                explanation != null ? explanation : toString());
        item.putUS(Tags.LUTData, plut);
        return dsLut;
    }


    /**
     *  Gets the shapePLUT attribute of the PLut object
     *
     * @return    The shapePLUT value
     */
    public Dataset getShapePLUT()
    {
        String code = (String) DICOM_SHAPES.get(shape);
        if (code == null) {
            throw new IllegalStateException("shape=" + shape);
        }
        Dataset dsLUT = dof.newDataset();
        dsLUT.putCS(Tags.PresentationLUTShape, code);
        return dsLUT;
    }


    /**
     *  Description of the Method
     *
     * @param  plutDir          Description of the Parameter
     * @return                  Description of the Return Value
     * @exception  IOException  Description of the Exception
     */
    public Dataset loadPLUT(String plutDir)
        throws IOException
    {
        if (shape == null) {
            throw new IllegalStateException("shape=null");
        }
        InputStream in = new BufferedInputStream(
                new FileInputStream(toFile(plutDir, shape)));
        Dataset dsLut = dof.newDataset();
        try {
            dsLut.readFile(in, FileFormat.DICOM_FILE, -1);
        } finally {
            try {
                in.close();
            } catch (IOException ignore) {}
        }
        return dsLut;
    }


    /**
     *  Description of the Method
     *
     * @param  plutDir          Description of the Parameter
     * @return                  Description of the Return Value
     * @exception  IOException  Description of the Exception
     */
    public Dataset createOrLoadPLUT(String plutDir)
        throws IOException
    {
        return (shape == null)
                 ? toDataset(create())
                 : DICOM_SHAPES.containsKey(shape)
                 ? getShapePLUT()
                 : loadPLUT(plutDir);
    }


    /**
     *  Description of the Method
     *
     * @return    Description of the Return Value
     */
    public FileMetaInfo makeFileMetaInfo()
    {
        return dof.newFileMetaInfo(UIDs.PresentationLUT,
                uid != null ? uid : UIDGenerator.getInstance().createUID(),
                UIDs.ExplicitVRLittleEndian);
    }
}

