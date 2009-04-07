/*
Request from the client to a JSON based web service.
*/

function ServiceRequest() {
};


// Return the document that is indicated by the url
ServiceRequest.prototype.request = function SR_request(url) {
	// TODO: Implement as a relative path
	// TODO: Add version / ae fixes.
	
	console.info("Requesting JSON data "+url);
		
	var jsonStr = this.requestDocumentByURL(url);
	return jsonStr;
};

// Request the document from the server 
ServiceRequest.prototype.requestDocumentByURL = function SR_requestDocumentByURL(url)
{
	// TODO: Add a way to return errors or status values.
	var request = new XMLHttpRequest();
	request.open("GET",url,false);
	request.send(null);
	return request.responseText;
}
