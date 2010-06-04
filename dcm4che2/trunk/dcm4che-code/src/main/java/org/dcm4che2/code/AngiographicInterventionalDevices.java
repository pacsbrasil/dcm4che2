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
 * CID 8 Angiographic Interventional Devices.
 *
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @version $Revision$ $Date$
 * @since Jun 2, 2010
 */
public class AngiographicInterventionalDevices {

  /** (A-25600, SRT, "Atherectomy device") */
  public static final String AtherectomyDevice = "A-25600\\SRT";

  /** (A-26800, SRT, "Catheter") */
  public static final String Catheter = "A-26800\\SRT";

  /** (A-27322, SRT, "Detachable balloon") */
  public static final String DetachableBalloon = "A-27322\\SRT";

  /** (A-25614, SRT, "Embolization ball") */
  public static final String EmbolizationBall = "A-25614\\SRT";

  /** (A-25612, SRT, "Embolization coil") */
  public static final String EmbolizationCoil = "A-25612\\SRT";

  /** (A-25616, SRT, "Embolization particulate") */
  public static final String EmbolizationParticulate = "A-25616\\SRT";

  /** (A-26A06, SRT, "Fixed object") */
  public static final String FixedObject = "A-26A06\\SRT";

  /** (C-20005, SRT, "Glue") */
  public static final String Glue = "C-20005\\SRT";

  /** (A-26A08, SRT, "Grid") */
  public static final String Grid = "A-26A08\\SRT";

  /** (A-26802, SRT, "Guiding catheter") */
  public static final String GuidingCatheter = "A-26802\\SRT";

  /** (A-81080, SRT, "Laser") */
  public static final String Laser = "A-81080\\SRT";

  /** (A-10141, SRT, "Measuring ruler") */
  public static final String MeasuringRuler = "A-10141\\SRT";

  /** (A-26912, SRT, "Percutaneous transluminal angioplasty balloon") */
  public static final String PercutaneousTransluminalAngioplastyBalloon = "A-26912\\SRT";

  /** (A-25610, SRT, "Rotational atherectomy device") */
  public static final String RotationalAtherectomyDevice = "A-25610\\SRT";

  /** (122485, DCM, "Sphere") */
  public static final String Sphere = "122485\\DCM";

  /** (A-25500, SRT, "Stent") */
  public static final String Stent = "A-25500\\SRT";
}
