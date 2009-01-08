<li><a class="userName toolItem" href="#">$userName$
$if(layout.layouts)$
$if(IS_IE6)$<table cellspacing="0" cellpadding="0" border="0"><tr><td>$else$</a>$endif$
<ul>
$layout.layouts:{layout|$if(layout.used)$$(layout.template)()$$endif$}$
</ul>
$if(IS_IE6)$</td></tr></table></a>$endif$
$else$
</a>
$endif$
</li>