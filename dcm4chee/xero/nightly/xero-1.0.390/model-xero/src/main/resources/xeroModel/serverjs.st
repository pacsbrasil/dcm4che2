/** Contains the server side javascript for the model */
$rhino()$

// Setup a SLF4J Logger bridge for logging
$logger()$
var console = new Logger("js.console");

// HACK!  Read in from the commandline options
getViewportSize = function() {
	return [ 600, 400 ];
};

// HACK!  We don't actually preload images in the server!
function Image(){};

$sharedStatic()$

$model()$

// Server-side data request method:  uses URIResolver to bind the required data.
ServiceRequest.prototype.requestDocumentByURL = function SR_requestDocumentByURL(url)
{
	var source = this.resolver.resolve(url,null);
	var reader = new java.io.BufferedReader(source.getReader());
	
	var document = new java.lang.StringBuilder();
	while(true)
	{
		var line = reader.readLine();
		if(line == null) break;
		document.append(line);
	}
	return document.toString();
};

$if(HAS_CONTROLLER)$
/**************************************************************************************************************
 * Server controller include
 **************************************************************************************************************
$controller(IS_SERVER="true")$

// Update the client UI using the indicated URL
XeroController.prototype.updateClientWithResolver = function(url,resolver)
{
	console.info("Controller.updateClient() invoked");
	
	// Assign the resolver to the current service request.
	this.service.resolver = resolver;
	this.updateClient(url);
};

$else$

function XeroController() {};
	
$endif$

