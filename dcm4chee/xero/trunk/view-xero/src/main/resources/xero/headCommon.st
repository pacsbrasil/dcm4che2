<head>
  <meta http-equiv="Content-Type" content="text/xhtml"/>
  <meta content="initial-scale=1.0" name="viewport"/>

  $! TODO - combine the stylesheets dynamically !$
  <link type="text/css" rel="stylesheet" href="style/general.css?v=$version$"> </link>
  <link type="text/css" rel="stylesheet" href="local/style.css?v=$version$"> </link>
  $if(i18n.cssTheme)$
  <link type="text/css" rel="stylesheet" href="$i18n.cssTheme$?v=$version$"> </link>
  $endif$
  $if(IS_IE)$
    <style> v\:* { behavior: url(#default#VML); display: inline-block; overflow: hidden; position: absolute; }
    $if(IS_IE6)$
    div.hoverMenu li {	filter:alpha(opacity=100);  }
    $endif$
      </style>
    <link type="text/cass" rel="stylesheet" href="style/ie.css?v=$version$"> </link>
  $elseif(IS_FIREFOX)$
  	<style>
  		table { height: 100%; }
  		tbody { overflow: auto; height: 95%; }
  	</style>
  $endif$
  <title>Xero Query</title>
  
  $firebugInclude()$
  
  <link rel="icon" href="favicon.ico?v=$version$" type="image/x-icon" />
  <link rel="shortcut icon" href="favicon.ico?v=$version$" type="image/x-icon" /> 
</head>