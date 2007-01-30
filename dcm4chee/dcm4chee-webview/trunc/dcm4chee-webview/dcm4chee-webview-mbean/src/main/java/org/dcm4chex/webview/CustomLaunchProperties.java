/*
 * Created on Oct 3, 2006
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.dcm4chex.webview;

import java.util.Map;
import java.util.Properties;

/**
 * Customization Class to set Applet Parameters as Properties not supported in LaunchProperties Class.
 * <p>
 * The Method <code>addCustomProperties</code> is called in <code>LaunchProperties.getProperties</code> 
 * with the Properties and C-FIND results.
 * <p>
 * Properties:<br>
 * key:   is the Name of Applet Parameter.<br>
 * value: the value of Applet Parameter.
 * <p>
 * Ensure that all attributes you need here are in the list of <code>getResultAttributes</code>.
 * 
 * @author franz.willer@agfa.com
 * @version $Revision:$ $Date:$
 * @since 04.10.2006
 *
 * 
 */
public interface CustomLaunchProperties {
    /**
     * Add customized properties to <code>p</code>.
     * <p>
     * <code>queryResult</code> contains the result of C-FIND:<br>
     * key:   Series Instance UID<br>
     * value: List of DicomObject objects of the series.
     * 
     * @param p
     * @param queryResult
     */
    void addCustomProperties(Properties p, Map queryResult);

    /**
     * List of attributes that this CustomLaunchProperties class need in the C-FIND result.
     * <p>
     * Attributes must be given as int[] (a tagPath) to allow attributes within sequences.
     * <p>
     * e.g. [0][0]=00400275; [0][1]=00400009 for SPS ID in the Request Attributes Sequence.
     * 
     * @return
     */
    int[][] getResultAttributes();
}
