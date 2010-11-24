/*******************************************************************************
 * Copyright (c) 2010 Nicolas Roduit.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Nicolas Roduit - initial API and implementation
 ******************************************************************************/
package org.weasis.core.api.media.data;

import java.awt.image.DataBuffer;
import java.awt.image.IndexColorModel;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.ParameterBlock;
import java.io.IOException;
import java.lang.ref.Reference;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.media.jai.JAI;
import javax.media.jai.PlanarImage;
import javax.media.jai.RenderedOp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.core.api.image.util.ImageToolkit;
import org.weasis.core.api.image.util.Unit;

public class ImageElement extends MediaElement<PlanarImage> {

    /**
     * Logger for this class
     */
    private static final Logger logger = LoggerFactory.getLogger(ImageElement.class);
    // Imageio issue with native library in multi-thread environment (to avoid JVM crash let only one simultaneous
    // thread)
    // (https://jai-imageio-core.dev.java.net/issues/show_bug.cgi?id=126)
    public static final ExecutorService IMAGE_LOADER = Executors.newFixedThreadPool(1);

    private static final SoftHashMap<ImageElement, PlanarImage> mCache = new SoftHashMap<ImageElement, PlanarImage>() {

        @Override
        public void removeElement(Reference<? extends PlanarImage> soft) {
            ImageElement key = reverseLookup.remove(soft);
            if (key != null) {
                hash.remove(key);
                MediaReader<PlanarImage> reader = key.getMediaReader();
                if (reader != null) {
                    // Close the image stream
                    reader.close();
                }
            }
        }
    };
    protected boolean readable = true;

    protected double pixelSizeX = 1.0;
    protected double pixelSizeY = 1.0;
    protected Unit pixelSpacingUnit = Unit.PIXEL;
    protected String pixelSizeCalibrationDescription = null;
    protected String pixelValueUnit = null;

    protected float maxValue;
    protected float minValue;

    public ImageElement(MediaReader<PlanarImage> mediaIO, Object key) {
        super(mediaIO, key);
    }

    public void findMinMaxValues(RenderedImage img) throws OutOfMemoryError {
        // This function can be called several times from the inner class Load.
        // Do not compute min and max it has already be done
        if (img != null && minValue == 0 && maxValue == 0) {
            int datatype = img.getSampleModel().getDataType();
            if (datatype == DataBuffer.TYPE_BYTE) {
                this.minValue = 0;
                this.maxValue = 255;
            } else {
                ParameterBlock pb = new ParameterBlock();
                pb.addSource(img);
                // ImageToolkit.NOCACHE_HINT to ensure this image won't be stored in tile cache
                RenderedOp dst = JAI.create("extrema", pb, ImageToolkit.NOCACHE_HINT); //$NON-NLS-1$
                double[][] extrema = (double[][]) dst.getProperty("extrema"); //$NON-NLS-1$
                double min = Double.MAX_VALUE;
                double max = -Double.MAX_VALUE;
                int numBands = dst.getSampleModel().getNumBands();
                for (int i = 0; i < numBands; i++) {
                    min = Math.min(min, extrema[0][i]);
                    max = Math.max(max, extrema[1][i]);
                }
                this.minValue = (int) min;
                this.maxValue = (int) max;
            }
        }
    }

    protected boolean isGrayImage(RenderedImage source) {
        // Binary images have indexColorModel
        if (source.getSampleModel().getNumBands() > 1 || source.getColorModel() instanceof IndexColorModel) {
            return false;
        }
        return true;
    }

    public float getPixelWindow(float window) {
        return window;
    }

    public float getPixelLevel(float level) {
        return level;
    }

    public float getDefaultWindow() {
        return maxValue - minValue;
    }

    public float getDefaultLevel() {
        return minValue + (maxValue - minValue) / 2.f;
    }

    public float getMaxValue() {
        return maxValue;
    }

    public float getMinValue() {
        return minValue;
    }

    public double getPixelSizeX() {
        return pixelSizeX;
    }

