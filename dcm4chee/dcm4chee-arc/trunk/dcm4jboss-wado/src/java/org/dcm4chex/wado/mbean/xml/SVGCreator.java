/*
 * Created on 13.01.2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.dcm4chex.wado.mbean.xml;

import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.Properties;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.Logger;
import org.dcm4chex.wado.mbean.ecg.WaveFormChannel;
import org.dcm4chex.wado.mbean.ecg.WaveFormGroup;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;


/**
 * @author franz.willer
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class SVGCreator implements XMLResponseObject{

	private static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("yyyyMMdd");
	private static final SimpleDateFormat DATETIME_FORMATTER = new SimpleDateFormat("yyyyMMddHHmmss");
	private static final AttributesImpl EMPTY_ATTRIBUTES = new AttributesImpl();
	
	private static final float PIX_PER_MV = -100f;// (negative because x=0 is top)  1mV/cm
	private static final float PIX_PER_SEC = 250f;// is400ms/cm -> 25mm/s / 1mm = 10 pix
	
	private static Logger log = Logger.getLogger( SVGCreator.class.getName() );
	
    private TransformerHandler th = null;
	private String width = "26.60cm";
	private String height = "20.3cm";
	private int viewBoxY1 = 0;
	private int viewBoxX1 = 0;
	private int viewBoxY2 = 2660;
	private int viewBoxX2 = 2030;
	
	private int graphicXOffset = 0;
	private int graphicYOffset = 0;
	
	private int graphicWidthCm = 25;//in cm
	private int graphicHeightCm = 19;//in cm
	
	private int graphicWidth = graphicWidthCm*100;//in pix
	private int graphicHeight = graphicHeightCm*100;//in pix
	
	private WaveFormGroup waveForms;
	
	public SVGCreator(WaveFormGroup wfgrp) {
		waveForms = wfgrp;
	}
	
	/**
	 * @return Returns the viewBoxX1.
	 */
	public int getViewBoxX1() {
		return viewBoxX1;
	}
	/**
	 * @param viewBoxX1 The viewBoxX1 to set.
	 */
	public void setViewBoxX1(int viewBoxX1) {
		this.viewBoxX1 = viewBoxX1;
	}
	/**
	 * @return Returns the viewBoxX2.
	 */
	public int getViewBoxX2() {
		return viewBoxX2;
	}
	/**
	 * @param viewBoxX2 The viewBoxX2 to set.
	 */
	public void setViewBoxX2(int viewBoxX2) {
		this.viewBoxX2 = viewBoxX2;
	}
	/**
	 * @return Returns the viewBoxY1.
	 */
	public int getViewBoxY1() {
		return viewBoxY1;
	}
	/**
	 * @param viewBoxY1 The viewBoxY1 to set.
	 */
	public void setViewBoxY1(int viewBoxY1) {
		this.viewBoxY1 = viewBoxY1;
	}
	/**
	 * @return Returns the viewBoxY2.
	 */
	public int getViewBoxY2() {
		return viewBoxY2;
	}
	/**
	 * @param viewBoxY2 The viewBoxY2 to set.
	 */
	public void setViewBoxY2(int viewBoxY2) {
		this.viewBoxY2 = viewBoxY2;
	}
	/**
	 * @return Returns the width.
	 */
	public String getWidth() {
		return width;
	}
	/**
	 * @param width The width to set.
	 */
	public void setWidth(String width) {
		this.width = width;
	}
	public void toXML( OutputStream out ) throws TransformerConfigurationException, SAXException {
			        SAXTransformerFactory tf = (SAXTransformerFactory) TransformerFactory.newInstance();

    	th = tf.newTransformerHandler();
    	th.getTransformer().setOutputProperty(OutputKeys.INDENT, "yes");
        th.setResult( new StreamResult(out) );
        th.startDocument();
        addSVGStart();
        	addTitleAndDesc();
        	addTextSegment();
        	addHeaderSeparator();
        	addShortmeasSegment();
        	addSeveritySegment();
        	addInterpContinue();
        	addLeftstatementSegment();
        	addRightstatementSegment();
        	addInterpSeparator();
        	addFooter();
        	addDefs();
        	addGrid( graphicWidthCm, graphicHeightCm );
        	add12LeadGraphic( graphicWidthCm, graphicHeightCm );
        endElement("svg");
        th.endDocument();
	    try {
			out.flush();
			out.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
				
        
	}
	
	private void addTitleAndDesc() throws SAXException {
        startElement("title", EMPTY_ATTRIBUTES );
        addValue( "TITLE" );
        endElement("title" );
        startElement("desc", EMPTY_ATTRIBUTES );
        addValue( "Description" );
        endElement("desc" );
	}
	
	private void addTextSegment() {
		//TODO
	}

	private void addHeaderSeparator() {
		//TODO
	}
	private void addShortmeasSegment() {
		//TODO
	}
	private void addSeveritySegment() {
		//TODO
	}
	private void addInterpContinue() {
		//TODO
	}
	private void addLeftstatementSegment() {
		//TODO
	}
	private void addRightstatementSegment() {
		//TODO
	}
	private void addInterpSeparator() {
		//TODO
	}
	private void addFooter() {
		//TODO
	}
	
	private void addDefs() throws SAXException {
        startElement("defs", EMPTY_ATTRIBUTES );
	    	addPath( "1mmX", "fill:none; stroke:pink", null, "M 0 "+graphicYOffset+" V "+(graphicYOffset+graphicHeight) );
	    	addPath( "1mmXd", "fill:none; stroke:red", null, "M 0 "+graphicYOffset+" V "+(graphicYOffset+graphicHeight) );
	    	addPath( "1mmY", "fill:none; stroke:pink", null, "M 0 0 H "+graphicWidth );
	    	addPath( "1mmYd", "fill:none; stroke:red", null, "M 0 0 H "+graphicWidth );
	    	//Y
	    	addG( "1cmY", null, null, null, null );
	    		for ( int i = 0, step = 10 ; i < 4 ; i++, step += 10 ) {
	    			addUse("#1mmY","translate(0,"+step+")",null);
	    			addUse("#1mmY","translate(0,"+(step+50)+")",null);
	    		}
	    		addUse("#1mmYd","translate(0,50)",null);
	    		addUse("#1mmYd","translate(0,100)",null);
	        endElement("g" );
    		//X
	    	addG( "1cmX", null, null, null, null );
	    		for ( int i = 0, step = 10 ; i < 4 ; i++, step += 10 ) {
	    			addUse("#1mmX","translate("+step+",0)",null);
	    			addUse("#1mmX","translate("+(step+50)+",0)",null);
	    		}
	    		addUse("#1mmXd","translate(50,0)",null);
	    		addUse("#1mmXd","translate(100,0)",null);
    		endElement("g" );
        endElement("defs" );
		
	}
	
	private void addGrid( int x, int y ) throws SAXException {
    	addG( "Xlines", null, null, null, null );
    		addUse("#1mmXd","translate(0,0)",null);//first dark line
	    	for ( int i = 0, step = 0 ; i < x ; i++, step+=100 ){
				addUse("#1cmX","translate("+(graphicYOffset+step)+",0)",null);
	    	}
		endElement("g" );

		addG( "Ylines", null, null, null, null );
			addUse("#1mmYd","translate(0,0)",null);//first dark line
	    	for ( int i = 0, step = 0 ; i < y ; i++, step+=100 ){
				addUse("#1cmY","translate(0,"+(graphicYOffset+step)+")",null);
	    	}
    	endElement("g" );
		
	}
	
	private void add12LeadGraphic( int x, int y ) throws SAXException{
		float deltaHeight = ((float) y) / 6f * 100f;
		float yTopPos = graphicYOffset;
		float leftGraphX = 120f;
		float graphWidth = ( (float) ( x * 100 - leftGraphX ) ) / 2f;
		float rightGraphX = leftGraphX + graphWidth;
		
		addG( "waveformSegment", "translate(0,"+graphicYOffset+")", null, "50", null );
		{
			for ( int i = 0 ; i < 6 ; i ++ ) { //6 rows
				addCalPulse( i, 10, yTopPos, deltaHeight );
				addWaveform( i, leftGraphX, yTopPos, deltaHeight, graphWidth );
				addWaveform( i+6, rightGraphX, yTopPos, deltaHeight, graphWidth );
				yTopPos += deltaHeight;
			}
		}
		endElement("g");
	}
	
	/**
	 * @param topPos
	 * @throws SAXException
	 */
	private void addCalPulse(int row, int xOffset, float topPos, float height) throws SAXException {
		float baseLineY = topPos + height/2f;
		addG( "calpulse row"+row, "translate("+xOffset+","+baseLineY+")",null, null, null );
			addPath( "calpulse", "fill:none; stroke:black", "3", "M 0 0 H 25 V -100 H 75 V 0 H 100");
		endElement("g");
		
	}
	
	private void addWaveform(int lead, float xOffset, float topPos, float height, float width) throws SAXException {
		float baseLineY = topPos + height/2f;
		addG( "lead"+lead, "translate("+xOffset+","+baseLineY+")",null, null, null );
			addText( "0", "-100", "green", "lead"+lead);
			addPath( "lead"+lead, "fill:none;stroke:black", "3", getWaveFormString( waveForms.getChannel( lead ), width ));
		endElement("g");
	}

	/**
	 * @param channel
	 * @return
	 */
	private String getWaveFormString(WaveFormChannel channel, float width) {
		StringBuffer sb = new StringBuffer();
		float xDelta = PIX_PER_SEC / waveForms.getSampleFreq();
		int len = waveForms.getNrOfSamples();
		log.info("len:"+len);
		if ( len * xDelta > width ) {
			log.info("correction: (len*xdelta):"+(len*xDelta)+">"+width);
			len = new Float( width / xDelta).intValue();
			log.info("xDelta:"+xDelta+" --> width/xDelta:"+( width / xDelta)+" len:"+len);
		}
		sb.append("M 0 0 L ");
		float currX = 0f;
		for ( int i = 0 ; i < len ; i++ ) {
			sb.append( currX ).append( " " ).append( channel.getValue() * PIX_PER_MV ).append(" ");
			currX += xDelta;
		}
		log.info( "getWaveFormString:"+sb );
		return sb.toString();
	}

	/**
	 * @param i
	 * @param j
	 * @param string
	 * @param string2
	 * @throws SAXException
	 */
	private void addText(String x, String y, String fill, String text) throws SAXException {
        AttributesImpl attr = new AttributesImpl();
        addAttribute( attr, "x", x );
        addAttribute( attr, "y", y );
        addAttribute( attr, "fill", fill );
		
        startElement("text", attr );
        	addValue(text);
        endElement("text" );
		
	}

	/**
	 * @param strokeWidth TODO
	 * @param string
	 * @param string2
	 * @param string3
	 * @throws SAXException
	 */
	private void addPath(String id, String style, String strokeWidth, String d) throws SAXException {
        AttributesImpl attr = new AttributesImpl();
        addAttribute( attr, "id", id );
        addAttribute( attr, "style", style );
        addAttribute( attr, "d", d );
		
        startElement("path", attr );
        endElement("path" );
		
	}
	
	private void addG( String id, String transform, String fill, String fontSize, Properties props ) throws SAXException {
        AttributesImpl attr = new AttributesImpl();
        if ( id != null ) addAttribute( attr, "id", id );
        if ( transform != null ) addAttribute( attr, "transform", transform );
        if ( fill != null ) addAttribute( attr, "fill", fill );
        if ( fontSize != null ) addAttribute( attr, "fontSize", fontSize );
        addAttributes( attr, props );

        startElement("g", attr );
        
	}

	/**
	 * @param xlinkHref
	 * @param transform
	 * @param object
	 * @throws SAXException
	 */
	private void addUse(String xlinkHref, String transform, Properties props) throws SAXException {
        AttributesImpl attr = new AttributesImpl();
        if ( xlinkHref != null ) addAttribute( attr, "xlink:href", xlinkHref );
        if ( transform != null ) addAttribute( attr, "transform", transform );
        addAttributes( attr, props );

        startElement("use", attr );
        endElement( "use" );
	}
	
	private void addAttributes( AttributesImpl attr, Properties props ) {
        if ( props != null ) {
        	Iterator iter = props.keySet().iterator();
        	String key;
        	while ( iter.hasNext() ) {
        		key = iter.next().toString();
        		addAttribute( attr, key, props.getProperty( key ) );
        	}
        }
	}
	
	private void addSVGStart() throws SAXException {
        AttributesImpl attr = new AttributesImpl();
        addAttribute( attr, "xmlns:xlink", "http://www.w3.org/1999/xlink" );
        addAttribute( attr, "onload", "initialize(evt)" );
        
        addAttribute( attr, "width", width );
        addAttribute( attr, "height", height );

        addAttribute( attr, "viewBox", getViewBoxString() );

        addAttribute( attr, "preserveAspectRatio", "xMinYMin splice" );

        startElement("svg", EMPTY_ATTRIBUTES );
	}

	/**
	 * @return
	 */
	private String getViewBoxString() {
		StringBuffer sb = new StringBuffer();
		sb.append( viewBoxX1 ).append(" ").append( viewBoxY1);
		sb.append( viewBoxX2 ).append(" ").append( viewBoxY2);
		return null;
	}

	private void startElement( String name, Attributes attr ) throws SAXException {
	       th.startElement("", name, name, attr );
	}
	private void endElement( String name ) throws SAXException {
	       th.endElement("", name, name );
	}
	
	private void addAttribute( AttributesImpl attr, String name, String value ) {
		if ( value == null ) return;
		attr.addAttribute("", name, name, "", value);		
	}
	
	private void addValue( String value ) throws SAXException {
        th.characters(value.toCharArray(), 0, value.length() );
	}
	
}
