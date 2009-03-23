$! Parameters rows, cols, nest where nest is the child nesting to use !$
<table id="$id$Table">
  <thead id="$id$Head">
  <tr>$cols.header:{<th id="$it.id$" $if(it.action)$class="sortableHeader" onclick="$it.action$('$layout.id$','$it.sort$')"$endif$>$if(it.i18n)$$i18n.(it.i18n)$$else$$it.name$$endif$ $if(layout.sortPatient.img.(it.sort))$<img alt="" src="$layout.sortPatient.img.(it.sort)$?v=$version$" class="sort"/>$endif$</th>}$</tr>
  </thead>
  <tbody id="$id$Body">
   $rows: 
     {rowTop | $html/nestedRow(cssClass="odd")$ },
     {rowTop | $html/nestedRow(cssClass="even")$ }
   $
   <tr style="height:95%;" />
   </tbody>
</table>

