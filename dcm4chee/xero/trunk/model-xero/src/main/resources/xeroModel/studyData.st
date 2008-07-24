/**
 * The StudyData object is an extension of XmlModel that
 * adds support for merging data at various levels, and creating a "children" index that
 * indexes the available children by the appropriate key.
 * The default constructor uses the uids to query for the study level information, and requests that gsps and key object defaults
 * are included.
 */

function StudyData(uids) {
	if( uids ) {
		var i,n=uids.length;
		var uidsBack = uids[0];
		for(i=1; i<n; i++) {
			uidsBack = uidsBack + "\\\\"+uids[i];
		}
		this.url = STUDY_QUERY+"?gsps=*&studyUID="+uidsBack;
	}
};

StudyData.prototype = new XmlModel();
StudyData.prototype.level = 0;
StudyData.prototype.idVars = {"patient":"PatientIdentifier", "study":"studyUID", "series":"seriesUID", "image":"Position", "gsps":"objectUID", "report":"objectUID", "ko":"objectUID"};

/** Gets a StudyData child, re-using the existing one of the right id if present, and indexing them
 * in a "children" element.
 */
StudyData.prototype.getChild = function StudyData_getChild(name,el,modifiers) {
	var lst = this[name];
	var isFirst = false;
	var modifier;
	// We need the modifier for the child level, not this level
	if( modifiers ) modifier = modifiers[this.level+1];
	if(! lst ) {
	  lst = new Array();
	  this.children = new Object();
	  this[name] = lst;
	  isFirst = true;
	}
	// For now, assume only 1 child type - that may not be a good assumption longer term.
	if(! this.childId ) {
		this.childId = this.idVars[name];
		if( !this.childId ) this.childId = "id";
		this.debug("Choose childID %s for level %d on child %s", this.childId,this.level,name); 
	}
	var id = el.getAttribute(this.childId);
	if( modifier ) id = id+modifier;
	this.debug("Found child %s=%s modifier %s modifiers %s", this.childId,id, modifier, modifiers);
	var xchild = this.children[id];
	if( !xchild ) {
		xchild = new StudyData();
		this.children[id] = xchild;
		xchild.level = this.level+1;
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
		this.requestUrl(key, queries[key]);
	}
};