<?xml version="1.0"?> 
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0" 
                xmlns:ns1="ns1">

  <!-- FileName: WHTE07 -->
  <!-- Document: http://www.w3.org/TR/xslt -->
  <!-- DocVersion: 19990922 -->
  <!-- Section: 3.4 -->
  <!-- Purpose: Test for element specifier that is a wildcard but qualified by a namespace. -->

<xsl:strip-space elements="ns1:*"/>
  
<xsl:template match="doc">
  <out>
    <xsl:apply-templates select="*"/>
  </out>
</xsl:template>
   
</xsl:stylesheet>
