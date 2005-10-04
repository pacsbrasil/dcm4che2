/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/

package org.dcm4che2.net;

/**
 * @author gunter zeilinger(gunterze@gmail.com)
 * @version $Reversion$ $Date$
 * @since Oct 2, 2005
 *
 */
public class NewThreadExecutor implements Executor
{
    private static int threadId = 0;

    private final String threadNamePrefix;
    
    public NewThreadExecutor(String threadNamePrefix)
    {
        if( threadNamePrefix == null )
        {
            throw new NullPointerException( "threadNamePrefix" );
        }
        threadNamePrefix = threadNamePrefix.trim();
        if( threadNamePrefix.length() == 0 )
        {
            throw new IllegalArgumentException( "threadNamePrefix is empty." );
        }
        this.threadNamePrefix = threadNamePrefix;
    }

    public final String getThreadNamePrefix()
    {
        return threadNamePrefix;
    }
    
    public void execute(Runnable runnable)
    {
        new Thread(runnable, threadNamePrefix + "-" + (++threadId)).start();        
    }

}
