/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is part of dcm4che, an implementation of DICOM(TM) in
 * Java(TM), hosted at http://sourceforge.net/projects/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * Bill Wallace, Agfa HealthCare Inc., 
 * Portions created by the Initial Developer are Copyright (C) 2007
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Bill Wallace <bill.wallace@agfa.com>
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 *
 * ***** END LICENSE BLOCK ***** */
/**
 * Create an XsltAjax object.  This object provides methods to
 * call a given remote URI to update a document, OR to update an existing
 * document in place, and then render that document using the specified XSLT
 * stylesheet.
 * @param (string) xsltURI
 */
function XsltAjax(xsltUri) {
   this.setXsltUri(xsltUri);
   this.itemsToUpdate = { };
   this.updateModel = { };
   this.noPostAction = false;
   this.inProgress = false;
}

XsltAjax.prototype.serializer = new XMLSerializer();
XsltAjax.prototype.WAIT_CURSOR = "wait";
XsltAjax.prototype.NORMAL_CURSOR = "auto";
XsltAjax.prototype.STATUS_OK = 200;

/**
 * Sets the cursor on the given element to the given type
 */
XsltAjax.prototype.updateCursor = function(target, cursorType) {
  if( (!target) || !cursorType ) {
  	return;
  }
  var i;
  if( (typeof target)=='string') {
  	this.updateCursor(document.getElementById(target),cursorType);
  }
  else if( target.nodeType ) {
     if( target.style && target.style.cursor!==undefined ) {
        target.style.cursor = cursorType;
     }
  }
  else if( target.length ){
    for(i=0; i<target.length; i++ ) {
    	this.updateCursor(target[i],cursorType);
    }
  }
};

/**
 * Returns the XML for xmlHttp.  Parses the XML if the file is local
 */
XsltAjax.prototype.asXml = function(xmlHttp) {
  var xml;
  if( xmlHttp && xmlHttp.responseXML ) {
     if( xmlHttp.responseXML.hasChildNodes() ) {
       return xmlHttp.responseXML; 
     }
     else {
       xml = (new DOMParser()).parseFromString(xmlHttp.responseText,"text/xml");
       return xml;
     }
  }
  return undefined;
};

/**
 * Sets the xslt uri, and causes it to be loaded (in the background).
 */
XsltAjax.prototype.setXsltUri = function(xsltUri) {
	this.xsltUri = xsltUri;
	if( xsltUri===undefined ) {
	  this.xsltProcessor = undefined;
	  return;
	}
	var xslObj = new XMLHttpRequest();
	var xsl;
    try {
       if( xslObj.overrideMimeType ) {
          xslObj.overrideMimeType('application/xml');
       }
       xslObj.open ('GET', xsltUri, false);
       xslObj.send('');
       if( xslObj.status==this.STATUS_OK || xslObj.status==0) {
         xsl = this.asXml(xslObj);
       }
    } 
    catch(e) {
    	alert("Couldn't load url "+xsltUri+" exception:"+e+" xslObj status="+xslObj.status);
    	return;
    }
    try {
       if( xslObj.status==this.STATUS_OK || xslObj.status==0) {
   	      this.xsltProcessor = new XSLTProcessor();
    	  this.xsltProcessor.importStylesheet(xsl);
       } else {
           alert("Error on "+xsltUri + " status:"+xslObj.status+" msg:"+xslObj.statusText);
       }
    }
    catch(e) {
       alert("Couldn't parse xslt "+xsltUri+" exception:"+e.message);
    }
};

/** gets an element by id.  Required since IE does not support this in their
 * DOM
 * @param (DOMNode) node
 * @param (string) item containing the id to search for.
 * @return (DOMNode) undefined if not found, otherwise a DOMNode matching
 */
