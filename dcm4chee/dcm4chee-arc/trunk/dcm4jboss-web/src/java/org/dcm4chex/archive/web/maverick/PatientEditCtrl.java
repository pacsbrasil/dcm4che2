/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/
package org.dcm4chex.archive.web.maverick;


import org.dcm4chex.archive.web.maverick.model.PatientModel;

/**
 * @author umberto.cappellini@tiani.com
 * @author gunter.zeilinger@tiani.com
 * @version $Revision$ $Date$
 */
public class PatientEditCtrl extends Dcm4JbossController
{
	private int pk;
	
	public final void setPk(int pk)
	{
		this.pk = pk;
	}
	
	public PatientModel getPatient() {
		return pk == -1 ? newPatient() : FolderForm.getFolderForm(getCtx().getRequest()).getPatientByPk(pk);
	}

    private PatientModel newPatient() {
        PatientModel pat = new PatientModel();
        pat.setSpecificCharacterSet("ISO_IR 100");
        return pat;
    }

}
