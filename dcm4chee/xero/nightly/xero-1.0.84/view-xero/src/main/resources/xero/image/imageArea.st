<div class="layout" $html/style()$ onmousewheel="return xeroController.navWheelImg('$layout.id$',window.event)" DOMMouseScroll="alert('DOMMouseScroll.')">
$layout.layouts:{layout|$if(layout.used)$$(layout.template)()$$endif$}$
</div>