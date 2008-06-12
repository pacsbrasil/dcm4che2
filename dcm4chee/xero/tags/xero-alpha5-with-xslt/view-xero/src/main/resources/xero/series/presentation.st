$layout.study.series:{$if(it.gsps)$
  <span class="gsps">Presentation</span>
  <ul class="gsps">
    $it.gsps:{
	  <li class="gsps">
		<span>$it.contentLabel$</span>
	  </li>
    }$
  </ul>
$endif$}$
