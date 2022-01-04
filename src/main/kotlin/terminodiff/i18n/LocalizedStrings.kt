package terminodiff.i18n

import terminodiff.engine.concepts.ConceptDiffItem
import terminodiff.engine.concepts.ConceptDiffResult
import terminodiff.engine.metadata.MetadataDiff.MetadataDiffItemResult
import java.io.File

/**
 * we pass around an instance of localizedstrings, since we want every composable
 * to recompose when the language changes.
 */
abstract class LocalizedStrings(
    val canonicalUrl: String,
    val changeLanguage: String,
    val conceptDiff: String,
    val contact: String,
    val code: String = "Code",
    val conceptDiffResults_: (ConceptDiffItem.ConceptDiffResultEnum) -> String,
    val date: String,
    val description: String,
    val definition: String = "Definition",
    val designation: String = "Designation",
    val display: String = "Display",
    val experimental: String,
    val id: String = "ID",
    val identical: String,
    val identifiers: String,
    val jurisdiction: String,
    val language: String,
    val loadLeftFile: String,
    val loadRightFile: String,
    val graphsOpenInOtherWindows: String,
    val metadataDiff: String,
    val metadataDiffResults_: (MetadataDiffItemResult) -> String,
    val name: String = "Name",
    val noDataLoadedTitle: String,
    val numberDifferent_: (Int) -> String,
    val onlyInLeft: String,
    val onlyInRight: String,
    val overallComparison: String,
    val publisher: String,
    val property: String = "Property",
    val showAll: String,
    val showDifferent: String,
    val showIdentical: String,
    val showLeftGraphButton: String,
    val showRightGraphButton: String,
    val status: String = "Status",
    val toggleDarkTheme: String,
    val title: String,
    val terminoDiff: String = "TerminoDiff",
    val version: String = "Version",
    val viewGraphTitle: String,
    val leftFileOpenFilename_: (File) -> String,
    val rightFileOpenFilename_: (File) -> String,
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
    conceptDiffResults_ = {
        when (it) {
            ConceptDiffItem.ConceptDiffResultEnum.DIFFERENT -> "Unterschiedlich"
            ConceptDiffItem.ConceptDiffResultEnum.IDENTICAL -> "Identisch"
        }
    },
    contact = "Kontakt",
    date = "Datum",
    description = "Beschreibung",
    experimental = "Experimentell?",
    identifiers = "IDs",
    identical = "Identisch",
    jurisdiction = "Jurisdiktion",
    language = "de",
    loadLeftFile = "Linke Datei laden",
    loadRightFile = "Rechte Datei laden",
    graphsOpenInOtherWindows = "Graphen öffnen sich in einem neuen Fenster.",
    metadataDiff = "Metadaten-Diff",
    metadataDiffResults_ = {
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
    numberDifferent_ = { "$it unterschiedlich" },
    onlyInLeft = "Nur links",
    onlyInRight = "Nur rechts",
    overallComparison = "Gesamt",
    publisher = "Herausgeber",
    showAll = "Alle anzeigen",
    showDifferent = "Nur unterschiedliche",
    showIdentical = "Nur identische",
    showLeftGraphButton = "Linken Graphen zeigen",
    showRightGraphButton = "Rechten Graphen zeigen",
    viewGraphTitle = "Graph anzeigen",
    title = "Titel",
    toggleDarkTheme = "Helles/Dunkles Thema",
    leftFileOpenFilename_ = { file -> "Linke Datei geöffnet: ${file.absolutePath}" },
    rightFileOpenFilename_ = { file -> "Rechte Datei geöffnet: ${file.absolutePath}" }
)

class EnglishStrings : LocalizedStrings(
    canonicalUrl = "Canonical URL",
    changeLanguage = "Change Language",
    conceptDiff = "Concept Diff",
    conceptDiffResults_ = {
        when (it) {
            ConceptDiffItem.ConceptDiffResultEnum.DIFFERENT -> "Different"
            ConceptDiffItem.ConceptDiffResultEnum.IDENTICAL -> "Identical"
        }
    },
    contact = "Contact",
    date = "Date",
    description = "Description",
    experimental = "Experimental?",
    identical = "Identical",
    identifiers = "Identifiers",
    jurisdiction = "Jurisdiction",
    language = "en",
    loadLeftFile = "Load left file",
    loadRightFile = "Load right file",
    graphsOpenInOtherWindows = "Graphs open in another window.",
    metadataDiff = "Metadata Diff",
    metadataDiffResults_ = {
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
    numberDifferent_ = { "$it different" },
    onlyInLeft = "Only left",
    onlyInRight = "Only right",
    overallComparison = "Overall",
    publisher = "Publisher",
    showAll = "Show all",
    showIdentical = "Only identical",
    showDifferent = "Only different",
    showLeftGraphButton = "Show left graph",
    showRightGraphButton = "Show right graph",
    title = "Title",
    toggleDarkTheme = "Toggle dark theme",
    viewGraphTitle = "View graph",
    leftFileOpenFilename_ = { file -> "Left file open: ${file.absolutePath}" },
    rightFileOpenFilename_ = { file -> "Right file open: ${file.absolutePath}" },
)

val defaultStrings = getStrings()

fun getStrings(locale: SupportedLocale = SupportedLocale.getDefaultLocale()): LocalizedStrings =
    when (locale) {
        SupportedLocale.DE -> GermanStrings()
        SupportedLocale.EN -> EnglishStrings()
    }
