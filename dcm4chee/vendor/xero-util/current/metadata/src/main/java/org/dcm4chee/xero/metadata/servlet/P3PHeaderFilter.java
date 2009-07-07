/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is part of dcm4che, an implementation of DICOM(TM) in
 * Java(TM), hosted at http://sourceforge.net/projects/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * Agfa HealthCare Inc., 
 * Portions created by the Initial Developer are Copyright (C) 2009
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Bill Wallace <bill.wallace@agfa.com>
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 *
 * ***** END LICENSE BLOCK ***** */
package org.dcm4chee.xero.metadata.servlet;

import java.io.IOException;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

import org.dcm4chee.xero.metadata.filter.FilterItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Adds a P3P header to the response.  Parameter p3p can be used to configure the p3p to use.
 * @author bwallace
 */
@SuppressWarnings("unchecked")
public class P3PHeaderFilter  implements Filter, org.dcm4chee.xero.metadata.filter.Filter {
    private static final Logger log = LoggerFactory.getLogger(P3PHeaderFilter.class);
    
    private String p3p = "ALL ADM DEV HIS PSA UNI STA COM INT IND";
    private String p3pCP = "CP=\""+p3p+"\"";
    
    public void doFilter(ServletRequest request, ServletResponse resp, FilterChain filterChain) throws IOException, ServletException {
        log.info("Adding privacy statement "+p3p);
        HttpServletResponse response = (HttpServletResponse) resp;
        response.setHeader("P3P",p3pCP);
        filterChain.doFilter(request,resp);
    }

    public String getP3p() {
        return p3p;
    }

    public void setP3p(String p3p) {
        this.p3p = p3p;
        this.p3pCP = "CP=\""+p3p+"\"";
    }

    public void destroy() {
    }

    public void init(FilterConfig conf) throws ServletException {
        String p3pTest = conf.getInitParameter("p3p");
        if( p3pTest!=null ) setP3p(p3pTest);
    }

    public Object filter(FilterItem filterItem, Map params) {
        log.info("Adding privacy statement in metadata filter "+p3p);
        HttpServletResponse response = (HttpServletResponse) params.get(MetaDataServlet.RESPONSE);
        response.addHeader("P3P",p3pCP);
        return filterItem.callNextFilter(params);

    }

}
