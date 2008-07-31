$if(IS_IE)$
<li>
  <a href="#" onclick="return false;">$baseItem$
	<table cellspacing="0" cellpadding="0" border="0"><tr><td><ul>
  	$childItems$
  	</ul></td></tr></table>
</a></li>
$else$
<li>
  $baseItem$
  <ul>
  	$childItems$
  </ul>
</li>
$endif$
