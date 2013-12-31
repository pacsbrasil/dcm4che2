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
import java.io.File;
import java.util.NavigableMap;
import java.util.NoSuchElementException;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author devishree
 */
public class Buffer {

    private ImagePanel imgPanelRef = null;
    private final NavigableMap<Integer, BufferedImage> map = new TreeMap<Integer, BufferedImage>();
    private int defaultBufferSize = 30;
    private boolean startBuffering = false;
    private ImageUpdatingThread imageUpdatingThread = null;
    private boolean initialPrev = true, initialNext = false;
    private NavigableMap<Integer, BufferedImage> tempBuffer = null;
    private int reverseThreshold = 0;
    private BackwardUpdator backwardUpdator = null;
    private TileLayoutThread tileLayoutThread = null;

    public Buffer(ImagePanel imgPanelRef) {
        this.imgPanelRef = imgPanelRef;
    }

    public Buffer(ImagePanel imgPanelRef, int updateFrom) {
        this.imgPanelRef = imgPanelRef;
        createUpdatingThread(updateFrom);
    }

    public void createThread(int startFrom) {
        tileLayoutThread = new TileLayoutThread();
        tileLayoutThread.setUpdateFrom(startFrom);
        tileLayoutThread.start();
    }

    private void createUpdatingThread(int updateFrom) {
        if (imgPanelRef.getTotalInstance() > defaultBufferSize) {
            tempBuffer = new TreeMap<Integer, BufferedImage>();
            reverseThreshold = imgPanelRef.getTotalInstance() - 10;
            backwardUpdator = new BackwardUpdator();
            imgPanelRef.createTask(backwardUpdator);
        } else {
            updateFrom = -1;
        }
        imageUpdatingThread = new ImageUpdatingThread(imgPanelRef);
        imageUpdatingThread.setUpdateFrom(updateFrom, true);
        imgPanelRef.createThread(imageUpdatingThread);
    }

