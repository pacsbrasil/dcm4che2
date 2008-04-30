/**
 * This class retrieves an XML response from a web service use the XMLHttpRequest object.  
 * It then converts the object into a hierarchical representation where attributes 
 * are converted into simple attributes on the object, and nested objects are converted into a list
 * with the name of the nested object type.
 * For example, if the XML is:
 * <results><patient name="Fred"><study id="1" /></patient></results> then the returned object would equivalent
 * to (using JSON notation):
 * { "patient":[{"name":"Fred", "study":[ {"id":1} ]}]}
 */
 
function XmlModel(urlSource) {
	this.urlSource = urlSource;
};

XmlModel.prototype.debug = console.debug;


/** Causes a request to be made to update the given object.  Currently creates synchronous requests
 * but eventually that should be changed to use asynchronous and to make the required requests up front
 * or to keep a set of continuations available to complete the request when the data is available.
 * The continuations idea is a good one, as it would allow parts of the view to be created and perhaps even 
 * displayed, but implementing it could be really hard.
 */
XmlModel.prototype.request = function XM_request() {
	var url = this.urlSource.url;
	this.requestUrl(url);
};

XmlModel.prototype.requestUrl = function XM_requestUrl(url) {
	var request;
	this.debug("Requesting XML data "+url);
	request = new XMLHttpRequest();
	request.open("GET",url,false);
	request.send(null);
	// TODO - add error handling.
	this.parse(request.responseXML.documentElement);
};

/** Parses the XML into the object */
XmlModel.prototype.parse = function XM_parse(el) {
    // Handle attribute children
	var children = el.attributes;
	var i,n=children.length;
	var child, colon;
	for(i=0; i<n;i++) {
		child = children.item(i);
		this[child.nodeName] = child.nodeValue;
	};
	
	// Handle element children
	children = el.childNodes;
	n = children.length;
	var lst, name,xchild;
	for(i=0; i<n; i++) {
		child = children.item(i);
		if( child.nodeType!==1 ) continue;
		name = localName(child);
		lst = this[name];
		xchild = new XmlModel();
		if( lst===null || lst===undefined ) {
		  lst = new Array();
		  this[name] = lst;
		  xchild.xmlFirst = true;
		} else xchild.xmlFirst = false;
		xchild._parent = this;
		xchild.parse(child);
		xchild._parent = this;
		lst.push(xchild);
	};
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