<html>
<head><title>Test Fetch</title></head>

<% System.out.println("Evaluating date now");
   java.util.Date date = new java.util.Date();
 %>
<body>
	The time is now <%= date %>
	Context = <%= request.getContextPath() %>
	PathInfo = <%= request.getPathInfo() %>
	PathTranslated = <%= request.getPathTranslated() %>
	ContextPath = <%= request.getContextPath() %>
	QueryString = <%= request.getQueryString() %>
	RequestURI = <%= request.getRequestURI() %>
	requestURL = <%= request.getRequestURL() %>
	servlet path = <%= request.getServletPath() %>
</body>
</html>