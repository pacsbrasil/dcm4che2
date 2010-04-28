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
 * Java(TM), hosted at http://sourceforge.net/projects/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * Agfa-Gevaert AG.
 * Portions created by the Initial Developer are Copyright (C) 2008
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

package org.dcm4chee.web.war.folder;

import java.io.IOException;
import java.util.Collection;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.ReflectionException;

import org.dcm4chee.web.common.delegate.BaseMBeanDelegate;
import org.dcm4chee.web.common.exceptions.SelectionException;
import org.dcm4chee.web.war.common.model.AbstractDicomModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Franz Willer <franz.willer@gmail.com>
 * @version $Revision$ $Date$
 * @since Aug 18, 2009
 */
public class ContentEditDelegate extends BaseMBeanDelegate {

    private static final String MSG_ERR_SELECTION_MOVE_SOURCE_LEVEL = "Selection for move entities wrong! Source must be one level beneath destination !";
    private static final String MSGID_ERR_SELECTION_MOVE_SOURCE_LEVEL = "folder.err_moveSelectionSrcLevel";
    private static final String MSG_ERR_SELECTION_MOVE_DESTINATION = "Selection for move entities wrong! Only one destination is allowed!";
    private static final String MSGID_ERR_SELECTION_MOVE_DESTINATION = "folder.err_moveSelectionDest";
    private static final String MSGID_ERR_SELECTION_MOVE_NO_SELECTION = "folder.err_moveNoSelection";
    private static final String MSG_ERR_SELECTION_MOVE_NO_SELECTION = "Nothing selected for move entities!";
    private static final String MSGID_ERR_SELECTION_MOVE_NO_SOURCE = "folder.err_moveNoSource";
    private static final String MSG_ERR_SELECTION_MOVE_NO_SOURCE = "Selection for move entities wrong! No source entities selected!";
    private static final String MSGID_ERR_SELECTION_MOVE_PPS = "folder.err_movePPS";
    private static final String MSG_ERR_SELECTION_MOVE_PPS = "Selection for move entities wrong! PPS entities are not allowed!";

    private static ContentEditDelegate delegate;

    private static Logger log = LoggerFactory.getLogger(ContentEditDelegate.class);

    private ContentEditDelegate() {
        super();
    }

    public boolean moveToTrash(SelectedEntities selected) {
        try {
            if ( selected.hasPatients()) {
                moveToTrash("movePatientsToTrash", toPks(selected.getPatients()));
            }
            if ( selected.hasStudies()) {
                moveToTrash("moveStudiesToTrash", toPks(selected.getStudies()));
            }
            if ( selected.hasSeries()) {
                moveToTrash("moveSeriesToTrash", toPks(selected.getSeries()));
            }
            if ( selected.hasInstances()) {
                moveToTrash("moveInstancesToTrash", toPks(selected.getInstances()));
            }
        } catch (Exception x) {
            String msg = "Move to trash failed! Reason:"+x.getMessage();
            log.error(msg,x);
            return false;
        }
        return true;
    }

    private void moveToTrash(String op, long[] pks)
            throws InstanceNotFoundException, MBeanException,
            ReflectionException, IOException {
        server.invoke(serviceObjectName, op, new Object[]{pks}, 
                new String[]{long[].class.getName()});
    }

    public int moveEntities(SelectedEntities selected) throws SelectionException {
        try {
            if (selected.getPpss().size() > 1) {
                throw new SelectionException(MSG_ERR_SELECTION_MOVE_PPS, MSGID_ERR_SELECTION_MOVE_PPS);
            } 
            // study -> pat
            int pats = selected.getPatients().size();
            if (pats > 1) {
                throw new SelectionException(MSG_ERR_SELECTION_MOVE_DESTINATION, MSGID_ERR_SELECTION_MOVE_DESTINATION);
            } 
            if( pats == 1) {
                if (selected.getStudies().size() < 1) {
                    if ( selected.hasSeries() || selected.hasInstances()) {
                        throw new SelectionException(MSG_ERR_SELECTION_MOVE_SOURCE_LEVEL, MSGID_ERR_SELECTION_MOVE_SOURCE_LEVEL);
                    } else {
                        throw new SelectionException(MSG_ERR_SELECTION_MOVE_NO_SOURCE, MSGID_ERR_SELECTION_MOVE_NO_SOURCE);
                    }
                }
                return moveEntities("moveStudiesToPatient", toPks(selected.getPatients())[0], toPks(selected.getStudies()));
            }
            // series -> study
            int nrOfStudies = selected.getStudies().size();
            if ( nrOfStudies > 1) {
                throw new SelectionException(MSG_ERR_SELECTION_MOVE_DESTINATION, MSGID_ERR_SELECTION_MOVE_DESTINATION);
            } 
            if( nrOfStudies == 1) {
                if (selected.getSeries().size() < 1) {
                    if ( selected.hasInstances()) {
                        throw new SelectionException(MSG_ERR_SELECTION_MOVE_SOURCE_LEVEL, MSGID_ERR_SELECTION_MOVE_SOURCE_LEVEL);
                    } else {
                        throw new SelectionException(MSG_ERR_SELECTION_MOVE_NO_SOURCE, MSGID_ERR_SELECTION_MOVE_NO_SOURCE);
                    }
                }
                return moveEntities("moveSeriesToStudy", toPks(selected.getStudies())[0], toPks(selected.getSeries()));
            }
            // instances -> series
            int nrOfSeries = selected.getSeries().size();
            if ( nrOfSeries > 1) {
                throw new SelectionException(MSG_ERR_SELECTION_MOVE_DESTINATION, MSGID_ERR_SELECTION_MOVE_DESTINATION);
            } 
            if( nrOfSeries == 1) {
                if (selected.getInstances().size() < 1) {
                    throw new SelectionException(MSG_ERR_SELECTION_MOVE_NO_SOURCE, MSGID_ERR_SELECTION_MOVE_NO_SOURCE);
                }
                return moveEntities("moveInstancesToSeries", toPks(selected.getSeries())[0], toPks(selected.getInstances()));
            }
            throw new SelectionException(MSG_ERR_SELECTION_MOVE_NO_SELECTION, MSGID_ERR_SELECTION_MOVE_NO_SELECTION);
        } catch (SelectionException x) {
            throw x;
        } catch (Exception x) {
            String msg = "Move selected Entities failed! Reason:"+x.getMessage();
            log.error(msg,x);
            return -1;
        }
    }
 
    private int  moveEntities(String op, long pk, long[] pks)
            throws InstanceNotFoundException, MBeanException,
            ReflectionException, IOException {
        return (Integer) server.invoke(serviceObjectName, op, new Object[]{pks, pk}, 
        new String[]{long[].class.getName(), long.class.getName()});
}
    
    private long[] toPks(Collection<? extends AbstractDicomModel> models) {
        long[] pks = new long[models.size()];
        int i=0;
        for (AbstractDicomModel m : models) {
            pks[i++] = m.getPk();
        }
        return pks;
    }

    @Override
    public String getInitParameterName() {
        return "contentEditServiceName";
    }

    public static ContentEditDelegate getInstance() {
        if (delegate==null)
            delegate = new ContentEditDelegate();
        return delegate;
    }

}
