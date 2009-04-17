<?xml version="1.0" encoding="utf-8"?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
<%@ page import="java.util.*" %>
<%@ page import="java.io.*" %>
<%@ page import="org.jahia.bin.*" %>
<%@ page import="org.jahia.utils.JahiaTools" %>

<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<%
        boolean exists = (new File("webapps/ROOT/WEB-INF/etc/config/jahia.properties")).exists();
        if (exists) {
        %>
            <meta http-equiv="Refresh" content="10;url=/cms/">
        <%
            // File did not exist and was created
        } else {
        	%>
        	 <meta http-equiv="Refresh" content="10;url=/config/">
        	<%
            // File already exists
        }
%>	
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
<title>Loading Server...</title>
<link href="misc/startup.css" rel="stylesheet" type="text/css" />
</head>

<body>
<div id="page">
  <div id="content">
  	<h1 class="hide">Loading Jahia Server...</h1>
      <p><strong>Welcome to Jahia.</strong> </p>
      <p><strong>Loading Jahia Server...</strong></p><br />
		<img class="wait" src="img/wait.gif" alt="" />
  </div>
</div>
</body>
</html>
