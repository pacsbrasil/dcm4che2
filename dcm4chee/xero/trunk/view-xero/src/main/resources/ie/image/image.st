 <div class="image"  $html/style()$  >
     <v:group style="width:$layout.width$px; height: $layout.height$px;" coordorigin="0,0" coordsize="$layout.width$,$layout.height$">
      <v:group 
        style="width: $layout.dblColsDisp$px; height:$layout.dblRowsDisp$px; left: $layout.translateXIE$px; top: $layout.translateYIE$px; $if(layout.rotation)$rotation:$layout.rotation$;$endif$ $if(layout.flip)$flip:x;$endif$"
      	coordorigin="-$layout.image.Columns$,-$layout.image.Rows$" 
      	coordsize="$layout.dblCols$,$layout.dblRows$" 
      >
$layout.imageUrls:{
		<v:image id="im$layout.id$" src="$it.imageUrl$" style="width: $it.cols$px; height:$it.rows$px; left: $it.left$px;top:$it.top$;" />
}$
		<v:line from="0,0" to="$layout.image.Columns$,0" strokecolor="#202020" strokeweight="2"/>
		<v:line from="0,0" to="0,$layout.image.Rows$"  strokecolor="#202020" strokeweight="2"/>
		<v:line from="$layout.image.Columns$,0" to="$layout.image.Columns$,$layout.image.Rows$" strokecolor="#202020" strokeweight="2"/>
		<v:line from="0,$layout.image.Rows$" to="$layout.image.Columns$,$layout.image.Rows$" strokecolor="#202020" strokeweight="2" />
		
      </v:group>

      <v:group class="imageDisplayRel" style="width: 100%; height: 100%; top: 0px; left: 0px;" coordsize="1000,1000" coordorigin="0,0">
      	 <!--
         <v:line from="0,0" to="1000,1000" strokecolor="yellow" strokeweight="2" />
		 <v:line from="0,1000" to="1000,0" strokecolor="yellow" strokeweight="2" />
		 -->
      </v:group>

     </v:group>
 </div>