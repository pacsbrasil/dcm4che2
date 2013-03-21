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
 * CID 3254 Electrophysiology Procedure Phase.
 *
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @version $Rev$ $Date::            $
 * @since Jun 2, 2010
 */
public class ElectrophysiologyProcedurePhase {

  /** (G-729D, SRT, "Atrial Effective Refractory Period, evaluation of") */
  public static final String AtrialEffectiveRefractoryPeriodEvaluationOf = "G-729D\\SRT";

  /** (G-7304, SRT, "Carotid Sinus Massage procedure phase") */
  public static final String CarotidSinusMassageProcedurePhase = "G-7304\\SRT";

  /** (G-7306, SRT, "Electrophysiology Mapping phase") */
  public static final String ElectrophysiologyMappingPhase = "G-7306\\SRT";

  /** (G-729A, SRT, "Electrophysiology procedure baseline phase") */
  public static final String ElectrophysiologyProcedureBaselinePhase = "G-729A\\SRT";

  /** (G-7408, SRT, "Post-ablation phase") */
  public static final String PostAblationPhase = "G-7408\\SRT";

  /** (G-7305, SRT, "Post-defibrillation procedure phase") */
  public static final String PostDefibrillationProcedurePhase = "G-7305\\SRT";

  /** (G-729F, SRT, "Radiofrequency Ablation procedure phase") */
  public static final String RadiofrequencyAblationProcedurePhase = "G-729F\\SRT";

  /** (G-729C, SRT, "Sinus Node Recovery Time, evaluation of") */
  public static final String SinusNodeRecoveryTimeEvaluationOf = "G-729C\\SRT";

  /** (G-729E, SRT, "Ventricular Effective Refractory Period, evaluation of") */
  public static final String VentricularEffectiveRefractoryPeriodEvaluationOf = "G-729E\\SRT";
}
