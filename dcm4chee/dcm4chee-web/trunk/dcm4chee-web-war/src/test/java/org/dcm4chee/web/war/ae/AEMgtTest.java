package org.dcm4chee.web.war.ae;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Locale;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;

import org.apache.wicket.Page;
import org.apache.wicket.authorization.strategies.role.IRoleCheckingStrategy;
import org.apache.wicket.authorization.strategies.role.RoleAuthorizationStrategy;
import org.apache.wicket.authorization.strategies.role.Roles;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.security.actions.Actions;
import org.apache.wicket.security.hive.HiveMind;
import org.apache.wicket.util.tester.FormTester;
import org.apache.wicket.util.tester.WicketTester;
import org.dcm4chee.archive.entity.AE;
import org.dcm4chee.archive.entity.FileSystem;
import org.dcm4chee.web.dao.ae.AEHomeBean;
import org.dcm4chee.web.war.MainPage;
import org.dcm4chee.web.war.WASPTestUtil;
import org.dcm4chee.web.war.WicketApplication;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bm.testsuite.BaseSessionBeanFixture;

public class AEMgtTest extends BaseSessionBeanFixture<AEHomeBean>
{
    
    private AEMgtApplication testApplicaton;
    private WicketTester wicketTester;
    private ArrayList<AE> aeList = new ArrayList<AE>(5);

    private static Logger log = LoggerFactory.getLogger(AEMgtTest.class);
    
    private static final Class<?>[] usedBeans = {FileSystem.class, AE.class};

    public AEMgtTest() throws Exception {
        super(AEHomeBean.class, usedBeans);
    }
    
    @Override
    public void setUp() throws Exception
    {
        super.setUp();
        testApplicaton = new AEMgtApplication();
        wicketTester = WASPTestUtil.getWicketTester(testApplicaton);
        aeList.add(getTestAE("AE_TEST", "localhost", 11112));
        //aeList.add(getTestAE("AE_FAILED", "localhost", 12222));
        //aeList.add(getTestAE("AE_TEST2", "localhost", 11113));
        //aeList.add(getTestAE("AE_TEST3", "localhost", 11114));
    }
    
    @Test
    public void testOpenAEMgt() {
        wicketTester.startPage(AETestPage.class);
        doLogin("aemgr","aemgr");
        wicketTester.assertRenderedPage(AETestPage.class);
        wicketTester.assertComponent("aelist", AEListPanel.class);
    }

    @Test
    public void testOpenEditWindow() {
        wicketTester.startPage(AETestPage.class);
        doLogin("aemgr","aemgr");
        wicketTester.assertRenderedPage(AETestPage.class);
        wicketTester.getComponentFromLastRenderedPage("aelist").getSession().setLocale(new Locale("en"));
        wicketTester.clickLink("aelist:newAET");
        wicketTester.assertNoErrorMessage();
        assertTrue("ModalWindow.isShown:", ((ModalWindow)wicketTester.getComponentFromLastRenderedPage("aelist:modal-window")).isShown());
    }
    
    @Test
    public void testEditAERequiredFields() {
        wicketTester.startPage(AETestPage.class);
        doLogin("aemgr","aemgr");
        wicketTester.assertRenderedPage(AETestPage.class);
        wicketTester.startPage(new CreateOrEditAETPage(new ModalWindow("test"), new AE(), null));
        wicketTester.assertRenderedPage(CreateOrEditAETPage.class);
        wicketTester.getComponentFromLastRenderedPage("form").getSession().setLocale(new Locale("en"));
        FormTester formTester = wicketTester.newFormTester("form");
        formTester.setValue("title", "");
        formTester.setValue("hostName", "");
        formTester.setValue("port", "0");
        formTester.submit();
        String[] expectedErrors = new String[]{"Field 'title' is required.",
                "Field 'hostName' is required.",
                "0 is not between 1 and 65535."};
        wicketTester.assertErrorMessages(expectedErrors);
    }
    
