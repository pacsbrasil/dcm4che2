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
 * CID 3675 Other Filters.
 *
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @version $Rev$ $Date::            $
 * @since Jun 2, 2010
 */
public class OtherFilters {

  /** (5.4.5-29-2, SCPECG[1.3], "Artifact filter") */
  public static final String ArtifactFilter = "5.4.5-29-2\\SCPECG[1.3]";

  /** (5.4.5-29-3, SCPECG[1.3], "Baseline filter") */
  public static final String BaselineFilter = "5.4.5-29-3\\SCPECG[1.3]";

  /** (5.4.5-29-1, SCPECG[1.3], "50 Hertz notch filter") */
  public static final String FiftyHertzNotchFilter = "5.4.5-29-1\\SCPECG[1.3]";

  /** (5.4.5-29-0, SCPECG[1.3], "60 Hertz notch filter") */
  public static final String SixtyHertzNotchFilter = "5.4.5-29-0\\SCPECG[1.3]";
}
