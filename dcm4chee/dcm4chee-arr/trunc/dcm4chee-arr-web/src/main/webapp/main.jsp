<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://jboss.com/products/seam/taglib" prefix="s" %>
<html>
 <head>
  <title>Audit Records</title>
 </head>
 <body>
  <f:view>
<div class="section">
  <h:form>        
     <h:commandButton value="Refresh" action="#{auditRecordList.find}" styleClass="button"/>
  </h:form>
</div>
<div class="section">
     <h:outputText value="No Audit Records found" rendered="#{records.rowCount==0}"/>
     <h:dataTable var="record" value="#{records}" rendered="#{records.rowCount>0}">
        <h:column>
           <f:facet name="header">
              <h:outputText value="Event Date/Time"/>
           </f:facet>
           <h:outputText value="#{record.eventDateTime}">
              <f:convertDateTime type="both" dateStyle="medium" timeStyle="short"/>
           </h:outputText>
        </h:column>
     </h:dataTable>
</div>
  </f:view>
 </body>
</html>