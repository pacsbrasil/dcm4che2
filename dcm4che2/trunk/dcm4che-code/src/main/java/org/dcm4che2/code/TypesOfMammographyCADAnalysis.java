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
 * CID 6043 Types of Mammography CAD Analysis.
 *
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @version $Revision$ $Date$
 * @since Jun 2, 2010
 */
public class TypesOfMammographyCADAnalysis {

  /** (P5-B3412, SRT, "Asymmetric breast tissue analysis") */
  public static final String AsymmetricBreastTissueAnalysis = "P5-B3412\\SRT";

  /** (P5-B3414, SRT, "Breast composition analysis") */
  public static final String BreastCompositionAnalysis = "P5-B3414\\SRT";

  /** (P5-B3410, SRT, "Focal asymmetric density analysis") */
  public static final String FocalAsymmetricDensityAnalysis = "P5-B3410\\SRT";

  /** (P5-B3408, SRT, "Image quality analysis") */
  public static final String ImageQualityAnalysis = "P5-B3408\\SRT";

  /** (111233, DCM, "Individual Impression / Recommendation Analysis") */
  public static final String IndividualImpressionRecommendationAnalysis = "111233\\DCM";

  /** (111234, DCM, "Overall Impression / Recommendation Analysis") */
  public static final String OverallImpressionRecommendationAnalysis = "111234\\DCM";

  /** (P5-B3402, SRT, "Spatial collocation analysis") */
  public static final String SpatialCollocationAnalysis = "P5-B3402\\SRT";

  /** (P5-B3404, SRT, "Spatial proximity analysis") */
  public static final String SpatialProximityAnalysis = "P5-B3404\\SRT";

  /** (P5-B3406, SRT, "Temporal correlation") */
  public static final String TemporalCorrelation = "P5-B3406\\SRT";
}
