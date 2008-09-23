/** 
 * The Navigation object defines the desired view/navigation positions relative to some named navigation information.
 * Then, the Navigation object is provided External information and uses that information to determine which
 * object is to be used/displayed in that navigation spot, and provides event notifications when a different External object
 * (or no object) applies to a given Navigation.
 *
 * Given the external information, it is possible to figure out if there is conversational or merged information to apply - conversational
 * information is local to the given window and does not inherit any values from the merged/external information, where as the 
 * merged information is a separate class that merges external and local changes to be applied to various objects.  Which merged object to use
 * can be computed from the external information.
 * 
 */
 function Navigation(level, offset) {
	// Listeners includes both explicit listeners, in implicitly, children of this object.
	this.children = new Object();
	if( level && level>0 ) {
		this.level = level;
		this.offset = offset;
		this.levelName = this.names[level];
		this.listeners = new Array();
	} else {
		this.merged = new Merged();
		this.conversation = new NavConversation();
		this.external = new StudyData();
		// The id is an indicator that the parent is valid and has a valid location.
		this.id = "results";
	}
	this.childType = this.childTypes[this.level];
	this.childIdVar = this.idVars[this.level];
};

/** A key is used to identify a particular element in the set, typically a UID.
 * Offsets from this follow the natural list order.
 */
Navigation.prototype.KEY="nav";

Navigation.prototype.isDebug = false;
if( Navigation.prototype.isDebug ) Navigation.prototype.debug = console.debug;
else Navigation.prototype.debug = function() {};

Navigation.prototype.names = ["result", "patient", "study", "series", "image"];
Navigation.prototype.level =0;
Navigation.prototype.idVars = ["PatientIdentifier", "studyUID", "seriesUID", "Position"];

Navigation.prototype.childTypes = ["patient", "study", "series", "image"];

Navigation.prototype.IMAGE_QUERY_SIZE=64;
Navigation.prototype.QUERY_SIZE=16;

// Event types.
Navigation.prototype.NAVIGATION_CHANGED = 100;
Navigation.prototype.INVALID = 110;
Navigation.prototype.EXTERNAL_CHANGED = 120;

Navigation.prototype.QUERY = "/wado2/image.xml";
Navigation.prototype.STUDY_QUERY = "/wado2/study.xml";

Navigation.prototype.error = function(msg) {
	console.error(msg);
	throw new Error(msg);
};

/** 
 * Generates the URL that loads the external data for the given child Id (which maybe undefined to mean all/unknown child.)
 * Puts the new value into the return object if it isn't already
 * present, and returns that object.
 * TODO - strictly speaking, this is controller logic, NOT model logic so at some point there should be a setter
 * method that indicates how these queries are to be generated.  However, until such a time as that is actually needed
 * this method can be left here
 */
Navigation.prototype.generateQuery = function Navigation_generateQuery(child, ret) {
	// So we don't have any external data for the child object, but we do have it's id
	if(!ret) ret = new Object();
	var url, modifiers;
	
	var childId = child.id;
	if( child.conversation ) {
		modifiers = child.conversation.assignDefaultModifiers(child.external);
	}
	if( this.level===3 ) {
		this.debug("Adding image level query at %d of size %d initial offset %d", childId, this.IMAGE_QUERY_SIZE, this.QUERY_SIZE);
		position = childId - this.QUERY_SIZE;
		position -= (position % this.IMAGE_QUERY_SIZE);
		position += this.QUERY_SIZE;
		url = this.QUERY+"?seriesUID="+this.id+"&Count="+this.IMAGE_QUERY_SIZE+"&Position="+position+"&pixelInfo=true";
		console.info("Adding series extension query %s",url); 
	} else if( this.level===2 ) {
		url = this.QUERY+"?Position=0&Count="+this.QUERY_SIZE+"&studyUID="+this.id+"&pixelInfo=true&regroup=true";
		console.warn("Adding pixelInfo query - this should be done only as needed because it is expensive query: %s", url);
		this.debug("Adding image query to %s because child with id %s and external children %s", this.id, childId, this.external.children);
	} else if( this.level===1 && modifiers!=null && modifiers[2]!=undefined) {
		// Fix the modifiers, and do an image level query.
		url = this.QUERY+"?Position=0&Count="+this.QUERY_SIZE+"&studyUID="+child.id+"&pixelInfo=true&regroup=true";
		console.warn("Adding pixelInfo query - this should be done only as needed because it is expensive query: %s", url);
		this.debug("Adding updated image, with study query to %s because child with id %s and external children %s", this.id, childId, this.external.children);
	} else if( this.level===1 ) {
		// This happens when the user views a NEW study in the same patient
		url = this.STUDY_QUERY+"?gsps=*&studyUID="+childId;
		console.info("Adding new study query %s", url);
	} else {
		console.error("Unknown id/query level %d for child id %s", this.level, childId);
		return ret;
	}
	if( url ) ret[url] = modifiers;
	return ret;
};

