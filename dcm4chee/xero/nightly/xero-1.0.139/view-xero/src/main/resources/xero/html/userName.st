<li><a class="userName toolItem" href="#">$userName$
$if(layout.layouts)$
$if(IS_IE)$<table cellspacing="0" cellpadding="0" border="0"><tr><td>$else$</a>$endif$
<ul>
$layout.layouts:{layout|$if(layout.used)$$(layout.template)()$$endif$}$
</ul>
$if(IS_IE)$</td></tr></table></a>$endif$
$else$
</a>
$endif$
</li>