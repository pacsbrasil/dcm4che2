/**
 * Contains the patient/study/series/image information that merges local changes with the
 * original, source data.
 * Also has a reference to conversational data - this is data that is not merged with local changes but is referenced/kept as necessary
 * for navigation and other localized changes.
 */

/** Creates a new merged overall object */
function Merged(parent) {
	if( parent ) {
		this.level = parent.level+1;
		this.parent = parent;
	} else {
		this.level = 0;
		this.external = new Object();
	};
};

/** Looks up the given child value if available - children are found by Patient ID/Study UID/SeriesUID/SOPInstanceUID plus modifiers - the modifiers
 * are query modifiers that affect the ordering/positioning of the returned values, separated by an ampersand.  Thus, applying GSPS to a study
 * modifies the StudyUID - it is thus entirely possible to have the same study displayed two or more times by using different modifiers, although
 * the more typical case is that there is just a single display of a study with a single modifier.
 *
 * You can't create/get a child merged value until you have the external data for that child, although you can provide essentially any external
 * data, whether or not it matches the given key.
 */
Merged.prototype.getChild = function Merged_getChild(value, external) {
	if(!this.children ) this.children = new Object();
	var child = this.children[value];
	if(! child ) {
		child = new Merged(this);
		this.children[value] = child;
	}
	if( external ) child.external = external;
	return child;
};

/** Get or create a new conversational object */
Merged.prototype.getConversation = function Merged_getConversation() {
	if(!this.conversation ) this.conversation = new Object();
	return this.conversation;
};

/** Looks up the given key value - first checks all merged information in the hierarchy up to the patient level, then checks for the same in
 * the external hierarchy.  This is not particularly cheap, as it has to test quite a few levels to make the update.
 */
Merged.prototype.get = function Merged_get(key) {
	var val, parent = this;
	do {
		val = parent[key];
		if( val!==undefined ) return val;
	} while(parent.level > 2);
	
	parent = this;
	do {
		val = parent.external[key];
		if( val!==undefined ) return val;
	} while(parent.level > 2);  
};

