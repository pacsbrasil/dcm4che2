function checkAll(isOnload){
	var frm = document.frmArrAction;
	for (var i = 0; i < frm.elements.length; i++) {
		var e = frm.elements[i];
		if ((e.name != 'allbox') && (e.type=='checkbox')) {
			e.checked = frm.allbox.checked;
		}
	}
}
