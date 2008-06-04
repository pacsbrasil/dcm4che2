<div class="menu" $html/style()$>
	<ul>
	$if(layout.patient)$
	<li>$layout.patient.PatientName$</li>
	<li>| $layout.patient.PatientID$</li>
	
	$image/wlmenu()$
	
	$html/nestedMenu(baseItem="Layouts", childItems=image/layoutMenu())$
	$else$
	No Patient
	$endif$	
	</ul>
</div>