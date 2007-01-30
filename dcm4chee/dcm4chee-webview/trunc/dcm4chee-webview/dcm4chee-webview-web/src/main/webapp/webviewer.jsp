<%-- Version $Revision:$ $Date:$ --%>
 <%@ page import="java.util.Properties" %>
 <%@ page import="java.util.Iterator" %>
 <%@ page import="java.util.List" %>
 <jsp:useBean id="delegate" class="org.dcm4chex.webview.WebViewDelegate" scope="request"/>
 <%
 	if ( request.getParameter("ignorePR") != null ) delegate.setIgnorePR(new Boolean(request.getParameter("ignorePR") ) );
 	if ( request.getParameter("selectPR") != null ) delegate.setSelectPR(new Boolean(request.getParameter("selectPR") ) );
 	Properties props = delegate.getLaunchProperties( request.getParameterMap() );
	String mode = (String) props.remove("launchMode");
	String key;
 %>
<html><head><META http-equiv="Content-Type" content="text/html; charset=UTF-8"><%
%><title><%=props.getProperty("title")%></title><%
%></head><%
	if ( !"select".equals( mode ) ) {
%><body>
	<script language="JavaScript"><!--
		var W=600,H=600;
		if (parseInt(navigator.appVersion)>3) 
		{
			if (navigator.appName=="Netscape") 
			{
				W = window.innerWidth-20;
				H = window.innerHeight-35;
			}
			if (navigator.appName.indexOf("Microsoft")!=-1) 
			{
				W = document.body.offsetWidth-30;
				H = document.body.offsetHeight-35;
			}
			else
			{
				W = window.innerWidth-15;
				H = window.innerHeight-20;
			}
		}

		document.writeln('<APPLET WIDTH ='+W+' HEIGHT = '+H+' >\
		<%      
		for ( Iterator iter = props.keySet().iterator() ; iter.hasNext() ;) {	
			key = (String) iter.next();
			%><PARAM NAME = "<%=key %>" VALUE ="<%=props.getProperty(key) %>" />  \
	<% } 
			%><PARAM NAME="type" VALUE="application/x-java-applet;version=1.4" /> \
			<PARAM NAME="scriptable" VALUE="false" /> \
		</APPLET> ');
	//--></script>
</body>
<% } else { 
		String url = request.getRequestURL().append( "?ignorePR=true&amp;" ).append( request.getQueryString() ).toString();
%>
<body>
	<table border="0" cellspacing="0" cellpadding="0" width="100%">
		<tr>
			<td>
				<a title="Without Presentation State" href="<%= url%>">No Presentation State</a>
			</td>
		</tr>
<%
	for ( Iterator iter=props.keySet().iterator() ; iter.hasNext() ; ) {
		key = (String) iter.next();
%>		
		<tr>
			<td>
				<a title="Presentation State" href="webviewer.jsp?prUID=<%=key %>">Presentation State (<%=props.getProperty(key) %>)</a>
			</td>
		</tr>
<% 
	} %>

	</table>
</body>	
<% } %>
</html>
