package terminodiff.i18n

import terminodiff.engine.metadata.MetadataDiff.MetadataDiffItemResult

abstract class LocalizedStrings(
    val canonicalUrl: String,
    val changeLanguage: String,
    val contact: String,
    val date: String,
    val description: String,
    val experimental: String,
    val id: String = "ID",
    val identifiers: String,
    val jurisdiction: String,
    val language: String,
    val loadLeftFile: String,
    val loadRightFile: String,
    val graphsOpenInOtherWindows: String,
    val metadataDiffResults: (MetadataDiffItemResult) -> String,
    val name: String = "Name",
    val publisher: String,
    val status: String = "Status",
    val terminoDiff: String = "TerminoDiff",
    val title: String,
    val version: String = "Version",
    val viewGraphTitle: String,
    val showLeftGraphButton: String,
    val showRightGraphButton: String,
    val toggleDarkTheme: String
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
    metadataDiffResults = {
        when (it) {
            MetadataDiffItemResult.BOTH_EMPTY -> "beide leer"
            MetadataDiffItemResult.DIFFERENT -> "unterschiedlich"
            MetadataDiffItemResult.IDENTICAL -> "identisch"
            MetadataDiffItemResult.DIFFERENT_COUNT -> "unterschiedliche Anzahl"
            MetadataDiffItemResult.BOTH_NULL -> "beide Null"
            MetadataDiffItemResult.DIFFERENT_TEXT -> "unterschiedlicher Text"
        }
    },
    publisher = "Herausgeber",
    title = "Titel",
    viewGraphTitle = "Graph anzeigen",
    showLeftGraphButton = "Linken Graphen zeigen",
    showRightGraphButton = "Rechten Graphen zeigen",
    toggleDarkTheme = "Helles/Dunkles Thema"
)

class EnglishStrings : LocalizedStrings(
    canonicalUrl = "Canonical URL",
    changeLanguage = "Change Language",
    contact = "Contact",
    date = "Date",
    description = "Description",
    experimental = "Experimental?",
    identifiers = "Identifiers"
    ,
    jurisdiction = "Jurisdiction",
    language = "en",
    loadLeftFile = "Load left file",
    loadRightFile = "Load right file",
    graphsOpenInOtherWindows = "Graphs open in another window.",
    metadataDiffResults = {
        when (it) {
            MetadataDiffItemResult.BOTH_EMPTY -> "both empty"
            MetadataDiffItemResult.DIFFERENT -> "different"
            MetadataDiffItemResult.IDENTICAL -> "identical"
            MetadataDiffItemResult.DIFFERENT_COUNT -> "different count"
            MetadataDiffItemResult.BOTH_NULL -> "both null"
            MetadataDiffItemResult.DIFFERENT_TEXT -> "different text"
        }
    },
    publisher = "Publisher",
    title = "Title",
    viewGraphTitle = "View graph",
    showLeftGraphButton = "Show left graph",
    showRightGraphButton = "Show right graph",
    toggleDarkTheme = "Toggle dark theme"
)

val defaultStrings = getStrings()

fun getStrings(locale: SupportedLocale = SupportedLocale.getDefaultLocale()): LocalizedStrings =
    when (locale) {
        SupportedLocale.DE -> GermanStrings()
        SupportedLocale.EN -> EnglishStrings()
    }
