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
package in.raster.mayam.models;

/**
 *
 * @author Devishree
 * @version 2.0
 *
 */
public class PresetModel {

    private int pk;
    private String modality;
    private String presetName;
    private String windowWidth;
    private String windowLevel;

    public PresetModel() {
    }

    public PresetModel(String modality, String presetName, String windowWidth, String windowLevel) {
        this.modality = modality;
        this.presetName = presetName;
        this.windowWidth = windowWidth;
        this.windowLevel = windowLevel;
    }

    public PresetModel(int pk, String modality, String presetName, String windowWidth, String windowLevel) {
        this.pk = pk;
        this.modality = modality;
        this.presetName = presetName;
        this.windowWidth = windowWidth;
        this.windowLevel = windowLevel;
    }

    public int getPk() {
        return pk;
    }

    public void setPk(int pk) {
        this.pk = pk;
    }

    public String getModality() {
        return modality;
    }

    public void setModality(String modality) {
        this.modality = modality;
    }

    public String getPresetName() {
        return presetName;
    }

    public void setPresetName(String presetName) {
        this.presetName = presetName;
    }

    public String getWindowLevel() {
        return windowLevel;
    }

    public void setWindowLevel(String windowLevel) {
        this.windowLevel = windowLevel;
    }

    public String getWindowWidth() {
        return windowWidth;
    }

    public void setWindowWidth(String windowWidth) {
        this.windowWidth = windowWidth;
    }
}