/** 
 * Adds external data for the the study.
 * The external data is owned by this object, but the act of updating/reading the data is handled by the controller.
 * This function returns undefined to indicate all required external data is available, or it returns a set of URLs as the keys in an object.  (The values are
 * used to store information about what has been retrieved and how to merge it into the StudyData object).
 */  
Navigation.prototype.mergeExternal = function Navigation_mergeExternal(external,ret) {
	if( external==null && this.level==0 ) external = this.external;
	if( this.external!==external ) {
		this.external = external;
		if( this.merged ) this.merged.external = this.external;
		this.fireChanged(this.EXTERNAL_CHANGED);
	}
	this.debug("Adding external data to %s at level %d", this.id,this.level);
	var child,childKey;
	var childExt,url,position,extId;
	for(childKey in this.children) {
		child = this.children[childKey];
		if(child.id==null) {
			child.invalidate(false);
		}
		// Null means no object there, whereas undefined means unknown, so if it is null, just continue.
		if( child.id===null ) continue;
		if( child.id!==undefined && external.children) {
			extId = child.conversation.getUrlId();
			this.debug("Looking for external id %s on base id %s",extId,child.id);
			childExt = external.children[extId];
			if( childExt ) {
				ret = child.mergeExternal(childExt,ret);
				continue;
			}
		}
		ret = this.generateQuery(child,ret);
		// No point continuing to look at series children as a single query is used to get ALL series children information at once.		
		if( this.level==2 ) break;
	}
	return ret;
}; 

/** Sets a given named navigation key object value. */
Navigation.prototype.setNavigation = function Navigation_setNavigation(value,key) {
	if(! key ) key = this.KEY;
	this.createConversation();
	if( this.conversation[key] == value ) {
		this.debug("setNavigation key is unchanged - continuing with doing anything.");
		return;
	}
	this.debug("setNavigation on %s is %s=%s", this.id, key, value);
	this.conversation[key] = value;

	this.fireChanged(this.NAVIGATION_CHANGED);
	console.info("Finished fire changed - about to invalidate.");
	this.invalidate(true);
	console.info("Finished invalidate.");
};

/** Invalidates the given key element, if there are any children depending on it. */
Navigation.prototype.invalidate = function Navigation_invalidate(isTop) {
	if( !isTop ) {
		this.debug("Invalidating child object - resetting ids at level %d",this.level);
		var oldId = this.id;
		// Compute the new id if we can
		this.id = this.parent.lookupId(this.offset);
		this.debug("Looked up id = %s", this.id);
		if( this.id === oldId ) {
			return;
		}
		this.debug("Changed data for old id %s is now %s",oldId,this.id);
		this.conversation = undefined;
		var msg = this.INVALID;
		this.external = undefined;
		this.merged = undefined;
		if( this.id!=null ) {
			this.createMerged();
			if( this.external ) msg = this.EXTERNAL_CHANGED;
		}	
		this.fireChanged(msg);
	};
	
	var childKey, child;
	for(childKey in this.children ) {
		child = this.children[childKey];
		child.invalidate(false);		
	}
};

