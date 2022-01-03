package terminodiff.engine.resources

import ca.uhn.fhir.context.FhirContext
import ca.uhn.fhir.parser.DataFormatException
import li.flor.nativejfilechooser.NativeJFileChooser
import org.hl7.fhir.r4.model.CodeSystem
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import terminodiff.engine.concepts.ConceptDiffItem
import terminodiff.engine.graph.CodeSystemDiffBuilder
import terminodiff.engine.graph.CodeSystemGraphBuilder
import terminodiff.engine.graph.CodeSystemRole
import terminodiff.i18n.LocalizedStrings
import terminodiff.preferences.AppPreferences
import java.awt.Cursor
import java.io.File
import javax.swing.SwingWorker
import javax.swing.filechooser.FileNameExtensionFilter

val logger: Logger = LoggerFactory.getILoggerFactory().getLogger("DiffDataContainer")

class DiffDataContainer(
    var leftCodeSystem: CodeSystem? = null,
    var rightCodeSystem: CodeSystem? = null,
) {

    val isInitialized
        get() = when {
            leftCodeSystem == null -> false
            rightCodeSystem == null -> false
            else -> true
        }

    var leftGraphBuilder: CodeSystemGraphBuilder? = null
    var rightGraphBuilder: CodeSystemGraphBuilder? = null
    var codeSystemDiff: CodeSystemDiffBuilder? = null

    fun computeDiff(localizedStrings: LocalizedStrings): Boolean {
        return when {
            !isInitialized -> false
            else -> {
                leftGraphBuilder = CodeSystemGraphBuilder(leftCodeSystem!!, CodeSystemRole.LEFT)
                rightGraphBuilder = CodeSystemGraphBuilder(rightCodeSystem!!, CodeSystemRole.RIGHT)
                codeSystemDiff = buildDiff(leftGraphBuilder!!, rightGraphBuilder!!, localizedStrings)
                true
            }
        }
    }

    private fun buildDiff(
        leftGraphBuilder: CodeSystemGraphBuilder,
        rightGraphBuilder: CodeSystemGraphBuilder,
        localizedStrings: LocalizedStrings
    ): CodeSystemDiffBuilder {
        return CodeSystemDiffBuilder(leftGraphBuilder, rightGraphBuilder).build().also {
            logger.info("${it.onlyInLeftConcepts.size} code(-s) only in left: ${it.onlyInLeftConcepts.joinToString(", ")}")
            logger.info("${it.onlyInRightConcepts.size} code(-s) only in right: ${it.onlyInRightConcepts.joinToString(", ")}")
            val differentConcepts =
                it.conceptDifferences.filterValues { d -> d.conceptComparison.any { c -> c.result != ConceptDiffItem.ConceptDiffResultEnum.IDENTICAL } || d.propertyComparison.size != 0 }
            logger.info(
                "${differentConcepts.size} concept-level difference(-s): ${
                    differentConcepts.entries.joinToString(separator = "\n - ") { (key, diff) ->
                        "$key -> ${
                            diff.toString(
                                localizedStrings
                            )
                        }"
                    }
                }"
            )
        }
    }

}

class FhirLoader(private val frame: NativeJFileChooser, private val file: File, private val fhirContext: FhirContext) :
    SwingWorker<Pair<File, CodeSystem>?, Void>() {

    init {
        frame.cursor = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)
    }

    override fun doInBackground(): Pair<File, CodeSystem>? {
        return try {
            when (file.extension.lowercase()) {
                "xml" -> file to fhirContext.newXmlParser()
                    .parseResource(CodeSystem::class.java, file.reader())
                "json" -> file to fhirContext.newJsonParser()
                    .parseResource(CodeSystem::class.java, file.reader())
                else -> {
                    logger.error("The file at ${file.absolutePath} has an unsupported file type")
                    null
                }
            }
        } catch (e: DataFormatException) {
            logger.error("The file at ${file.absolutePath} could not be parsed as FHIR", e)
            null
        }
    }

    override fun done() {
        try {
            get()
        } finally {
            frame.cursor = Cursor.getDefaultCursor()
        }
    }

}

fun loadFile(title: String, fhirContext: FhirContext): Pair<File, CodeSystem>? =
    NativeJFileChooser(AppPreferences.fileBrowserDirectory).apply {
        dialogTitle = title
        isAcceptAllFileFilterUsed = false
        addChoosableFileFilter(FileNameExtensionFilter("FHIR+JSON (*.json)", "json", "JSON"))
        addChoosableFileFilter(FileNameExtensionFilter("FHIR+XML (*.xml)", "xml", "XML"))
    }.let { chooser ->
        return@let when (chooser.showOpenDialog(null)) {
            NativeJFileChooser.CANCEL_OPTION -> null
            NativeJFileChooser.APPROVE_OPTION -> {
                val selectedFile = chooser.selectedFile?.absoluteFile ?: return null
                chooser.currentDirectory.absolutePath.let {
                    AppPreferences.fileBrowserDirectory = it
                    FhirLoader(chooser, selectedFile, fhirContext).let { loader ->
                        loader.execute()
                        loader.get()
                    }
                }
            }
            else -> null
        }
    }

