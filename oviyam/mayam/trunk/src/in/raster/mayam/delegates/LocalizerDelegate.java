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

import in.raster.mayam.context.ApplicationContext;
import in.raster.mayam.form.ImagePanel;
import in.raster.mayam.form.LayeredCanvas;
import in.raster.mayam.models.ScoutLineInfoModel;
import in.raster.mayam.util.localizer.SliceLocator;
import javax.swing.JPanel;
import javax.swing.JSplitPane;

/**
 *
 * @author BabuHussain
 * @version 0.6
 *
 */
public class LocalizerDelegate extends Thread {

    private SliceLocator locator = new SliceLocator();
    boolean borderAlreadyPresent;

    public LocalizerDelegate(boolean borderAlreadyPresent) {
        this.borderAlreadyPresent = borderAlreadyPresent;
    }

    @Override
    public void run() {
        drawScoutLineWithBorder();
    }

    public static void hideScoutLine() {
        JPanel panel = ((JPanel) ((JSplitPane) ApplicationContext.tabbedPane.getSelectedComponent()).getRightComponent());
        if (ApplicationContext.layeredCanvas.imgpanel != null) {
            if (!ApplicationContext.layeredCanvas.imgpanel.isLocalizer()) {
                for (int j = 0; j < panel.getComponentCount(); j++) {
                    try {
                        LayeredCanvas temp = ((LayeredCanvas) ((JPanel) panel.getComponent(j)).getComponent(0));
                        if (ApplicationContext.layeredCanvas.imgpanel.getReferencedSOPInstanceUID() != null && temp.imgpanel != null && ApplicationContext.layeredCanvas.imgpanel.getReferencedSOPInstanceUID().equalsIgnoreCase(temp.imgpanel.getInstanceUID())) {
                            if (ApplicationContext.layeredCanvas.imgpanel.getFrameOfReferenceUID().equalsIgnoreCase(temp.imgpanel.getFrameOfReferenceUID())) {
                                temp.imgpanel.repaint();
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public boolean drawScoutLineWithBorder() {
        boolean retVal = false;
        JPanel panel = ((JPanel) ((JSplitPane) ApplicationContext.tabbedPane.getSelectedComponent()).getRightComponent());
        if (ApplicationContext.layeredCanvas.imgpanel != null) {
            if (!ApplicationContext.layeredCanvas.imgpanel.isLocalizer()) {
                ImagePanel.setDisplayScout(true);
                for (int j = 0; j < panel.getComponentCount(); j++) {
                    try {
                        LayeredCanvas temp = ((LayeredCanvas) ((JPanel) panel.getComponent(j)).getComponent(0));
                        if (ApplicationContext.layeredCanvas.imgpanel.getReferencedSOPInstanceUID() != null && temp.imgpanel != null && ApplicationContext.layeredCanvas.imgpanel.getReferencedSOPInstanceUID().equalsIgnoreCase(temp.imgpanel.getInstanceUID())) {
                            projectSlice(temp);
                        } else {
                            if (temp.imgpanel != null && temp.imgpanel.isLocalizer()) {
                                if (ApplicationContext.layeredCanvas.imgpanel.getFrameOfReferenceUID().equalsIgnoreCase(temp.imgpanel.getFrameOfReferenceUID())) {
                                    projectSlice(temp);
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return retVal;
    }

    private boolean projectSlice(LayeredCanvas temp) {
        ScoutLineInfoModel scoutParmas = temp.imgpanel.getCurrentScoutDetails();
        ScoutLineInfoModel scoutLineDetails = ApplicationContext.layeredCanvas.imgpanel.getCurrentScoutDetails();
        if (!borderAlreadyPresent) {
            ScoutLineInfoModel[] borderLineArray = ApplicationContext.layeredCanvas.imgpanel.prepareScoutBorder();
            locator.projectSlice(scoutParmas.getImagePosition(), scoutParmas.getImageOrientation(), scoutParmas.getImagePixelSpacing(), scoutParmas.getImageRow(), scoutParmas.getImageColumn(), borderLineArray[0].getImagePosition(), borderLineArray[0].getImageOrientation(), borderLineArray[0].getImagePixelSpacing(), borderLineArray[0].getImageRow(), borderLineArray[0].getImageColumn());
            temp.imgpanel.setScoutBorder1Coordinates((int) locator.getBoxUlx(), (int) locator.getBoxUly(), (int) locator.getBoxLlx(), (int) locator.getBoxLly());
            temp.imgpanel.setAxis1Coordinates((int) locator.getmAxisLeftx(), (int) locator.getmAxisLefty(), (int) locator.getmAxisRightx(), (int) locator.getmAxisRighty(), (int) locator.getmAxisTopx(), (int) locator.getmAxisTopy(), (int) locator.getmAxisBottomx(), (int) locator.getmAxisBottomy());
            locator.projectSlice(scoutParmas.getImagePosition(), scoutParmas.getImageOrientation(), scoutParmas.getImagePixelSpacing(), scoutParmas.getImageRow(), scoutParmas.getImageColumn(), borderLineArray[1].getImagePosition(), borderLineArray[1].getImageOrientation(), borderLineArray[1].getImagePixelSpacing(), borderLineArray[1].getImageRow(), borderLineArray[1].getImageColumn());
            temp.imgpanel.setScoutBorder2Coordinates((int) locator.getBoxUlx(), (int) locator.getBoxUly(), (int) locator.getBoxLlx(), (int) locator.getBoxLly());
            temp.imgpanel.setAxis2Coordinates((int) locator.getmAxisLeftx(), (int) locator.getmAxisLefty(), (int) locator.getmAxisRightx(), (int) locator.getmAxisRighty(), (int) locator.getmAxisTopx(), (int) locator.getmAxisTopy(), (int) locator.getmAxisBottomx(), (int) locator.getmAxisBottomy());
        }
        locator.projectSlice(scoutParmas.getImagePosition(), scoutParmas.getImageOrientation(), scoutParmas.getImagePixelSpacing(), scoutParmas.getImageRow(), scoutParmas.getImageColumn(), scoutLineDetails.getImagePosition(), scoutLineDetails.getImageOrientation(), scoutLineDetails.getImagePixelSpacing(), scoutLineDetails.getImageRow(), scoutLineDetails.getImageColumn());
        temp.imgpanel.setScoutCoordinates((int) locator.getBoxUlx(), (int) locator.getBoxUly(), (int) locator.getBoxLlx(), (int) locator.getBoxLly(), (int) locator.getBoxUrx(), (int) locator.getBoxUry(), (int) locator.getBoxLrx(), (int) locator.getBoxLry());
        temp.imgpanel.setAxisCoordinates((int) locator.getmAxisLeftx(), (int) locator.getmAxisLefty(), (int) locator.getmAxisRightx(), (int) locator.getmAxisRighty(), (int) locator.getmAxisTopx(), (int) locator.getmAxisTopy(), (int) locator.getmAxisBottomx(), (int) locator.getmAxisBottomy());
        return true;
    }

    public static void hideAllScoutLines() {
        ImagePanel.setDisplayScout(false);
        JPanel panel = ((JPanel) ((JSplitPane) ApplicationContext.tabbedPane.getSelectedComponent()).getRightComponent());
        if (ApplicationContext.layeredCanvas.imgpanel != null) {
            if (!ApplicationContext.layeredCanvas.imgpanel.isLocalizer()) {
                for (int j = 0; j < panel.getComponentCount(); j++) {
                    try {
                        LayeredCanvas temp = ((LayeredCanvas) ((JPanel) panel.getComponent(j)).getComponent(0));
                        if (temp.imgpanel != null) {
                            temp.imgpanel.repaint();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}