Navigation.prototype.fireChanged = function Merge_fireChanged(type) {
	if( this.level===0 ) return;
	if( this.conversation && this.conversation.navigateListener ) 
		this.conversation.navigateListener(this,type);
	var i,n=this.listeners.length;
	var listener;
	for(i=0; i<n;i++) {
		this.listeners[i].navigateListener(this,type);
	}
};

/** Given a particular key name, figure out what the child element should be. 
 */
Navigation.prototype.lookupId = function Navigation_lookupId(offset,key) {
	if( !this.id ) return null;
	this.createConversation();
	
	if(! key ) key = this.KEY;
	
	var val = this.conversation[key];
	var type = typeof(val);
	if(val==undefined ) {
		this.debug("Failed to find any navigation information for key %s at level %d id %s", key, this.level, this.id);
		if( this.level===3 ) type = "number";
		else type="string";
	}
	
	if( type==="object" ) {
		console.info("Looking up offset %s in array/object value %s", offset, val[offset]);
		if( offset < val.length && offset >=0 ) return val[offset];
		return null;
	}
	else if( type==="number" ) {
		this.debug("Looking up offset %s from number %s", offset, val);
		if(! this.external ) return undefined;
		if(!val) val = 0;
		if( val+offset<0 ) return null;
		if(! this.external.Viewable ) return undefined;
		if( this.external.Viewable > val+offset ) return val+offset;
		return null;
	}
	else if( type==="string" ) {
		this.debug("Looking up relative position to %s offset %s external %s", val, offset, this.external);
		// Key type - one case is to avoid any searching at all, although it doesn't
		// allow you to find out much extra information in the child.
		if( offset===0 && val ) {
			this.debug("Returning 0 offset information directly.");
			return val;
		}
		if( !this.external ) return undefined;
		var extChildren = this.getExtChildren();
		if( !extChildren ) {
			console.info("Found no children defined for %s under the name %s at level %d offset %s", this.id,this.childType, this.level, offset);
			return undefined;
		}
		if( val ) {
			var posn = this.findExternal(val);
			if( posn!==undefined ) {
				offset = offset + posn;
			} else {
				console.warn("Didn't find offset %d for %s at position %s",offset,key,posn);
				return undefined;
			}
		}
		if( extChildren.length > offset && offset>=0 ) {
			return extChildren[offset][this.childIdVar];
		}
		// Return null because there ARE children, but the offset is outside the bounds
		// so that means none available as opposed to unknown.
		return null;
	}
	else {
		this.error("Unknown key type "+key);
	}
};

/** Gets the external children list.
 * Normally, this is just the simple children objects, but
 * for the series child list (of a study), it only returns series that contain images.  
 * Other types of listeners/data is needed for non-image series.
 */
Navigation.prototype.getExtChildren = function Navigation_getExtChildren() {
	var ret = this.external[this.childType];
	if( this.level!==2 || !ret) return ret;
	// For the study level, the getExtChildren returns only series image children in the default list.
	// To explicitly use another type of list, use a different key type such as a key set.
	
	var seriesImage = this.external._seriesImage;
	// The available series doesn't change unless the entire object is refreshed, once the
	// first set of series has been loaded.
	if( seriesImage ) return seriesImage;
	seriesImage = new Array();
	var ser, i,n=ret.length;
	for(i=0; i<n; i++) {
		ser = ret[i];
		if( !ser.image ) continue;
		seriesImage.push(ser);
	}
	this.external._seriesImage = seriesImage;
	return seriesImage;
};

