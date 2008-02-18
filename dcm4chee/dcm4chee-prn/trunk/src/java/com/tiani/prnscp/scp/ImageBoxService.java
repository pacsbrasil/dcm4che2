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
 * TIANI Medgraph AG.
 * Portions created by the Initial Developer are Copyright (C) 2002-2005
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Gunter Zeilinger <gunter.zeilinger@tiani.com>
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

package com.tiani.prnscp.scp;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;

import org.dcm4che.data.Command;
import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmElement;
import org.dcm4che.data.DcmEncodeParam;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.data.DcmParser;
import org.dcm4che.data.DcmParserFactory;
import org.dcm4che.dict.Status;
import org.dcm4che.dict.Tags;
import org.dcm4che.dict.UIDs;
import org.dcm4che.net.ActiveAssociation;
import org.dcm4che.net.DcmServiceBase;
import org.dcm4che.net.DcmServiceException;
import org.dcm4che.net.Dimse;
import org.dcm4che.util.UIDGenerator;

/**
 *  <description>
 *
 * @author  <a href="mailto:gunter@tiani.com">gunter zeilinger</a>
 * @created  November 9, 2002
 * @version  $Revision$
 */
class ImageBoxService extends DcmServiceBase
{
    // Constants -----------------------------------------------------
    private final static int BUFFER_LEN = 512;

    // Attributes ----------------------------------------------------
    private final DcmParserFactory dpf = DcmParserFactory.getInstance();
    private final DcmObjectFactory dof = DcmObjectFactory.getInstance();
    private final UIDGenerator uidgen = UIDGenerator.getInstance();
    private final PrintScpService scp;


    // Static --------------------------------------------------------

    // Constructors --------------------------------------------------
    /**
     *  Constructor for the ImageBoxService object
     *
     * @param  scp Description of the Parameter
     */
    public ImageBoxService(PrintScpService scp)
    {
        this.scp = scp;
    }


    // Public --------------------------------------------------------

    // DcmServiceBase overrides --------------------------------------

    /**
     *  Description of the Method
     *
     * @param  as Description of the Parameter
     * @param  rq Description of the Parameter
     * @param  rspCmd Description of the Parameter
     * @return  Description of the Return Value
     * @exception  IOException Description of the Exception
     * @exception  DcmServiceException Description of the Exception
     */
    protected Dataset doNSet(ActiveAssociation as, Dimse rq, Command rspCmd)
        throws IOException, DcmServiceException
    {
        InputStream in = rq.getDataAsStream();
        try {
            Command cmd = rq.getCommand();
            String cuid = cmd.getRequestedSOPClassUID();
            String boxuid = cmd.getRequestedSOPInstanceUID();
            String tuid = rq.getTransferSyntaxUID();
            FilmSession session = scp.getFilmSession(as);
            HashMap pluts = scp.getPresentationLUTs(as);
            checkRefImageBoxSeq(cuid, boxuid, session);
            int stopTag = session.isColor()
                     ? Tags.BasicColorImageSeq
                     : Tags.BasicGrayscaleImageSeq;
            DcmParser parser = dpf.newDcmParser(in);
            Dataset box = dof.newDataset();
            parser.setDcmHandler(box.getDcmHandler());
            parser.parseDataset(tuid, stopTag);
            if (stopTag != parser.getReadTag()) {
                throw new DcmServiceException(Status.MissingAttribute,
                        "Missing " + Tags.toString(stopTag));
            }
            int sqLen = parser.getReadLength();
            if (sqLen != 0) {
                int itemTag = parser.parseHeader();
                if (sqLen == -1 && itemTag == Tags.SeqDelimitationItem) {
                    sqLen = 0;
                }
            }
            if (sqLen != 0) {
                int itemLen = parser.getReadLength();

                String hcuid = uidgen.createUID();
                Dataset hc = createHC(hcuid, session, box);
                parser.setDcmHandler(hc.getDcmHandler());
                parser.parseDataset(tuid, Tags.PixelData);
                scp.logDataset("Store as Hardcopy:\n", hc);
                File hcdir = new File(session.dir(),
                        PrintScpService.SPOOL_HARDCOPY_DIR_SUFFIX);
                File hcfile = new File(hcdir, hcuid);
                OutputStream out = new BufferedOutputStream(
                        new FileOutputStream(hcfile));
                try {
                    hc.writeFile(out, null);
                    hc.writeHeader(out, DcmEncodeParam.EVR_LE,
                            parser.getReadTag(),
                            parser.getReadVR(),
                            parser.getReadLength());
                    copy(in, out, parser.getReadLength());
                } finally {
                    try {
                        out.close();
                    } catch (IOException ignore) {}
                }
                if (itemLen == -1) {
                    parser.parseHeader();
                    // skip Item Delim
                }
                if (sqLen == -1) {
                    parser.parseHeader();
                    // skip Seq Delim
                }
                parser.setDcmHandler(box.getDcmHandler());
            }
            parser.parseDataset(tuid, -1);
            scp.logDataset("Set Image Box:\n", box);
            session.getCurrentFilmBox().setImageBox(boxuid, box, pluts, rspCmd);
            return null;
        } catch (DcmServiceException e) {
            scp.getLog().warn("Failed to set Image Box SOP Instance", e);
            throw e;
        } finally {
            try {
                in.close();
            } catch (IOException ignore) {}
        }
    }


