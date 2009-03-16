<?xml version="1.0" encoding="utf-8"?>
<?xml-stylesheet type="text/xsl" href="pretty.xsl"?>
<!--

Pretty XML Tree Viewer 1.0 (15 Oct 2001):
An XPath/XSLT visualisation tool for XML documents

Written by Mike J. Brown and Jeni Tennison.
No license; use freely, but please credit the authors if republishing elsewhere.

Use this stylesheet to produce an HTML document containing an ASCII art
representation of an XML document's node tree, as exposed by the XML parser
and interpreted by the XSLT processor. Note that the parser may not expose
comments to the XSLT processor.

Usage notes
===========

The output from this stylesheet is HTML that relies heavily on the tree-view.css
stylesheet. If you need plain text output, use the ASCII-only version, not this
stylesheet.

By default, this stylesheet will not show namespace nodes. If the XSLT processor
supports the namespace axis and you want to see namespace nodes, just pass a
non-empty "show_ns" parameter to the stylesheet. Example using Instant Saxon:

    saxon somefile.xml tree-view.xsl show_ns=yes

If you want to ignore whitespace-only text nodes, uncomment the xsl:strip-space
instruction below. This is recommended if you are a beginner.

-->

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:output method="html" indent="no"/>

<!--
  <xsl:strip-space elements="*"/>
