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
/** Miscellaneous event handling helper code bits */

/** Returns the local name of the node */
function localName(node) {
	if( node.localName!==undefined && node.localName!==null ) return node.localName;
	if( node.baseName!==undefined ) return node.baseName;
	var tn = node.tagName;
	var i = tn.indexOf(":");
	if( i>=0 ) tn = ""+tn.substring(i+1);
	return tn;
};

/** Provides information about the frame width.  Defaults to 1024, 768 if nothing else found.*/
function getViewportSize() {
    var size = [1024, 768];
    if (typeof window.innerWidth != 'undefined')
    {
    	size = [ window.innerWidth, window.innerHeight ];
    }
    else if (typeof document.documentElement != 'undefined' &&
             typeof document.documentElement.clientWidth != 'undefined' &&
             document.documentElement.clientWidth != 0)
    {
     size = [ document.documentElement.clientWidth, document.documentElement.clientHeight ];
    }
    else
    {
     var body = document.getElementsByTagName('body')[0];
     size = [ body.clientWidth, body.clientHeight ];
    }

    return size;
};

/** Given an element, hookup the given callback (must be a function) to listen to that event */
function hookEvent(element, eventName, callback)
{
  if(typeof(element) == "string")
    element = document.getElementById(element);
  if(element == null)
    return;
  if(element.addEventListener)
  {
    if(eventName == 'mousewheel')
    {
      element.addEventListener('DOMMouseScroll',
        callback, false); 
    }
    element.addEventListener(eventName, callback, false);
  }
  else if(element.attachEvent)
    element.attachEvent("on" + eventName, callback);
}

/** Unhook the given callback from the event. */
function unhookEvent(element, eventName, callback)
{
  if(typeof(element) == "string")
    element = document.getElementById(element);
  if(element == null)
    return;
  if(element.removeEventListener)
  {
    if(eventName == 'mousewheel')
    {
      element.removeEventListener('DOMMouseScroll',
        callback, false); 
    }
    element.removeEventListener(eventName, callback, false);
  }
  else if(element.detachEvent)
    element.detachEvent("on" + eventName, callback);
}

/** Cancels an events default behaviour */
function cancelEvent(e)
{
  e = e ? e : window.event;
  if(e.stopPropagation)
    e.stopPropagation();
  if(e.preventDefault)
    e.preventDefault();
  e.cancelBubble = true;
  e.cancel = true;
  e.returnValue = false;
  return false;
}