    private void checkRefImageBoxSeq(String cuid, String boxuid, FilmSession session)
        throws DcmServiceException
    {
        if (session == null) {
            throw new DcmServiceException(Status.NoSuchObjectInstance);
        }
        FilmBox filmbox = session.getCurrentFilmBox();
        if (filmbox == null) {
            throw new DcmServiceException(Status.NoSuchObjectInstance);
        }
        DcmElement sq = filmbox.getAttributes().get(Tags.RefImageBoxSeq);
        for (int i = 0, n = sq.countItems(); i < n; ++i) {
            Dataset ref = sq.getItem(i);
            if (ref.getString(Tags.RefSOPInstanceUID).equals(boxuid)) {
                if (ref.getString(Tags.RefSOPClassUID).equals(cuid)) {
                    return;
                }
                throw new DcmServiceException(Status.ClassInstanceConflict);
            }
        }
    }


    private void copy(InputStream in, OutputStream out, int len)
        throws IOException, DcmServiceException
    {
        byte[] buffer = new byte[BUFFER_LEN];
        int c;
        int toread = len;
        while (toread > 0) {
            c = in.read(buffer, 0, Math.min(toread, BUFFER_LEN));
            if (c == -1) {
                throw new DcmServiceException(Status.InvalidAttributeValue);
            }
            out.write(buffer, 0, c);
            toread -= c;
        }
    }


    private Dataset createHC(String hcuid, FilmSession session, Dataset box)
    {
        Dataset ds = dof.newDataset();
        String cuid = session.getHardcopyCUID();
        // use Film Session Instance UID as Study UID
        String studyuid = session.uid();
        // use Film Box Instance UID as Series UID
        String seriesuid = session.getCurrentFilmBoxUID();
        ds.setFileMetaInfo(
                dof.newFileMetaInfo(cuid, hcuid, UIDs.ExplicitVRLittleEndian));
        Dataset item = box.putSQ(Tags.RefImageSeq).addNewItem();
        item.putAE(Tags.RetrieveAET, "UNKOWN");
        ds.putCS(Tags.ImageType, new String[]{"DERIVED", "SECONDARY"});
        ds.putUI(Tags.SOPClassUID, cuid);
        item.putUI(Tags.RefSOPClassUID, cuid);
        ds.putUI(Tags.SOPInstanceUID, hcuid);
        item.putUI(Tags.RefSOPInstanceUID, hcuid);
        ds.putDA(Tags.StudyDate);
        ds.putTM(Tags.StudyTime);
        ds.putSH(Tags.AccessionNumber);
        ds.putCS(Tags.Modality, "HC");
        ds.putLO(Tags.Manufacturer, "TIANI MEDGRAPH AG");
        ds.putPN(Tags.PatientName);
        ds.putLO(Tags.PatientID);
        item.putLO(Tags.PatientID);
        ds.putDA(Tags.PatientBirthDate);
        ds.putCS(Tags.PatientSex);
        ds.putUI(Tags.StudyInstanceUID, studyuid);
        item.putUI(Tags.StudyInstanceUID, studyuid);
        ds.putUI(Tags.SeriesInstanceUID, seriesuid);
        item.putUI(Tags.SeriesInstanceUID, seriesuid);
        ds.putSH(Tags.StudyID);
        ds.putIS(Tags.SeriesNumber);
        ds.putIS(Tags.InstanceNumber);
        ds.putCS(Tags.PatientOrientation);
        return ds;
    }

    // Package protected ---------------------------------------------

    // Protected -----------------------------------------------------

    // Private -------------------------------------------------------

    // Inner classes -------------------------------------------------
}

