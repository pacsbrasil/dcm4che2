<!-- This is the per-study control containing the series thumbnails and controls within this study. -->
<div class="studyTray" $html/style()$>
$if(layout.study)$
	$layout.study.StudyDescription$ <br />
	Study ID: $layout.study.StudyID$ <br />
	Acc#: $layout.study.AccessionNumber$ <br />
	$shared.studyTrayItems:{$(it)()$}$

$else$
No Study
$endif$
</div>