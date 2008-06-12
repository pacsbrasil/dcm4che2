$layout.study.series:{$if(it.report)$
  <span class="report">Reports</span>
  <ul class="report">
    $it.report:{
	  <li class="report">
		<span onclick="xeroController.viewReport('$it.SOPInstanceUID$')" class="jslink">$it.conceptMeaning$</span>
	  </li>
    }$
  </ul>
$endif$}$
