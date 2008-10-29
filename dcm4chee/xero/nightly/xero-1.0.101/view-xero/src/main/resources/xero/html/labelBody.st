<a
$if(layout.action)$ onclick="return $layout.action$('$layout.id$');"
$elseif(layout.href)$ href="$layout.href;format="xml"$" $if(layout.target)$target="$layout.target$"$endif$$endif$>

$if(layout.i18n)$
$if(layout.icon)$
<img class="icon" src="$i18n.(layout.icon)$" alt="$i18n.(layout.i18n);format="xml"$" />
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
</a>
$if(layout.layouts)$
<ul>
$layout.layouts:{layout|$if(layout.used)$$(layout.template)()$$endif$}$
</ul>
$endif$