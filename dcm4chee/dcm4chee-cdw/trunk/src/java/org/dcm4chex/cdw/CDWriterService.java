/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/
package org.dcm4chex.cdw;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import javax.management.ObjectName;

import org.dcm4che.data.Dataset;
import org.dcm4che.dict.Tags;
import org.jboss.system.ServiceMBeanSupport;


/**
 * @author gunter.zeilinter@tiani.com
 * @version $Revision$ $Date$
 * @since 22.06.2004
 *
 */
public class CDWriterService extends ServiceMBeanSupport {

    private SpoolDirDelegate spoolDir = new SpoolDirDelegate(this);

    private final MessageListener listener = new MessageListener()
	{

		public void onMessage(Message msg)
		{
			TextMessage txtmsg = (TextMessage) msg;
			try
			{
			    CDWriterService.this.process(txtmsg.getText(),
				        txtmsg.getIntProperty(JMSDelegate.PROPERTY_RETRY));
			} catch (Throwable e)
			{
				log.error(e.getMessage(), e);
			}
		}

	};

    public final ObjectName getSpoolDirName() {
        return spoolDir.getSpoolDirName();
    }

    public final void setSpoolDirName(ObjectName spoolDirName) {
        spoolDir.setSpoolDirName(spoolDirName);
    }

	protected void startService() throws Exception
	{
		JMSDelegate.getInstance().listenPending(listener);
	}

	protected void stopService() throws Exception
	{
		JMSDelegate.getInstance().listenPending(null);
	}

    private void process(String iuid, int retry) {
        File f = spoolDir.getMediaCreationRequestFile(iuid);
        Dataset mcrq;
        try {
            mcrq = spoolDir.readDatasetFrom(f);
        } catch (FileNotFoundException e) {
            return; // was canceled
        } catch (IOException e) {
            return;
        }
        mcrq.putCS(Tags.ExecutionStatus, ExecutionStatus.CREATING);
        mcrq.putCS(Tags.ExecutionStatusInfo, ExecutionStatusInfo.NORMAL);
        try {
            spoolDir.writeDatasetTo(mcrq, f);
        } catch (IOException ignore) {
        }
        //TODO
        try {
            Thread.sleep(100000L);
        } catch (InterruptedException e1) {
        }
        
        spoolDir.deleteMediaLayouts(iuid);
        spoolDir.deleteRefInstances(mcrq);
        mcrq.putCS(Tags.ExecutionStatus, ExecutionStatus.DONE);
        mcrq.putCS(Tags.ExecutionStatusInfo, ExecutionStatusInfo.NORMAL);
        try {
            spoolDir.writeDatasetTo(mcrq, f);
        } catch (IOException ignore) {
        }
    }
}
