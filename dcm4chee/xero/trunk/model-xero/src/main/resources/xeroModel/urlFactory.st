/**
 * A URL Factory takes form data and a base URL and constructs a URL to query
 * It constructs the URL from the given form.  If the form does not exist currently,
 * it uses previously remembered values to construct the object.  It is assumed that the
 * form can be reconstructed from local values contained in the UrlFactory.
 */

function UrlFactory(base, formId) {
	this.base = base;
	this.formId = formId;
	this.url = base;
};

UrlFactory.prototype.debug = function() {};

/** Updates the local parameters with values from the form being currently displayed, if it can be found.
  * If not, then it just silently returns.
  */
UrlFactory.prototype.updateUrl = function URLF_updateUrl() {
	var form = document.getElementById(this.formId);
	if( form!==undefined && form!==null ) {
		this.debug("Update url %s from form %s", this.url, this.formId);
		this.sep = "?";
		this.url = this.base;
		if( this.url.indexOf("?")>=0 ) this.sep = "&";
		this.updateUrlFromForm(form);
	}
	else {
		console.info("Not updating url %s as no form found named %s",this.url,this.formId); 
	}
};

UrlFactory.prototype.updateUrlFromForm = function URLF_updateUrlFromForm(node) {
	if( node.nodeType!==1 ) return;
	var atts, name, val;
	var nname = node.nodeName;
	if( nname==="INPUT" || nname==="SELECT" || nname==="input" || nname==="select") {
		atts = node.attributes;
		name = atts.getNamedItem("name").nodeValue;
		val = node.value;
	};
	if( name ) {
		this.debug("Adding parameter %s=%s",name,val);
		this[name] = val;
		if( val && val!=="" ) {
			this.url = this.url + this.sep + name+"="+val;
			this.sep = "&";
		};
	};
	
	var children = node.childNodes;
	var i,n=children.length;
	for(i=0; i<n; i++) {
		this.updateUrlFromForm(children.item(i));
	};
};