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
 * CID 6200 Colon Overall Assessment.
 *
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @version $Revision$ $Date$
 * @since Jun 2, 2010
 */
public class ColonOverallAssessment {

  /** (112240, DCM, "C0 - Inadequate Study/Awaiting Prior Comparisons") */
  public static final String C0InadequateStudyAwaitingPriorComparisons = "112240\\DCM";

  /** (112241, DCM, "C1 - Normal Colon or Benign Lesion") */
  public static final String C1NormalColonOrBenignLesion = "112241\\DCM";

  /** (112242, DCM, "C2 - Intermediate Polyp or Indeterminate Finding") */
  public static final String C2IntermediatePolypOrIndeterminateFinding = "112242\\DCM";

  /** (112243, DCM, "C3 - Polyp, Possibly Advanced Adenoma") */
  public static final String C3PolypPossiblyAdvancedAdenoma = "112243\\DCM";

  /** (112244, DCM, "C4 - Colonic Mass, Likely Malignant") */
  public static final String C4ColonicMassLikelyMalignant = "112244\\DCM";
}
