<!-- This is the per-study control containing the series thumbnails and controls within this study. -->
<div class="studyTray" $html/style()$>
$if(layout.study)$
	<span class="studyDescription toolItem">$layout.study.StudyDescription$ </span>
	<span class="studyid toolItem">Study ID: $layout.study.StudyID$ </span>
	<span class="accNum toolItem">Acc#: $layout.study.AccessionNumber$ </span>
	$shared.studyTrayItems:{$(it)()$}$

$else$
No Study
$endif$
</div>