    public void put(int instanceNo, BufferedImage image) {
        if (map.size() < defaultBufferSize - 1) {
//            System.out.println("Put : " + instanceNo);
            map.put(instanceNo, image);
        } else {
            map.put(instanceNo, image);
            synchronized (map) {
                try {
//                    System.out.println("Waiting : " + instanceNo);
                    map.wait();
                } catch (InterruptedException ex) {
                    Logger.getLogger(Buffer.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        if (tempBuffer != null && instanceNo < 10) {
            tempBuffer.put(instanceNo, image);
        }
    }

    public BufferedImage getForward(int instanceNo) {
//        initialPrev = true;
//        if (startBuffering && !map.containsKey(imgPanelRef.getTotalInstance() - 1)) {            
//            map.remove(map.firstKey());
//            if (initialNext) {
//                initialNext = false;
//                imageUpdatingThread.setUpdateFrom(map.lastKey(), true);
//            }
//            synchronized (map) {
//                map.notify();
//            }
//        }        
//        return map.get(instanceNo);
        initialPrev = true;
        if (startBuffering && instanceNo % 5 == 0 && !map.containsKey(imgPanelRef.getTotalInstance() - 1)) {
//            map.remove(map.firstKey());
            map.headMap(map.firstKey() + 5).clear();
            if (initialNext) {
                initialNext = false;
                imageUpdatingThread.setUpdateFrom(map.lastKey(), true);
            }
            synchronized (map) {
                map.notify();
            }
        }
        return map.get(instanceNo);
    }

    public BufferedImage getBackward(int instanceNo) {
//        initialNext = true;
//        if (!map.containsKey(0) && instanceNo <= reverseThreshold) {
//            map.remove(map.lastKey());
//            if (initialPrev) {
//                initialPrev = false;
//                imageUpdatingThread.setUpdateFrom(map.firstKey(), false);
//            }
//            synchronized (map) {
//                map.notify();
//            }
//        }
//        return map.get(instanceNo);
        initialNext = true;
        if (!map.containsKey(0) && instanceNo % 5 == 0 && instanceNo <= reverseThreshold) {
            map.tailMap(instanceNo + 5).clear();
            if (initialPrev) {
                initialPrev = false;
                imageUpdatingThread.setUpdateFrom(map.firstKey(), false);
            }
            synchronized (map) {
                map.notify();
            }
        }
        return map.get(instanceNo);
    }

    public void setStartBuffering(boolean startBuffering) {
        this.startBuffering = startBuffering;
    }

    public void makeMeWait() {
        synchronized (map) {
            try {
                System.out.println("Make me wait");
                map.wait(20);
            } catch (InterruptedException ex) {
                Logger.getLogger(Buffer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void makeMeWait1() {
        synchronized (map) {
            try {
                map.wait();
            } catch (InterruptedException ex) {
                Logger.getLogger(Buffer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void terminateThread() {
        imageUpdatingThread.interrupt();
        synchronized (map) {
            map.notify();
        }
        imageUpdatingThread.terminateThread();
        map.clear();
    }

    public void getForwardLoopBack() {
        initialPrev = true;
        if (tempBuffer != null) {
            startBuffering = false;
            map.clear();
            map.putAll(tempBuffer.headMap(10));
            imageUpdatingThread.setUpdateFrom(11, true);
            synchronized (map) {
                map.notify();
            }
        }
    }

    public void getBackwardLoopBack() {
        initialNext = true;
        if (tempBuffer != null) {
            map.clear();
            map.putAll(tempBuffer.tailMap(10));
            imageUpdatingThread.setUpdateFrom(reverseThreshold, false);
            synchronized (map) {
                map.notify();
            }
        }
    }

    private class BackwardUpdator implements Runnable {

        public BackwardUpdator() {
        }

        @Override
        public void run() {
            for (int i = imgPanelRef.getTotalInstance() - 1; i > imgPanelRef.getTotalInstance() - 11; i--) {
                tempBuffer.put(i, DicomImageReader.readDicomFile(new File(imgPanelRef.getFileLocation(i))));
            }
            if (!tempBuffer.containsKey(0)) {
                for (int i = 0; i < 10; i++) {
                    tempBuffer.put(i, DicomImageReader.readDicomFile(new File(imgPanelRef.getFileLocation(i))));
                }
            }
        }
    }

    public int getDefaultBufferSize() {
        return defaultBufferSize;
    }

    public boolean isImageExist(int instanceNo) {
        if (map.containsKey(instanceNo)) {
            return true;
        }
        return false;
    }

    public void update(int instanceNo) {
        try {
            if (instanceNo - map.firstKey() < 5 || map.lastKey() - instanceNo < 5) {
                map.clear();
                startBuffering = true;
                if (instanceNo - 10 < 0) {
                    imageUpdatingThread.setUpdateFrom(0, true);
                } else {
                    imageUpdatingThread.setUpdateFrom(instanceNo - 10, true);
                }
                synchronized (map) {
                    map.notify();
                }
                initialPrev = true;
            }
        } catch (NoSuchElementException ex) {
            System.out.println("No such element exception : " + instanceNo);
        }
    }

    public BufferedImage getImage(int instanceNo) {
        return map.get(instanceNo);
    }

    public void setBufferSize(int bufferSize) {
        this.defaultBufferSize = bufferSize;
    }

    public int getFirstKey() {
        return map.firstKey();
    }

    public int getLowerKey(int threshold) {
        return map.lowerKey(threshold);
    }

    public synchronized BufferedImage waitAndGet(int instanceNo) {
        do {
            try {
                wait(10);
            } catch (InterruptedException ex) {
                Logger.getLogger(Buffer.class.getName()).log(Level.SEVERE, null, ex);
            }
        } while (!map.containsKey(instanceNo));
        return map.get(instanceNo);
    }

    public void terminateTileLayoutThread() {
        tileLayoutThread.terminateThread();
        tileLayoutThread = null;
    }

    public int getLastKey() {
        return map.lastKey();
    }

    public void clearBuffer() {
        map.clear();
        synchronized (map) {
            map.notify();
        }
    }

    public void clearFrom(int from) {
        map.tailMap(from).clear();
    }

    public void clearTo(int to) {
        map.headMap(to).clear();
    }

    public void updateFrom(int updateFrom) {
        if (updateFrom < 0) {
            updateFrom = -1;
        }
        tileLayoutThread.setUpdateFrom(updateFrom);
        synchronized (map) {
            map.notify();
        }
    }
}
