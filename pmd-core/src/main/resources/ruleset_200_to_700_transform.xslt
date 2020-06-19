<!-- MIGRATION STYLESHEET -->
<!-- Converts an old ruleset v2.0.0 to the new schema -->
<!-- Doesn't handle rule refs yet -->

<xsl:stylesheet version="2.0"
                xmlns="https://pmd-code.org/ruleset/7.0.0"
                xmlns:m="http://pmd.sourceforge.net/ruleset/2.0.0"
                xmlns:mf="localFunctions"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                exclude-result-prefixes="m mf xs"> <!-- Excludes the xmlns declarations from the output -->

    <xsl:output cdata-section-elements="example expr" method="xml" indent="yes"/>

    <xsl:variable name="newNs">https://pmd-code.org/ruleset/7.0.0</xsl:variable>

    <!-- Local function definitions -->

    <xsl:function name="mf:trim" as="xs:string">
        <xsl:param name="str" as="xs:string"/>
        <xsl:sequence select="replace($str, '^\s*(.+?)\s*$', '$1')"/>
    </xsl:function>

    <xsl:function name="mf:xpath_10_to_20_automigrate" as="xs:string">
        <xsl:param name="str" as="xs:string"/>
        <!-- TODO -->
        <xsl:sequence select="$str"/>
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

    <xsl:function name="mf:get-named-property" as="xs:string?">
        <xsl:param name="ctx" as="element(m:rule)"/>
        <xsl:param name="name" as="xs:string"/>
        <xsl:value-of select="mf:get-property($ctx/m:properties/m:property[@name=$name])"/>
    </xsl:function>

    <xsl:function name="mf:is-ignored-xp" as="xs:string">
        <xsl:param name="prop" as="element(m:property)*"/>
        <xsl:value-of select="mf:trim(mf:get-property-internal($prop))"/>
    </xsl:function>

    <xsl:function name="mf:priority-to-symbolic" as="xs:string">
        <xsl:param name="p" as="xs:integer?"/>
        <xsl:choose>
            <xsl:when test="$p=1">highest</xsl:when>
            <xsl:when test="$p=2">high</xsl:when>
            <xsl:when test="$p=3">medium</xsl:when>
            <xsl:when test="$p=4">low</xsl:when>
            <xsl:when test="$p=5">lowest</xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$p"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:function>

    <!-- Match rule defs  -->
    <xsl:template match="m:rule[@class]">
        <rule-def>
            <name>
                <xsl:value-of select="@name"/>
            </name>
            <message>
                <xsl:value-of select="@message"/>
            </message>
            <xsl:apply-templates select="m:description"/>
            <since>
                <xsl:value-of select="@since"/>
            </since>
            <xsl:if test="@deprecated">
                <deprecated/>
            </xsl:if>
            <xsl:if test="./priority/text()">
                <priority>
                    <xsl:value-of select="mf:priority-to-symbolic(priority/text())"/>
                </priority>
            </xsl:if>

            <xsl:call-template name="suppression-props"/>

            <impl>
                <language>
                    <xsl:attribute name="id">
                        <xsl:choose>
                            <xsl:when test="@language">
                                <xsl:value-of select="@language"/>
                            </xsl:when>
                            <xsl:otherwise>java</xsl:otherwise>
                        </xsl:choose>
                    </xsl:attribute>
                </language>

                <xsl:choose>
                    <!-- XPath rules                   -->
                    <xsl:when test="self::m:rule[@class='net.sourceforge.pmd.lang.rule.XPathRule']">
                        <xsl:variable name="xpath-text" select="mf:get-named-property(., 'xpath')"/>
                        <xsl:variable name="xpath-version" select="mf:get-named-property(., 'version')"/>
                        <xpath>
                            <expr>
                                <xsl:value-of select="$xpath-text"/>
                            </expr>

                            <!-- Property defs -->
                            <xsl:if test="some $p in ./m:properties/m:property satisfies $p/@type">
                                <property-defs>
                                    <xsl:apply-templates select="./m:properties/m:property[@type]"/>
                                </property-defs>
                            </xsl:if>
                        </xpath>
                    </xsl:when>
                    <xsl:otherwise>
                        <!-- A java rule -->
                        <class>
                            <xsl:value-of select="@class"/>
                        </class>
                    </xsl:otherwise>
                </xsl:choose>
            </impl>

            <!-- Add the other properties below the XPath expression -->
            <xsl:apply-templates select="./m:properties"/>

            <xsl:if test="m:example">
                <examples>
                    <xsl:apply-templates select="m:example"/>
                </examples>
            </xsl:if>
        </rule-def>

    </xsl:template>

    <xsl:template name="suppression-props" match="m:rule">
        <xsl:param name="vregex" select="mf:get-named-property(., 'violationSuppressRegex')"/>
        <xsl:param name="vxpath" select="mf:get-named-property(., 'violationSuppressXPath')"/>
        <xsl:if test="$vregex or $vxpath">
            <suppressions>
                <xsl:if test="$vregex">
                    <regex>
                        <xsl:value-of select="$vregex"/>
                    </regex>
                </xsl:if>
                <xsl:if test="$vxpath">
                    <xpath>
                        <xsl:value-of select="$vxpath"/>
                    </xpath>
                </xsl:if>
            </suppressions>
        </xsl:if>
    </xsl:template>

    <xsl:template name="property-overrides" match="m:properties">
        <xsl:param name="props"
                   select="m:property[not(@type) and not(@name=('xpath', 'version', 'violationSuppressRegex', 'violationSuppressXPath'))]"/>
        <xsl:if test="$props">
            <!-- Otherwise nothing is output, no actual properties -->
            <properties>
                <xsl:for-each select="$props">
                    <xsl:call-template name="xp-rule-override"/>
                </xsl:for-each>
            </properties>
        </xsl:if>
    </xsl:template>

    <!-- XPath rule property def -->
    <xsl:template name="xp-rule-def" match="m:property[@type]">
        <property-def>
            <xsl:copy-of select="@name"/>
            <type>
                <xsl:value-of select="@type"/>
            </type>
            <description><xsl:value-of select="@description"/></description>
            <default-value>
                <value>
                    <xsl:value-of select="mf:get-property-internal(.)"/>
                </value>
            </default-value>
        </property-def>
    </xsl:template>

    <!-- Rule override def -->
    <xsl:template name="xp-rule-override" match="m:property">
        <property>
            <xsl:copy-of select="@name"/>
            <value>
                <xsl:value-of select="mf:get-property-internal(.)"/>
            </value>
        </property>
    </xsl:template>


    <xsl:template match="m:ruleset">
        <xsl:element name="ruleset"
                     namespace="{$newNs}">

            <xsl:attribute name="schemaLocation" namespace="http://www.w3.org/2001/XMLSchema-instance"><![CDATA[https://pmd-code.org/ruleset/7.0.0 file:///home/clifrr/Documents/Git/pmd/pmd-core/src/main/resources/ruleset_700.xsd]]></xsl:attribute>

            <name>
                <xsl:value-of select="@name"/>
            </name>
            <xsl:apply-templates/>
        </xsl:element>

    </xsl:template>

    <!-- Identity for elements, which doesn't copy namespaces -->
    <xsl:template match="m:*">
        <xsl:element name="{local-name()}" namespace="{$newNs}">
            <xsl:apply-templates select="node()" />
        </xsl:element>
    </xsl:template>

    <!-- Identity for the rest of the nodes -->
    <xsl:template match="@*|node()">
        <xsl:copy copy-namespaces="no">
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>
</xsl:stylesheet>