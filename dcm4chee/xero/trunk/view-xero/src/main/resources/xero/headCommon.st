<head>
  <meta http-equiv="Content-Type" content="text/xhtml"/>
  <meta content="initial-scale=1.0" name="viewport"/>
  <link type="text/css" rel="stylesheet" href="style/general.css?v=$version$"> </link>
  <link type="text/css" rel="stylesheet" href="local/style.css?v=$version$"> </link>
  $if(i18n.cssTheme)$
  <link type="text/css" rel="stylesheet" href="$i18n.cssTheme$?v=$version$"> </link>
  $endif$
  $if(firebug)$
   <script type='text/javascript' src='http://getfirebug.com/releases/lite/1.2/firebug-lite-compressed.js'></script>
  $else$
   <script type='text/javascript' src='firebug/firebugx.js?v=$version$'></script>
  $endif$
  $if(IS_IE)$
    <style> v\:* { behavior: url(#default#VML); display: inline-block; overflow: hidden; position: absolute; }
    $if(IS_IE6)$
    div.hoverMenu ul {	visibility: visible;  }
    $endif$
      </style>
    <link type="text/cass" rel="stylesheet" href="style/ie.css?v=$version$"> </link>
  $elseif(IS_FIREFOX)$
  	<style>
  		table { height: 75%; }
  		tbody { overflow: auto; height: 95%; }
  	</style>
  $endif$
  <title>Xero Query</title>
  $model/scripts()$
  <script type="text/javascript" src="modelI18n.jst?language=$locale;format="xml"$&amp;theme=$i18n.theme$&amp;v=$version$"> </script>
  <script>
/** Define per-user information as required TODO - move this to a child script */
model.userName = "$userName$";  
model.ae = "$ae;format="xml"$";
model.version = "$version$";
  </script>
  <link rel="icon" href="favicon.ico?v=$version$" type="image/x-icon" />
  <link rel="shortcut icon" href="favicon.ico?v=$version$" type="image/x-icon" /> 
</head>