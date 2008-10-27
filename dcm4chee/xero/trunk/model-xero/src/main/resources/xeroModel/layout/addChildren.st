/** Children added from static metadata */
var child;
$children:{child|
	$if(child.template)$
	child = new Layout(this.id+"-$child.name$","$child.template$");
	$elseif(child.label)$
	child = new LabelLayout(this.id+"-$child.label;format="js"$","$child.label;format="js"$","$child.i18n$", "$child.icon$", "$child.action$", "$child.href$");
	$elseif(child.codeTemplate)$
	child = $(child.codeTemplate)()$;
	$else$
	/* Unknown child type $child$ */
	child = null;
	$endif$
	if( child ) {
		$if(child.target)$child.target="$child.target;format="js"$";$endif$
		this.add(child);
	};
}$