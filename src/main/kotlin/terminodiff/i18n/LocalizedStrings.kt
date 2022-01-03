package terminodiff.i18n

import terminodiff.engine.concepts.ConceptDiffItem
import terminodiff.engine.concepts.ConceptDiffResult
import terminodiff.engine.metadata.MetadataDiff.MetadataDiffItemResult
import java.io.File

/**
 * we pass around an instance of localizedstrings, since we want every composable to recompose when the language changes.
 */
abstract class LocalizedStrings(
    val canonicalUrl: String,
    val changeLanguage: String,
    val conceptDiff: String,
    val contact: String,
    val code: String = "Code",
    val `conceptDiffResults$`: (ConceptDiffResult) -> String,
    val date: String,
    val description: String,
    val definition: String = "Definition",
    val designation: String = "Designation",
    val display: String = "Display",
    val experimental: String,
    val id: String = "ID",
    val identifiers: String,
    val jurisdiction: String,
    val language: String,
    val loadLeftFile: String,
    val loadRightFile: String,
    val graphsOpenInOtherWindows: String,
    val metadataDiff: String,
    val `metadataDiffResults$`: (MetadataDiffItemResult) -> String,
    val name: String = "Name",
    val noDataLoadedTitle: String,
    val publisher: String,
    val property: String = "Property",
    val status: String = "Status",
    val terminoDiff: String = "TerminoDiff",
    val title: String,
    val version: String = "Version",
    val viewGraphTitle: String,
    val showLeftGraphButton: String,
    val showRightGraphButton: String,
    val toggleDarkTheme: String,
    val `leftFileOpenFilename$`: (File) -> String,
    val `rightFileOpenFilename$`: (File) -> String,
)

enum class SupportedLocale {
    EN,
    DE;

    companion object {
        fun getDefaultLocale() = EN
    }
}

class GermanStrings : LocalizedStrings(
    canonicalUrl = "Kanonische URL",
    changeLanguage = "Sprache wechseln",
    conceptDiff = "Konzept-Diff",
    `conceptDiffResults$` = {
        when (it.result) {
            ConceptDiffItem.ConceptDiffResultEnum.DIFFERENT -> "Unterschiedlich"
            ConceptDiffItem.ConceptDiffResultEnum.IDENTICAL -> "Identisch"
        }
    },
    contact = "Kontakt",
    date = "Datum",
    description = "Beschreibung",
    experimental = "Experimentell?",
    identifiers = "IDs",
    jurisdiction = "Jurisdiktion",
    language = "de",
    loadLeftFile = "Linke Datei laden",
    loadRightFile = "Rechte Datei laden",
    graphsOpenInOtherWindows = "Graphen öffnen sich in einem neuen Fenster.",
    metadataDiff = "Metadaten-Diff",
    `metadataDiffResults$` = {
        when (it) {
            MetadataDiffItemResult.BOTH_EMPTY -> "beide leer"
            MetadataDiffItemResult.DIFFERENT -> "unterschiedlich"
            MetadataDiffItemResult.IDENTICAL -> "identisch"
            MetadataDiffItemResult.DIFFERENT_COUNT -> "unterschiedliche Anzahl"
            MetadataDiffItemResult.BOTH_NULL -> "beide Null"
            MetadataDiffItemResult.DIFFERENT_TEXT -> "unterschiedlicher Text"
        }
    },
    noDataLoadedTitle = "Keine Daten geladen",
    publisher = "Herausgeber",
    title = "Titel",
    viewGraphTitle = "Graph anzeigen",
    showLeftGraphButton = "Linken Graphen zeigen",
    showRightGraphButton = "Rechten Graphen zeigen",
    toggleDarkTheme = "Helles/Dunkles Thema",
    `leftFileOpenFilename$` = { file -> "Linke Datei geöffnet: ${file.absolutePath}" },
    `rightFileOpenFilename$` = { file -> "Rechte Datei geöffnet: ${file.absolutePath}" }
)

class EnglishStrings : LocalizedStrings(
    canonicalUrl = "Canonical URL",
    changeLanguage = "Change Language",
    conceptDiff = "Concept Diff",
    `conceptDiffResults$` = {
        when (it.result) {
            ConceptDiffItem.ConceptDiffResultEnum.DIFFERENT -> "Different"
            ConceptDiffItem.ConceptDiffResultEnum.IDENTICAL -> "Identical"
        }
    },
    contact = "Contact",
    date = "Date",
    description = "Description",
    experimental = "Experimental?",
    identifiers = "Identifiers",
    jurisdiction = "Jurisdiction",
    language = "en",
    loadLeftFile = "Load left file",
    loadRightFile = "Load right file",
    graphsOpenInOtherWindows = "Graphs open in another window.",
    metadataDiff = "Metadata Diff",
    `metadataDiffResults$` = {
        when (it) {
            MetadataDiffItemResult.BOTH_EMPTY -> "both empty"
            MetadataDiffItemResult.DIFFERENT -> "different"
            MetadataDiffItemResult.IDENTICAL -> "identical"
            MetadataDiffItemResult.DIFFERENT_COUNT -> "different count"
            MetadataDiffItemResult.BOTH_NULL -> "both null"
            MetadataDiffItemResult.DIFFERENT_TEXT -> "different text"
        }
    },
    noDataLoadedTitle = "No data loaded",
    publisher = "Publisher",
    title = "Title",
    viewGraphTitle = "View graph",
    showLeftGraphButton = "Show left graph",
    showRightGraphButton = "Show right graph",
    toggleDarkTheme = "Toggle dark theme",
    `leftFileOpenFilename$` = { file -> "Left file open: ${file.absolutePath}" },
    `rightFileOpenFilename$` = { file -> "Right file open: ${file.absolutePath}" },
)

val defaultStrings = getStrings()

fun getStrings(locale: SupportedLocale = SupportedLocale.getDefaultLocale()): LocalizedStrings =
    when (locale) {
        SupportedLocale.DE -> GermanStrings()
        SupportedLocale.EN -> EnglishStrings()
    }
