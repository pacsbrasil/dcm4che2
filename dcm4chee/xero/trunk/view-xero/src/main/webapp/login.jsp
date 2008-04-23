<%--
	$Id: login.jsp 3384 2007-06-13 12:27:53Z javawilli $
	$Source$
	
	Autologin for WebViewer:
	To allow external Webviewer access without login you can use request parameter to achieve autologin.
	1) accNr (Accession number) not null! 
	2) loginuser (user name)
	3) passwd4user (password for username in Base64)
	Be aware that this critical infos are visible in browser window!
	Therefore this user should only have WebUser role.
--%>
<%@ page import='org.dcm4che.util.Base64' %>
<%@ page import='java.net.InetAddress' %>
<html>
<head>
  <meta name="viewport" content="initial-scale=1.0"/> 
  <title>Login</title>
  <link href="style.css" rel="stylesheet" type="text/css">
</head>

<%
	String nodeInfo = System.getProperty("dcm4che.archive.nodename", InetAddress.getLocalHost().getHostName() );
    String agent = request.getHeader("USER-AGENT");
%>
<body onload="self.focus();document.login.j_username.focus()">
<% boolean isMobile = (agent.indexOf("Mobile")>=0 || agent.indexOf("BlackBerry")>=0);
   if( !isMobile ) { %>
<table border="0" cellspacing="0" cellpadding="0" width="100%">
 <tr>
  <td><img src="/dcm4chee-web/white48.jpg" width="100%" height="5px"></td>
 </tr>
 <tr>
  <td background="/dcm4chee-web/white48.jpg">
    <img src="/dcm4chee-web/white48.jpg" width="10px" height="24px"><img src="/dcm4chee-web/logo.gif" alt="DCM4CHEE"> Xero
  </td>
 </tr>
 <tr>
  <td><img src="/dcm4chee-web/line.jpg" width="100%" height="20px" alt="line"></td>
 </tr>
</table>
<% } %>
<center>
<h1>User Login at <%= nodeInfo %></h1>
<br>

<%
  if( request.getParameter("logout")!=null ) {
	 session.invalidate();
	 %> <h1>Logged out.  To login again, enter your name/password below.</h1> <% 
  }
%>
<form name="login" method="POST" action="j_security_check">
<table>
	<tr valign="middle">
	  <td><div class="text">Name:</div></td>
	  <td><input class="textfield" type="text" name="j_username" value=""/></td>
	</tr>
	<tr valign="middle">
	  <td><div class="text">Password:</div></td>
	  <td><input class="textfield" type="password" name="j_password" value=""/></td>
	</tr>
	<tr><td>&nbsp;</td></tr>
	<tr valign="middle">
	  <td>&nbsp;</td>
	  <td align="center"><input class="button" type="submit" value="Log in"></td>
	</tr>
</table>
</center>
<% if( !isMobile ) { %>
<a href="http://www.dcm4che.org/confluence/display/ee2/Xero">Xero WIKI</a><br />
<a href="http://www.dcm4che.org/jira/browse/XER">Xero Bug Reports</a><br />
<a href="http://sourceforge.net/project/showfiles.php?group_id=37982&package_id=236929">Xero Download</a><br />
<h2>Known Issues</h2>
<ul>
<li>Same Series/Study UID - if a study with the same UID for study/series/object is attempted to be shown, IE will show an error, while Firefox will just show a black series tray.</li>
<li>IE XSLT Bug - If IE only updates the mode/page once the user clicks on a second series to view, or redisplays the page, then it is probably caused by a bug in the MS XSLT - upgrade to a newer version of the XML library from MS.</li>
<li>Opera - never updates the page on mode changes etc, but does display.</li>
<li>Safari - only displays, no image change functionality (not sure what causes this yet.)</li>
<li>Some images in YBR 422 are corrupted when displayed.</li>
<li>US, MG are not split by echo and/or multi-frame split correctly.</li>
<li>IE will sometimes backup when playing CINE.</li>
<li><a href="/xero/ipod.html">iPOD/iPhone</a></li>
</ul>
<% } else { %>
Mobile
<% } %>

</form>
</body>
</html>
