<div class="$if(layout.displayClass)$$layout.displayClass$$else$menu$endif$" $html/style()$>
	<ul $if(layout.ulClass)$class="$layout.ulClass$"$endif$>
$layout.layouts:{layout|$if(layout.used)$$(layout.template)()$$endif$}$
	</ul>
</div>