<!-- Parameters rows, cols, nest where nest is the child nesting to use -->
<table id="$id$">
  <tr>$cols.header:{<th $if(it.action)$class="sortableHeader" onclick="$it.action$('$layout.id$','$it.sort$')"$endif$>$it.name$ $if(layout.sortPatient.img.(it.sort))$<img alt="" src="$layout.sortPatient.img.(it.sort)$" class="sort"/>$endif$</th>}$</tr>
   $rows: 
     {rowTop | $html/nestedRow(cssClass="odd")$ },
     {rowTop | $html/nestedRow(cssClass="even")$ }
   $
</table>

