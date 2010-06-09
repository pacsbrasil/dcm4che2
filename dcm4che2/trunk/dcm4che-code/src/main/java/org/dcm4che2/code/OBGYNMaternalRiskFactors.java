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
 * CID 6088 OB-GYN Maternal Risk Factors.
 *
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @version $Rev$ $Date::            $
 * @since Jun 2, 2010
 */
public class OBGYNMaternalRiskFactors {

  /** (111573, DCM, "Current pregnancy, known or suspected malformations/syndromes") */
  public static final String CurrentPregnancyKnownOrSuspectedMalformationsSyndromes = "111573\\DCM";

  /** (111574, DCM, "Family history, fetal malformation/syndrome") */
  public static final String FamilyHistoryFetalMalformationSyndrome = "111574\\DCM";

  /** (111567, DCM, "Gynecologic condition") */
  public static final String GynecologicCondition = "111567\\DCM";

  /** (111568, DCM, "Gynecologic surgery") */
  public static final String GynecologicSurgery = "111568\\DCM";

  /** (G-0335, SRT, "History of - cardiovascular disease") */
  public static final String HistoryOfCardiovascularDisease = "G-0335\\SRT";

  /** (G-023F, SRT, "History of - diabetes mellitus") */
  public static final String HistoryOfDiabetesMellitus = "G-023F\\SRT";

  /** (G-031E, SRT, "History of - eclampsia") */
  public static final String HistoryOfEclampsia = "G-031E\\SRT";

  /** (G-0304, SRT, "History of - ectopic pregnancy") */
  public static final String HistoryOfEctopicPregnancy = "G-0304\\SRT";

  /** (G-0269, SRT, "History of - hypertension") */
  public static final String HistoryOfHypertension = "G-0269\\SRT";

  /** (G-0319, SRT, "History of infertility") */
  public static final String HistoryOfInfertility = "G-0319\\SRT";

  /** (111572, DCM, "History of multiple fetuses") */
  public static final String HistoryOfMultipleFetuses = "111572\\DCM";

  /** (G-0244, SRT, "History of - obesity") */
  public static final String HistoryOfObesity = "G-0244\\SRT";

  /** (G-0305, SRT, "History of - premature delivery") */
  public static final String HistoryOfPrematureDelivery = "G-0305\\SRT";

  /** (G-02D0, SRT, "History of - regular medication") */
  public static final String HistoryOfRegularMedication = "G-02D0\\SRT";

  /** (G-031F, SRT, "History of - severe pre-eclampsia") */
  public static final String HistoryOfSeverePreEclampsia = "G-031F\\SRT";

  /** (G-0338, SRT, "History of substance abuse") */
  public static final String HistoryOfSubstanceAbuse = "G-0338\\SRT";

  /** (D8-20100, SRT, "Multiple pregnancy") */
  public static final String MultiplePregnancy = "D8-20100\\SRT";

  /** (111570, DCM, "Previous fetal malformation/syndrome") */
  public static final String PreviousFetalMalformationSyndrome = "111570\\DCM";

  /** (111569, DCM, "Previous LBW or IUGR birth") */
  public static final String PreviousLBWOrIUGRBirth = "111569\\DCM";

  /** (111571, DCM, "Previous RH negative or blood dyscrasia at birth") */
  public static final String PreviousRHNegativeOrBloodDyscrasiaAtBirth = "111571\\DCM";

  /** (111566, DCM, "Spontaneous Abortion") */
  public static final String SpontaneousAbortion = "111566\\DCM";

  /** (111565, DCM, "Uterine malformations") */
  public static final String UterineMalformations = "111565\\DCM";
}
