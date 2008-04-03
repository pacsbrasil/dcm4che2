/** This demonstrates what we want direct macros to look like.
  */
  
function includedMacro(templates) {
};


function includedMacro_merge(context) {
	var ret = "<html><body>";
	ret = ret+getChildTemplates().macIndirect();
	ret = ret+getChildTemplates().macOverride();
	ret = ret+"</body></html>";
};

function includedMacro_macOverride(context) {
	return "<h1>This is the correct, overridden text.</h1>";
};

function includedMacro_getChildTemplates() {
	// Initialize the default child templates - or at least do so for macIndirect and macOverride 
	if( this.child_templates==null ) {
		this.child_templates = new default_child_templates();
		this.child_templates.macOverride = includeMacro_macOverride;
	}
};