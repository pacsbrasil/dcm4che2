<!-- Parameters rows, cols, nest where nest is the child nesting to use -->
<table id="$id$">
  <tr>$cols.header:{<th>$it.name$</th>}$</tr>
   $rows: 
     {rowTop | $html/nestedRow(cssClass="odd")$ },
     {rowTop | $html/nestedRow(cssClass="even")$ }
   $
</table>

