<head>
  <meta http-equiv="Content-Type" content="text/xhtml"/>
  <meta content="initial-scale=1.0" name="viewport"/>
  <link type="text/css" rel="stylesheet" href="style/general.css"> </link>
  <link type="text/css" rel="stylesheet" href="local/style.css"> </link>
  $if(i18n.cssTheme)$
  <link type="text/css" rel="stylesheet" href="$i18n.cssTheme$"> </link>
  $endif$
  $if(firebug)$
   <script type='text/javascript' src='http://getfirebug.com/releases/lite/1.2/firebug-lite-compressed.js'></script>
  $else$
   <script type='text/javascript' src='firebug/firebugx.js'></script>
  $endif$
  $if(IS_IE)$
    <style> v\:* { behavior: url(#default#VML); display: inline-block; overflow: hidden; position: absolute; }  </style>
  $endif$
  <title>Xero Query</title>
  $model/scripts()$
  <script type="text/javascript" src="modelI18n.jst?language=$locale;format="xml"$&amp;theme=$i18n.theme$"> </script>
  <script>
/** Define per-user information as required TODO - move this to a child script */
model.userName = "$userName$";  
  </script>
  <link rel="icon" href="favicon.ico" type="image/x-icon" />
  <link rel="shortcut icon" href="favicon.ico" type="image/x-icon" /> 
</head>