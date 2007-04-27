package org.dcm4chex.archive.hsm.spi.client;

import org.dcm4chex.archive.hsm.spi.HsmClient;

import java.io.File;

/**
 * @author Fuad Ibrahimov
 * @version $Id$
 * @since Apr 22, 2007
 */
public class SimpleHsmClient implements HsmClient {

    public void retrieve(String filespace, String filePath, File destination) throws Exception {
    }

    public void archive(String filespace, File file) throws Exception {
    }

}