    public double getPixelSizeY() {
        return pixelSizeY;
    }

    public Unit getPixelSpacingUnit() {
        return pixelSpacingUnit;
    }

    public String getPixelValueUnit() {
        return pixelValueUnit;
    }

    public String getPixelSizeCalibrationDescription() {
        return pixelSizeCalibrationDescription;
    }

    public boolean isImageInMemory() {
        return mCache.get(this) != null;
    }

    public boolean hasSameSizeAndSpatialCalibration(ImageElement image) {
        if (image != null) {
            if (pixelSizeX == image.getPixelSizeX() && pixelSizeY == image.getPixelSizeY()) {
                PlanarImage img = getImage();
                PlanarImage img2 = image.getImage();
                if (img != null && img2 != null) {
                    if (img.getWidth() == img2.getWidth() && img.getHeight() == img2.getHeight()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Loads the original image. Must load and return the original image.
     * 
     * @throws Exception
     * 
     * @throws IOException
     */

    protected PlanarImage loadImage() throws Exception {
        return mediaIO.getMediaFragment(this);
    }

    /**
     * Returns the full size, original image. Returns null if the image is not loaded.
     * 
     * @return
     */
    public synchronized PlanarImage getImage() {
        PlanarImage cacheImage;
        try {
            cacheImage = startImageLoading();
        } catch (OutOfMemoryError e1) {
            // Appends when loading a big image without tiling, the memory left is not enough for the renderedop (like
            // Extrema)
            logger.warn("Out of MemoryError: {}", getMediaURI()); //$NON-NLS-1$
            mCache.expungeStaleEntries();
            System.gc();
            try {
                Thread.sleep(100);
            } catch (InterruptedException et) {
            }
            cacheImage = startImageLoading();
        }
        return cacheImage;
    }

    private PlanarImage startImageLoading() throws OutOfMemoryError {
        PlanarImage cacheImage;
        if ((cacheImage = mCache.get(this)) == null && readable && setAsLoading()) {
            logger.debug("Asking for reading image: {}", this); //$NON-NLS-1$
            Load ref = new Load();
            Future<PlanarImage> future = IMAGE_LOADER.submit(ref);
            PlanarImage img = null;
            try {
                img = future.get();

            } catch (InterruptedException e) {
                // Re-assert the thread's interrupted status
                Thread.currentThread().interrupt();
                // We don't need the result, so cancel the task too
                future.cancel(true);
            } catch (ExecutionException e) {
                if (e.getCause() instanceof OutOfMemoryError) {
                    setAsLoaded();
                    throw new OutOfMemoryError();
                } else {
                    readable = false;
                    logger.error("Cannot read pixel data!: {}", getMediaURI()); //$NON-NLS-1$
                    e.printStackTrace();
                }
            }
            if (img != null) {
                readable = true;
                mCache.put(this, img);
                cacheImage = img;
            }
            setAsLoaded();
        }
        return cacheImage;
    }

    // private void freeMemory() {
    // Entry<K, V> eldest = mCache..after;
    // if (removeEldestEntry(eldest)) {
    // removeEntryForKey(eldest.key);
    // }
    // }

    public boolean isReadable() {
        return readable;
    }

    @Override
    public void dispose() {
        // TODO find a clue to not dispose the display image
        // Or do nothing, let the soft reference mechanism do its job

        // Close image reader and image stream
        // if (mediaIO != null) {
        // mediaIO.close();
        // }
        // // Unload image from memory
        // PlanarImage temp = mCache.remove(this);
        // if (temp != null) {
        // temp.dispose();
        //
        // }
        // if (image != null) {
        // PlanarImage temp = image.get();
        // if (temp != null) {
        // temp.dispose();
        // }
        // // image = null;
        // }
    }

    class Load implements Callable<PlanarImage> {

        public PlanarImage call() throws Exception {
            PlanarImage img = loadImage();
            findMinMaxValues(img);
            return img;
        }

    }
}
