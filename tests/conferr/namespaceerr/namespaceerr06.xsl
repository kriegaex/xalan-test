<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
                xmlns:baz1="http://xsl.lotus.com/ns1"
                xmlns:baz2="http://xsl.lotus.com/ns2">

  <!-- FileName: namespaceerr06 -->
  <!-- Document: http://www.w3.org/TR/xpath -->
  <!-- DocVersion: 19991116 -->
  <!-- Section: 4.1 Node Set Functions. -->
  <!-- Creator: David Marston -->
  <!-- Purpose: Test of 'local-name()' with too many arguments. -->
  <!-- ExpectedException: local-name() has too many arguments. -->
  <!-- ExpectedException: FuncLocalPart only allows 0 or 1 arguments -->

<xsl:template match="baz2:doc">
  <out>
    <xsl:value-of select="local-name(baz2:b,..)"/>
  </out>
</xsl:template>

</xsl:stylesheet>