XsltAjax.prototype.getElementById = function (node, item) {
   var children, i, ret, attrs, attr;
   if( node.getElementById ) {
      return node.getElementById(item);
   }
   else {
      //IE
      children = node.childNodes;
      if( node.attributes && node.attributes.length>0 ) {
         attrs = node.attributes;
         for(i=0; i<attrs.length; i++) {
            attr = attrs.item(i);
            if( attr.name=='id' && attr.nodeValue==item ) {
               return node;
            }
         }
      }
      if( node.attributes!==undefined && node.attributes!==null && node.attributes.id!==undefined && node.attributes.id==item ) {
         return node;
      }
      for(i=0; i<children.length; i++ ) {
         ret = this.getElementById(children.item(i), item); 
         if( ret!==undefined ) {
           return ret;
         }
      }
      return undefined;
   }
};

/** Copies all attrributes from replaceBy into replaced. */
XsltAjax.prototype.replaceAttributes = function(replaced, replaceBy) {
	var srcAttrs = replaceBy.attributes;
	var src,i;
	for(i=0; i<srcAttrs.length; i++) {
		src = srcAttrs.item(i);
		replaced.setAttribute(src.name, src.value);
	}
};

/** Replaces all nodes with names from items in document with the corresponding elements from fromDoc.
 * Custom handling for image nodes - replaces only the attribut values.
 * @param items is a string or array of strings containing id names to replace.  
 * @param fromDoc is an XML DOM document also containing nodes labelled form items, and the items node in the document
 * are replaced with the same from fromDoc.
 */
XsltAjax.prototype.replaceNode = function (items, fromDoc) {
   if( !items ) {
      return;
   }
   var replaceBy, replaced, parentNode,i;
   if( typeof items != 'string' ) {
      for(i=0; i<items.length; i++) {
         this.replaceNode(items[i],fromDoc);
      }
   }
   else {
      replaceBy = this.getElementById(fromDoc,items);
      replaced = document.getElementById(items);
      if( replaceBy===undefined || replaceBy===null ) {
         alert("No item named "+items+" in result "+this.asString(fromDoc));
         return;
      }
      if( replaced===undefined ) {
         alert("No item named "+items+" in current document tree.");
         return;
      }
      if( replaceBy.tagName==='img' && replaced.tagName==='img') {
      	this.replaceAttributes(replaced, replaceBy);
      	return;
      }
      if( replaceBy.xml!==undefined ) {
        replaced.outerHTML =replaceBy.xml;
      }
      else {
        parentNode = replaced.parentNode;
        parentNode.replaceChild(replaceBy, replaced);
      }
   }
};

/** Convert a DOM node to a string */
XsltAjax.prototype.asString = function(xml) {
  return this.serializer.serializeToString(xml);
};


/**
 * Reads the given file in from the server and renders the specified
 * elements from item.
 * @param (string) file is a URI to load
 * @param (string or array of strings) item is a set of id's to update
 */
XsltAjax.prototype.ajaxRead = function (file, item, params){
  if(!file) {
  	alert("Ajax read requested from undefined URL - did you forget to have a node with the id the name of the action and the right href or set the URL?");
  }
  this.inProgress = true;
  var xmlObj = null;
  var usethis = this;
  this.url = file;
  xmlObj = new XMLHttpRequest();
  xmlObj.onreadystatechange = function(){
     if(xmlObj.readyState == 4){
     	this.fromServerTime = (new Date()).getTime();
        usethis.updateObj(item, usethis.asXml(xmlObj));
     }
  };
  if( xmlObj.overrideMimeType ) {
     xmlObj.overrideMimeType('application/xml');
  }
  if( !params ) {
     xmlObj.open ('GET', file, true);
  } else {
     xmlObj.open('POST', file, true);
     xmlObj.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');
  }
  xmlObj.send (params);
}

/**
 * Updates the  given nodes with id equal to item, from the xml.  Transforms
 * the XML first with the xslt if there is any xslt.
 * @param (string or array of string) item is the set of items to update.
 * @param
 */
