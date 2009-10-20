package org.dcm4chee.web.wicket;

import org.apache.wicket.Page;
import org.apache.wicket.Request;
import org.apache.wicket.Response;
import org.apache.wicket.Session;
import org.apache.wicket.authentication.AuthenticatedWebApplication;
import org.apache.wicket.authentication.AuthenticatedWebSession;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.protocol.http.WebApplication;

/**
 * Application object for your web application. If you want to run this
 * application without deploying, run the Start class.
 * 
 * @see wicket.myproject.Start#main(String[])
 */
public class WicketApplication extends AuthenticatedWebApplication {
    /**
     * Constructor
     */
    public WicketApplication() {
    }
    
    @Override
    protected void init() {
        super.init();
        getApplicationSettings().setAccessDeniedPage(LoginPage.class);
        getApplicationSettings().setPageExpiredErrorPage(LoginPage.class);
        mountBookmarkablePage("/login", LoginPage.class);
    }

    @Override
    public Class<? extends Page> getHomePage() {
        return MainPage.class;
    }

    @Override
    protected Class<? extends WebPage> getSignInPageClass() {
        return LoginPage.class;
    }

    @Override
    protected Class<? extends AuthenticatedWebSession> getWebSessionClass() {
        return JaasWicketSession.class;
    }

}
