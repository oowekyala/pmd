<!-- MIGRATION STYLESHEET -->
<!-- Converts an old ruleset v2.0.0 to the new schema -->
<!-- Currently maps XPath rules to a new representation -->

<xsl:stylesheet version="2.0"
                xmlns="http://pmd.sourceforge.net/ruleset/2.0.0"
                xmlns:m="http://pmd.sourceforge.net/ruleset/2.0.0"
                xmlns:mf="localFunctions"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                exclude-result-prefixes="m mf xs"> <!-- Excludes the xmlns declarations from the output -->

    <xsl:output cdata-section-elements="m:example"/>
    <xsl:output cdata-section-elements="m:xpath"/>
    <xsl:output method="xml" indent="yes"/>

    <!-- Local function definitions -->

    <xsl:function name="mf:trim" as="xs:string">
        <xsl:param name="str" as="xs:string"/>
        <xsl:sequence select="replace($str, '^\s*(.+?)\s*$', '$1')"/>
    </xsl:function>

    <xsl:function name="mf:get-property-internal" as="xs:string">
        <xsl:param name="prop" as="element(m:property)*"/>
        <xsl:choose>
            <xsl:when test="empty($prop)">
                <xs:empty-sequence/>
            </xsl:when>
            <xsl:when test="$prop/m:value">
                <xsl:value-of select="$prop/m:value/text()"/>
            </xsl:when>
            <xsl:when test="$prop/@value">
                <xsl:value-of select="$prop/@value"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$prop/text()"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:function>

    <!-- Function that retrieves the value of a property (accounting for the different syntaxes) -->
    <xsl:function name="mf:get-property" as="xs:string">
        <xsl:param name="prop" as="element(m:property)*"/>
        <xsl:value-of select="mf:trim(mf:get-property-internal($prop))"/>
    </xsl:function>

    <!-- Match XPath rules  -->
    <xsl:template match="m:rule[@class='net.sourceforge.pmd.lang.rule.XPathRule']">
        <xsl:param name="xpath-text" select="mf:get-property(m:properties/m:property[@name='xpath'])"/>
        <xsl:param name="xpath-version" select="mf:get-property(m:properties/m:property[@name='version'])"/>

        <!-- The rule tag is replaced with this xpath-rule  -->
        <rule-def>

            <!-- Copy attributes in standard order -->
       
            <xsl:copy-of select="@name"/>
            <xsl:copy-of select="@language"/>
            <xsl:copy-of select="@since"/>
            <xsl:copy-of select="@message"/>
            <xsl:copy-of select="@externalInfoUrl"/>
            <xsl:copy-of select="@*[name()!=('name', 'language', 'since', 'message', 'externalInfoUrl')]"/>

            <!-- Copy the description and priority elements -->
            <xsl:apply-templates select="./*[not(self::m:example or self::m:properties)]"/>

            <!-- Add a new xpath tag -->
            <xpath>
                <!-- Add the version attribute if it was specified -->
                <!-- This will need to be adjusted if we change the default XPath version -->
                <xsl:choose>
                    <xsl:when test="not($xpath-version)"/>
                    <xsl:otherwise>
                        <xsl:attribute name="version">
                            <xsl:value-of select="$xpath-version"/>
                        </xsl:attribute>
                    </xsl:otherwise>
                </xsl:choose>
                <!-- Add the XPath expression as the child of this node -->
                <xsl:value-of select="$xpath-text"/>
            </xpath>

            <!-- Add the other properties below the XPath expression -->
            <xsl:apply-templates select="./m:properties"/>
            <!-- Finally add examples -->
            <xsl:apply-templates select="./m:example"/>
        </rule-def>

    </xsl:template>

    <!-- Template for the properties of XPath rules -->
    <xsl:template match="m:properties[../self::m:rule[@class='net.sourceforge.pmd.lang.rule.XPathRule']]">
        <xsl:choose>
            <!-- If the only properties are xpath and/or version, then remove the properties element altogether -->
            <xsl:when test="every $p in ./m:property satisfies $p/@name='xpath' or $p/@name='version'"/>
            <!-- Otherwise copy the properties, excluding these two -->
            <xsl:otherwise>
                <properties>
                    <xsl:copy-of select="./m:property[@name!='xpath' and @name!='version']"/>
                </properties>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <!-- Keep the formatting of examples -->
    <xsl:template match="m:example/text()">
        <xsl:value-of select="."/>
    </xsl:template>

    <!-- Identity for the rest of the nodes -->
    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>
</xsl:stylesheet>