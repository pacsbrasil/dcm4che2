/**
 * The StudyData object is an extension of XmlModel that
 * adds support for merging data at various levels, and creating a "children" index that
 * indexes the available children by the appropriate key.
 */

function StudyData(uids) {
	if( uids ) {
		var i,n=uids.length;
		var uidsBack = uids[0];
		for(i=1; i<n; i++) {
			uidsBack = uidsBack + "\\\\"+uids[i];
		}
		this.url = STUDY_QUERY+"?StudyInstanceUID="+uidsBack;
	}
};

StudyData.prototype = new XmlModel();
StudyData.prototype.level = 0;
StudyData.prototype.idVars = ["PatientIdentifier", "StudyInstanceUID", "SeriesInstanceUID", "Position"];
StudyData.prototype.childId = "PatientIdentifier";

/** Gets a StudyData child, re-using the existing one of the right id if present, and indexing them
 * in a "children" element.
 */
StudyData.prototype.getChild = function StudyData_getChild(name,el) {
	var lst = this[name];
	var isFirst = false;
	if(! lst ) {
	  lst = new Array();
	  this.children = new Object();
	  this[name] = lst;
	  isFirst = true;
	}
	var id = el.getAttribute(this.childId);
	this.debug("Found child %s=%s", this.childId,id);
	var xchild = this.children[id];
	if( !xchild ) {
		xchild = new StudyData();
		this.children[id] = xchild;
		xchild.level = this.level+1;
		xchild.childId = this.idVars[xchild.level];
		xchild.xmlFirst = isFirst;
		lst.push(xchild);
	}
	return xchild;
};

/**
 * Makes the requested queries for additional information to merge.
 */
StudyData.prototype.addQueries = function StudyData_addQueries(queries) {
	var key;
	for(key in queries) {
		this.debug("Adding information from %s",key);
		this.requestUrl(key);
	}
};