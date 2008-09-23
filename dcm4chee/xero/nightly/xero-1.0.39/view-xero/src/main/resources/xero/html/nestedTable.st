<!-- Parameters rows, cols, nest where nest is the child nesting to use -->
<table id="$id$">
  <tr>$cols.header:{<th $if(it.action)$onclick="$it.action$('$layout.id$','$it.sort$')"$endif$>$it.name$<img alt="" src="$layout.sortPatient.img.(it.sort)$" style="width:20px; height: 20px; vertical-align: middle;"/></th>}$</tr>
   $rows: 
     {rowTop | $html/nestedRow(cssClass="odd")$ },
     {rowTop | $html/nestedRow(cssClass="even")$ }
   $
</table>

