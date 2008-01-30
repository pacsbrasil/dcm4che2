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
 * Portions created by the Initial Developer are Copyright (C) 2008
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
function LookAheadTest(name) 
{
	TestCase.call(this, name);
};

function LookAheadTest_setUp()
{
	var la = new LookAheadImage();
	this.lookAhead = la;
	info("Setting up view positions.");
	la.setViewPosition(0);
	this.url = "http://wado?studyUID=1&seriesUID=1&objectUID=1.2.3.4&rows=35&columns=26&region=0,0,0.5,0.75";
	this.view = new Image(this.url);
	la.setView(0,this.view);
};

/** Reads all 57 images from 5 files into memory. */
function LookAheadTest_readAll() {
	debug("Reading XML into the image view.");
	var la = this.lookAhead;
	la.readImageXml(readXml("image/image-12-0.xml"));
	la.readImageXml(readXml("image/image-12-12.xml"));
	la.readImageXml(readXml("image/image-12-24.xml"));
	la.readImageXml(readXml("image/image-12-36.xml"));
	la.readImageXml(readXml("image/image-12-48.xml"));
	debug("Done look ahead test setup.");
}

function LookAheadTest_testLoadImageRef()
{
	this.readAll();
	info("Starting test load image ref.");
	var la = this.lookAhead;
	this.assertEquals("There are 57 images in the example xml data", 57, la.getImageCount());
	this.assertNotNull("Image ref 0 isn't null.", la.getImageRef(0));
	this.assertNotUndefined("Image ref 0 is defined.", la.getImageRef(0));
	this.assertNotNull("Image ref 11 isnt null.", la.getImageRef(11));
	this.assertNotUndefined("Image ref 11 is defined.", la.getImageRef(11));
};

function LookAheadTest_testImageFetch()
{
	this.readAll();
	info("Starting test image fetch.");
	var la = this.lookAhead;
	var view = this.view;
	var url="http://wado?studyUID=1&seriesUID=1&objectUID=1.3.12.2.1107.5.2.6.14157.6.0.1686049290512338&windowCenter=37&windowWidth=56&rows=35&columns=26&region=0,0,0.5,0.75";
    var ir = la.getImageRef(0);
    debug("About to set window level.");
    ir.setWindowLevel(37,56);
    debug("About to fetch");
    ir.fetch();
    debug("Getting fetch view.");
    var fetchView = ir.getFetchView();
    debug("Testing url:"+fetchView.src);
    this.assertSame("URL must be the first URL:", url, 	fetchView.src);
    this.assertFalse("Image should not be loaded yet.",ir.isLoaded());
    debug("Delivering the image now.");
    fetchView.deliver();
    this.assertTrue("Image should be loaded after being delivered.",ir.isLoaded());
    debug("Checking that the view is still set.");
    view = la.getViewAtPosition(0);
    this.assertNotNull(view);
    debug("Checking that the view is NOT set for the next element.");
    this.assertNull(la.getViewAtPosition(1));
    debug("Checking that the displayed URL is also correct.");
    this.assertSame("URL must be correct for source image as it is being displayed", url, view.src);
};

/**
 * Test the simplest CINE - no quality/resolution degradation etc, just fetching some number of
 * extra images.  This also currently assumes all images have meta-data.
 */
function LookAheadTest_testCine()
{
	this.readAll();
	info("Starting test CINE.");
	var la = this.lookAhead;
	var view = this.view;
	var cine = new Cine(la);
	info("Created cine object.");
	cine.setRate(5);
	cine.start();
	info("Called cine start.");
	this.assertFalse(cine.isStarted());
	var n = cine.getReadAhead();
	for(var i=0; i<n; i++) {
		la.getImageRef(i+1).getFetchView().deliver();
	}
	window.deliverInterval(1000);
	this.assertTrue("Cine should be started after the look ahead images are delivered.",cine.isStarted());
	this.assertNull("Image fetch should be cleared for the previous image.", la.getImageRef(1).getFetchView());
	this.assertTrue("Image should record that it has been fetched.", la.getImageRef(1).isLoaded());
	this.assertNotNull("Image should have a view it is being displayed in.", la.getViewAtPosition(1));
	this.assertEquals("URL of displayed view should be the fetched view.", la.getViewAtPosition(1).src, la.getImageRef(1).src);
	this.assertNull("Previous view should be cleared.", la.getViewAtPosition(0));
};

/** The DOM doesn't actually specify that the "id" attribute is always an id, so the DOM getElementId
 * only works if there is a schema that specifies an id.  Since I don't have one, and don't want to, this
 * version is written instead that does a brute force depth first search.
 */
function getElementById(xml,id) {
	var nds = xml.getChildNodes();
	if( nds===null || nds===undefined ) return null;
	var n = nds.getLength();
	if( n<1 ) return null;
	var child, ret;
	var testid;
	for(var i=0; i<n; i++ ) {
		child = nds.item(i);
		if( child.getNodeType()!=child.ELEMENT_NODE ) continue;
		testid = child.getAttribute("id");
		if( testid==id ) {
			debug("Found child "+testid);
			return child;
		}
		ret = getElementById(child,id);
		if( ret!==null ) return ret;
	}
	return null;
};

