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
*
* The Initial Developer of the Original Code is
* Raster Images
* Portions created by the Initial Developer are Copyright (C) 2009-2010
* the Initial Developer. All Rights Reserved.
*
* Contributor(s):
* Babu Hussain A
* Meer Asgar Hussain B
* Prakash J
* Suresh V
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
package in.raster.mayam.util;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import org.dcm4che2.data.DicomElement;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.io.DicomInputStream;
import org.dcm4che2.util.TagUtils;

/**
 *
 * @author prakash.j
 * @version 0.5
 */
public class DicomTagsReader {

    public static ArrayList<DicomTags> tagsArray = new ArrayList<DicomTags>();

    public DicomTagsReader() {
    }

    // Method to Parse Tags from the DICOM File
    public static void ReadTags(File dcmfile) {
        tagsArray = new ArrayList<DicomTags>();
        DicomObject dcmObject = null;

        try {

            DicomInputStream dcmInputStream = new DicomInputStream(dcmfile);
            dcmObject = dcmInputStream.readDicomObject();
            dcmInputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        //Call to Parsing Tags in an Array List        
        listTags(dcmObject);       
    }

    //Method to parse tags from a DICOM Object
    public static void listTags(DicomObject dcmObject){
        Iterator<DicomElement> tagsIterator=null;
        if(dcmObject!=null){
        tagsIterator= dcmObject.datasetIterator();   

        while (tagsIterator.hasNext()) {
            DicomElement dcmElement = tagsIterator.next();

            DicomTags dcmTags = new DicomTags();

            int tag = dcmElement.tag();
            try {

                String tagName = dcmObject.nameOf(tag);
                String tagAddr = TagUtils.toString(tag);
                String tagVR = dcmObject.vrOf(tag).toString();
                String tagLength = String.valueOf(dcmElement.length());


                //Recursive calling for "SQ" Tags

                if (tagVR.equals("SQ")) {
                    if (dcmElement.hasItems()) {

                        dcmTags.setTag(tagAddr);
                        dcmTags.setTagName(tagName);
                        dcmTags.setVR(tagVR);

                        //Recursive call
                        listTags(dcmElement.getDicomObject());
                        continue;
                    }
                }

                //Adding parsed tags to DicomTags Object

                dcmTags.setTag(tagAddr);
                dcmTags.setTagName(tagName);
                dcmTags.setVR(tagVR);
                dcmTags.setTagLength(tagLength);
                dcmTags.setTagValue(dcmObject.getString(tag));

            }catch(UnsupportedOperationException e){}
            catch (Exception e) {
                e.printStackTrace();
            }

            //Adding DicomTags object to Tags ArrayList
            tagsArray.add(dcmTags);

        }
        }

    }

    // A method which gets DICOM File as an input and returns tags in a ArrayList
    public static ArrayList<DicomTags> getTags(File dcmfile) {
        ReadTags(dcmfile);
        return tagsArray;
    }
}
