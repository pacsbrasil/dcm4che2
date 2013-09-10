/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 *
 * The Initial Developer of the Original Code is
 * Raster Images
 * Portions created by the Initial Developer are Copyright (C) 2009-2010
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Babu Hussain A
 * Devishree V
 * Meer Asgar Hussain B
 * Prakash J
 * Suresh V
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 *
 * ***** END LICENSE BLOCK ***** */
package in.raster.mayam.delegates;

import in.raster.mayam.form.ImagePanel;
import java.awt.image.BufferedImage;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Devishree
 * @version 2.0
 */
public class ImageBuffer {

    NavigableMap<Integer, BufferedImage> map = new TreeMap<Integer, BufferedImage>();
    ImagePanel imgPanel;
    boolean updationStarted = false;
    int defaultBufferSize = 25, halfBufferSize = defaultBufferSize / 2;
    int totalInstances = 0;

    public ImageBuffer(ImagePanel imgPanel) {
        this.imgPanel = imgPanel;
        this.totalInstances = imgPanel.getTotalInstance();
    }

    public synchronized void put(int instanceNumber, BufferedImage image) {
        try {
            if (map.size() <= defaultBufferSize) {
                map.put(instanceNumber, image);
//                System.out.println("Assignment in producer : " + instanceNumber);
            }
        } catch (Exception ex) {
//            System.out.println("Exception in put() : " + ex.getMessage());
        }
    }

    public synchronized BufferedImage get(int instanceNumber, boolean isForward) {
        try {
            if (defaultBufferSize < totalInstances) {
                if (isForward && !updationStarted && map.lastKey() - instanceNumber <= halfBufferSize && map.lastKey() + (defaultBufferSize - map.size()) < totalInstances) {
                    updationStarted = true;
                    map.headMap(instanceNumber).clear();
                    imgPanel.imageUpdator.terminateThread();
                    imgPanel.imageUpdator = new ImageGenerator(this, imgPanel, false);
                    if (map.size() != 0) {
                        imgPanel.imageUpdator.setParameters(map.lastKey() + 1, map.lastKey() + (defaultBufferSize - map.size()), true);
                    } else {
                        imgPanel.imageUpdator.setParameters(instanceNumber, instanceNumber + defaultBufferSize, true);
                    }
                    imgPanel.imageUpdator.start();
                } else if (!isForward && !updationStarted && instanceNumber - map.firstKey() <= halfBufferSize && map.firstKey() - (defaultBufferSize - map.size()) > 0) {
                    updationStarted = true;
                    map.tailMap(instanceNumber + 1).clear();
                    imgPanel.imageUpdator.terminateThread();
                    imgPanel.imageUpdator = new ImageGenerator(this, imgPanel, false);
                    if (map.size() != 0) {
                        imgPanel.imageUpdator.setParameters(map.firstKey(), map.firstKey() - (defaultBufferSize - map.size()), false);
                    } else {
                        imgPanel.imageUpdator.setParameters(instanceNumber, instanceNumber - defaultBufferSize, false);
                    }
                    imgPanel.imageUpdator.start();
                }
            }
            if (!map.containsKey(instanceNumber)) {
                wait(5);
            }
        } catch (Exception ex) {
//            System.out.println("Exception in GET : " + ex.getMessage());
        }
        return map.get(instanceNumber);
    }

    public synchronized BufferedImage get(int instanceNumber) {
        do {
            try {
//                System.out.println("Waiting for image : " + instanceNumber);
                wait(10);
            } catch (InterruptedException ex) {
                Logger.getLogger(ImageBuffer.class.getName()).log(Level.SEVERE, null, ex);
            }
        } while (!map.containsKey(instanceNumber));
        return map.get(instanceNumber);
    }

    public synchronized BufferedImage getImmediately(int instanceNumber) {
        return map.get(instanceNumber);
    }

    public void setUpdationStarted(boolean updationStarted) {
        this.updationStarted = updationStarted;
    }

    public void clearBuffer() {
        map.clear();
    }

    public int getDefaultBufferSize() {
        return defaultBufferSize;
    }

    public boolean isImageExists(int instanceNumber) {
        return map.containsKey(instanceNumber);
    }

    public void setDefaultBufferSize(int bufferSize) {
        this.defaultBufferSize = bufferSize;
    }

    public void clearSubMap(int fromKey, int toKey) {
        map.subMap(fromKey, toKey).clear();
    }

    public ImagePanel getImagePanelRef() {
        return imgPanel;
    }

    public int getLastKey() {
        return map.lastKey();
    }

    public int getFirstKey() {
        return map.firstKey();
    }

    public int getLowerKey(int key) {
        return map.lowerKey(key);
    }

    public void clearElementsForward(int lowerKey) {
        map.tailMap(lowerKey).clear();//Strictly greater than higher key
    }

    public void clearElementsBackward(int higherKey) {
        map.headMap(higherKey).clear();//Strictly less than lower key
    }

    public int getCurrentBufferSize() {
        return map.size();
    }
}
