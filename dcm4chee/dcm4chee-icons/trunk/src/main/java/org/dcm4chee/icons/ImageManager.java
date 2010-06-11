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
 * Portions created by the Initial Developer are Copyright (C) 2002-2005
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

package org.dcm4chee.icons;

import org.apache.wicket.ResourceReference;

/**
 * @author Robert David <robert.david@agfa.com>
 * @version $Revision$ $Date$
 * @since 13.04.2010
 */
public class ImageManager {

    private static final String iconset = "oxygen";
    private static final String treetable = "treetable";
    
    public static int defaultWidth = 16;
    public static int defaultHeight = 16;
    
    // workaround for treetable
    public static final ResourceReference IMAGE_TREETABLE_EXPAND = 
        new ResourceReference(ImageManager.class, treetable + "/treetable-expand.png");
    public static final ResourceReference IMAGE_TREETABLE_COLLAPSE = 
        new ResourceReference(ImageManager.class, treetable + "/treetable-collapse.png");   
    public static final ResourceReference IMAGE_TREETABLE_FILESYSTEM_GROUP = 
        new ResourceReference(ImageManager.class, treetable + "/treetable-filesystem-group.png");
    public static final ResourceReference IMAGE_TREETABLE_FILESYSTEM_TAR = 
        new ResourceReference(ImageManager.class, treetable + "/treetable-filesystem-tar.png");
    public static final ResourceReference IMAGE_TREETABLE_FILESYSTEM_FOLDER = 
        new ResourceReference(ImageManager.class, treetable + "/treetable-filesystem-folder.png");
    public static final ResourceReference IMAGE_TREETABLE_QUEUE = 
        new ResourceReference(ImageManager.class, treetable + "/treetable-queue.png");
    public static final ResourceReference IMAGE_TREETABLE_MESSAGE = 
        new ResourceReference(ImageManager.class, treetable + "/treetable-message.png");
    public static final ResourceReference IMAGE_TREETABLE_REPORT_FOLDER = 
        new ResourceReference(ImageManager.class, treetable + "/treetable-report-folder.png");
    public static final ResourceReference IMAGE_TREETABLE_REPORT = 
        new ResourceReference(ImageManager.class, treetable + "/treetable-report.png");
    public static final ResourceReference IMAGE_TREETABLE_PROPERTY_FOLDER = 
        new ResourceReference(ImageManager.class, treetable + "/treetable-folder-property.png");
    public static final ResourceReference IMAGE_TREETABLE_PROPERTY = 
        new ResourceReference(ImageManager.class, treetable + "/treetable-property.png");
    
    public static final ResourceReference IMAGE_EXPAND = 
        new ResourceReference(ImageManager.class, iconset + "/actions/go-down.png");
    public static final ResourceReference IMAGE_COLLAPSE = 
        new ResourceReference(ImageManager.class, iconset + "/actions/go-up.png");
    public static final ResourceReference IMAGE_EXPAND_ALL = 
        new ResourceReference(ImageManager.class, iconset + "/actions/go-bottom.png");
    public static final ResourceReference IMAGE_DELETE = 
        new ResourceReference(ImageManager.class, iconset + "/actions/list-remove.png");
    public static final ResourceReference IMAGE_EDIT = 
        new ResourceReference(ImageManager.class, iconset + "/actions/document-edit.png");
    public static final ResourceReference IMAGE_DETAIL = 
        new ResourceReference(ImageManager.class, iconset + "/categories/preferences-desktop-peripherals.png");
    public static final ResourceReference IMAGE_TRASH = 
        new ResourceReference(ImageManager.class, iconset + "/places/user-trash.png");
    public static final ResourceReference IMAGE_TRASH_REMOVE_ALL = 
        new ResourceReference(ImageManager.class, iconset + "/actions/edit-bomb.png");
    public static final ResourceReference IMAGE_MOVE = 
        new ResourceReference(ImageManager.class, iconset + "/actions/system-switch-user.png");
    public static final ResourceReference IMAGE_EXPORT = 
        new ResourceReference(ImageManager.class, iconset + "/actions/mail-receive.png");
    public static final ResourceReference IMAGE_ECHO = 
        new ResourceReference(ImageManager.class, iconset + "/apps/preferences-desktop-sound.png");
    public static final ResourceReference IMAGE_NEW = 
        new ResourceReference(ImageManager.class, iconset + "/actions/appointment-new.png");
    public static final ResourceReference IMAGE_CHANGE_PASSWORD = 
        new ResourceReference(ImageManager.class, iconset + "/apps/preferences-desktop-user-password.png");

    public static final ResourceReference IMAGE_REPORT_NEW = 
        new ResourceReference(ImageManager.class, iconset + "/places/document-multiple.png");
    public static final ResourceReference IMAGE_DIAGRAM = 
        new ResourceReference(ImageManager.class, iconset + "/mimetypes/application-vnd.oasis.opendocument.chart.png");
    public static final ResourceReference IMAGE_TABLE = 
        new ResourceReference(ImageManager.class, iconset + "/mimetypes/application-x-siag.png");
    public static final ResourceReference IMAGE_DIAGRAM_TABLE = 
        new ResourceReference(ImageManager.class, iconset + "/mimetypes/application-vnd.ms-powerpoint.png");
    public static final ResourceReference IMAGE_DIAGRAM_DOWNLOAD = 
        new ResourceReference(ImageManager.class, iconset + "/places/folder-downloads.png");
    public static final ResourceReference IMAGE_REPORT_XML = 
        new ResourceReference(ImageManager.class, iconset + "/mimetypes/text-xml.png");
    public static final ResourceReference IMAGE_REPORT_CSV = 
        new ResourceReference(ImageManager.class, iconset + "/mimetypes/text-csv.png");
    public static final ResourceReference IMAGE_REPORT_PRINT = 
        new ResourceReference(ImageManager.class, iconset + "/apps/preferences-desktop-printer.png");
    public static final ResourceReference IMAGE_STATUS_LINKED = 
        new ResourceReference(ImageManager.class, "status/linked.png");
    public static final ResourceReference IMAGE_STATUS_UNLINKED = 
        new ResourceReference(ImageManager.class, "status/unlinked.png");
    public static final ResourceReference IMAGE_INSERT_LINK = 
        new ResourceReference(ImageManager.class, iconset + "/actions/insert-link.png");
    public static final ResourceReference IMAGE_WEBVIEWER = 
        new ResourceReference(ImageManager.class, "actions/webview.gif");
    public static final ResourceReference IMAGE_WADO = 
        new ResourceReference(ImageManager.class, "actions/wado.gif");
    public static final ResourceReference IMAGE_TRASH_RESTORE = 
        new ResourceReference(ImageManager.class, iconset + "/actions/system-switch-user.png");
    public static final ResourceReference IMAGE_ADD_STUDY = 
        new ResourceReference(ImageManager.class, "actions/add.gif");
    public static final ResourceReference IMAGE_ADD_SERIES = 
        new ResourceReference(ImageManager.class, "actions/add.gif");
    public static final ResourceReference IMAGE_HOURGLASS = 
        new ResourceReference(ImageManager.class, "actions/ajax-loader.gif");

}
