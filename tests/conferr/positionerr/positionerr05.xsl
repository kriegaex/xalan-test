<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

  <!-- FileName: positionerr05 -->
  <!-- Document: http://www.w3.org/TR/xpath -->
  <!-- DocVersion: 19991116 -->
  <!-- Section: 4.1 Node Set Functions -->
  <!-- Creator: David Marston -->
  <!-- Purpose: last() should not have any arguments. -->
  <!-- ExpectedException: zero arguments expected  -->
  <!-- ExpectedException: FuncLast only allows 0 arguments -->

<xsl:template match="doc">
  <out>
    <xsl:value-of select="position()=last(a)"/>
  </out>
</xsl:template>

</xsl:stylesheet>