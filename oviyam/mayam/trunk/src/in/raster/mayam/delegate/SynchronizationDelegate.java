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
package in.raster.mayam.delegate;

import in.raster.mayam.context.ApplicationContext;
import in.raster.mayam.form.ImagePanel;
import in.raster.mayam.form.LayeredCanvas;
import javax.swing.JPanel;

/**
 *
 * @author  BabuHussain
 * @version 0.9
 *
 */
public class SynchronizationDelegate {

    public SynchronizationDelegate() {
    }

    public static void setSyncStartInstanceInAllTiles() {
        JPanel outerComponent = (JPanel) ApplicationContext.imgPanel.getCanvas().getLayeredCanvas().getParent();
        for (int i = 0; i < outerComponent.getComponentCount(); i++) {
            LayeredCanvas layeredCanvas = (LayeredCanvas) outerComponent.getComponent(i);
            ImagePanel imgPanel = layeredCanvas.imgpanel;
            if (ApplicationContext.imgPanel != imgPanel) {
                imgPanel.updateSyncStartInstance();
            }
        }
    }

    public void doTileSync() {
        JPanel outerComponent = (JPanel) ApplicationContext.imgPanel.getCanvas().getLayeredCanvas().getParent();
        for (int i = 0; i < outerComponent.getComponentCount(); i++) {
            LayeredCanvas layeredCanvas = (LayeredCanvas) outerComponent.getComponent(i);
            ImagePanel imgPanel = layeredCanvas.imgpanel;
            if (ApplicationContext.imgPanel != imgPanel) {
                if (ApplicationContext.imgPanel.getStudyUID().equalsIgnoreCase(imgPanel.getStudyUID())) {
                    if (ApplicationContext.imgPanel.getSeriesUID().equalsIgnoreCase(imgPanel.getSeriesUID()) && !imgPanel.isMulitiFrame()) {
                        imgPanel.setInstanceArryFromList();
                        imgPanel.setCurrentInstanceNo(ApplicationContext.imgPanel.getCurrentInstanceNo());
                        imgPanel.setImage(ApplicationContext.imgPanel.getCurrentInstanceNo());
                    } else {
                        if (ApplicationContext.imgPanel.getSliceLocation() != null && !ApplicationContext.imgPanel.getSliceLocation().equalsIgnoreCase("")) {
                            imgPanel.setInstanceArryFromList();
                            imgPanel.setImage(ApplicationContext.imgPanel.getSliceLocation());
                        }
                    }
                } else {
                    if (imgPanel.canBeProcessed()) {
                        int diff = ApplicationContext.imgPanel.getSyncDifference();
                        int syncStartInstance = imgPanel.getSyncStartInstance();
                        int instanceTobeSet = syncStartInstance + diff;
                        imgPanel.setInstanceArryFromList();
                        imgPanel.setCurrentInstanceNo(instanceTobeSet);
                        imgPanel.setImage(instanceTobeSet);
                    }
                }
            }
        }
    }
}
