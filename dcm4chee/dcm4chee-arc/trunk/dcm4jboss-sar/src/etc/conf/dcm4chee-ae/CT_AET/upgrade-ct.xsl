<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
 version="1.0">
 <xsl:output method="xml" />

 <xsl:template match="/dicomfile">
  <dicomfile>
   <xsl:apply-templates select="filemetainfo" />
   <xsl:apply-templates select="dataset" />
  </dicomfile>
 </xsl:template>

 <xsl:template match="filemetainfo">
  <filemetainfo>
   <!-- Transfer Syntax UID -->
   <xsl:copy-of select="attr[@tag='00020010']" />
  </filemetainfo>
 </xsl:template>

 <xsl:template match="dataset">
  <dataset>
   <xsl:apply-templates select="attr" />
   <!-- SOP Class UID -->
   <attr tag="00080016" vr="UI">1.2.40.0.13.1.5.1.4.1.1.2.1</attr>
   <!-- Shared Functional Groups Sequence -->
   <attr tag="52009229" vr="SQ">
    <item>
     <!-- Derivation Image Sequence -->
     <attr tag="00089124" vr="SQ">
      <item>
       <xsl:apply-templates select="attr" mode="DerivationImage" />
      </item>
     </attr>
     <!-- CT Acquisition Type Sequence -->
     <attr tag="00189301" vr="SQ">
      <item>
       <xsl:apply-templates select="attr" mode="CTAcquisitionType" />
      </item>
     </attr>
     <!-- CT Acquisition Details Sequence -->
     <attr tag="00189304" vr="SQ">
      <item>
       <xsl:apply-templates select="attr" mode="CTAcquisitionDetails" />
      </item>
     </attr>
     <!-- CT Table Dynamics Sequence -->
     <attr tag="00189308" vr="SQ">
      <item>
       <xsl:apply-templates select="attr" mode="CTTableDynamics" />
      </item>
     </attr>
     <!-- CT Geometry Sequence -->
     <attr tag="00189312" vr="SQ">
      <item>
       <xsl:apply-templates select="attr" mode="CTGeometry" />
      </item>
     </attr>
     <!-- CT Reconstruction Sequence -->
     <attr tag="00189314" vr="SQ">
      <item>
       <xsl:apply-templates select="attr" mode="CTReconstruction" />
      </item>
     </attr>
     <!-- CT Exposure Sequence -->
     <attr tag="00189321" vr="SQ">
      <item>
       <xsl:apply-templates select="attr" mode="CTExposure" />
      </item>
     </attr>
     <!-- CT X-ray Details Sequence -->
     <attr tag="00189325" vr="SQ">
      <item>
       <xsl:apply-templates select="attr" mode="CTXrayDetails" />
      </item>
     </attr>
     <!--CT Image Frame Type Sequence -->
     <attr tag="00189329" vr="SQ">
      <item>
       <xsl:apply-templates select="attr" mode="CTImageFrameType" />
      </item>
     </attr>
     <!-- Irradiation Event Identification Sequence -->
     <attr tag="00189477" vr="SQ">
      <item>
       <xsl:apply-templates select="attr" mode="IrradiationEventIdentification" />
      </item>
     </attr>
     <!-- Frame Anatomy Sequence -->
     <attr tag="00209071" vr="SQ">
      <item>
       <xsl:apply-templates select="attr" mode="FrameAnatomy" />
      </item>
     </attr>
     <!-- Plane Orientation Sequence -->
     <attr tag="00209116" vr="SQ">
      <item>
       <xsl:apply-templates select="attr" mode="PlaneOrientation" />
      </item>
     </attr>
     <!-- Patient Orientation in Frame Sequence -->
     <attr tag="00209450" vr="SQ">
      <item>
       <xsl:apply-templates mode="PatientOrientation" select="attr" />
      </item>
     </attr>
     <!-- Pixel Measures Sequence -->
     <attr tag="00289110" vr="SQ">
      <item>
       <xsl:apply-templates select="attr" mode="PixelMeasures" />
      </item>
     </attr>
     <!-- Frame VOI LUT Sequence -->
     <attr tag="00289132" vr="SQ">
      <item>
       <xsl:apply-templates select="attr" mode="FrameVOILUT" />
      </item>
     </attr>
     <!-- Pixel Value Transformation Sequence -->
     <attr tag="00289145" vr="SQ">
      <item>
       <xsl:apply-templates select="attr" mode="PixelValueTransformation" />
      </item>
     </attr>
    </item>
   </attr>
   <!-- Per-frame Functional Groups Sequence -->
   <attr tag="52009230" vr="SQ">
    <item>
     <!-- Referenced Image Sequence -->
     <attr tag="00081140" vr="SQ">
      <item>
       <xsl:apply-templates select="attr" mode="ReferencedImage" />
       <!-- Purpose of Reference Code Sequence (0040,A170) -->
       <attr tag="0040A170" vr="SQ">
        <item>
         <!--Code Value  -->
         <attr tag="00080100" vr="LO">121326</attr>
         <!-- Coding Scheme Designator -->
         <attr tag="00080102" vr="LO">DCM</attr>
         <!-- Code Meaning  -->
         <attr tag="00080104" vr="LO">Alternate SOP Class instance</attr>
        </item>
       </attr>
      </item>
      <xsl:apply-templates select="attr[@tag='00081140']/item" />
     </attr>
     <!-- Frame Content Sequence -->
     <attr tag="00209111" vr="SQ">
      <item>
       <xsl:apply-templates select="attr" mode="FrameContent" />
      </item>
     </attr>
     <!-- Plane Position Sequence -->
     <attr tag="00209113" vr="SQ">
      <item>
       <xsl:apply-templates select="attr" mode="PlanePosition" />
      </item>
     </attr>
    </item>
   </attr>
  </dataset>
 </xsl:template>

 <!-- (+) - Element not part of DICOM Enhanced CT IOD -->
 <!--
  Pixel Measures Functional Group
 -->
 <!-- Slice Thickness -->
 <xsl:template match="attr[@tag='00180050']" />
 <xsl:template match="attr[@tag='00180050']" mode="PixelMeasures">
  <xsl:copy-of select="." />
 </xsl:template>
 <!-- Pixel Spacing -->
 <xsl:template match="attr[@tag='00280030']" />
 <xsl:template match="attr[@tag='00280030']" mode="PixelMeasures">
  <xsl:copy-of select="." />
 </xsl:template>
 <xsl:template match="attr" mode="PixelMeasures" />

 <!--
  Frame Content Functional Group
 -->
 <!-- Acquisition Date -->
 <!-- Acquisition Time -->
 <xsl:template match="attr[@tag='00080022']" />
 <xsl:template match="attr[@tag='00080032']" />
 <xsl:template match="attr[@tag='00080022']" mode="FrameContent">
  <!-- Frame Acquisition Datetime -->
  <attr tag="00189074" vr="DT">
   <xsl:value-of select="." />
   <xsl:value-of select="../attr[@tag='00080032']" />
  </attr>
 </xsl:template>
 <!-- Acquisition Datetime -->
 <xsl:template match="attr[@tag='0008002A']" />
 <xsl:template match="attr[@tag='0008002A']" mode="FrameContent">
  <!-- Frame Acquisition Datetime -->
  <attr tag="00189074" vr="DT">
   <xsl:value-of select="." />
  </attr>
 </xsl:template>
 <!-- Aquisition Number -->
 <xsl:template match="attr[@tag='00200012']" />
 <xsl:template match="attr[@tag='00200012']" mode="FrameContent">
  <!-- Frame Acquisition Number -->
  <attr tag="00209156" vr="US">
   <xsl:value-of select="normalize-space()" />
  </attr>
 </xsl:template>
 <!-- Image Comments -->
 <xsl:template match="attr[@tag='00204000']" />
 <xsl:template match="attr[@tag='00204000']" mode="FrameContent">
  <!-- Frame Comments -->
  <attr tag="00209158" vr="LT">
   <xsl:value-of select="." />
  </attr>
 </xsl:template>
 <!-- Instance Creation Date(+) -->
 <xsl:template match="attr[@tag='00080012']" />
 <xsl:template match="attr[@tag='00080012']" mode="FrameContent">
  <xsl:copy-of select="." />
 </xsl:template>
 <!-- Instance Creation Time(+) -->
 <xsl:template match="attr[@tag='00080013']" />
 <xsl:template match="attr[@tag='00080013']" mode="FrameContent">
  <xsl:copy-of select="." />
 </xsl:template>
 <!-- Content Date(+) -->
 <xsl:template match="attr[@tag='00080023']" />
 <xsl:template match="attr[@tag='00080023']" mode="FrameContent">
  <xsl:copy-of select="." />
 </xsl:template>
 <!-- Content Time(+) -->
 <xsl:template match="attr[@tag='00080033']" />
 <xsl:template match="attr[@tag='00080033']" mode="FrameContent">
  <xsl:copy-of select="." />
 </xsl:template>
 <!-- Instance Number(+) -->
 <xsl:template match="attr[@tag='00200013']" />
 <xsl:template match="attr[@tag='00200013']" mode="FrameContent">
  <xsl:copy-of select="." />
 </xsl:template>
 <xsl:template match="attr" mode="FrameContent" />

 <!--
  Plane Position Functional Group
 -->
 <!-- Image Position (Patient) -->
 <xsl:template match="attr[@tag='00200032']" />
 <xsl:template match="attr[@tag='00200032']" mode="PlanePosition">
  <xsl:copy-of select="." />
 </xsl:template>
 <!-- Slice Location(+) -->
 <xsl:template match="attr[@tag='00201041']" />
 <xsl:template match="attr[@tag='00201041']" mode="PlanePosition">
  <xsl:copy-of select="." />
 </xsl:template>
 <!-- Image Position (Retired)(+) -->
 <xsl:template match="attr[@tag='00200030']" />
 <xsl:template match="attr[@tag='00200030']" mode="PlanePosition">
  <xsl:copy-of select="." />
 </xsl:template>
 <xsl:template match="attr" mode="PlanePosition" />

 <!--
  Plane Orientation Functional Group
 -->
 <!-- Image Orientation (Patient) -->
 <xsl:template match="attr[@tag='00200037']" />
 <xsl:template match="attr[@tag='00200037']" mode="PlaneOrientation">
  <xsl:copy-of select="." />
 </xsl:template>
 <!-- Image Orientation (Retired)(+) -->
 <xsl:template match="attr[@tag='00200035']" />
 <xsl:template match="attr[@tag='00200035']" mode="PlaneOrientation">
  <xsl:copy-of select="." />
 </xsl:template>
 <xsl:template match="attr" mode="PlaneOrientation" />

 <!--
  Referenced Image Functional Group
 -->
 <!-- SOP Class UID -->
 <xsl:template match="attr[@tag='00080016']" />
 <xsl:template match="attr[@tag='00080016']" mode="ReferencedImage">
  <!-- Referenced SOP Class UID -->
  <attr tag="00081150" vr="UI">
   <xsl:value-of select="." />
  </attr>
 </xsl:template>
 <!-- SOP Instance UID -->
 <xsl:template match="attr[@tag='00080018']" />
 <xsl:template match="attr[@tag='00080018']" mode="ReferencedImage">
  <!-- Referenced SOP Instance UID -->
  <attr tag="00081155" vr="UI">
   <xsl:value-of select="." />
  </attr>
 </xsl:template>
 <xsl:template match="attr" mode="ReferencedImage" />

 <!--
  Derivation Image Functional Group
 -->
 <!-- Derivation Description -->
 <xsl:template match="attr[@tag='00082111']" />
 <xsl:template match="attr[@tag='00082111']" mode="DerivationImage">
  <xsl:copy-of select="." />
 </xsl:template>
 <!-- Derivation Code Sequence -->
 <xsl:template match="attr[@tag='00089215']" />
 <xsl:template match="attr[@tag='00089215']" mode="DerivationImage">
  <xsl:copy-of select="." />
 </xsl:template>
 <!-- Source Image Sequence -->
 <xsl:template match="attr[@tag='00082112']" />
 <xsl:template match="attr[@tag='00082112']" mode="DerivationImage">
  <xsl:copy-of select="." />
 </xsl:template>
 <xsl:template match="attr" mode="DerivationImage" />

 <!--
  Frame Anatomy Functional Group
 -->
 <!-- Laterality -->
 <xsl:template match="attr[@tag='00200060']" mode="FrameAnatomy">
  <!-- Frame Laterality -->
  <attr tag="00209072" vr="CS">
   <xsl:value-of select="." />
  </attr>
 </xsl:template>
 <!-- Anatomic Region Sequence -->
 <xsl:template match="attr[@tag='00082218']" />
 <xsl:template match="attr[@tag='00082218']" mode="FrameAnatomy">
  <xsl:copy-of select="." />
 </xsl:template>
 <xsl:template match="attr" mode="FrameAnatomy" />

 <!--
  Pixel Value Transformation Functional Group
 -->
 <!-- Rescale Intercept -->
 <xsl:template match="attr[@tag='00281052']" />
 <xsl:template match="attr[@tag='00281052']" mode="PixelValueTransformation">
  <xsl:copy-of select="." />
 </xsl:template>
 <!-- Rescale Slope -->
 <xsl:template match="attr[@tag='00281053']" />
 <xsl:template match="attr[@tag='00281053']" mode="PixelValueTransformation">
  <xsl:copy-of select="." />
 </xsl:template>
 <xsl:template match="attr" mode="PixelValueTransformation" />

 <!--
  Frame VOI LUT Functional Group
 -->
 <!-- Window Center -->
 <xsl:template match="attr[@tag='00281050']" />
 <xsl:template match="attr[@tag='00281050']" mode="FrameVOILUT">
  <xsl:copy-of select="." />
 </xsl:template>
 <!-- Window Width -->
 <xsl:template match="attr[@tag='00281051']" />
 <xsl:template match="attr[@tag='00281051']" mode="FrameVOILUT">
  <xsl:copy-of select="." />
 </xsl:template>
 <xsl:template match="attr" mode="FrameVOILUT" />

 <!--
  Patient Orientation Type Functional Group
 -->
 <!-- Patient Orientation -->
 <xsl:template match="attr[@tag='00200020']" />
 <xsl:template match="attr[@tag='00200020']" mode="PatientOrientation">
  <xsl:copy-of select="." />
 </xsl:template>
 <xsl:template match="attr" mode="PatientOrientation" />

 <!--
  Irradiation Event Identification Functional Group
 -->
 <!-- Irradiation Event UID -->
 <xsl:template match="attr[@tag='00083010']" />
 <xsl:template match="attr[@tag='00083010']" mode="IrradiationEventIdentification">
  <xsl:copy-of select="." />
 </xsl:template>
 <xsl:template match="attr" mode="IrradiationEventIdentification" />

 <!--
  CT Image Frame Type Functional Group
 -->
 <!-- Image Type -->
 <xsl:template match="attr[@tag='00080008']" />
 <xsl:template match="attr[@tag='00080008']" mode="CTImageFrameType">
  <!-- Frame Type -->
  <attr tag="00089007" vr="CS">
   <xsl:value-of select="." />
  </attr>
 </xsl:template>
 <xsl:template match="attr" mode="CTImageFrameType" />
 
 <!--
  CT Acquisition Type Functional Group
 -->
