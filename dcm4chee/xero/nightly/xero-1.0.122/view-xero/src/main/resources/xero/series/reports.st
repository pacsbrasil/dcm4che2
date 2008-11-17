$layout.study.series:{$if(it.report)$
  <span class="report">Reports</span>
  <ul class="report">
    $it.report:{
	  <li class="report">
		<span onclick="xeroController.viewReport('$it.objectUID$')" class="jslink">$it.conceptMeaning; format="xml"$</span>
	  </li>
    }$
  </ul>
$endif$}$
