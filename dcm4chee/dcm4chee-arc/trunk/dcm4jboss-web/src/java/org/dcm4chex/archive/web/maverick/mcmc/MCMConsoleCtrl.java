/*
 * Created on 20.12.2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.dcm4chex.archive.web.maverick.mcmc;

import java.text.ParseException;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.servlet.http.HttpServletRequest;

import org.dcm4chex.archive.ejb.interfaces.MediaDTO;
import org.dcm4chex.archive.util.JMSDelegate;
import org.dcm4chex.archive.web.maverick.Dcm4JbossFormController;
import org.dcm4chex.archive.web.maverick.mcmc.model.MCMFilter;
import org.dcm4chex.archive.web.maverick.mcmc.model.MCMModel;
import org.dcm4chex.archive.web.maverick.mcmc.model.MediaData;

/**
 * @author franz.willer
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class MCMConsoleCtrl extends Dcm4JbossFormController {

	public static final String QUEUE = "MCMScu";

	private MCMModel model;

    protected Object makeFormBean() {
        return MCMModel.getModel(getCtx().getRequest());
    }
	

	
    protected String perform() throws Exception {
        try {
            HttpServletRequest request = getCtx().getRequest();
    		model = MCMModel.getModel(request);
            if ( request.getParameter("filter.x") != null ) {//action from filter button
        		checkFilter( request );
            	model.filterMediaList( true );
            } else if ( request.getParameter("nav") != null ) {//action from a nav button. (next or previous)
            	String nav = request.getParameter("nav");
            	if ( nav.equals("prev") ) {
            		model.performPrevious();
            	} else if ( nav.equals("next") ) {
            		model.performNext();
            	}
            } else {
            	String action = request.getParameter("action");
            	if ( action != null ) {
            		performAction( action, request );
            	}
            }
            return "success";
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


	/**
	 * @param action
	 * @param request
	 */
	private void performAction(String action, HttpServletRequest request) {
		if ( action.equalsIgnoreCase("queue") ) {
			int mediaPk = Integer.parseInt( request.getParameter("mediaPk"));
           	MediaData md = model.mediaDataFromList( mediaPk );
           	if ( md != null ) {
	           try {
	 				JMSDelegate.queue( QUEUE,
					        md.asMediaDTO(),
					        Message.DEFAULT_PRIORITY,
					        0L);
				} catch (JMSException e) {
					
					model.updateMediaStatus( mediaPk, MediaDTO.QUEUED, e.getMessage() );
				}
				model.updateMediaStatus( mediaPk, MediaDTO.QUEUED, "" );
           	} //TODO 
		} else {
			model.setErrorCode( MCMModel.ERROR_UNSUPPORTED_ACTION );
		}
		
	}



	/**
	 * @param rq
	 * @throws ParseException
	 * 
	 */
	private boolean checkFilter(HttpServletRequest rq) throws ParseException {
		MCMFilter filter = model.getFilter();
		if ( rq.getParameter("mediaStatus") != null ) filter.setSelectedStatus(rq.getParameter("mediaStatus") );
		if ( rq.getParameter("startCreationDate") != null ) filter.setStartCreationDate(rq.getParameter("startCreationDate") );
		if ( rq.getParameter("endCreationDate") != null ) filter.setEndCreationDate(rq.getParameter("endCreationDate") );
		if ( rq.getParameter("startUpdateDate") != null ) filter.setStartUpdateDate(rq.getParameter("startUpdateDate") );
		if ( rq.getParameter("endUpdateDate") != null ) filter.setEndUpdateDate(rq.getParameter("endUpdateDate") );
		if ( rq.getParameter("createOrUpdateDate") != null ) filter.setCreateOrUpdateDate(rq.getParameter("createOrUpdateDate") );
		return filter.isChanged();
	}
	
}