<!-- Scan Options(+) -->
 <xsl:template match="attr[@tag='00180022']" />
 <xsl:template match="attr[@tag='00180022']" mode="CTAcquisitionType">
  <xsl:copy-of select="." />
 </xsl:template>
 <xsl:template match="attr" mode="CTAcquisitionType" />
 
 <!--
  CT Acquisition Details Functional Group
 -->
<!-- Rotation Direction -->
 <xsl:template match="attr[@tag='00181140']" />
 <xsl:template match="attr[@tag='00181140']" mode="CTAcquisitionDetails">
  <xsl:copy-of select="." />
 </xsl:template>
<!-- Revolution Time -->
 <xsl:template match="attr[@tag='00189305']" />
 <xsl:template match="attr[@tag='00189305']" mode="CTAcquisitionDetails">
  <xsl:copy-of select="." />
 </xsl:template>
<!-- Single Collimation Width -->
 <xsl:template match="attr[@tag='00181140']" />
 <xsl:template match="attr[@tag='00181140']" mode="CTAcquisitionDetails">
  <xsl:copy-of select="." />
 </xsl:template>
<!-- Total Collimation Width -->
 <xsl:template match="attr[@tag='00189307']" />
 <xsl:template match="attr[@tag='00189307']" mode="CTAcquisitionDetails">
  <xsl:copy-of select="." />
 </xsl:template>
