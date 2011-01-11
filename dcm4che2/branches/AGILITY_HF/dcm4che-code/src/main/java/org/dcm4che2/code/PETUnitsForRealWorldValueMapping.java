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
 * CID 84 PET Units for Real World Value Mapping.
 *
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @version $Rev$ $Date::            $
 * @since Jun 2, 2010
 */
public class PETUnitsForRealWorldValueMapping {

  /** (Bq/ml, UCUM, "Becquerels/milliliter") */
  public static final String BecquerelsMilliliter = "Bq/ml\\UCUM";

  /** (/cm, UCUM, "/Centimeter") */
  public static final String Centimeter = "/cm\\UCUM";

  /** (cm^2, UCUM, "Centimeter**2") */
  public static final String Centimeter2 = "cm^2\\UCUM";

  /** ({counts}, UCUM, "Counts") */
  public static final String Counts = "{counts}\\UCUM";

  /** ({counts}/s, UCUM, "Counts per second") */
  public static final String CountsPerSecond = "{counts}/s\\UCUM";

  /** (umol/ml, UCUM, "Micromole/milliliter") */
  public static final String MicromoleMilliliter = "umol/ml\\UCUM";

  /** (umol/min/ml, UCUM, "Micromole/minute/milliliter") */
  public static final String MicromoleMinuteMilliliter = "umol/min/ml\\UCUM";

  /** (mg/min/ml, UCUM, "Milligrams/minute/milliliter") */
  public static final String MilligramsMinuteMilliliter = "mg/min/ml\\UCUM";

  /** (ml/g, UCUM, "Milliliter/gram") */
  public static final String MilliliterGram = "ml/g\\UCUM";

  /** (ml/min/g, UCUM, "Milliliter/minute/gram") */
  public static final String MilliliterMinuteGram = "ml/min/g\\UCUM";

  /** (%, UCUM, "Percent") */
  public static final String Percent = "%\\UCUM";

  /** ({propcounts}, UCUM, "Proportional to counts") */
  public static final String ProportionalToCounts = "{propcounts}\\UCUM";

  /** ({propcounts}/s, UCUM, "Proportional to counts per second") */
  public static final String ProportionalToCountsPerSecond = "{propcounts}/s\\UCUM";

  /** ({SUVbsa}cm2/ml, UCUM, "Standardized Uptake Value body surface area") */
  public static final String StandardizedUptakeValueBodySurfaceArea = "{SUVbsa}cm2/ml\\UCUM";

  /** ({SUVbw}g/ml, UCUM, "Standardized Uptake Value body weight") */
  public static final String StandardizedUptakeValueBodyWeight = "{SUVbw}g/ml\\UCUM";

  /** ({SUVlbm}g/ml, UCUM, "Standardized Uptake Value lean body mass") */
  public static final String StandardizedUptakeValueLeanBodyMass = "{SUVlbm}g/ml\\UCUM";
}
