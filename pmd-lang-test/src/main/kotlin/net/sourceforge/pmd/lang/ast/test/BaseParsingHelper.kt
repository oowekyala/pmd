/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */
package net.sourceforge.pmd.lang.ast.test

import net.sourceforge.pmd.*
import net.sourceforge.pmd.lang.Language
import net.sourceforge.pmd.lang.LanguageRegistry
import net.sourceforge.pmd.lang.LanguageVersion
import net.sourceforge.pmd.lang.LanguageVersionHandler
import net.sourceforge.pmd.lang.ast.*
import net.sourceforge.pmd.processor.AbstractPMDProcessor
import net.sourceforge.pmd.reporting.GlobalAnalysisListener
import net.sourceforge.pmd.util.IOUtil
import net.sourceforge.pmd.util.datasource.DataSource
import java.io.InputStream
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path

/**
 * Language-independent base for a parser utils class.
 * Implementations are language-specific.
 */
abstract class BaseParsingHelper<Self : BaseParsingHelper<Self, T>, T : RootNode>(
        protected val langName: String,
        private val rootClass: Class<T>,
        protected val params: Params
) {

    data class Params(
            val doProcess: Boolean,
            val defaultVerString: String?,
            val resourceLoader: Class<*>?,
            val resourcePrefix: String,
            val suppressMarker: String = PMD.SUPPRESS_MARKER,
    ) {
        companion object {

            @JvmStatic
            val default = Params(true, null, null, "")

        }
    }

    internal val resourceLoader: Class<*>
        get() = params.resourceLoader ?: javaClass

    internal val resourcePrefix: String get() = params.resourcePrefix

    /**
     * Returns the language version with the given version string.
     * If null, this defaults to the default language version for
     * this instance (not necessarily the default language version
     * defined by the language module).
     */
    fun getVersion(version: String?): LanguageVersion {
        val language = language
        return if (version == null) language.defaultVersion
        else language.getVersion(version)
            ?: throw AssertionError("Unsupported version $version for language $language")
    }

    val language: Language
        get() = LanguageRegistry.getLanguage(langName)
                ?: throw AssertionError("'$langName' is not a supported language (available ${LanguageRegistry.getLanguages()})")

    val defaultVersion: LanguageVersion
        get() = getVersion(params.defaultVerString)


    protected abstract fun clone(params: Params): Self

    @JvmOverloads
    fun withProcessing(doProcess: Boolean = true): Self =
            clone(params.copy(doProcess = doProcess))

    /**
     * Returns an instance of [Self] for which all parsing methods
     * default their language version to the provided [version]
     * If the [version] is null, then the default language version
     * defined by the language module is used instead.
     */
    fun withDefaultVersion(version: String?): Self =
            clone(params.copy(defaultVerString = version))

    /**
     * Returns an instance of [Self] for which [parseResource] uses
     * the provided [contextClass] and [resourcePrefix] to load resources.
     */
    @JvmOverloads
    fun withResourceContext(contextClass: Class<*>, resourcePrefix: String = ""): Self =
            clone(params.copy(resourceLoader = contextClass, resourcePrefix = resourcePrefix))


    fun withSuppressMarker(marker: String): Self =
            clone(params.copy(suppressMarker = marker))

    fun getHandler(version: String): LanguageVersionHandler {
        return getVersion(version).languageVersionHandler
    }

    val defaultHandler: LanguageVersionHandler
        get() = defaultVersion.languageVersionHandler


    @JvmOverloads
    fun <R : Node> getNodes(target: Class<R>, source: String, version: String? = null): List<R> =
                parse(source, version).descendants(target).crossFindBoundaries(true).toList()

    /**
     * Parses the [sourceCode] with the given [version]. This may execute
     * additional processing passes if this instance is configured to do
     * so.
     */
    @JvmOverloads
    open fun parse(
        sourceCode: String,
        version: String? = null,
        fileName: String = "src/a/test-file-name.${language.extensions[0]}"
    ): T {
        val lversion = if (version == null) defaultVersion else getVersion(version)
        val handler = lversion.languageVersionHandler
        val source = DataSource.forString(sourceCode, fileName)
        val toString = DataSource.readToString(source, StandardCharsets.UTF_8) // this removed the BOM
        val task = Parser.ParserTask(lversion, fileName, toString, SemanticErrorReporter.noop())
        task.properties.also {
            handler.declareParserTaskProperties(it)
            it.setProperty(Parser.ParserTask.COMMENT_MARKER, params.suppressMarker)
        }
        return doParse(params, task)
    }

    protected open fun doParse(params: Params, task: Parser.ParserTask): T {
        val parser = task.languageVersion.languageVersionHandler.parser
        return rootClass.cast(parser.parse(task))
    }

    /**
     * Fetches and [parse]s the [resource] using the context defined for this
     * instance (by default uses this class' classloader, but can be configured
     * with [withResourceContext]).
     */
    @JvmOverloads
    open fun parseResource(resource: String, version: String? = null): T =
        parse(readResource(resource), version, fileName = resource)

    /**
     * Fetches and [parse]s the [path].
     */
    @JvmOverloads
    open fun parseFile(path: Path, version: String? = null): T =
            parse(IOUtil.readToString(Files.newBufferedReader(path)), version, fileName = path.toAbsolutePath().toString())

    /**
     * Fetches the source of the given [clazz].
     */
    @JvmOverloads
    open fun parseClass(clazz: Class<*>, version: String? = null): T =
            parse(readClassSource(clazz), version)

    fun readResource(resourceName: String): String {

        val input = resourceLoader.getResourceAsStream(params.resourcePrefix + resourceName)
                ?: throw IllegalArgumentException("Unable to find resource file ${params.resourcePrefix + resourceName} from $resourceLoader")

        return consume(input)
    }

    private fun consume(input: InputStream) =
            IOUtil.readToString(input, StandardCharsets.UTF_8)
                    .replace(Regex("\\R"), "\n")  // normalize line-endings

    /**
     * Gets the source from the source file in which the class was declared.
     * Returns the source of the whole file even it it is not a top-level type.
     *
     * @param clazz Class to find the source for
     *
     * @return The source
     *
     * @throws IllegalArgumentException if the source file wasn't found
     */
    fun readClassSource(clazz: Class<*>): String {
        var sourceFile = clazz.name.replace('.', '/') + ".java"
        // Consider nested classes
        if (clazz.name.contains("$")) {
            sourceFile = sourceFile.substring(0, clazz.name.indexOf('$')) + ".java"
        }
        val input = (params.resourceLoader ?: javaClass).classLoader.getResourceAsStream(sourceFile)
                ?: throw IllegalArgumentException("Unable to find source file $sourceFile for $clazz")

        return consume(input)
    }


    /**
     * Execute the given [rule] on the [code]. Produce a report with the violations
     * found by the rule. The language version of the piece of code is determined by the [params].
     */
    @JvmOverloads
    fun executeRule(
        rule: Rule,
        code: String,
        fileName: String = "testfile.${language.extensions[0]}"
    ): Report {
        val config = PMDConfiguration().apply {
            suppressMarker = params.suppressMarker
            setDefaultLanguageVersion(defaultVersion)
        }

        val reportBuilder = Report.GlobalReportBuilderListener()
        val fullListener = GlobalAnalysisListener.tee(listOf(GlobalAnalysisListener.exceptionThrower(), reportBuilder))


        AbstractPMDProcessor.runSingleFile(
            listOf(RuleSet.forSingleRule(rule)),
            DataSource.forString(code, fileName),
            fullListener,
            config
        )

        fullListener.close()
        return reportBuilder.result
    }

    fun executeRuleOnResource(rule: Rule, resourcePath: String): Report =
        executeRule(rule, code = readResource(resourcePath))

    fun executeRuleOnFile(rule: Rule, path: Path): Report =
        executeRule(
            rule,
            code = Files.newBufferedReader(path).readText(),
            fileName = path.toString()
        )
}
