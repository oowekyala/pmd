package net.sourceforge.pmd.lang.xpath.ast

import io.kotlintest.specs.FunSpec

/**
 * @author Clément Fournier
 * @since 6.7.0
 */
class ParserTests : FunSpec({


    parserTest("Test nested comments") {
        parseXPathRoot("(: (: we're nested :) :) //hello  ")
    }

})