/**
 * Tests that the image meta-data can be read by finding out image information from a DOM node, and then reading the rest from there.
 */
function LookAheadTest_testSetupFromHtml()
{
	var imgUrl = "/xero/wado?requestType=WADO&studyUID=1&seriesUID=1.3.12.2.1107.5.2.6.14157.6.0.1686047132209758";
	var dataUrl = "/xero/image/image.xml?seriesUID=1.3.12.2.1107.5.2.6.14157.6.0.1686047132209758";
	info("Starting testSetupFromHtml");
	var xml = readXml("image/example.xhtml");
	info("exampleHtml  type="+xml.getNodeType());
	var la = new LookAheadImage();
	// Default fetch size would fetch everything, and it is easier to test smaller fetch sizes.
	la.fetchSize = 12;
	info(0.1);
	var imgEl = getElementById(xml,"imgi0");
	this.assertNotNull("Should have an imgi0 element",imgEl);
	info(0.3);
	la.init(imgEl);
	info(1);
	this.assertEquals("Should have the right image URL.", imgUrl, la.imgUrl);
	this.assertEquals("Should have the right data URL.", dataUrl, la.dataUrl);
	this.assertEquals("Should have 1 view only.", 1, la.viewCount);
	info("About to deliver XML data.");
	var xurl = dataUrl + "&Position=0&Count=12";
	var xml0 = readXml("image/image-12-0.xml");
	this.assertNotUndefined(xml0);
	this.assertNotNull(xml0);
	XMLHttpRequest.prototype.deliverXml(xurl,xml0);
	info("Done delivering XML data.");
	this.assertNotNull("Image ref 0 isnt null.", la.getImageRef(0));
	this.assertNotUndefined("Image ref 0 isnt defined.", la.getImageRef(0));
	this.assertNotNull("Image ref 11 isnt null.", la.getImageRef(11));
	this.assertNotUndefined("Image ref 11 isnt defined.", la.getImageRef(11));
	this.assertUndefined("Image ref 57 is defined", la.getImageRef(57));
};
 
/**
 * Navigation occurs when the user changes the desired view position.  Fast navigation occurs when
 * the user navigates one or more additional times before the selected view can be displayed.
 * The goal of fast navigation is to display SOME of the navigated elements, and when navigation is
 * complete, only then to reload the new image positions.  
 * 
 * Not all image meta-data maybe read in at the time of the view.  This test will assume all meta-data
 * is available at the time the user wants to view.
 * 
 * When the navigation occurs, there are several possibilities:
 *    Hit - the images are in cache, try displaying them, but otherwise treat as a miss.
 *    Miss - the data/images are not available.  If there is a fetch scheduled, continue on.
 *        If no fetch is scheduled, then measure the distance from the current position to the last
 *        fetch, and schedule a new fetch for the future, the same positional distance in the future as 
 *        the last fetch, minus 1, but not before the current position.  
 *        For example, if fetch_0 is at 0, and the user navigates to 1, the current position is the minimal location, so fetch it.
 *        If the user then navigates again, before fetch_1 completes, but fetch_1 completes before they navigate a subsequent time,
 *        then continue to fetch the current position, but don't do a clean refresh.
 * 		  Once the user navigates more than once, then a position beyond where the user has navigated will be fetched.
 *    Fetch - this was a fetch request, display the images (whethe they are in process or still waiting to be fetched)
 * The First fetch is considered to be at the current position.
 * If the scheduled fetch completes before any additional navigation has completed, then schedule the
 * current position at the default lossy levels/position.
 * 
 * Degraded image quality can be used for intermediate images.
 * No customized markup need be displayed on intermediate images.
 * 
 * TODO - this works really well for 1 frame, but what should be done with multi-frames?
 */
function LookAheadTest_fastNav()
{
	var la = this.la;
	var nav = new ImageNav(la);
	nav.setCurrentPosition(0);
	nav.navigate(1);
	// Fetch is keeping up
	for(var i=1; i<10; i++) {
		nav.navigate(fetchPosn+1);
		this.assertEquals("Fetch position must be the current position for slow navigation",i,nav.getFetch().getPosition());
		nav.getFetch().getImageRef().getFetchView().deliver();		
	}
	// TODO - add fetch falling behind checking.
}

LookAheadTest.prototype = new TestCase();
LookAheadTest.glue();

function LookAheadTestSuite()
{
	TestSuite.call(this, "LookAheadTestSuite");
	this.addTestSuite( LookAheadTest );
}
LookAheadTestSuite.prototype=new TestSuite();
LookAheadTestSuite.prototype.suite = function () { return new LookAheadTestSuite(); };

