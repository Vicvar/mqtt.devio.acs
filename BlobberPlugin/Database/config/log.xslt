<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:output method="html"/>
	<!-- Match the document root -->
	<xsl:template match="/">
		<html>
			<head>
                <script language="JavaScript">
				function makeObjectPointer(objectId)
				{
					var theObj;
				    if (typeof objectId == "string")
				    {
				    	if(document.getElementById &amp;&amp; document.getElementById(objectId))
						{
							theObj = document.getElementById(objectId);
						}
						else if (document.all &amp;&amp; document.all(objectId))
						{
							theObj = document.all(objectId);
						}
					    else if (document.layers &amp;&amp; document.layers[objectId])
					    {
							theObj = document.layers[objectId];
					    }
					}
					else
					{
						theObj = objectId;
					}
					return theObj;
				}
				
				// flip return the alternate state given the current one
				function toggle(objectId)
				{
					objectRef=makeObjectPointer(objectId);							
					(objectRef.className=="expand") ? objectRef.className="collapse" : objectRef.className="expand";	
				}
				</script>
                <style type="text/css">
                  tr.head {background-color: grey}
                  .collapse { display: none }
                  .expand { }
                </style>
			</head>
			<body>
			    <table class="page"><tr><td width="800"><div align="justify">
			    <xsl:apply-templates select="//module"/>
			    </div></td></tr></table>
			</body>	
		</html>
	</xsl:template>
	
	<!-- match entity -->
	<xsl:template match="module">
		<h2><xsl:value-of select="@name"/></h2>
		<xsl:apply-templates select="log"/>
	</xsl:template>
	
	<xsl:template match="log">
	    <table width="100%">
	        <tr>
	            <td rowspan="3" valign="top" width="20"><xsl:value-of select="@id"/></td>
	            <td><xsl:value-of select="@message"/></td>
	        </tr>
	        <tr>
	            <td><xsl:value-of select="comments"/></td>
	        </tr>
	        <tr>
	            <td>
	                <xsl:value-of select="@packageName"/>
	                <xsl:text> </xsl:text>
	                <xsl:value-of select="@fileName"/>
	                <xsl:text> </xsl:text>
	                <xsl:value-of select="@line"/>
	                
	                <a class="head" href="#">
						<xsl:attribute name="onClick">
							<xsl:text>toggle(&apos;</xsl:text>
							<xsl:value-of select="@id"/>
							<xsl:text>&apos;);return false;</xsl:text>
						</xsl:attribute>
						code
					</a>
	            </td>
	        </tr>
	        <tr id="{@id}" class="collapse">
	            <td colspan="2">
	                <pre>
<xsl:value-of select="code"/>
	                </pre>
	            </td>
	        </tr>
	    </table>
	    <br/>
	</xsl:template>
</xsl:stylesheet>