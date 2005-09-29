/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/

package org.dcm4che2.net.pdu;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author gunter zeilinger(gunterze@gmail.com)
 * @version $Reversion$ $Date$
 * @since Sep 15, 2005
 *
 */
public class PresentationContext {
    
    public static final int ACCEPTANCE = 0;
    public static final int USER_REJECTION = 1;
    public static final int PROVIDER_REJECTION = 2;
    public static final int ABSTRACT_SYNTAX_NOT_SUPPORTED = 3;
    public static final int TRANSFER_SYNTAX_NOT_SUPPORTED = 4;
    
    private static final String UNDEFINED = "undefined";
    private static final String[] RESULT = {
        "acceptance",
        "user-rejection",
        "no-reason (provider rejection)",
        "abstract-syntax-not-supported (provider rejection)",
        "transfer-syntaxes-not-supported (provider rejection)"    
    };
    
    private int pcid;
    private int result;
    public String abstractSyntax;
    public Set transferSyntaxes = new LinkedHashSet();

    public final int getPCID() {
        return pcid;
    }

    public final void setPCID(int pcid) {
        this.pcid = pcid;
    }

    public final int getResult() {
        return result;
    }

    public final void setResult(int result) {
        this.result = result;
    }

    public final String getAbstractSyntax() {
        return abstractSyntax;
    }

    public final void setAbstractSyntax(String abstractSyntax) {
        this.abstractSyntax = abstractSyntax;
    }

    public final Collection getTransferSyntaxes() {
        return Collections.unmodifiableCollection(transferSyntaxes);
    }
    
    public final boolean addTransferSyntax(String tsuid) {
        if (tsuid == null)
            throw new NullPointerException();
        
        return transferSyntaxes.add(tsuid);
    }
    
    public final boolean removeTransferSyntax(String tsuid) {
        return transferSyntaxes.remove(tsuid);
    }

    public int length() {
        int len = 8;
        if (abstractSyntax != null)
            len += 4 + abstractSyntax.length();
        for (Iterator it = transferSyntaxes.iterator(); it.hasNext();)
            len += 4 + ((String) it.next()).length();
        return len;
    }

    public String getResultAsString() {
        try {
            return RESULT[result];
        } catch (IndexOutOfBoundsException e) {
            return UNDEFINED;
        }
    }

}
