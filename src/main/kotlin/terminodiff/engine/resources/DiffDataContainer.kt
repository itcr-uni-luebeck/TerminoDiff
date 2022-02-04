package terminodiff.engine.resources

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import ca.uhn.fhir.context.FhirContext
import ca.uhn.fhir.parser.DataFormatException
import org.hl7.fhir.r4.model.CodeSystem
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import terminodiff.engine.concepts.ConceptDiffItem
import terminodiff.engine.graph.CodeSystemDiffBuilder
import terminodiff.engine.graph.CodeSystemGraphBuilder
import terminodiff.i18n.LocalizedStrings
import terminodiff.terminodiff.engine.resources.InputResource
import java.util.*

private val logger: Logger = LoggerFactory.getILoggerFactory().getLogger("DiffDataContainer")

class DiffDataContainer(private val fhirContext: FhirContext, strings: LocalizedStrings) {

    var localizedStrings by mutableStateOf(strings)
    var loadState: UUID by mutableStateOf(UUID.randomUUID())
    //var leftFilename: File? by mutableStateOf(null)
    //var rightFilename: File? by mutableStateOf(null)
    var leftResource: InputResource? by mutableStateOf(null)
    var rightResource: InputResource? by mutableStateOf(null)

    //all other properties are dependent and flow down from the filename changes
    val leftCodeSystem: CodeSystem? by derivedStateOf { loadCodeSystemResource(leftResource, Side.LEFT) }
    val rightCodeSystem: CodeSystem? by derivedStateOf { loadCodeSystemResource(rightResource, Side.RIGHT) }
    val leftGraphBuilder: CodeSystemGraphBuilder? by derivedStateOf { buildCsGraph(leftCodeSystem) }
    val rightGraphBuilder: CodeSystemGraphBuilder? by derivedStateOf { buildCsGraph(rightCodeSystem) }

    val codeSystemDiff: CodeSystemDiffBuilder? by derivedStateOf {
        buildDiff(
            leftGraphBuilder, rightGraphBuilder, localizedStrings
        )
    }

    fun reload() {
        loadState = UUID.randomUUID()
    }

    enum class Side {
        LEFT, RIGHT
    }

    private fun loadCodeSystemResource(resource: InputResource?, side: Side): CodeSystem? {
        if (resource == null) return null
        when (resource.kind) {
            InputResource.Kind.FILE -> {
                val file = resource.localFile!!
                logger.info("Loading $side ${resource.kind} resource from ${file.absolutePath}")
                return try {
                    when (file.extension.lowercase()) {
                        "xml" -> fhirContext.newXmlParser().parseResource(CodeSystem::class.java, file.reader())
                        "json" -> fhirContext.newJsonParser().parseResource(CodeSystem::class.java, file.reader())
                        else -> {
                            logger.error("The file at ${file.absolutePath} has an unsupported file type")
                            null
                        }
                    }.also {
                        if (it != null) {
                            logger.info("Loaded $side CodeSystem with URL ${it.url} and version '${it.version}', state = $loadState")
                        }
                    }
                } catch (e: DataFormatException) {
                    logger.error("The file at ${file.absolutePath} could not be parsed as FHIR", e)
                    null
                }
            }
            else -> TODO()
        }
    }

    private fun buildCsGraph(codeSystem: CodeSystem?): CodeSystemGraphBuilder? = when (codeSystem) {
        null -> null
        else -> CodeSystemGraphBuilder(codeSystem, localizedStrings)
    }

    private fun buildDiff(
        leftGraphBuilder: CodeSystemGraphBuilder?,
        rightGraphBuilder: CodeSystemGraphBuilder?,
        localizedStrings: LocalizedStrings,
    ): CodeSystemDiffBuilder? {
        if (leftGraphBuilder == null || rightGraphBuilder == null) return null
        logger.info("building diff")
        return CodeSystemDiffBuilder(leftGraphBuilder, rightGraphBuilder, localizedStrings).build().also {
            logger.info(
                "${it.onlyInLeftConcepts.size} code(-s) only in left: ${
                    it.onlyInLeftConcepts.joinToString(
                        separator = ", ", limit = 50
                    )
                }"
            )
            logger.info(
                "${it.onlyInRightConcepts.size} code(-s) only in right: ${
                    it.onlyInRightConcepts.joinToString(
                        separator = ", ", limit = 50
                    )
                }"
            )
            val differentConcepts =
                it.conceptDifferences.filterValues { d -> d.conceptComparison.any { c -> c.result != ConceptDiffItem.ConceptDiffResultEnum.IDENTICAL } || d.propertyComparison.size != 0 }
            logger.debug("${differentConcepts.size} concept-level difference(-s): ${
                differentConcepts.entries.joinToString(separator = "\n - ") { (key, diff) ->
                    "$key -> ${
                        diff.toString(
                            localizedStrings
                        )
                    }"
                }
            }")
        }
    }
}