<!-- Table Height -->
 <xsl:template match="attr[@tag='00181130']" />
 <xsl:template match="attr[@tag='00181130']" mode="CTAcquisitionDetails">
  <xsl:copy-of select="." />
 </xsl:template>
<!-- Gantry/Detector Tilt -->
 <xsl:template match="attr[@tag='00181120']" />
 <xsl:template match="attr[@tag='00181120']" mode="CTAcquisitionDetails">
  <xsl:copy-of select="." />
 </xsl:template>
<!-- Data Collection Diameter -->
 <xsl:template match="attr[@tag='00180090']" />
 <xsl:template match="attr[@tag='00180090']" mode="CTAcquisitionDetails">
  <xsl:copy-of select="." />
 </xsl:template>
 <xsl:template match="attr" mode="CTAcquisitionDetails" />
 
 <!--
  CT Table Dynamics Functional Group
 -->
<!-- Table Speed -->
 <xsl:template match="attr[@tag='00189309']" />
 <xsl:template match="attr[@tag='00189309']" mode="CTTableDynamics">
  <xsl:copy-of select="." />
 </xsl:template>
<!-- Table Feed per Rotation -->
 <xsl:template match="attr[@tag='00189310']" />
 <xsl:template match="attr[@tag='00189310']" mode="CTTableDynamics">
  <xsl:copy-of select="." />
 </xsl:template>
