package org.dcm4chex.archive.hsm.spi.client;

import org.dcm4chex.archive.hsm.spi.HsmClient;

import java.io.File;

/**
 * An implementation of {@link org.dcm4chex.archive.hsm.spi.HsmClient} using underlying OS level commands
 * to interface HSM systems. 
 * @author Fuad Ibrahimov
 * @since Apr 22, 2007
 */
public class SimpleHsmClient implements HsmClient {

    public void retrieve(String filespace, String filePath, File destination) throws Exception {
        // TODO implement me
    }

    public void archive(String filespace, File file) throws Exception {
        // TODO implement me
    }

}
