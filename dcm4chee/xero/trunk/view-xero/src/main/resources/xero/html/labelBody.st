$if(layout.icon)$
<a><img class="icon" src="$layout.icon$" alt="$layout.label$" /></a>
$elseif(layout.label)$
<a>$layout.label$</a>
$else$
<a>Unknown label</a>
$endif$
$if(layout.layouts)$
<ul>
$layout.layouts:{layout|$if(layout.used)$$(layout.template)()$$endif$}$
</ul>
$endif$