<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

  <!-- FileName: selecterr16 -->
  <!-- Document: http://www.w3.org/TR/xslt -->
  <!-- DocVersion: 19991116 -->
  <!-- Section: 5.4 -->
  <!-- Creator: David Marston -->
  <!-- Purpose: Try to use a string where a node-set is needed in apply-templates.-->
  <!-- ExpectedException: XPATH: Can not convert #UNKNOWN to a NodeList -->
  <!-- ExpectedException: Can not convert #STRING to a NodeList! -->

<xsl:variable name="which" select="'fish'"/>

<xsl:template match="/doc">
  <out>
    <xsl:apply-templates select="$which"/>
  </out>
</xsl:template>

<xsl:template match="*">
  <xsl:text>.</xsl:text>
</xsl:template>

</xsl:stylesheet>