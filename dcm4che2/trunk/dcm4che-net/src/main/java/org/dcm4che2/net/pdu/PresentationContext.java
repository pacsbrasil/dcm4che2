/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/

package org.dcm4che2.net.pdu;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author gunter zeilinger(gunterze@gmail.com)
 * @version $Reversion$ $Date$
 * @since Sep 15, 2005
 *
 */
public class PresentationContext {

    private int pcid;
    private int result;
    public String abstractSyntax;
    public List transferSyntaxList = new ArrayList();

    public final int pcid() {
        return pcid;
    }

    public final void pcid(int pcid) {
        this.pcid = pcid;
    }

    public final int result() {
        return result;
    }

    public final void result(int result) {
        this.result = result;
    }

    public final String getAbstractSyntax() {
        return abstractSyntax;
    }

    public final void setAbstractSyntax(String abstractSyntax) {
        this.abstractSyntax = abstractSyntax;
    }

    public final List getTransferSyntaxList() {
        return Collections.unmodifiableList(transferSyntaxList);
    }

    public int length() {
        // TODO Auto-generated method stub
        return 0;
    }

}
