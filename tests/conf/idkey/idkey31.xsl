<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

  <!-- FileName: idkey31 -->
  <!-- Document: http://www.w3.org/TR/xslt -->
  <!-- DocVersion: 19991116 -->
  <!-- Section: 12.4 Miscellaneous Additional Functions  -->
  <!-- Creator: David Marston -->
  <!-- Purpose: Test of 'generate-id()' on namespace nodes -->
  <!-- Results will vary by processor. -->

<xsl:template match="/">
  <out>
    <xsl:apply-templates/>
  </out>
</xsl:template>

<xsl:template match="*">
  <xsl:text>Namespaces for </xsl:text><xsl:value-of select="name(.)"/><xsl:text>:
</xsl:text>
  <xsl:for-each select="namespace::*">
    <xsl:value-of select="name(.)"/>
    <xsl:text>=</xsl:text>
    <xsl:value-of select="generate-id()"/>
    <xsl:text>,&#010;</xsl:text>
  </xsl:for-each>
  <xsl:apply-templates/>
</xsl:template>

</xsl:stylesheet>