<!-- Spiral Pitch Factor -->
 <xsl:template match="attr[@tag='00189311']" />
 <xsl:template match="attr[@tag='00189311']" mode="CTTableDynamics">
  <xsl:copy-of select="." />
 </xsl:template>
 <xsl:template match="attr" mode="CTTableDynamics" />
 
 <!--
  CT Geometry Functional Group
 -->
<!-- Distance Source to Detector -->
 <xsl:template match="attr[@tag='00181110']" />
 <xsl:template match="attr[@tag='00181110']" mode="CTGeometry">
  <xsl:copy-of select="." />
 </xsl:template>
<!-- Distance Source to Patient(+) -->
 <xsl:template match="attr[@tag='00181111']" />
 <xsl:template match="attr[@tag='00181111']" mode="CTGeometry">
  <xsl:copy-of select="." />
 </xsl:template>
 <xsl:template match="attr" mode="CTGeometry" />

 <!--
  CT Reconstruction Functional Group
 -->
<!-- Convolution Kernel -->
 <xsl:template match="attr[@tag='00181210']" />
 <xsl:template match="attr[@tag='00181210']" mode="CTReconstruction">
  <xsl:copy-of select="." />
 </xsl:template>
 <!-- Reconstruction Diameter -->
 <xsl:template match="attr[@tag='00181100']" />
 <xsl:template match="attr[@tag='00181100']" mode="CTReconstruction">
  <xsl:copy-of select="." />
 </xsl:template>
 <xsl:template match="attr" mode="CTReconstruction" />

 <!--
  CT Exposure Functional Group
 -->
