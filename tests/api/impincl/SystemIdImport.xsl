<?xml version='1.0'?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
  <!-- FileName: SystemIdInclude.xsl -->
  <!-- Author: shane_curcuru@lotus.com -->
  <!-- Purpose: Basic import and include tests with differen systemId's. -->

<xsl:template match="list">
  <import-list-level0>
    <xsl:apply-templates/>
  </import-list-level0>
</xsl:template>

<xsl:template match="item[@match-by='import']">
  <matched-by-import-level0>
    <xsl:value-of select="." />
  </matched-by-import-level0>
</xsl:template>

<xsl:template match="item">
  <matched-by-import-also-level0>
    <xsl:value-of select="." />
  </matched-by-import-also-level0>
</xsl:template>

</xsl:stylesheet>