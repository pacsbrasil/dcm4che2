<div class="menu" $html/style()$>
	<ul>
	$if(layout.patient)$
$layout.layouts:{layout|$if(layout.used)$$(layout.template)()$$endif$}$

	<li>$layout.patient.PatientName;format="xml"$</li>
	<li>| $layout.patient.PatientID;format="xml"$</li>
	<li> $layout.patient.PatientSex;format="xml"$</li>
	<li> $layout.patient.birthDateF;format="xml"$</li>

	$else$
	No Patient
	$endif$	
	</ul>
</div>