XsltAjax.prototype.updateObj = function (item, xml){
    this.currentXml = xml;
    this.endParseTime = (new Date()).getTime();
	if( this.xsltProcessor!==undefined ) {
      xml = this.xsltProcessor.transformToDocument(xml);
   }
   this.xslTime = (new Date()).getTime();
   this.replaceNode(item,xml);
   this.updateCursor(item,this.NORMAL_CURSOR);
   this.inProgress = false;
   this.replaceNodeTime = (new Date()).getTime();
   
   if( pageDebug ) {
     alert("2.Server load:"+(this.fromServerTime-this.startTime)+
     " parse:"+(this.endParseTime-this.fromServerTime)+
     " xslt:"+(this.xslTime-this.endParseTime)+" replace:"+(this.replaceNodeTime-this.xslTime)+
     " total:"+(this.replaceNodeTime-this.startTime));
   }
};

/** Add the object to be used as document(namespaceURI)
 * 
 */
XSLTProcessor.prototype.addObject = function(obj, namespaceURI) {
   	if( this.processor ) {
  		this.processor.addObject(obj,namespaceURI);
  	}
};

/**
 * Adds an object to the xsl processor, so it doesn't re-load remotely.
 */
XsltAjax.prototype.addObject = function (obj, namespaceURI) {
	if( this.xsltProcessor.addObject ) {
		obj = this.asXml(obj);
		this.xsltProcessor.addObject(obj,namespaceURI);
	}
};

/**
 * Performs the given action, including any navigation
 * By default, this posts to the current URL adding the
 * parameters, and then re-renders the view for the entire page (body only)
 * It will follow redirection, which will cause an entirely new view to appear.
 *
 * This behaviour can be customized in several ways:
 *   If this.itemsToUpdate[actionName] is non-null, then only the specified
 *   items are updated (specified by id).  Otherwise "body" is updated.
 * 
 *   If this.updateModel[actionName] is non-null, then that action is called
 *   to update the model - AND the model can be directly used thereafter.
 * 
 *   The updateModel function can return:
 *   undefined - if so, then the assumption is that the URL has been 
 *   changed internally, or that the data has been changed internally.
 *   These can be identified by whether there is a currentXML - this will
 *   be used if present, otherwise the get value will be fetched.  If the
 *   URL hasn't been changed, then this can result in cached data being
 *   used (from the browser cache), so do not count on modifying currentXML
 *   to null to cause a re-load.
 *   The updateModel can also return the arguments to send
 *
 *   The updateModel function can also return arguments, which are then
 *   used in a POST command.  This should be the normal course of events.
 *   The default arguments are provided to the updateModel.
 *   
 *   
 *   Actions that redirect the page or a parent of the page are just encoded
 *   using standard targets, as they will take arguments etc, but will be
 *   standard links.
 *
 *   If this.NoPostAction is true, then only the local action is performed,
 *   no remote post is done at all (regardless of modelUpdate's return).
 *   This allows for entirely local running of Xero.
 */
XsltAjax.prototype.action = function(actionName,postArgs) {
   if( this.inProgress ) return false;
   this.startTime = (new Date()).getTime();
   this.fromServerTime = this.startTime;
   this.inProgress = true;
   var items = this.itemsToUpdate[actionName];
   if( !items ) items = "body";
   this.updateCursor(items,this.WAIT_CURSOR);
   
   var updateModel = this.updateModel[actionName];
   
   var postUpdate = "action="+actionName+"&"+postArgs;
   var actionIdItem = document.getElementById(actionName);
   var actionHref;
   if( updateModel ) {
      //alert("Post update to "+actionName+" on id "+items); 
      postUpdate = updateModel(this,actionName,postUpdate);
   }
   else if( actionIdItem ){
   	 // Special case - action is from an id with the same name that
   	 // ALSO contains an href URL - assume the URL is a new GET action
   	 // to be executed instead...
   	 actionHref = actionIdItem.getAttribute('href');
   	 if( actionHref ) {
   	    //alert("Found href item to update action "+actionName+" on id "+items+" href="+actionHref+" args "+postArgs);
   	 	this.currentXml = null;
   	 	postUpdate = postArgs;
   	 	this.url = actionHref;
   	 }
   	 //else alert("No action href found to update, using get on "+this.url+" items "+items);   }

   if( updateModel && this.currentXml) {
      // A direct update, no refresh/post/anything.
      this.updateObj(items, this.currentXml);
   }
   else {
	  this.ajaxRead(this.url, items, postUpdate);
   }
   return false;
};

