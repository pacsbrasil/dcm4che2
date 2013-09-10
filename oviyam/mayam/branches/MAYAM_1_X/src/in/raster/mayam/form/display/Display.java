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
package in.raster.mayam.form.display;

import java.awt.Toolkit;
import javax.swing.JDialog;
import javax.swing.JFrame;

/**
 *
 * @author  BabuHussain
 * @version 0.5
 *
 */
public class Display {

    public Display() {
    }
    /**
     * This routine used to align the specified frame to the center position of the screen.
     * @param toBeAligned
     */
    public static void alignScreen(JFrame toBeAligned) {
        int frameWidh = toBeAligned.getWidth();
        int frameHeight = toBeAligned.getHeight();
        int scrWidth = Toolkit.getDefaultToolkit().getScreenSize().width;
        int scrHeight = Toolkit.getDefaultToolkit().getScreenSize().height;
        int centerX = scrWidth / 2;
        int cenetrY = scrHeight / 2;
        toBeAligned.setLocation(centerX - (frameWidh / 2), cenetrY - (frameHeight / 2));
    }
    /**
     * This routine used to align the specified dialog to the center position of the screen.
     * @param toBeAligned
     */
    public static void alignScreen(JDialog toBeAligned) {
        int frameWidh = toBeAligned.getWidth();
        int frameHeight = toBeAligned.getHeight();
        int scrWidth = Toolkit.getDefaultToolkit().getScreenSize().width;
        int scrHeight = Toolkit.getDefaultToolkit().getScreenSize().height;
        int centerX = scrWidth / 2;
        int cenetrY = scrHeight / 2;
        toBeAligned.setLocation(centerX - (frameWidh / 2), cenetrY - (frameHeight / 2));
    }
}
