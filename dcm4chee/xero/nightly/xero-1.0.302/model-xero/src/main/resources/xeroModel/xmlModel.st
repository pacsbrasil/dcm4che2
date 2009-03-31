/**
 * This class retrieves an XML response from a web service use the XMLHttpRequest object.  
 * It then converts the object into a hierarchical representation where attributes 
 * are converted into simple attributes on the object, and nested objects are converted into a list
 * with the name of the nested object type.
 * For example, if the XML is:
 * <results><patient name="Fred"><study id="1" /></patient></results> then the returned object would equivalent
 * to (using JSON notation):
 * { "patient":[{"name":"Fred", "study":[ {"id":1} ]}]}
 
 * The resulting structure can be customized if necessary, for example, to generate synthetic elements
 * based on the actual elements, or to auto-sort the children etc.  To do so, add a function customize_<local-name>
 *
 */
 
function XmlModel(urlSource) {
	this.urlSource = urlSource;
	if( this.isDebug ) this.debug = console.info;
};

XmlModel.prototype.debug = function() {};
//XmlModel.prototype.isDebug = true;

/** Causes a request to be made to update the given object.  Currently creates synchronous requests
 * but eventually that should be changed to use asynchronous and to make the required requests up front
 * or to keep a set of continuations available to complete the request when the data is available.
 * The continuations idea is a good one, as it would allow parts of the view to be created and perhaps even 
 * displayed, but implementing it could be really hard.
 */
XmlModel.prototype.request = function XM_request() {
	if( !this.url ) {
		this.url = this.urlSource.url;
		if( !this.url ) {
			console.warn("No URL specified.");
			return;
		}
	}
	this.requestUrl(this.url);
};

/**
 * Makes the given request, along with any modifiers such as GSPS application.  
 */
XmlModel.prototype.requestUrl = function XM_requestUrl(url, modifiers, lazy) {
	var request;
	if( modifiers ) {
		for(key in modifiers) {
			url = url + modifiers[key]; 
		}
	}
	this.debug("Requesting XML data "+url);
	url=url+"&v="+model.version;
	request = new XMLHttpRequest();
	if( lazy ) {
		var usethis = this;
		request.onreadystatechange = function() {
			if( request.readyState!=4 ) return;
			usethis.parse(request.responseXML.documentElement,modifiers);
			usethis.url = "Done Request.";
		};
		request.open("GET",url,true);
		request.send(null);
		return;
	}
	request.open("GET",url,false);
	request.send(null);
	// TODO - add error handling.
	this.parse(request.responseXML.documentElement,modifiers);
	this.url = "Done Request.";
};

/** Parses the XML into the object */
XmlModel.prototype.parse = function XM_parse(el, modifiers) {
    // Handle attribute children
	var children = el.attributes;
	var i,n=children.length;
	var child, colon;
	
	for(i=0; i<n;i++) {
		child = children.item(i);
		// No guarantee of name space prefixes, so for now strip out namespace info.
		this[localName(child)] = child.nodeValue;
	};
	
	// Handle element children
	children = el.childNodes;
	n = children.length;
	var lst, name,xchild;
	if( this.TEXT ) this.TEXT = "";
	for(i=0; i<n; i++) {
		child = children.item(i);
		if( child.nodeType===3 ) {
			if(! this.TEXT ) this.TEXT="";
			this.TEXT = this.TEXT+child.data;
			continue;
		}
		if( child.nodeType!==1 ) continue;
		name = localName(child);
		xchild = this.getChild(name,child,modifiers,i);
		xchild._parent = this;
		xchild.parse(child,modifiers);
	};

	var nodeType = localName(el);
	if( this["customize_"+nodeType]!=null ) this["customize_"+nodeType]();	
};

/** Parses a String into XML and thence into this object */
XmlModel.prototype.parseString = function XM_parse(str,modifiers) {
	var parser = new DOMParser();
	var doc = parser.parseFromString(str,"text/xml");
	return this.parse(doc.documentElement,modifiers);
};

/** Gets a given child, specified by the given name.  May use other attributes from el to look it up. */
XmlModel.prototype.getChild = function XM_getChild(name,el,modifiers, i) {		
	var lst = this[name];
	var xchild = new XmlModel();
	if( !lst ) {
	  lst = new Array();
	  this[name] = lst;
	  xchild.xmlFirst = true;
	} else xchild.xmlFirst = false;
	lst.push(xchild);
	return xchild;
};

/** Finds the given element in the list child element, having attribute att with the given value 
 * TODO - construct an index instead, and use it consistently.
 */
XmlModel.prototype.find = function XM_find(childName, att, value) {
	var lst = this[childName];
	var i,n=lst.length;
	for(i=0; i<n; i++) {
		if( lst[i][att]==value ) return lst[i];
	}
};

/** Gets an element from this or any parent of this. */
XmlModel.prototype.get = function (at) {
	var ret=this[at];
	if( ret!==undefined ) return ret;
	if( this._parent ) return this._parent.get(at);
};