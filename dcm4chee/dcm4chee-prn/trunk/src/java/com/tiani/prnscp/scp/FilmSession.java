/*****************************************************************************
 *                                                                           *
 *  Copyright (c) 2002 by TIANI MEDGRAPH AG                                  *
 *                                                                           *
 *  This file is part of dcm4che.                                            *
 *                                                                           *
 *  This library is free software; you can redistribute it and/or modify it  *
 *  under the terms of the GNU Lesser General Public License as published    *
 *  by the Free Software Foundation; either version 2 of the License, or     *
 *  (at your option) any later version.                                      *
 *                                                                           *
 *  This library is distributed in the hope that it will be useful, but      *
 *  WITHOUT ANY WARRANTY; without even the implied warranty of               *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU        *
 *  Lesser General Public License for more details.                          *
 *                                                                           *
 *  You should have received a copy of the GNU Lesser General Public         *
 *  License along with this library; if not, write to the Free Software      *
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA  *
 *                                                                           *
 *****************************************************************************/

package com.tiani.prnscp.scp;

import org.dcm4che.data.Dataset;
import org.dcm4che.dict.Status;
import org.dcm4che.dict.Tags;
import org.dcm4che.dict.UIDs;
import org.dcm4che.net.DcmServiceException;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * <description>
 *
 * @see <related>
 * @author  <a href="mailto:gunter@tiani.com">gunter zeilinger</a>
 * @version $Revision$
 * @since November 14, 2002
 *
 * <p><b>Revisions:</b>
 *
 * <p><b>yyyymmdd author:</b>
 * <ul>
 * <li> explicit fix description (no line numbers but methods) go
 *            beyond the cvs commit message
 * </ul>
 */
class FilmSession {
   
   // Constants -----------------------------------------------------
   
   // Attributes ----------------------------------------------------
   private final PrintScpService scp;
   private final File dir;
   private final String uid;
   private final Dataset session;
   private final String imageBoxCUID;
   private final String hardcopyCUID;
   private final int imageSeqTag;
   private final LinkedHashMap filmBoxes = new LinkedHashMap();
   private String curFilmBoxUID = null;
   private FilmBox curFilmBox = null;
   
   // Static --------------------------------------------------------
   
   // Constructors --------------------------------------------------
   public FilmSession(PrintScpService scp, String asuid, String uid,
         Dataset session, File dir)
   {
      this.scp = scp;
      if (asuid.equals(UIDs.BasicGrayscalePrintManagement)) {
         imageBoxCUID = UIDs.BasicGrayscaleImageBox;
         hardcopyCUID = UIDs.HardcopyGrayscaleImageStorage;
         imageSeqTag = Tags.BasicGrayscaleImageSeq;
      } else if (asuid.equals(UIDs.BasicColorPrintManagement)) {
         imageBoxCUID = UIDs.BasicColorImageBox;
         hardcopyCUID = UIDs.HardcopyColorImageStorage;
         imageSeqTag = Tags.BasicColorImageSeq;
      } else {
         throw new IllegalArgumentException("asuid: " + asuid);
      }
      this.uid = uid;
      this.session = session;
      this.dir = dir;
   }
   
   // Public --------------------------------------------------------
   public String toString() {
      return "FilmSession[uid=" + uid + ", " + filmBoxes.size() + " FilmBoxes]";
   }      
   
   public String uid() {
      return uid;
   }   

   public File dir() {
      return dir;
   }   
   
   public Dataset getDataset() {
      return session;
   }
   
   public String getImageBoxCUID() {
      return imageBoxCUID;
   }
   
   public String getHardcopyCUID() {
      return hardcopyCUID;
   }
   
   public int getImageSeqTag() {
      return imageSeqTag;
   }

   public void setDataset(Dataset modification) {
      session.putAll(modification);
   }

   public void addFilmBox(String uid, FilmBox filmbox) {
      if (filmBoxes.containsKey(uid)) {
         throw new IllegalStateException();
      }
      this.curFilmBoxUID = uid;
      this.curFilmBox = filmbox;
      filmBoxes.put(uid, filmbox);
   }

   public String getCurrentFilmBoxUID() {
      return curFilmBoxUID;
   }   

   public FilmBox getCurrentFilmBox() {
      return curFilmBox;
   }   

   public LinkedHashMap getFilmBoxes() {
      return filmBoxes;
   }   
   
   public boolean containsFilmBox(String uid) {
      return filmBoxes.containsKey(uid);
   }   
   
   public void setFilmBox(Dataset filmbox, HashMap pluts)
      throws DcmServiceException
   {
      if (curFilmBox == null) {
         throw new IllegalStateException();
      }
      curFilmBox.setDataset(filmbox, pluts);
   }
   
   public void deleteFilmBox() {
      if (curFilmBox == null) {
         throw new IllegalStateException();
      }
      filmBoxes.remove(curFilmBoxUID);
      curFilmBoxUID = null;
      curFilmBox = null;
   }
      
   // Private -------------------------------------------------------
   
}
