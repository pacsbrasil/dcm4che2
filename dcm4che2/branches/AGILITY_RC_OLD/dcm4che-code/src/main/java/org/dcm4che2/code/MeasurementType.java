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
 * CID 3627 Measurement Type.
 *
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @version $Rev$ $Date::            $
 * @since Jun 2, 2010
 */
public class MeasurementType {

  /** (R-002E1, SRT, "Best value") */
  public static final String BestValue = "R-002E1\\SRT";

  /** (R-41D2D, SRT, "Calculated") */
  public static final String Calculated = "R-41D2D\\SRT";

  /** (R-10260, SRT, "Estimated") */
  public static final String Estimated = "R-10260\\SRT";

  /** (R-00317, SRT, "Mean") */
  public static final String Mean = "R-00317\\SRT";

  /** (R-41D41, SRT, "Measured") */
  public static final String Measured = "R-41D41\\SRT";

  /** (R-00319, SRT, "Median") */
  public static final String Median = "R-00319\\SRT";

  /** (R-0032E, SRT, "Mode") */
  public static final String Mode = "R-0032E\\SRT";

  /** (R-00353, SRT, "Peak to peak") */
  public static final String PeakToPeak = "R-00353\\SRT";

  /** (R-00355, SRT, "Point source measurement") */
  public static final String PointSourceMeasurement = "R-00355\\SRT";

  /** (R-41D27, SRT, "Visual estimation") */
  public static final String VisualEstimation = "R-41D27\\SRT";
}
