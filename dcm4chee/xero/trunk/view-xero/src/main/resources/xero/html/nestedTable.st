<!-- Parameters rows, cols, nest where nest is the child nesting to use -->
<table id="$id$">
  <tr>$cols.header:{<th>$it.name$</th>}$</tr>
   $rows: 
     {rowTop | $rowTop.(nested): { row |<tr>$cols.header:{<td>$row.(it.var)$</td>}$ </tr>}$ $\n$ },
     {rowTop | $rowTop.(nested): { row |<tr class="even">$cols.header:{<td>$row.(it.var)$</td>}$ </tr>}$ $\n$ }
   $
</table>

