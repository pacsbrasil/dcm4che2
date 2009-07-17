package org.dcm4chee.web.wicket;

import org.apache.wicket.Page;
import org.apache.wicket.Request;
import org.apache.wicket.Response;
import org.apache.wicket.Session;
import org.apache.wicket.protocol.http.WebApplication;

/**
 * Application object for your web application. If you want to run this
 * application without deploying, run the Start class.
 * 
 * @see wicket.myproject.Start#main(String[])
 */
public class WicketApplication extends WebApplication {
    /**
     * Constructor
     */
    public WicketApplication() {
    }

    @Override
    public Class<? extends Page> getHomePage() {
        return MainPage.class;
    }

    @Override
    public Session newSession(Request request, Response response) {
         return new WicketSession(request);
    }

}