<!-- Exposure Time -->
 <xsl:template match="attr[@tag='00181150']" />
 <xsl:template match="attr[@tag='00181150']" mode="CTExposure">
  <!-- Exposure Time in ms -->
  <attr tag="00189328" vr="FD">
   <xsl:value-of select="." />
  </attr>
 </xsl:template>
<!-- X-ray Tube Current -->
 <xsl:template match="attr[@tag='00181151']" />
 <xsl:template match="attr[@tag='00181151']" mode="CTExposure">
  <!-- X-ray Tube Current in mA -->
  <attr tag="00189330" vr="FD">
   <xsl:value-of select="." />
  </attr>
 </xsl:template>
<!-- Exposure -->
 <xsl:template match="attr[@tag='00181152']" />
 <xsl:template match="attr[@tag='00181152']" mode="CTExposure">
  <!-- Exposure in mAs -->
  <attr tag="00189332" vr="FD">
   <xsl:value-of select="." />
  </attr>
 </xsl:template>
<!-- Exposure in μAs (0018,1153) -->
 <xsl:template match="attr[@tag='00181153']" />
 <xsl:template match="attr[@tag='00181153']" mode="CTExposure">
  <xsl:variable name="s" select="normalize-space()" />
  <xsl:variable name="l" select="string-length(s)" />
  <!-- Exposure in mAs -->
  <attr tag="00189332" vr="FD">
   <xsl:value-of select="substring(s,1,l-3)" />
   <xsl:text>.</xsl:text>
   <xsl:value-of select="substring(s,l-2)" />
  </attr>
 </xsl:template>
