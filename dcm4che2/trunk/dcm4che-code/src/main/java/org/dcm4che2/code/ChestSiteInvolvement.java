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
 * The Original Code is part of dcm4che, an implementation of DICOM(TM) in
 * Java(TM), available at http://sourceforge.net/projects/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * Agfa HealthCare.
 * Portions created by the Initial Developer are Copyright (C) 2010
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * See listed authors below.
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

package org.dcm4che2.code;

/**
 * CID 6129 Chest Site Involvement.
 *
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @version $Revision$ $Date$
 * @since Jun 2, 2010
 */
public class ChestSiteInvolvement {

  /** (T-42000, SRT, "Aorta") */
  public static final String Aorta = "T-42000\\SRT";

  /** (R-40939, SRT, "Bronchial") */
  public static final String Bronchial = "R-40939\\SRT";

  /** (T-D3050, SRT, "Chest wall") */
  public static final String ChestWall = "T-D3050\\SRT";

  /** (T-28080, SRT, "Hilum of lung") */
  public static final String HilumOfLung = "T-28080\\SRT";

  /** (T-1A007, SRT, "Interstitial tissue") */
  public static final String InterstitialTissue = "T-1A007\\SRT";

  /** (112158, DCM, "Lobar") */
  public static final String Lobar = "112158\\DCM";

  /** (T-28000, SRT, "Lung") */
  public static final String Lung = "T-28000\\SRT";

  /** (T-D3300, SRT, "Mediastinum") */
  public static final String Mediastinum = "T-D3300\\SRT";

  /** (T-29000, SRT, "Pleural structure") */
  public static final String PleuralStructure = "T-29000\\SRT";

  /** (T-D4001, SRT, "Upper abdomen") */
  public static final String UpperAbdomen = "T-D4001\\SRT";
}
