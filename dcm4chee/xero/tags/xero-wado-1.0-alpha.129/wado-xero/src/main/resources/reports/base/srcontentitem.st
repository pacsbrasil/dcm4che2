<font size="+1">$it.ConceptNameCodeSequence.dicomObjects:{$it.CodeMeaning$}$</font>$if(it.ReferencedSOPSequence)$
$it.ReferencedSOPSequence.dicomObjects:srimage()$
$elseif(it.TextValue)$
<p>$it.TextValue$</p>
$elseif(it.MeasuredValueSequence)$: $it.MeasuredValueSequence.dicomObjects:{ $it.NumericValue$ $it.MeasurementUnitsCodeSequence.dicomObjects:{$it.CodeMeaning$}$ }$
$elseif(it.ConceptCodeSequence)$: $it.ConceptCodeSequence.dicomObjects:{$it.CodeMeaning$}$
$elseif(it.PersonName)$
$it.PersonName$
$elseif(it.ContentSequence)$
<ul>
  $it.ContentSequence.dicomObjects:{<li>$srcontentitem()$</li>}$
</ul>
$else$
<i>Unknown Value Type $it.ValueType$</i>
$endif$
