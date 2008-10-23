<div class="$layout.displayClass$ layout" $html/style()$ $if(layout.onmousewheel)$ onmousewheel="return $layout.onmousewheel$('$layout.id$',window.event,this)"$endif$>
$if(layout.named.north)$$(layout.named.north.template)(layout=layout.named.north)$$endif$
$if(layout.named.west)$$(layout.named.west.template)(layout=layout.named.west)$$endif$
$if(layout.named.east)$$(layout.named.east.template)(layout=layout.named.east)$$endif$
$layout.layouts:{layout|$if(layout.used)$$(layout.template)()$$endif$}$
$if(layout.named.south)$$(layout.named.south.template)(layout=layout.named.south)$$endif$
</div>