<!-- Exposure Modulation Type (0018,9323) -->
 <xsl:template match="attr[@tag='00189323']" />
 <xsl:template match="attr[@tag='00189323']" mode="CTExposure">
  <xsl:copy-of select="." />
 </xsl:template>
<!-- Estimated Dose Saving (0018,9324) -->
 <xsl:template match="attr[@tag='00189324']" />
 <xsl:template match="attr[@tag='00189324']" mode="CTExposure">
  <xsl:copy-of select="." />
 </xsl:template>
<!-- CTDIvol (0018,9345) -->
 <xsl:template match="attr[@tag='00189345']" />
 <xsl:template match="attr[@tag='00189345']" mode="CTExposure">
  <xsl:copy-of select="." />
 </xsl:template>
 <xsl:template match="attr" mode="CTExposure" />

 <!--
  CT X-ray Details Functional Group
 -->
 <!-- KVP -->
 <xsl:template match="attr[@tag='00180060']" />
 <xsl:template match="attr[@tag='00180060']" mode="CTXrayDetails">
  <xsl:copy-of select="." />
 </xsl:template>
 <!-- Filter Type -->
 <xsl:template match="attr[@tag='00181160']" />
 <xsl:template match="attr[@tag='00181160']" mode="CTXrayDetails">
  <xsl:copy-of select="." />
 </xsl:template>
<!-- Generator Power(+) -->
 <xsl:template match="attr[@tag='00181170']" />
 <xsl:template match="attr[@tag='00181170']" mode="CTXrayDetails">
  <xsl:copy-of select="." />
 </xsl:template>
<!-- Focal Spot -->
 <xsl:template match="attr[@tag='00181190']" />
 <xsl:template match="attr[@tag='00181190']" mode="CTXrayDetails">
  <xsl:copy-of select="." />
 </xsl:template>
 <xsl:template match="attr" mode="CTXrayDetails" />

<!--
  Attributes taken from DB records (=> allow to differ between source images)
 -->
 <!-- Study Time -->
 <xsl:template match="attr[@tag='00080030']" />
 <!-- Study Description -->
 <xsl:template match="attr[@tag='00081030']" />
 <!-- Series Time -->
 <xsl:template match="attr[@tag='00080031']" />
 <!-- Series Description -->
 <xsl:template match="attr[@tag='0008103E']" />

 <xsl:template match="attr">
  <!-- exclude private elements -->
  <xsl:if test="translate(substring(@tag,4,1),'13579BCF','')">
   <xsl:copy-of select="." />
  </xsl:if>
 </xsl:template>
 <xsl:template match="item">
  <xsl:copy-of select="." />
 </xsl:template>
</xsl:stylesheet>
