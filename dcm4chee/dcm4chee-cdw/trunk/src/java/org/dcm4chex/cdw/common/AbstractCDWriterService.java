/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/
package org.dcm4chex.cdw.common;

import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import javax.management.ObjectName;

import org.jboss.system.ServiceMBeanSupport;


/**
 * @author gunter.zeilinter@tiani.com
 * @version $Revision$ $Date$
 * @since 22.06.2004
 *
 */
public abstract class AbstractCDWriterService extends ServiceMBeanSupport {

    protected SpoolDirDelegate spoolDir = new SpoolDirDelegate(this);

    private final MessageListener listener = new MessageListener()
	{

		public void onMessage(Message msg)
		{
			TextMessage txtmsg = (TextMessage) msg;
			try
			{
			    AbstractCDWriterService.this.process(txtmsg.getText(),
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

    protected abstract void process(String iuid, int retry);
}
