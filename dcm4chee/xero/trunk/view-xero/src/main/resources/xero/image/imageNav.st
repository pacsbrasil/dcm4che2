$! Contains image navigation operations !$
$if(layout.used)$
<div id="imageNav$layout.id$" class="menu" style="position: absolute; width: 10em; height: 2em; bottom: 0;left:50%;margin-left:-5em;">
<ul>
<li><img id="prev$layout.id$" src="$if(layout.isFirst)$$i18n.PrevDisabledIcon$$else$$i18n.PrevIcon$$endif$?v=$version$" onclick="controller.navImg('$layout.id$',-1)"/></li>
<li>$layout.x$</li>
<li>
<img id="next$layout.id$" src="$if(layout.isLast)$$i18n.NextDisabledIcon$$else$$i18n.NextIcon$$endif$?v=$version$" onclick="controller.navImg('$layout.id$',1)"/>
</li>
<li>$i18n.Of$ $layout.n$</li></ul>
</div>
$endif$