    @Test
    public void testEditAEValidators() {
        wicketTester.startPage(AETestPage.class);
        doLogin("aemgr","aemgr");
        wicketTester.assertRenderedPage(AETestPage.class);
        wicketTester.startPage(new CreateOrEditAETPage(new ModalWindow("test"), new AE(), null));
        wicketTester.getComponentFromLastRenderedPage("form").getSession().setLocale(new Locale("en"));
        FormTester formTester = wicketTester.newFormTester("form");
        formTester.setValue("title", "AE_TEST_TO_LONGLONG");
        formTester.setValue("hostName", "");
        formTester.setValue("port", "100000000");
        formTester.setValue("wadoURL", "http://127.0.0.1.1/wado");
        formTester.submit();
        String[] expectedErrors = new String[]{"'AE_TEST_TO_LONGLONG' is not between 1 and 16 characters long.",
              "Field 'hostName' is required.",
              "100000000 is not between 1 and 65535.",
              "'http://127.0.0.1.1/wado' is not a valid URL."};
        wicketTester.assertErrorMessages(expectedErrors);
    }
    
    @Test
    public void testNewAE() {
        wicketTester.startPage(AETestPage.class);
        doLogin("aemgr","aemgr");
        wicketTester.assertRenderedPage(AETestPage.class);
        for ( AE ae : aeList ) {
            wicketTester.startPage(new CreateOrEditAETPage(new ModalWindow("test"), new AE(), null));
            FormTester formTester = wicketTester.newFormTester("form");
            formTester.setValue("title", ae.getTitle());
            formTester.setValue("hostName", ae.getHostName());
            formTester.setValue("port", String.valueOf(ae.getPort()));
            getEntityManager().getTransaction().begin();
            formTester.submit("submit");
            getEntityManager().getTransaction().commit();
            wicketTester.assertNoErrorMessage();
        }
        wicketTester.startPage(AETestPage.class);
        wicketTester.assertListView("aelist:list", aeList);
    }
/*
    @Test
    public void testOpenEchoFromList() {
        initDummyMBean();
        wicketTester.startPage(AETestPage.class);
        doLogin("aemgr","aemgr");
        wicketTester.assertRenderedPage(AETestPage.class);
        wicketTester.assertListView("aelist:list", aeList);
        wicketTester.clickLink("aelist:list:0:echo");
        wicketTester.assertNoErrorMessage();
        wicketTester.assertComponent("aelist:echoPanel:content", DicomEchoWindow.DicomEchoPage.class);        
        assertTrue("DicomEchoWindow.isShown:", ((ModalWindow)wicketTester.getComponentFromLastRenderedPage("aelist:echoPanel")).isShown());
    }
/*
    @Test
    public void testUnauthorizedEdit() {
        wicketTester.startPage(AETestPage.class);
        doLogin("aemgr","aemgr");
        wicketTester.assertRenderedPage(AETestPage.class);
        wicketTester.getComponentFromLastRenderedPage("aelist").getSession().setLocale(new Locale("en"));
        wicketTester.assertInvisible("aelist:newAET");
//        wicketTester.assertInvisible("aelist:list:0:editAET");
//        wicketTester.assertInvisible("aelist:list:0:removeAET");
    }
    
    private void initDummyMBean() {
        MBeanServer mbServer = MBeanServerFactory.createMBeanServer();
        try {
            mbServer.createMBean("org.dcm4chee.web.war.ae.DummyEchoMBean", 
                    new ObjectName("dcm4chee.web:service=EchoService"));
        } catch (Exception ignore) {log.error("Can't create DummyEchoMBean!",ignore);}        
    }
/*_*/
    private AE getTestAE(String title, String host, int port) {
        AE ae = new AE();
        ae.setTitle(title);
        ae.setHostName(host);
        ae.setPort(port);
        return ae;
    }

    private void doLogin(String user, String passwd) {
        FormTester formTester = wicketTester.newFormTester("signInPanel:signInForm");
        formTester.setValue("username", user);
        formTester.setValue("password", passwd);
        formTester.submit();
    }
}
