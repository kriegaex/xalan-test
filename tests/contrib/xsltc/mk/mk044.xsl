<xsl:transform
 xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
 version="1.0"
>

  <!-- Test FileName: mk044.xsl -->
  <!-- Source Attribution: 
       This test was written by Michael Kay and is taken from 
       'XSLT Programmer's Reference' published by Wrox Press Limited in 2000;
       ISBN 1-861003-12-9; copyright Wrox Press Limited 2000; all rights reserved. 
       Now updated in the second edition (ISBN 1861005067), http://www.wrox.com.
       No part of this book may be reproduced, stored in a retrieval system or 
       transmitted in any form or by any means - electronic, electrostatic, mechanical, 
       photocopying, recording or otherwise - without the prior written permission of 
       the publisher, except in the case of brief quotations embodied in critical articles or reviews.
  -->
  <!-- Example: authors.xml, word-count.xsl -->
  <!-- Chapter/Page: 7-494 -->
  <!-- Purpose: Using normalize-space to get a word count -->

<xsl:template name="word-count">
    <xsl:param name="text"/>
    <xsl:variable name="ntext" select="normalize-space($text)"/>
    <xsl:choose>
    <xsl:when test="$ntext">        
        <xsl:variable name="remainder">
            <xsl:call-template name="word-count">
                <xsl:with-param name="text" select="substring-after($ntext, ' ')"/>
            </xsl:call-template>
        </xsl:variable>
        <xsl:value-of select="$remainder + 1"/>
    </xsl:when>    
    <xsl:otherwise>0</xsl:otherwise>
    </xsl:choose>
</xsl:template>

<xsl:template match="/">
    <xsl:for-each select="//*">
        <xsl:variable name="length">
            <xsl:call-template name="word-count">
                <xsl:with-param name="text" select="."/>
            </xsl:call-template>
        </xsl:variable>
        <element name="{name()}" words="{$length}"/>;
    </xsl:for-each>
</xsl:template>

</xsl:transform>
