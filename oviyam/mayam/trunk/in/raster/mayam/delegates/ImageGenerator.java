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

/**
 *
 * @author Devishree
 * @version 2.0
 */
public class ImageGenerator extends Thread {

    ImageBuffer buffer;
    ImagePanel imgPanel;
    int startFrom = 0, end = 0, total = 0;
    boolean isForward, terminate, isImageLayout;

    public ImageGenerator(ImageBuffer buffer, ImagePanel imgPanel, boolean isImageLayout) {
        this.buffer = buffer;
        this.imgPanel = imgPanel;
        this.total = imgPanel.getTotalInstance();
        terminate = false;
        this.isImageLayout = isImageLayout;
    }

    @Override
    public void run() {
        if (!isImageLayout) {
            if (isForward) {
                if (end > total) {
                    end = total;
                }
                if (startFrom < 0) {
                    startFrom = 0;
                }
                for (int i = startFrom; i < end; i++) {
                    if (!terminate) {
                        buffer.put(i, imgPanel.readDicomFile(i));
                    } else {
                        break;
                    }

                }
            } else {
                if (startFrom < 0) {
                    startFrom = 0;
                }
                if (end < 0) {
                    end = 0;
                }
                for (int i = startFrom; i >= end; i--) {
                    if (!terminate) {
                        buffer.put(i, imgPanel.readDicomFile(i));
                    } else {
                        break;
                    }
                }
            }
        } else {
            if (startFrom < end) {
                if (end > total) {
                    end = total;
                }
                for (int i = startFrom; i < end; i++) {
                    if (!terminate) {
                        buffer.put(i, imgPanel.readDicomFile(i));
                    } else {
                        break;
                    }
                }
            } else {
                if (startFrom < 0) {
                    startFrom = 0;
                }
                for (int i = startFrom; i < total; i++) {
                    if (!terminate) {
                        buffer.put(i, imgPanel.readDicomFile(i));
                    } else {
                        break;
                    }
                }

                for (int i = 0; i < end; i++) {
                    if (!terminate) {
                        buffer.put(i, imgPanel.readDicomFile(i));
                    } else {
                        break;
                    }
                }
            }
        }
        terminate = true;
        buffer.setUpdationStarted(false);
    }

    public void setParameters(int start, int end, boolean isForward) {
        this.startFrom = start;
        this.end = end;
        this.isForward = isForward;
    }

    public void terminateThread() {
        terminate = true;
    }
}
