<xsl:stylesheet version="1.0"
                      xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                      xmlns:out="output.xsl">

  <!-- Test FileName: mk024.xsl -->
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
  <!-- Example: any xml, alias.xsl -->
  <!-- Chapter/Page: 4-236 -->
  <!-- Purpose: Example of namespace alias -->

<xsl:param name="variable-name">v</xsl:param>
<xsl:param name="default-value"/>
<xsl:output indent="yes"/>
<xsl:namespace-alias
               stylesheet-prefix="out"
               result-prefix="xsl"/>

<xsl:template match="/">
   <out:stylesheet version="1.0">
   <out:variable name="{$variable-name}">
      <xsl:value-of select="$default-value"/>
   </out:variable>
   </out:stylesheet>
</xsl:template>


</xsl:stylesheet>
