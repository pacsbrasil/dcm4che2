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
 * CID 3261 Stress Protocols.
 *
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @version $Rev$ $Date::            $
 * @since Jun 2, 2010
 */
public class StressProtocols {

  /** (P2-31109, SRT, "Adenosine Stress protocol") */
  public static final String AdenosineStressProtocol = "P2-31109\\SRT";

  /** (P2-7131C, SRT, "Balke protocol") */
  public static final String BalkeProtocol = "P2-7131C\\SRT";

  /** (P2-7131A, SRT, "Bruce protocol") */
  public static final String BruceProtocol = "P2-7131A\\SRT";

  /** (P2-3110A, SRT, "Dipyridamole Stress protocol") */
  public static final String DipyridamoleStressProtocol = "P2-3110A\\SRT";

  /** (P2-31108, SRT, "Dobutamine Stress protocol") */
  public static final String DobutamineStressProtocol = "P2-31108\\SRT";

  /** (P2-7131D, SRT, "Ellestad protocol") */
  public static final String EllestadProtocol = "P2-7131D\\SRT";

  /** (P2-31010, SRT, "Exercise stress ECG test") */
  public static final String ExerciseStressECGTest = "P2-31010\\SRT";

  /** (P2-7131B, SRT, "Modified Bruce protocol") */
  public static final String ModifiedBruceProtocol = "P2-7131B\\SRT";

  /** (P2-713A1, SRT, "Modified Naughton protocol") */
  public static final String ModifiedNaughtonProtocol = "P2-713A1\\SRT";

  /** (P2-713A0, SRT, "Naughton protocol") */
  public static final String NaughtonProtocol = "P2-713A0\\SRT";

  /** (P2-7131F, SRT, "Pepper protocol") */
  public static final String PepperProtocol = "P2-7131F\\SRT";

  /** (P2-31011, SRT, "Pharmacologic and exercise stress test") */
  public static final String PharmacologicAndExerciseStressTest = "P2-31011\\SRT";

  /** (P2-31107, SRT, "Pharmacologic Stress protocol") */
  public static final String PharmacologicStressProtocol = "P2-31107\\SRT";

  /** (P2-7131E, SRT, "Ramp protocol") */
  public static final String RampProtocol = "P2-7131E\\SRT";

  /** (P2-31102, SRT, "Stress test using Bicycle Ergometer") */
  public static final String StressTestUsingBicycleErgometer = "P2-31102\\SRT";

  /** (P2-3110B, SRT, "Stress test using cardiac pacing") */
  public static final String StressTestUsingCardiacPacing = "P2-3110B\\SRT";
}
