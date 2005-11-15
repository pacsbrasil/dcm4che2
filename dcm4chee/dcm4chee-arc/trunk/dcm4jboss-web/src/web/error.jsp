<%--
	$Id$
	$Source$
--%>
<html>
<head>
	<title>DCM Folder Login</title>

<style type="text/css">
.text
{
  font-size: 14px;
  color: #dddddd;
  line-height: 21px;
  font-family: Verdana,Arial,Helvetica;
}

.head
{
  font-size: 18px;
  color: #dddddd;
  line-height: 21px;
  font-family: Verdana,Arial,Helvetica;
}

.textfield
{
  color: #000000;
  background-color: #99bbaa;
  border-color: #000000;
  border-style: solid;
}

.button {
	border-style: solid;
	border-color: #000000;
	background-color: #99BBAA;
	color: #000000;
}

body
{
  background-color: #666666;
}

a:link {
	text-decoration: none;
	color: #99bbaa;
	font-size: 14px;
   line-height: 21px;
   font-family: Verdana,Arial,Helvetica;
}

a:visited {
	text-decoration: none;
	color: #99bbaa;
	font-size: 14px;
   line-height: 21px;
   font-family: Verdana,Arial,Helvetica;
}

a:hover {
	text-decoration: none;
	color: #99bbaa;
	font-size: 14px;
   line-height: 21px;
   font-family: Verdana,Arial,Helvetica;
}

a:active {
	text-decoration: none;
	color: #99bbaa;
	font-size: 14px;
   line-height: 21px;
   font-family: Verdana,Arial,Helvetica;
}
</style>

</head>
<body onload="self.focus();document.login.j_username.focus()">
	<div class="head">DCM Folder Login Failed!</div>
	<br>
	<form name="login" method="POST" action="j_security_check">
		<table>
	  		<tr valign="middle">
	  			<td><div class="text">Name:</div></td>
	   			<td><input class="textfield" type="text" name="j_username"/></td>
	   		</tr>
	  		<tr valign="middle">
	  			<td><div class="text">Password:</div></td>
	   			<td><input class="textfield" type="password" name="j_password"/></td>
	   		</tr>
			<tr><td>&nbsp;</td></tr>
	  		<tr valign="middle">
				<td>&nbsp;</td>
	   			<td align="center"><input class="button" type="submit" value="Log in"></td>
	   		</tr>
	</form>
</body>
</html>
