<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:fo="http://www.w3.org/1999/XSL/Format" version="1.0">
    <xsl:output method="xml" indent="yes" media-type="text/xml-fo"/>
    <xsl:include href="common.xsl"/>
    <xsl:variable name="StudyInstanceUID" select="dicomfile/dataset/attr[@tag='0020000D']"/>
    
    <!-- the stylesheet processing entry point -->
	<xsl:template match="/">
	  <xsl:apply-templates select="dicomfile/dataset"/>
	</xsl:template>
    
    <xsl:template match="dataset">
        <fo:root xmlns:fo="http://www.w3.org/1999/XSL/Format">
            <fo:layout-master-set>
                <fo:simple-page-master master-name="page" page-height="297mm" page-width="210mm"
                    margin-left="45mm" margin-right="45mm" margin-top="20mm" margin-bottom="20mm">
                      <fo:region-before extent="1cm"/>
                      <fo:region-body margin-top="1cm"/>
                      <fo:region-after extent="1.5cm"/>
                </fo:simple-page-master>
            </fo:layout-master-set>
            <fo:page-sequence master-reference="page">
            	<fo:static-content flow-name="xsl-region-before">
            		<fo:block font-size="20pt"  text-align="center" font-weight="bold" >
            			<xsl:value-of select="attr[@tag='0040A043']/item/attr[@tag='00080104']"/>
            		</fo:block>
            	</fo:static-content>
                <fo:flow flow-name="xsl-region-body">
            		<fo:block font-size="10pt" text-align="center">
                		By <xsl:value-of select="attr[@tag='00080080']"/>, Ref. Phys. 
							<xsl:call-template name="formatPN">
								<xsl:with-param name="pn" select="attr[@tag='00080090']"/>
							</xsl:call-template>
            		</fo:block>
            		<fo:table border-style="solid" table-layout="fixed">
						<fo:table-column column-number="1" column-width="50mm"/>
						<fo:table-column column-number="2" column-width="50mm"/>
						<fo:table-body>
							<fo:table-row>
								<fo:table-cell border-style="solid">
									<fo:block font-size="10pt" padding="3mm">Patient Name:</fo:block>
								</fo:table-cell>
								<fo:table-cell border-style="solid">
									<fo:block font-size="10pt" padding="3mm">
										<xsl:call-template name="formatPN">
											<xsl:with-param name="pn" select="attr[@tag='00100010']"/>
										</xsl:call-template>
									</fo:block>
								</fo:table-cell>
							</fo:table-row>
							<fo:table-row>
								<fo:table-cell border-style="solid">
									<fo:block font-size="10pt" padding="3mm">Patient ID:</fo:block>
								</fo:table-cell>
								<fo:table-cell border-style="solid">
									<fo:block font-size="10pt" padding="3mm"><xsl:value-of select="attr[@tag='00100020']"/></fo:block>
								</fo:table-cell>
							</fo:table-row>
							<fo:table-row>
								<fo:table-cell border-style="solid">
									<fo:block font-size="10pt" padding="3mm">Patient Birthdate:</fo:block>
								</fo:table-cell>
								<fo:table-cell border-style="solid">
									<fo:block font-size="10pt" padding="3mm">
										<xsl:call-template name="formatDate">
											<xsl:with-param name="date" select="attr[@tag='00100030']"/>
										</xsl:call-template>
									</fo:block>
								</fo:table-cell>
							</fo:table-row>
							<fo:table-row>
								<fo:table-cell border-style="solid">
									<fo:block font-size="10pt" padding="3mm">Patient Sex:</fo:block>
								</fo:table-cell>
								<fo:table-cell border-style="solid">
									<fo:block font-size="10pt" padding="3mm"><xsl:value-of select="attr[@tag='00100040']"/></fo:block>
								</fo:table-cell>
							</fo:table-row>
						</fo:table-body>
					</fo:table>
	  				<xsl:apply-templates select="attr[@tag='0040A730']/item" mode="content"/>
			   </fo:flow>
            </fo:page-sequence>
        </fo:root>
    </xsl:template>

<!--
  Contentsequence output starts here
-->
	<xsl:template match="item" mode="content">

	<fo:block font-size="12pt" font-weight="bold"><xsl:value-of select="attr[@tag='0040A043']/item/attr[@tag='00080104']"/></fo:block>

	<fo:list-block>
	<fo:list-item>
		<fo:list-item-label end-indent="label-end()">
			<fo:block>-</fo:block>
		</fo:list-item-label>
		<fo:list-item-body start-indent="body-start()">
		  <xsl:choose>
			  <xsl:when test="attr[@tag='0040A040']='TEXT'">
				<fo:block font-size="12pt"><xsl:value-of select="attr[@tag='0040A160']"/></fo:block>
			  </xsl:when>

			  <xsl:when test="attr[@tag='0040A040']='IMAGE '">
			  	<fo:block font-size="12pt">Image not supported!</fo:block>
			  </xsl:when>
	
			  <xsl:when test="attr[@tag='0040A040']='CODE'">
				<fo:block font-size="12pt"><xsl:value-of select="attr[@tag='0040A168']/item/attr[@tag='00080104']"/></fo:block>
		      </xsl:when>		
		      
			  <xsl:when test="attr[@tag='0040A040']='PNAME '">
				<fo:block font-size="12pt">
					<xsl:call-template name="formatPN">
						<xsl:with-param name="pn" select="attr[@tag='0040A123']"/>
					</xsl:call-template>
				</fo:block>
			  </xsl:when>		

	  		  <xsl:when test="attr[@tag='0040A040']='NUM '">
				<fo:block font-size="12pt"><xsl:value-of select="attr[@tag='0040A043']/item/attr[@tag='00080104']"/> Measurement not supported yet </fo:block>
			  </xsl:when>		

			  <xsl:when test="attr[@tag='0040A040']='CONTAINER '">
    			<xsl:apply-templates select="attr[@tag='0040A730']/item" mode="content"/>
			  </xsl:when>
		
	  		  <xsl:otherwise>
				<fo:block font-size="12pt"><xsl:value-of select="attr[@tag='0040A040']"/> (Value Type not supported yet)</fo:block>
	  		  </xsl:otherwise>
		  </xsl:choose>
		</fo:list-item-body>
	</fo:list-item>
	</fo:list-block>
	
	</xsl:template>
   
</xsl:stylesheet>
