/* $Id$
 * Copyright (c) 2002,2003 by TIANI MEDGRAPH AG
 *
 * This file is part of dcm4che.
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package org.dcm4chex.archive.web.maverick;

import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.dcm4chex.archive.ejb.interfaces.ContentEdit;
import org.dcm4chex.archive.ejb.interfaces.ContentEditHome;
import org.dcm4chex.archive.ejb.interfaces.ContentManager;
import org.dcm4chex.archive.ejb.interfaces.ContentManagerHome;
import org.dcm4chex.archive.ejb.interfaces.StudyFilterDTO;
import org.dcm4chex.archive.util.EJBHomeFactory;

/**
 * 
 * @author gunter.zeilinger@tiani.com
 * @version $Revision$ $Date$
 * @since 28.01.2004
 */
public class FolderSubmitCtrl extends FolderCtrl {

    protected String perform() throws Exception {
        try {
            FolderForm folderForm = (FolderForm) getForm();
            setSticky(folderForm.getStickyPatients(), "stickyPat");
            setSticky(folderForm.getStickyStudies(), "stickyStudy");
            setSticky(folderForm.getStickySeries(), "stickySeries");
            setSticky(folderForm.getStickyInstances(), "stickyInst");
            HttpServletRequest rq = getCtx().getRequest();
            if (rq.getParameter("filter") != null) {
                return query(true);
            }
            if (rq.getParameter("prev") != null
                || rq.getParameter("next") != null) {
                return query(false);
            }
            if (rq.getParameter("ae") != null) {
            	return delete();                
            }
            if (rq.getParameter("del") != null) {
                return delete();                
            }
            if (rq.getParameter("merge") != null) {
                /* TODO */
                return FOLDER;                
            }
            if (rq.getParameter("move") != null) {
                /* TODO */
                return FOLDER;                
            }
            return FOLDER;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    // private methods 
    
    private String query(boolean newQuery) throws Exception {

        ContentManager cm = lookupContentManager();

        try {
            FolderForm folderForm = (FolderForm) getForm();
            StudyFilterDTO filter = folderForm.getStudyFilter();
            if (newQuery) {
                folderForm.setTotal(cm.countStudies(filter));
            }
            folderForm.updatePatients(
                cm.listPatients(
                    filter,
                    folderForm.getOffset(),
                    folderForm.getLimit()));
        } finally {
            try {
                cm.remove();
            } catch (Exception e) {
            }
        }
        return FOLDER;
    }

    private String delete() throws Exception
	{
    	ContentEdit edit = lookupContentEdit();
    	FolderForm folderForm = (FolderForm) getForm();
    	
    	//deleting Patients
    	for (Iterator i = folderForm.getStickyPatients().iterator(); i.hasNext();)
    	{	
    		try
			{
				edit.deletePatient(Integer.parseInt((String)i.next()));
			}
			catch (NumberFormatException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			catch (RemoteException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}

    	//deleting Studies
    	for (Iterator i = folderForm.getStickyStudies().iterator(); i.hasNext();)
    	{	
    		try
			{
    			edit.deleteStudy(Integer.parseInt((String)i.next()));
    		}
    		catch (NumberFormatException e)
			{
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		}
    		catch (RemoteException e)
			{
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		}
    	}

    	//deleting Series
    	for (Iterator i = folderForm.getStickySeries().iterator(); i.hasNext();)
    	{	
    		try
			{
    			edit.deleteSeries(Integer.parseInt((String)i.next()));
    		}
    		catch (NumberFormatException e)
			{
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		}
    		catch (RemoteException e)
			{
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		}
    	}
    	
    	//deleting Instances
    	for (Iterator i = folderForm.getStickyInstances().iterator(); i.hasNext();)
    	{	
    		try
			{
    			edit.deleteInstance(Integer.parseInt((String)i.next()));
    		}
    		catch (NumberFormatException e)
			{
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		}
    		catch (RemoteException e)
			{
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		}
    	}
    	
    	folderForm.removeStickies();
    	
    	return FOLDER;
    }
    
    private void setSticky(Set stickySet, String attr) {
        stickySet.clear();
        String[] newValue = getCtx().getRequest().getParameterValues(attr);
        if (newValue != null) {
            stickySet.addAll(Arrays.asList(newValue));
        }
    }
    
    private ContentEdit lookupContentEdit() throws Exception
	{
    	ContentEditHome home =
    		(ContentEditHome) EJBHomeFactory.getFactory().lookup(
    				ContentEditHome.class,
					ContentEditHome.JNDI_NAME);
    	return home.create();
    }

    private ContentManager lookupContentManager() throws Exception
	{
    	ContentManagerHome home =
    		(ContentManagerHome) EJBHomeFactory.getFactory().lookup(
    				ContentManagerHome.class,
    				ContentManagerHome.JNDI_NAME);
    	return home.create();
    }
    
    
}
