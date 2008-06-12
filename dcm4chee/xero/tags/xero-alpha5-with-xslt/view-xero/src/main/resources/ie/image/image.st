<div class="image" $html/style()$ >

<v:group id="vml$layout.id$" class="imageDisplay" onmousedown="return xeroController.imageMouseDown();" $html/style()$>
	<v:rect style="width: 100%; height: 100%;" fillcolor="black" />
$if( layout.image )$
	<v:group class="imageCoord" style="width:100%;height:100%;" coordOrigin="$layout.translateXDisp$,$layout.translateYDisp$" coordsize="$layout.widthImg$,$layout.heightImg$">
$layout.imageUrls:{
		<v:image id="im$layout.id$" src="$it.imageUrl$" style="width: $it.cols$; height:$it.rows$;" />
}$
		<v:line from="0,0" to="0,$layout.image.Rows$" strokecolor="#202020" strokeweight="2" />
	    <v:line from="0,0" to="$layout.image.Columns$,0" strokecolor="#202020" strokeweight="2" />
		<v:line from="0,$layout.image.Rows$" to="$layout.image.Columns$,$layout.image.Rows$" strokecolor="#202020" strokeweight="2" />
		<v:line from="$layout.image.Columns$,0" to="$layout.image.Columns$,$layout.image.Rows$" strokecolor="#202020" strokeweight="2" />
	</v:group>
	<v:group class="imageDisplayRel" style="width: 100%; height: 100%;" coordsize="1000,1000" coordorigin="0,0">
	</v:group>
$else$
No Image
$endif$
</v:group>
</div>