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
import javax.jms.ObjectMessage;

import org.jboss.system.ServiceMBeanSupport;


/**
 * @author gunter.zeilinter@tiani.com
 * @version $Revision$ $Date$
 * @since 22.06.2004
 *
 */
public abstract class AbstractMediaWriterService extends ServiceMBeanSupport {

    private final MessageListener listener = new MessageListener()
	{

		public void onMessage(Message msg)
		{
			ObjectMessage objmsg = (ObjectMessage) msg;
			try
			{
			    AbstractMediaWriterService.this.process((MediaCreationRequest)objmsg.getObject());
			} catch (Throwable e)
			{
				log.error(e.getMessage(), e);
			}
		}

	};

	protected void startService() throws Exception
	{
		JMSDelegate.getInstance().setMediaWriterListener(listener);
	}

	protected void stopService() throws Exception
	{
		JMSDelegate.getInstance().setMediaWriterListener(null);
	}

    protected abstract void process(MediaCreationRequest rq);
}
