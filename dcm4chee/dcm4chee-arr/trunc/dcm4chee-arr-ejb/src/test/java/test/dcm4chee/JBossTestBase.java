package test.dcm4chee;

import java.util.Iterator;
import java.util.Set;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectInstance;
import javax.management.ObjectName;

import org.apache.log4j.Logger;
import org.jboss.ejb3.embedded.EJB3StandaloneBootstrap;
import org.jboss.ejb3.embedded.EJB3StandaloneDeployer;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;

/**
 * Boots the JBoss Microcontainer with an EJB3 configuration. The container will be initialized per
 * test suite.
 * <p>
 * 
 * @author fyang
 * @version $id$
 * @since Jul 25, 2006 11:57:40 PM
 * 
 */
public class JBossTestBase {

	protected final static Logger log = Logger.getLogger(JBossTestBase.class);

	private static final String SERVICE_FILTER_NAME = "dcm4chee.*:*";

	private EJB3StandaloneDeployer deployer;

	@BeforeSuite(groups={"slsb","jmx", "mdb", "integration.jmx", "stress.jmx"})
	public void startup() {
		try {
			log.info(">>> Start Bootstrapping JBoss EJB3 Microcontainer ......");

			// Boot the JBoss Microcontainer with EJB3 settings, loads ejb3-interceptors-aop.xml
			EJB3StandaloneBootstrap.boot(null);
			
			// Deploy additional beans
			log.info("  > start deploying addition resource xml ....");
			EJB3StandaloneBootstrap.deployXmlResource("jboss-jms-beans.xml");
			EJB3StandaloneBootstrap.deployXmlResource("testjms.xml");
			log.info("  < done!");
			
			log.info("  > start deploying by scanning classpath ....");
			EJB3StandaloneBootstrap.scanClasspath();
			log.info("  < done!");

			deployer = new EJB3StandaloneDeployer();
			deployer.setKernel(EJB3StandaloneBootstrap.getKernel());

			// Deploy everything we got
			deployer.create();
			deployer.start();
			
			startMBeans();
			
			log.info("<<< End of bootstrapping EJB3 Microcontainer .... ");			
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new RuntimeException(ex);
		}
	}
	
	/**
	 * We have to start the JMX MBean manually! This is a workaround to the
	 * failure to invoke JMX lifecycle methods.
	 * 
	 * @see http://jira.jboss.com/jira/browse/EJBTHREE-606
	 */
	private void startMBeans() throws Exception {
		log.info("  > starting all available JMX MBeans ...");
		MBeanServer server = (MBeanServer) MBeanServerFactory.findMBeanServer(null).get(0);
		Set set = server.queryMBeans(new ObjectName(SERVICE_FILTER_NAME), null);
		Iterator iterator = set.iterator();
		while (iterator.hasNext())
        {
			ObjectInstance oi = (ObjectInstance) iterator.next();
	        ObjectName on = oi.getObjectName();
	        try { server.invoke(on, "start", new Object[] {},new String[] {}); } catch(Exception ex) {};		        
        }
		
		// Make sure the server is running
		Thread.sleep(500);
	}
	
	private void stopMBeans() throws Exception {
		log.info("  > stopping all available JMX MBeans ...");
		MBeanServer server = (MBeanServer) MBeanServerFactory.findMBeanServer(null).get(0);
		Set set = server.queryMBeans(new ObjectName(SERVICE_FILTER_NAME), null);
		Iterator iterator = set.iterator();
		while (iterator.hasNext())
        {
			ObjectInstance oi = (ObjectInstance) iterator.next();
	        ObjectName on = oi.getObjectName();
	        try { server.invoke(on, "stop", new Object[] {},new String[] {}); } catch(Exception ex) {};		        
        }
	}

	@AfterSuite(groups={"slsb","jmx", "mdb", "integration.jmx", "stress.jmx"})
	public void shutdown() {
		try {
			// Make sure all spawned threads are finished 
			Thread.sleep(1000);
			
			stopMBeans();
			
			log.info(">>> Shutting down JBoss  EJB3 Microcontainer ......");
			deployer.stop();
			deployer.destroy();
			EJB3StandaloneBootstrap.shutdown();
			log.info("<<< End of shutting down JBoss  EJB3 Microcontainer ......");
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new RuntimeException(ex);
		}
	}
	
	
}
