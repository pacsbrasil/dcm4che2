package org.dcm4chee.imagetest;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;

/**
 * Performs a different on two images. Optionally writes the differenced
 * (second) image out, and writes out information about the differences.
 * 
 * @author bwallace
 * 
 */
public class ImageDiff {
   private static Logger log = LoggerFactory.getLogger(ImageDiff.class);

	public static boolean writeImage = true;

	public static boolean writeDiff = true;

	public static boolean writeInfo = true;

	private long sumDifference = 0;

	private long sumSqrDifference = 0;

	private long pixelCount = 0;
	
	private long maxDiff = 0;
	
	private long allowedDiff;
	StringBuffer diffPos = new StringBuffer();

	/** Compares two images, i1 and i2, writing information out to fileBase about the
	 * differences.
	 * 
	 * @param i1 - this is typically the "correct" image.
	 * @param i2 - this is typically the image you are testing.
	 * @param fileBase - this is the location to use for saving files
	 * @param allowedDiff - this is the amount of difference, per pixel that is allowed.
	 * @throws IOException
	 */
	public ImageDiff(BufferedImage i1, BufferedImage i2, String fileBase, long allowedDiff)
			throws IOException {
		this.allowedDiff = allowedDiff;
		assert i1 != null;
		assert i1.getWidth() == i2.getWidth();
		assert i1.getHeight() == i2.getHeight();
        BufferedImage i3 = new BufferedImage(i1.getWidth(), i1.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
		computeDiffs(i1, i2, i3);
		if (writeImage) {
			writeImage(i2, fileBase);
		}
		
		if (writeDiff) {
			// Difference object is always a byte array
			writeImage(i3, fileBase + "-diff");
		}
		if (writeInfo) {
			writeInfo(fileBase, i3);
		}
	}

	/** Write info about the differences to the file - only if there are significant differences */
	private void writeInfo(String fileBase, BufferedImage i3) throws IOException {
		File f = new File(fileBase+".txt");
		if( maxDiff <= allowedDiff ) {
			if( f.exists() ) f.delete();
			return;
		}
		Writer w = new FileWriter(fileBase+".txt");
		w.write("# Image base "+fileBase+" information.\n");
		w.write("sumDifference="+sumDifference+"\n");
		double avg = sumDifference/(double) pixelCount;
		w.write("avgDifference="+avg+"\n");
		double avgSqr = sumSqrDifference/(double) pixelCount;
		w.write("stdDeviation="+Math.sqrt(avgSqr - avg*avg)+"\n");
		w.write("maxDiff="+maxDiff+"\n");
		w.write(diffPos.toString());
		w.close();
	}

	/**
	 * Write the image to the file base (as PNG, so as to preserve full
	 * fidelity)
	 */
	private void writeImage(BufferedImage i2, String fileBase) throws IOException {
		File f = new File(fileBase+".png");
		if( maxDiff <= allowedDiff ) {
			log.debug("Not writing image - difference "+ maxDiff+"<="+ allowedDiff);
			if( f.exists() ) f.delete();
			return;
		}
		log.warn("Writing image "+fileBase+".png - difference "+ maxDiff+">"+ allowedDiff);
		ImageIO.write(i2,"png", new File(fileBase+".png"));
	}

	protected void computeDiffs(BufferedImage i1, BufferedImage i2,
			BufferedImage i3) {
		if( i1.getColorModel().getNumComponents()==1 ) {
			computeDiffsGray(i1,i2,i3);
		}
		else {
			computeDiffsRGB(i1,i2,i3);
		}
	}

	/** Compute differences for RGB */
	private void computeDiffsRGB(BufferedImage i1, BufferedImage i2, BufferedImage i3) {
		int w = i1.getWidth();
		int h = i1.getHeight();
		int[] d1 = new int[w];
		int[] d2 = new int[w];
		int[] d3 = new int[w];
		int delta;
		for(int y=0; y<h; y++) {
			d1 = i1.getRGB(0,y,w,1,d1,0,0);
			d2 = i2.getRGB(0,y,w,1,d2,0,0);
			for(int x=0; x<w; x++ ) {
				delta = colourDiff(d1[x],d2[x]);
				d3[x] = colourDiffPixel(d1[x],d2[x]);
				pixelCount++;
				sumDifference += delta;
				sumSqrDifference += delta*delta;
				if( delta>maxDiff ) {
					maxDiff = delta;
					diffPos.append("Additional diff at ").append(x).append(",").append(y).append(" delta ");
					diffPos.append(delta).append(" source ").append(Integer.toHexString(d1[x])).append(" final ").append(Integer.toHexString(d2[x])).append("\n");
				}
			}
			if( i3!=null ) {
				i3.getRaster().setPixels(0,y,w,1,d3);
			}
		}
	}

	/** Returns the difference in the colour component */
	protected int colourDiff(int c1, int c2) {
	        c1 = 0xFFFFFF & c1;
	        c2 = 0xFFFFFF & c2;
		int a = ((c1 >> 24) & 0xFF) - ((c2 >> 24) & 0xFF);
		int r = ((c1 >> 16) & 0xFF) - ((c2 >> 16) & 0xFF);
		int g = ((c1 >> 8) & 0xFF) -  ((c2 >> 8) & 0xFF);
		int b = ((c1 & 0xFF)) - (c2 & 0xFF);
		int ret = (int) Math.sqrt(a*a+r*r+g*g+b*b);
		return ret;
	}

	/** Returns a pixel representing the difference in the colour component */
	protected int colourDiffPixel(int c1, int c2) {
		int a = (c1 >> 24) & 0xFF - ((c2 >> 24) & 0xFF);
		int r = (c1 >> 16) & 0xFF - ((c2 >> 16) & 0xFF);
		int g = (c1 >> 8) & 0xFF -  ((c2 >> 8) & 0xFF);
		int b = c1 & 0xFF - (c2 & 0xFF);
		int ret = ((a + 255)/2 << 24) | ((r + 255)/2 << 16) | ((g+255)/2 << 8) | (b+255/2);
		return ret;
	}
	
	/** Returns a gray pixel diff */
	protected int grayDiffPixel(int diff) {
		if( diff==0 ) return diff;
		// Diff 1..255 creates output 16..127
		if( diff < 256 ) return diff*111/255+16;
		// Diff 255..64k creates output 127..256
		return diff/512+127;
	}
	
	/** Compute differences for grayscale */
	private void computeDiffsGray(BufferedImage i1, BufferedImage i2, BufferedImage i3) {
		int w = i1.getWidth();
		int[] d1 = new int[w];
		int[] d2 = new int[w];
		int[] d3 = new int[w];
		int delta;
		for(int y=0; y<i1.getHeight(); y++ ) {
			d1 = i1.getRaster().getPixels(0,y,w,1,d1);
			d2 = i2.getRaster().getPixels(0,y,w,1,d2);
			for(int x=0; x<w; x++ ) {
				delta = Math.abs(d1[x]-d2[x]);
				d3[x] = grayDiffPixel(delta);
				pixelCount++;
				sumDifference += delta;
				sumSqrDifference += delta*delta;
				if( delta>maxDiff ) {
					maxDiff = delta;
					diffPos.append("Additional diff at ").append(x).append(",").append(y).append(" source ");
					diffPos.append(delta).append(" final ").append(d2[x]).append("\n");
				}
			}
			if( i3!=null ) {
				i3.getRaster().setPixels(0,y,w,1,d3);
			}
		}
	}

	public long getMaxDiff() {
		return maxDiff;
	}

	public long getPixelCount() {
		return pixelCount;
	}

	public long getSumDifference() {
		return sumDifference;
	}

	public long getSumSqrDifference() {
		return sumSqrDifference;
	}

}
