<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE xsl:stylesheet [
<!ENTITY nbsp "&#160;">
]>
<xsl:stylesheet version="1.0" 
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:internal="urn:my-internal-data">
   <internal:data>
      <months>
         <month value=""></month>      
         <month value="01">Jan</month>
         <month value="02">Feb</month>
         <month value="03">Mar</month>
         <month value="04">Apr</month>
         <month value="05">May</month>
         <month value="06">Jun</month>
         <month value="07">Jul</month>
         <month value="08">Aug</month>
         <month value="09">Sep</month>
         <month value="10">Oct</month>
         <month value="11">Nov</month>
         <month value="12">Dec</month>
      </months>

      <days>
         <day value=""></day>      
         <day value="01">01</day>
         <day value="02">02</day>
         <day value="03">03</day>
         <day value="04">04</day>
         <day value="05">05</day>
         <day value="06">06</day>
         <day value="07">07</day>
         <day value="08">08</day>
         <day value="09">09</day>
         <day value="10">10</day>
         <day value="11">11</day>
         <day value="12">12</day>
         <day value="13">13</day>
         <day value="14">14</day>
         <day value="15">15</day>
         <day value="16">16</day>
         <day value="17">17</day>
         <day value="18">18</day>
         <day value="19">19</day>
         <day value="20">20</day>
         <day value="21">21</day>
         <day value="22">22</day>
         <day value="23">23</day>
         <day value="24">24</day>
         <day value="25">25</day>
         <day value="26">26</day>
         <day value="27">27</day>
         <day value="28">28</day>
         <day value="29">29</day>
         <day value="30">30</day>
         <day value="31">31</day>
      </days>
   </internal:data>

   <xsl:variable name="gMonths" select="document('')/*/internal:data/months/month" />

   <xsl:variable name="gDays" select="document('')/*/internal:data/days/day" />

   <xsl:output method="html" indent="yes" encoding="ISO-8859-1" />

   <xsl:template match="/">
      <html>
         <head>
            <title>Edit Patient</title>
            <link rel="stylesheet" href="stylesheet.css" type="text/css" />
			<script language="JavaScript">window.name = "patient_edit";</script>
         </head>

         <body>
            <xsl:apply-templates select="model" />
         </body>
      </html>
   </xsl:template>

   <xsl:template match="model">
      <form action="patientUpdate.m" method="post" target="folder">
         <input name="pk" type="hidden" value="{pk}" />

         <table bgcolor="#eeeeee" border="0" width="100%">
            <tr>
               <td class="label">Patient ID:</td>
            </tr>

            <tr>
               <td>
                  <input size="25" name="patientID" type="text" value="{patientID}" />
               </td>
            </tr>

            <tr>
               <td class="label">Patient Name:</td>
            </tr>

            <tr>
               <td>
                  <input size="25" name="patientName" type="text" value="{patientName}" />
               </td>
            </tr>

            <tr>
               <td class="label">Patient Sex:</td>
            </tr>

            <tr>
               <td>
                  <input size="3" name="patientSex" type="text" value="{patientSex}" />
               </td>
            </tr>

            <tr>
               <td class="label">Patient Birth Date:</td>
            </tr>

            <tr>
               <td>
                  <select id="patientBirthDay" name="patientBirthDay" value="{patientBirthDay}">
                     <xsl:call-template name="options">
                        <xsl:with-param name="options" select="$gDays" />
                        <xsl:with-param name="current-value" select="number(patientBirthDay)" />
                     </xsl:call-template>
                  </select>

                  <select id="patientBirthMonth" name="patientBirthMonth" value="{patientBirthMonth}">
                     <xsl:call-template name="options">
                        <xsl:with-param name="options" select="$gMonths" />

                        <xsl:with-param name="current-value" select="number(patientBirthMonth)" />
                     </xsl:call-template>
                  </select>

                  <input size="4" name="patientBirthYear" type="text" value="{patientBirthYear}" />
               </td>
            </tr>

            <tr>
               <td align="right">
                  <input type="submit" name="update" value="Update" />
               </td>

               <td align="right">
                  <input type="button" name="close" value="Close" onClick="window.close()" />
               </td>
            </tr>
         </table>
      </form>
   </xsl:template>

   <xsl:template name="options">
      <xsl:param name="options" />
      <xsl:param name="current-value" />
      <xsl:for-each select="$options">
         <option value="{@value}">
            <xsl:if test="number(@value) = $current-value">
               <xsl:attribute name="selected">
		          <xsl:text>selected</xsl:text>
               </xsl:attribute>
            </xsl:if>
            <xsl:value-of select="." />
         </option>
      </xsl:for-each>
   </xsl:template>
   
</xsl:stylesheet>

