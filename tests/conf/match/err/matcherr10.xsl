<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

  <!-- FileName: MATCHerr10 -->
  <!-- Document: http://www.w3.org/TR/xslt -->
  <!-- DocVersion: 19991116 -->
  <!-- Section: 5.2 -->
  <!-- Creator: David Marston -->
  <!-- Purpose: Test variable in a match pattern -->
  <!-- ExpectedException: Extra illegal tokens -->

<xsl:variable name="screen" select="'section1'"/>

<xsl:template match="defaultcontent">
  <out>
    <xsl:apply-templates/>
  </out>
</xsl:template>

<xsl:template match="$screen">
  <xsl:value-of select="name(.)"/>
</xsl:template>

</xsl:stylesheet>