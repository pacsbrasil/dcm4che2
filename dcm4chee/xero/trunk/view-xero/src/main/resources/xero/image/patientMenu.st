<div class="menu" $html/style()$>
	<ul>
	$if(layout.patient)$
	<li>$layout.patient.PatientName;format="xml"$</li>
	<li>| $layout.patient.PatientID;format="xml"$</li>
	
	$image/wlmenu()$
	
	$html/nestedMenu(baseItem="Layouts", childItems=image/layoutMenu())$
	
	$else$
	No Patient
	$endif$	
	</ul>
</div>