/** Finds the position in the external data having the given id value */
Navigation.prototype.findExternal = function Navigation_findExternal(val) {
	var extChildren = this.external[this.childType];
	var i,n=extChildren.length;
	for(i=0; i<n; i++) {
		if( extChildren[i][this.childIdVar]==val ) return i;
	}
}

/** Creates the conversation context for this object - does not setup the merged context, as there might be changes to the conversation context
 * before the merged context is retrieved.
 */
Navigation.prototype.createConversation = function Navigation_createConversation() {
	if( this.conversation ) return;
	if( this.id==null ) this.error("This navigate object at level "+this.level+" doesn't yet have an ID - set the parent navigation/information first");
	this.parent.createConversation();
	this.conversation = this.parent.conversation.getChild(this.id);
};

/** Creates the merged and external contexts as appropriate */
Navigation.prototype.createMerged = function Navigation_createMerged() {
	if( this.merged ) return;
	this.createConversation();
	this.parent.createMerged();
	var mid = this.conversation.getPresentationId();
	if( mid==null ) this.error("Unable to get presentation id for "+this.id);
	this.merged = this.parent.merged.getChild(mid);
	this.createExternal();
};

/** Creates/sets up the external object, if it can be found, returning true if available.  id, merged and conversation must be setup already.
 */
Navigation.prototype.createExternal = function Navigation_createExternal() {
	if( this.external ) return true;
	if( !this.parent.createExternal() ) return false;
	if( !this.parent.external.children ) this.parent.external.children = new Object();
	var eid = this.conversation.getUrlId();
	this.external = this.parent.external.children[eid];
	if( this.external ) this.merged.external = this.external;	
	return this.external!=undefined;
};

/** Gets the merged context for this object, creating one if necessary.  Returns undefined
 * if the id isn't set.
 */
Navigation.prototype.getMerged = function Navigation_getMerged() {
	if(this.id==null) return undefined;
	if(this.external==null ) return null;
	this.createConversation();
	return this.merged;
}

/** Assigns results information to the various levels.
 */

/** Gets the given navigate child element.  The object will be disconnected from this navigate object
 * if on the next apply, no child elements are found reflecting that child information.
 */
Navigation.prototype.getChild = function Navigation_getChild(offset,key) {
	if(!key ) key = this.KEY;

	var ckey = key+":"+offset;
	var child =this.children[ckey];
	if( ! child ) {
		child = new Navigation(this.level+1, offset);
		child.parent = this;
		this.children[ckey] = child;
		// This will try to find information for this child element, at a minimum the id.
		child.invalidate(false);
	}
	return child;
};

/**
 * Indicate that a given object is a listener for this level of information - when a new assignment
 * is made, the values in the child object will be changed.  Multiple objects can listen for the same
 * position/offset.  The given function is assigned ot obj as navigateListener and called.  A given
 * object can't have 2 or more functions without some other means of combining them.
 * @return the child object that is being listened to.
 */
Navigation.prototype.addChildListener = function Navigation_addChildListener(obj, listenerF, offset, key) {
	return this.getChild(offset,key).addListener(obj,listenerF);
};

Navigation.prototype.addListener = function Navigation_addListener(obj,listenerF) {
	obj.navigateListener = listenerF;
	this.listeners.push(obj);
	obj.navigateListener(this);
	return this;
};

/**
 * Remove ALL listeners - used when the listeners are being destroyed/cleared and recreated.
 * Only removes listeners from this level and below.  This also removed all child Navigation objects.
 * Any previously created/used navigate objects are no longer valid. 
 */
Navigation.prototype.removeAllListeners = function Navigation_removeAllListeners() {
	if( this.level>0 ) {
		var i,n=this.listeners.length;
		for(i=0; i<n; i++) {
			this.listeners[i].navigateListener(undefined);
		}
		this.listeners = new Array();
	}
	var childKey, child;
	for(childKey in this.children ) {
		child = this.children[childKey];
		child.removeAllListeners();
	}
	this.children = new Object();
}; 