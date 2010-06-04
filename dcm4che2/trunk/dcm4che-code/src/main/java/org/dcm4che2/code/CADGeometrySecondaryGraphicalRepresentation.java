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
 * CID 6166 CAD Geometry Secondary Graphical Representation.
 *
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @version $Revision$ $Date$
 * @since Jun 2, 2010
 */
public class CADGeometrySecondaryGraphicalRepresentation {

  /** (113662, DCM, "Inner limits of fuzzy margin") */
  public static final String InnerLimitsOfFuzzyMargin = "113662\\DCM";

  /** (113665, DCM, "Linear spiculation") */
  public static final String LinearSpiculation = "113665\\DCM";

  /** (G-A185, SRT, "Long axis") */
  public static final String LongAxis = "G-A185\\SRT";

  /** (113669, DCM, "Orthogonal location arc") */
  public static final String OrthogonalLocationArc = "113669\\DCM";

  /** (113670, DCM, "Orthogonal location arc inner margin") */
  public static final String OrthogonalLocationArcInnerMargin = "113670\\DCM";

  /** (113671, DCM, "Orthogonal location arc outer margin") */
  public static final String OrthogonalLocationArcOuterMargin = "113671\\DCM";

  /** (113663, DCM, "Outer limits of fuzzy margin") */
  public static final String OuterLimitsOfFuzzyMargin = "113663\\DCM";

  /** (113661, DCM, "Outline of lobulations") */
  public static final String OutlineOfLobulations = "113661\\DCM";

  /** (113664, DCM, "Outline of spiculations") */
  public static final String OutlineOfSpiculations = "113664\\DCM";

  /** (113666, DCM, "Pixelated spiculations") */
  public static final String PixelatedSpiculations = "113666\\DCM";

  /** (G-A186, SRT, "Short axis") */
  public static final String ShortAxis = "G-A186\\SRT";
}
