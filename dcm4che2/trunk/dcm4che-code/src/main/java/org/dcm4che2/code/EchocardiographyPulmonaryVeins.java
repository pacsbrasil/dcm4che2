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
 * CID 12214 Echocardiography Pulmonary Veins.
 *
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @version $Revision$ $Date$
 * @since Jun 2, 2010
 */
public class EchocardiographyPulmonaryVeins {

  /** (8867-4, LN, "Heart rate") */
  public static final String HeartRate = "8867-4\\LN";

  /** (29453-8, LN, "Pulmonary Vein Atrial Contraction Reversal Peak Velocity") */
  public static final String PulmonaryVeinAtrialContractionReversalPeakVelocity = "29453-8\\LN";

  /** (G-038B, SRT, "Pulmonary Vein A-Wave Duration") */
  public static final String PulmonaryVeinAWaveDuration = "G-038B\\SRT";

  /** (29451-2, LN, "Pulmonary Vein Diastolic Peak Velocity") */
  public static final String PulmonaryVeinDiastolicPeakVelocity = "29451-2\\LN";

  /** (G-038D, SRT, "Pulmonary Vein D-Wave Velocity Time Integral") */
  public static final String PulmonaryVeinDWaveVelocityTimeIntegral = "G-038D\\SRT";

  /** (G-038C, SRT, "Pulmonary Vein S-Wave Velocity Time Integral") */
  public static final String PulmonaryVeinSWaveVelocityTimeIntegral = "G-038C\\SRT";

  /** (29450-4, LN, "Pulmonary Vein Systolic Peak Velocity") */
  public static final String PulmonaryVeinSystolicPeakVelocity = "29450-4\\LN";

  /** (29452-0, LN, "Pulmonary Vein Systolic to Diastolic Ratio") */
  public static final String PulmonaryVeinSystolicToDiastolicRatio = "29452-0\\LN";
}
