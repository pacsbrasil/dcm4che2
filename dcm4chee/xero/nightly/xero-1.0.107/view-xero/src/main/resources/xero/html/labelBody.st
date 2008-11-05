<a $if(layout.action)$ onclick="return $layout.action$('$layout.id$');" href="#"
$elseif(layout.href)$ href="$layout.href;format="xml"$" $if(layout.target)$target="$layout.target$"$endif$$else$ onclick="return true;" href="#"$endif$>
$if(layout.i18n)$
$if(layout.icon)$
<img class="$if(layout.isActive)$active $endif$icon" src="$i18n.(layout.icon)$" alt="$i18n.(layout.i18n);format="xml"$" />
$if(layout.showLabel)$ $i18n.(layout.i18n)$$endif$
$elseif(layout.label)$
$i18n.(layout.i18n)$
$else$
$layout.i18n$
$endif$
$elseif(layout.icon)$
<img class="icon" src="$layout.icon$" alt="$layout.label$" />
$if(layout.showLabel)$ $layout.label$$endif$
$elseif(layout.label)$
$layout.label$
$else$
Unknown label
$endif$
$if(layout.layouts)$
$if(IS_IE)$<table cellspacing="0" cellpadding="0" border="0"><tr><td>$else$</a>$endif$
<ul>
$layout.layouts:{layout|$if(layout.used)$$(layout.template)()$$endif$}$
</ul>
$if(IS_IE)$</td></tr></table></a>$endif$
$else$
</a>
$endif$