-->

  <xsl:param name="show_ns"/>
  <xsl:variable name="apos">'</xsl:variable>

  <xsl:template match="/">
    <html>
      <head>
         <title>Tree View</title>
        <link type="text/css" rel="stylesheet" href="/xero/pretty.css"/>
      </head>
      <body>
        <h3>tree-view.xsl output</h3>
        <xsl:apply-templates select="." mode="render"/>
      </body>
    </html>
  </xsl:template>

  <xsl:template match="/" mode="render">
    <span class="root">root</span>
    <br/>
    <xsl:apply-templates mode="render"/>
  </xsl:template>

  <xsl:template match="*" mode="render">
    <xsl:call-template name="ascii-art-hierarchy"/>
    <span class='connector'>___</span>
    <xsl:text> </xsl:text>
    &lt;
    <xsl:call-template name="namespace" />
    <span class="name">
      <xsl:value-of select="local-name()"/>
    </span>
    <xsl:apply-templates select="@*" mode="render"/>&gt;
    <br />
    <xsl:if test="$show_ns">
      <xsl:for-each select="namespace::*">
        <xsl:sort select="name()"/>
        <xsl:call-template name="ascii-art-hierarchy"/>
        <span class='connector'>  </span>
        <span class='connector'>\___</span>
        <span class="namespace">namespace</span>
        <xsl:text> </xsl:text>
        <xsl:choose>
          <xsl:when test="name()">
            <span class="name">
              <xsl:value-of select="name()"/>
            </span>
          </xsl:when>
          <xsl:otherwise>#default</xsl:otherwise>
        </xsl:choose>
      </xsl:for-each>
    </xsl:if>
    <xsl:apply-templates mode="render"/>
 </xsl:template>

 <xsl:template name="namespace">
    <xsl:choose>

       <xsl:when test="namespace-uri()='http://www.w3.org/1999/xlink'">xlink:</xsl:when>
      <xsl:when test="namespace-uri()='http://www.w3.org/2001/XMLSchema-instance'">xsi:</xsl:when>
       <xsl:when test="namespace-uri()='http://www.dcm4chee.org/xero/search/study/'">se:</xsl:when>
       <xsl:when test="namespace-uri()='http://www.w3.org/2000/svg'">svg:</xsl:when>
       <xsl:when test="namespace-uri()">
        <xsl:text>{</xsl:text>
        <span class="uri"><xsl:value-of select="namespace-uri()"/></span>
        <xsl:text>}</xsl:text>
      </xsl:when>
    </xsl:choose>
  </xsl:template>

  <xsl:template name="attrRender" match="@*" mode="render">
    <xsl:text> </xsl:text>
    <xsl:call-template name="namespace" />
    <span class="name">
      <xsl:value-of select="local-name()"/>
    </span>
    <xsl:text>="</xsl:text>
    <span class="value">
      <!-- make spaces be non-breaking spaces, since this is HTML -->
      <xsl:call-template name="escape-ws">
        <xsl:with-param name="text" select="translate(.,' ',' ')"/>
      </xsl:call-template>
   </span>
    <xsl:text>"</xsl:text>
  </xsl:template>

  <xsl:template match="@objectUID" mode="render">
  	 <a><xsl:attribute name="href">?requestType=WADO<xsl:value-of select="concat('&amp;studyUID=',../../../@studyUID, '&amp;seriesUID=',../../@seriesUID,'&amp;objectUID=',.)" /><xsl:if test="count(/*/@ae)=1">&amp;ae=<xsl:value-of select="/*/@ae" /></xsl:if></xsl:attribute><xsl:call-template name="attrRender" /></a>
  </xsl:template>

  <xsl:template match="@studyUID" mode="render">
     <a><xsl:attribute name="href">?requestType=SERIES&amp;studyUID=<xsl:value-of select="." /><xsl:if test="count(/*/@ae)=1">&amp;ae=<xsl:value-of select="/*/@ae" /></xsl:if></xsl:attribute><xsl:call-template name="attrRender" /></a>
  </xsl:template>

  <xsl:template match="@seriesUID" mode="render">
     <a><xsl:attribute name="href">?requestType=IMAGE&amp;seriesUID=<xsl:value-of select="." /><xsl:if test="count(/*/@ae)=1">&amp;ae=<xsl:value-of select="/*/@ae" /></xsl:if></xsl:attribute><xsl:call-template name="attrRender" /></a>
  </xsl:template>

  <xsl:template match="text()" mode="render">
    <xsl:call-template name="ascii-art-hierarchy"/>
    <br/>
    <xsl:call-template name="ascii-art-hierarchy"/>
    <span class='connector'>___</span>
    <span class="text">text</span>
    <xsl:text> </xsl:text>
    <span class="value">
      <!-- make spaces be non-breaking spaces, since this is HTML -->
      <xsl:call-template name="escape-ws">
        <xsl:with-param name="text" select="translate(.,' ',' ')"/>
      </xsl:call-template>
    </span>
    <br/>
  </xsl:template>

  <xsl:template match="comment()" mode="render">
    <xsl:call-template name="ascii-art-hierarchy"/>
    <br/>
    <xsl:call-template name="ascii-art-hierarchy"/>
    <span class='connector'>___</span>
    <span class="comment">comment</span>
    <xsl:text> </xsl:text>
    <span class="value">
      <!-- make spaces be non-breaking spaces, since this is HTML -->
      <xsl:call-template name="escape-ws">
        <xsl:with-param name="text" select="translate(.,' ',' ')"/>
      </xsl:call-template>
    </span>
    <br/>
  </xsl:template>

  <xsl:template match="processing-instruction()" mode="render">
    <xsl:call-template name="ascii-art-hierarchy"/>
    <br/>
    <xsl:call-template name="ascii-art-hierarchy"/>
    <span class='connector'>___</span>
    <span class="pi">processing instruction</span>
    <xsl:text> </xsl:text>
    <xsl:text>target=</xsl:text>
    <span class="value">
      <xsl:value-of select="name()"/>
    </span>
    <xsl:text> instruction=</xsl:text>
    <span class="value">
      <xsl:value-of select="."/>
    </span>
    <br/>
  </xsl:template>

  <xsl:template name="ascii-art-hierarchy">
    <xsl:for-each select="ancestor::*">
      <xsl:choose>
        <xsl:when test="following-sibling::node()">
          <span class='connector'>  </span>|<span class='connector'>  </span>
          <xsl:text> </xsl:text>
        </xsl:when>
        <xsl:otherwise>
          <span class='connector'>    </span>
          <span class='connector'>  </span>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:for-each>
    <xsl:choose>
      <xsl:when test="parent::node() and ../child::node()">
        <span class='connector'>  </span>
        <xsl:text>|</xsl:text>
      </xsl:when>
      <xsl:otherwise>
        <span class='connector'>   </span>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- recursive template to escape linefeeds, tabs -->
  <xsl:template name="escape-ws">
    <xsl:param name="text"/>
    <xsl:choose>
      <xsl:when test="contains($text, '&#xA;')">
        <xsl:call-template name="escape-ws">
          <xsl:with-param name="text" select="substring-before($text, '&#xA;')"/>
        </xsl:call-template>
        <span class="escape">\n</span>
        <xsl:call-template name="escape-ws">
          <xsl:with-param name="text" select="substring-after($text, '&#xA;')"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:when test="contains($text, '&#x9;')">
        <xsl:value-of select="substring-before($text, '&#x9;')"/>
        <span class="escape">\t</span>
        <xsl:call-template name="escape-ws">
          <xsl:with-param name="text" select="substring-after($text, '&#x9;')"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise><xsl:value-of select="$text"/></xsl:otherwise>
    </xsl:choose>
  </xsl:template>

</xsl:stylesheet>
