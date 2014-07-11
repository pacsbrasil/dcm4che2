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
 * Portions created by the Initial Developer are Copyright (C) 2014
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

import in.raster.mayam.context.ApplicationContext;
import in.raster.mayam.form.ImagePanel;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.logging.Level;
import javax.swing.SwingWorker;

/**
 *
 * @author devishree
 */
public class Buffer extends SwingWorker<Void, Void> {

    private ImagePanel imgPanelRef = null;
    private final NavigableMap<Integer, BufferedImage> map = new TreeMap<Integer, BufferedImage>();
    private int defaultBufferSize = 30;
    private boolean startBuffering = false;
    private boolean initialPrev = true, initialNext = false;
    private TileLayoutThread tileLayoutThread = null;
    private boolean isForward = true, terminate = false;
    private int i = -1;

    public Buffer(ImagePanel imgPanelRef) {
        this.imgPanelRef = imgPanelRef;
    }

    public Buffer(ImagePanel imgPanelRef, int updateFrom) {
        this.imgPanelRef = imgPanelRef;
        createUpdatingThread(updateFrom);
    }

    public void createThread(int startFrom) {
        tileLayoutThread = new TileLayoutThread();
        tileLayoutThread.setUpdateFrom(startFrom, imgPanelRef.getTotalInstance());
        tileLayoutThread.start();
    }

    private void createUpdatingThread(int updateFrom) {
        i = updateFrom;
        execute();
    }

    public void put(int instanceNo, BufferedImage image) {
        if (map.size() < defaultBufferSize - 1) {
            map.put(instanceNo, image);
        } else {
            map.put(instanceNo, image);
            synchronized (map) {
                try {
                    map.wait();
                } catch (InterruptedException ex) {
                    ApplicationContext.logger.log(Level.INFO, null, ex);
                }
            }
        }
    }

    public BufferedImage getForward(int instanceNo) {
        initialPrev = true;
        if (startBuffering && instanceNo % 5 == 0 && !map.containsKey(imgPanelRef.getTotalInstance() - 1)) {
            map.headMap(map.firstKey() + 5).clear();
            if (initialNext) {
                initialNext = false;
                i = map.lastKey();
                isForward = true;
            }
            synchronized (map) {
                map.notify();
            }
        }
        return map.get(instanceNo);
    }

    public BufferedImage getBackward(int instanceNo) {
        initialNext = true;
        if (!map.containsKey(0) && instanceNo % 5 == 0) {
            map.tailMap(instanceNo + 5).clear();
            if (initialPrev) {
                initialPrev = false;
                i = map.firstKey();
                isForward = false;
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
                ApplicationContext.logger.log(Level.INFO, null, ex);
            }
        }
    }

    public void makeMeWait1() {
        synchronized (map) {
            try {
                map.wait();
            } catch (InterruptedException ex) {
                ApplicationContext.logger.log(Level.INFO, null, ex);
            }
        }
    }

    public void terminateThread() {
        synchronized (map) {
            map.notify();
            terminate = true;
            map.clear();
        }
    }

    public void forwardLoopBack() {
        initialPrev = true;
        map.clear();
        i = -1;
        isForward = true;
        startBuffering = false;
        synchronized (map) {
            map.notify();
        }
    }

    public void getBackwardLoopBack() {
        initialNext = true;
        map.clear();
        i = imgPanelRef.getTotalInstance() - 1;
        isForward = false;
        synchronized (map) {
            map.notify();
        }
    }

    @Override
    protected Void doInBackground() throws Exception {
        do {
            if (isForward && i < imgPanelRef.getTotalInstance() - 1) {
                i++;
                put(i, DicomImageReader.readDicomFile(new File(imgPanelRef.getFileLocation(i))));
            } else if (!isForward && i > 0) {
                i--;
                put(i, DicomImageReader.readDicomFile(new File(imgPanelRef.getFileLocation(i))));
            } else if (getState().equals(Thread.State.RUNNABLE)) {
                makeMeWait();
            }
        } while (!terminate);
        return null;
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
        if (instanceNo - map.firstKey() < 5 || map.lastKey() - instanceNo < 5) {
            map.clear();
            startBuffering = true;
            if (instanceNo - 10 < 0) {
                i = 0;
                isForward = true;
            } else {
                i = instanceNo - 10;
                isForward = true;
            }
            synchronized (map) {
                map.notify();
            }
            initialPrev = true;
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
                ApplicationContext.logger.log(Level.SEVERE, null, ex);
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
        tileLayoutThread.setUpdateFrom(updateFrom < 0 ? -1 : updateFrom, imgPanelRef.getTotalInstance());
        synchronized (map) {
            map.notify();
        }
    }

    public String getFileLocation(int i) {
        return imgPanelRef.getFileLocation(i);
    }

    public void shutDown() {
        this.terminate = true;
    }

    public void setImgPanelRef(ImagePanel imgPanel) {
        this.imgPanelRef = imgPanel;
    }
}