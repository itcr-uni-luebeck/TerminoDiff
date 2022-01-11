package terminodiff.engine.resources

import androidx.compose.ui.window.FrameWindowScope
import ca.uhn.fhir.context.FhirContext
import ca.uhn.fhir.parser.DataFormatException
import li.flor.nativejfilechooser.NativeJFileChooser
import org.apache.commons.lang3.SystemUtils
import org.hl7.fhir.r4.model.CodeSystem
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import terminodiff.engine.concepts.ConceptDiffItem
import terminodiff.engine.graph.CodeSystemDiffBuilder
import terminodiff.engine.graph.CodeSystemGraphBuilder
import terminodiff.i18n.LocalizedStrings
import terminodiff.preferences.AppPreferences
import java.awt.Cursor
import java.io.File
import javax.swing.JFileChooser
import javax.swing.SwingWorker
import javax.swing.filechooser.FileNameExtensionFilter

private val logger: Logger = LoggerFactory.getILoggerFactory().getLogger("DiffDataContainer")

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
                leftGraphBuilder = CodeSystemGraphBuilder(leftCodeSystem!!)
                rightGraphBuilder = CodeSystemGraphBuilder(rightCodeSystem!!)
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
        logger.info("building diff")
        return CodeSystemDiffBuilder(leftGraphBuilder, rightGraphBuilder).build().also {
            logger.info("${it.onlyInLeftConcepts.size} code(-s) only in left: ${it.onlyInLeftConcepts.joinToString(", ")}")
            logger.info("${it.onlyInRightConcepts.size} code(-s) only in right: ${it.onlyInRightConcepts.joinToString(", ")}")
            val differentConcepts =
                it.conceptDifferences.filterValues { d -> d.conceptComparison.any { c -> c.result != ConceptDiffItem.ConceptDiffResultEnum.IDENTICAL } || d.propertyComparison.size != 0 }
            logger.debug(
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

class FhirLoader(private val frame: FrameWindowScope, private val file: File, private val fhirContext: FhirContext) :
    SwingWorker<Pair<File, CodeSystem>?, Void>() {

    init {
        frame.window.cursor = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)
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
            frame.window.cursor = Cursor.getDefaultCursor()
        }
    }
}

private fun getFileChooser(title: String): JFileChooser {
    return when (SystemUtils.IS_OS_MAC) {
        // NativeJFileChooser hangs on Azul Zulu 11 + JavaFX on macOS 12.1 aarch64.
        // with Azul Zulu w/o JFX, currently the file browser does not work at all on a M1 MBA.
        // hence, the non-native file chooser is used instead.
        true -> JFileChooser(AppPreferences.fileBrowserDirectory)
        else -> NativeJFileChooser(AppPreferences.fileBrowserDirectory)
    }.apply {
        dialogTitle = title
        isAcceptAllFileFilterUsed = false
        addChoosableFileFilter(FileNameExtensionFilter("FHIR+JSON (*.json)", "json", "JSON"))
        addChoosableFileFilter(FileNameExtensionFilter("FHIR+XML (*.xml)", "xml", "XML"))
    }
}

fun loadFile(title: String, fhirContext: FhirContext, frameWindow: FrameWindowScope): Pair<File, CodeSystem>? =
    getFileChooser(title).let { chooser ->
        return@let when (chooser.showOpenDialog(null)) {
            NativeJFileChooser.CANCEL_OPTION -> null
            NativeJFileChooser.APPROVE_OPTION -> {
                val selectedFile = chooser.selectedFile?.absoluteFile ?: return null
                AppPreferences.fileBrowserDirectory = selectedFile.parentFile.absolutePath
                FhirLoader(frameWindow, selectedFile, fhirContext).let { loader ->
                    loader.execute()
                    logger.info("loaded file at ${selectedFile.absolutePath}")
                    loader.get()
                }
            }
            else -> null
        }
    }

