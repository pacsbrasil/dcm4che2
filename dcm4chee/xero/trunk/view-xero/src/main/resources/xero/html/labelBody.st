<a class="jslink" $if(layout.action)$ onclick="$layout.action$('$layout.id$'$if(layout.arg)$,'$layout.arg;format="js"$'$endif$); return cancelEvent(event);" href="#"
$elseif(layout.href)$ href="$layout.href;format="xml"$?v=$version$" $if(layout.target)$target="$layout.target$"$endif$$else$ onclick="return cancelEvent(event);" href="#"$endif$>
$if(layout.i18n)$
$if(i18n.(layout.icon))$
<img class="$if(layout.isActive)$active $endif$icon" src="$i18n.(layout.icon)$?v=$version$" alt="$i18n.(layout.i18n);format="xml"$" />
$if(layout.showLabel)$ $i18n.(layout.i18n)$$endif$
$elseif(i18n.(layout.i18n))$
$i18n.(layout.i18n)$
$else$
$layout.i18n$
$endif$
$elseif(layout.icon)$
<img class="icon" src="$layout.icon$?v=$version$" alt="$layout.label;format="xml"$" />
$if(layout.showLabel)$ $layout.label;format="xml"$$endif$
$elseif(layout.label)$
$layout.label;format="xml"$
$else$
Unknown label
$endif$
$if(layout.layouts)$
$if(IS_IE6)$<table cellspacing="0" cellpadding="0" border="0"><tr><td>$else$</a>$endif$
<ul>
$layout.layouts:{layout|$if(layout.used)$$(layout.template)()$$endif$}$
</ul>
$if(IS_IE6)$</td></tr></table></a>$endif$
$else$
</a>
$endif$
