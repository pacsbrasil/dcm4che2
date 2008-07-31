<head>
  <meta http-equiv="Content-Type" content="text/xhtml"/>
  <meta content="initial-scale=1.0" name="viewport"/>
  <link type="text/css" rel="stylesheet" href="style/general.css"> </link>
  <link type="text/css" rel="stylesheet" href="local/style.css"> </link>
  $if(IS_IE)$
    <style> v\:* { behavior: url(#default#VML); display: inline-block; overflow: hidden; position: absolute; }  </style>
  $endif$
  <title>Xero Query</title>
  $model/scripts()$
  <script>
/** Define per-user information as required TODO - move this to a child script */
xeroModel.userName = "$userName$";  
  </script>
</head>