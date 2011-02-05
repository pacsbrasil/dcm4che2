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
import in.raster.mayam.util.localizer.SliceLocator;
import in.raster.mayam.model.ScoutLineInfoModel;
import javax.swing.JPanel;

/**
 *
 * @author  BabuHussain
 * @version 0.6
 *
 */
public class LocalizerDelegate {

    private SliceLocator locator = new SliceLocator();

    public LocalizerDelegate() {
    }

    public boolean drawScoutLine() {
        boolean retVal = false;
        if (ApplicationContext.imgPanel != null) {
            if (!ApplicationContext.imgPanel.isLocalizer()) {
                for (int j = 0; j < ((JPanel) ApplicationContext.imgView.jTabbedPane1.getSelectedComponent()).getComponentCount(); j++) {
                    try {
                        if (ApplicationContext.imgPanel.getReferencedSOPInstanceUID().equalsIgnoreCase(((LayeredCanvas) ((JPanel) ApplicationContext.imgView.jTabbedPane1.getSelectedComponent()).getComponent(j)).imgpanel.getInstanceUID())) {
                            String scoutPos = ((LayeredCanvas) ((JPanel) ApplicationContext.imgView.jTabbedPane1.getSelectedComponent()).getComponent(j)).imgpanel.getImagePosition();
                            String scoutOrientation = ((LayeredCanvas) ((JPanel) ApplicationContext.imgView.jTabbedPane1.getSelectedComponent()).getComponent(j)).imgpanel.getImageOrientation();
                            String scoutPixelSpacing = ((LayeredCanvas) ((JPanel) ApplicationContext.imgView.jTabbedPane1.getSelectedComponent()).getComponent(j)).imgpanel.getPixelSpacing();
                            int scoutRow = ((LayeredCanvas) ((JPanel) ApplicationContext.imgView.jTabbedPane1.getSelectedComponent()).getComponent(j)).imgpanel.getRow();
                            int scoutColumn = ((LayeredCanvas) ((JPanel) ApplicationContext.imgView.jTabbedPane1.getSelectedComponent()).getComponent(j)).imgpanel.getColumn();
                            String imgPos = ApplicationContext.imgPanel.getImagePosition();
                            String imgOrientation = ApplicationContext.imgPanel.getImageOrientation();
                            String imgPixelSpacing = ApplicationContext.imgPanel.getPixelSpacing();
                            int imgRow = ApplicationContext.imgPanel.getRow();
                            int imgColumn = ApplicationContext.imgPanel.getColumn();
                            locator.projectSlice(scoutPos, scoutOrientation, scoutPixelSpacing, scoutRow, scoutColumn, imgPos, imgOrientation, imgPixelSpacing, imgRow, imgColumn);
                            ((LayeredCanvas) ((JPanel) ApplicationContext.imgView.jTabbedPane1.getSelectedComponent()).getComponent(j)).imgpanel.setScoutCoordinates((int) locator.getBoxUlx(), (int) locator.getBoxUly(), (int) locator.getBoxLlx(), (int) locator.getBoxLly(), (int) locator.getBoxUrx(), (int) locator.getBoxUry(), (int) locator.getBoxLrx(), (int) locator.getBoxLry());
                            retVal = true;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return retVal;
    }

    public static void hideScoutLine() {
        if (ApplicationContext.imgPanel != null) {
            if (!ApplicationContext.imgPanel.isLocalizer()) {
                for (int j = 0; j < ((JPanel) ApplicationContext.imgView.jTabbedPane1.getSelectedComponent()).getComponentCount(); j++) {
                    try {
                        LayeredCanvas temp = ((LayeredCanvas) ((JPanel) ApplicationContext.imgView.jTabbedPane1.getSelectedComponent()).getComponent(j));
                        if (ApplicationContext.imgPanel.getReferencedSOPInstanceUID() != null && temp.imgpanel != null && ApplicationContext.imgPanel.getReferencedSOPInstanceUID().equalsIgnoreCase(temp.imgpanel.getInstanceUID())) {
                            if (ApplicationContext.imgPanel.getFrameOfReferenceUID().equalsIgnoreCase(temp.imgpanel.getFrameOfReferenceUID())) {
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
        if (ApplicationContext.imgPanel != null) {
            if (!ApplicationContext.imgPanel.isLocalizer()) {
                ImagePanel.setDisplayScout(true);
                for (int j = 0; j < ((JPanel) ApplicationContext.imgView.jTabbedPane1.getSelectedComponent()).getComponentCount(); j++) {
                    try {
                        LayeredCanvas temp = ((LayeredCanvas) ((JPanel) ApplicationContext.imgView.jTabbedPane1.getSelectedComponent()).getComponent(j));
                        if (ApplicationContext.imgPanel.getReferencedSOPInstanceUID() != null && temp.imgpanel != null && ApplicationContext.imgPanel.getReferencedSOPInstanceUID().equalsIgnoreCase(temp.imgpanel.getInstanceUID())) {
                            projectSlice(temp);

                        } else {
                            if (temp.imgpanel != null && temp.imgpanel.isLocalizer()) {
                                if (ApplicationContext.imgPanel.getFrameOfReferenceUID().equalsIgnoreCase(temp.imgpanel.getFrameOfReferenceUID())) {
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
        String scoutPos = temp.imgpanel.getImagePosition();
        String scoutOrientation = temp.imgpanel.getImageOrientation();
        String scoutPixelSpacing = temp.imgpanel.getPixelSpacing();
        int scoutRow = temp.imgpanel.getRow();
        int scoutColumn = temp.imgpanel.getColumn();
        String imgPos = ApplicationContext.imgPanel.getImagePosition();
        String imgOrientation = ApplicationContext.imgPanel.getImageOrientation();
        String imgPixelSpacing = ApplicationContext.imgPanel.getPixelSpacing();
        int imgRow = ApplicationContext.imgPanel.getRow();
        int imgColumn = ApplicationContext.imgPanel.getColumn();
        ScoutLineInfoModel[] borderLineArray = ApplicationContext.imgPanel.getScoutBorder();
        locator.projectSlice(scoutPos, scoutOrientation, scoutPixelSpacing, scoutRow, scoutColumn, borderLineArray[0].getImagePosition(), borderLineArray[0].getImageOrientation(), borderLineArray[0].getImagePixelSpacing(), borderLineArray[0].getImageRow(), borderLineArray[0].getImageColumn());
        temp.imgpanel.setScoutBorder1Coordinates((int) locator.getBoxUlx(), (int) locator.getBoxUly(), (int) locator.getBoxLlx(), (int) locator.getBoxLly());
        temp.imgpanel.setAxis1Coordinates((int) locator.getmAxisLeftx(), (int) locator.getmAxisLefty(), (int) locator.getmAxisRightx(), (int) locator.getmAxisRighty(), (int) locator.getmAxisTopx(), (int) locator.getmAxisTopy(), (int) locator.getmAxisBottomx(), (int) locator.getmAxisBottomy());
        locator.projectSlice(scoutPos, scoutOrientation, scoutPixelSpacing, scoutRow, scoutColumn, borderLineArray[1].getImagePosition(), borderLineArray[1].getImageOrientation(), borderLineArray[1].getImagePixelSpacing(), borderLineArray[1].getImageRow(), borderLineArray[1].getImageColumn());
        temp.imgpanel.setScoutBorder2Coordinates((int) locator.getBoxUlx(), (int) locator.getBoxUly(), (int) locator.getBoxLlx(), (int) locator.getBoxLly());
        temp.imgpanel.setAxis2Coordinates((int) locator.getmAxisLeftx(), (int) locator.getmAxisLefty(), (int) locator.getmAxisRightx(), (int) locator.getmAxisRighty(), (int) locator.getmAxisTopx(), (int) locator.getmAxisTopy(), (int) locator.getmAxisBottomx(), (int) locator.getmAxisBottomy());
        locator.projectSlice(scoutPos, scoutOrientation, scoutPixelSpacing, scoutRow, scoutColumn, imgPos, imgOrientation, imgPixelSpacing, imgRow, imgColumn);
        temp.imgpanel.setScoutCoordinates((int) locator.getBoxUlx(), (int) locator.getBoxUly(), (int) locator.getBoxLlx(), (int) locator.getBoxLly(), (int) locator.getBoxUrx(), (int) locator.getBoxUry(), (int) locator.getBoxLrx(), (int) locator.getBoxLry());
        temp.imgpanel.setAxisCoordinates((int) locator.getmAxisLeftx(), (int) locator.getmAxisLefty(), (int) locator.getmAxisRightx(), (int) locator.getmAxisRighty(), (int) locator.getmAxisTopx(), (int) locator.getmAxisTopy(), (int) locator.getmAxisBottomx(), (int) locator.getmAxisBottomy());
        return true;
    }
}
