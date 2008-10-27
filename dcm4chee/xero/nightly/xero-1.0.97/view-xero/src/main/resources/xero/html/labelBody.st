$if(layout.icon)$
<img class="icon" src="$layout.icon$" alt="$layout.label$" />
$elseif(layout.label)$
$layout.label$
$else$
Unknown label
$endif$