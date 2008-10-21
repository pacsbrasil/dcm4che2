<head>
  <meta http-equiv="Content-Type" content="text/xhtml"/>
  <meta content="initial-scale=1.0" name="viewport"/>
  <link type="text/css" rel="stylesheet" href="style/general.css"> </link>
  <link type="text/css" rel="stylesheet" href="local/style.css"> </link>
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
  <script>
/** Define per-user information as required TODO - move this to a child script */
model.userName = "$userName$";  
  </script>
</head>