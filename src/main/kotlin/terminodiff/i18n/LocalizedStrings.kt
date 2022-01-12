package terminodiff.i18n

import terminodiff.engine.concepts.ConceptDiffItem
import terminodiff.engine.graph.DiffGraphElementKind
import terminodiff.engine.metadata.MetadataDiffOld.MetadataDiffItemResultOld
import terminodiff.terminodiff.engine.metadata.MetadataDiff
import java.io.File

/**
 * we pass around an instance of LocalizedStrings, since we want every composable
 * to recompose when the language changes.
 */
abstract class LocalizedStrings(
    val bothListsAreEmpty: String,
    val bothValuesAreNull: String,
    val canonicalUrl: String,
    val changeLanguage: String,
    val conceptDiff: String,
    val contact: String,
    val code: String = "Code",
    val comparison: String,
    val conceptDiffResults_: (ConceptDiffItem.ConceptDiffResultEnum) -> String,
    val date: String,
    val description: String,
    val definition: String = "Definition",
    val designation: String = "Designation",
    val differentValue: String,
    val diffGraph: String,
    val display: String = "Display",
    val displayAndInWhich_: (String?, DiffGraphElementKind) -> String,
    val experimental: String,
    val id: String = "ID",
    val identical: String,
    val identifiers: String,
    val jurisdiction: String,
    val loadLeftFile: String,
    val loadRightFile: String,
    val leftValue: String,
    val rightValue: String,
    val metadataDiff: String,
    val metadataDiffResultsOld_: (MetadataDiffItemResultOld) -> String,
    val metadataDiffResults_: (MetadataDiff.MetadataComparisonResult) -> String,
    val name: String = "Name",
    val noDataLoadedTitle: String,
    val numberDifferent_: (Int) -> String,
    val numberItems_: (Int) -> String = {
        when (it) {
            1 -> "1 item"
            else -> "$it items"
        }
    },
    val oneValueIsNull: String,
    val onlyInLeft: String,
    val onlyConceptDifferences: String,
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
    bothListsAreEmpty = "Beide Listen sind leer",
    bothValuesAreNull = "Beide Werte sind null",
    canonicalUrl = "Kanonische URL",
    changeLanguage = "Sprache wechseln",
    conceptDiff = "Konzept-Diff",
    contact = "Kontakt",
    comparison = "Vergleich",
    conceptDiffResults_ = {
        when (it) {
            ConceptDiffItem.ConceptDiffResultEnum.DIFFERENT -> "Unterschiedlich"
            ConceptDiffItem.ConceptDiffResultEnum.IDENTICAL -> "Identisch"
        }
    },
    date = "Datum",
    description = "Beschreibung",
    differentValue = "Unterschiedliche Werte",
    diffGraph = "Differenz-Graph",
    displayAndInWhich_ = { display, inWhich ->
        val where = when (inWhich) {
            DiffGraphElementKind.LEFT -> "nur links"
            DiffGraphElementKind.RIGHT -> "nur rechts"
            DiffGraphElementKind.BOTH -> "in beiden"
        }
        "'$display' ($where)"
    },
    experimental = "Experimentell?",
    identical = "Identisch",
    identifiers = "IDs",
    jurisdiction = "Jurisdiktion",
    loadLeftFile = "Linke Datei laden",
    loadRightFile = "Rechte Datei laden",
    leftValue = "Linker Wert",
    rightValue = "Rechter Wert",
    metadataDiff = "Metadaten-Diff",
    metadataDiffResults_ = {
        when (it) {
            MetadataDiff.MetadataComparisonResult.IDENTICAL -> "identisch"
            MetadataDiff.MetadataComparisonResult.DIFFERENT -> "unterschiedlich"
        }
    },
    metadataDiffResultsOld_ = {
        when (it) {
            MetadataDiffItemResultOld.BOTH_EMPTY -> "beide leer"
            MetadataDiffItemResultOld.DIFFERENT -> "unterschiedlich"
            MetadataDiffItemResultOld.IDENTICAL -> "identisch"
            MetadataDiffItemResultOld.DIFFERENT_COUNT -> "unterschiedliche Anzahl"
            MetadataDiffItemResultOld.BOTH_NULL -> "beide Null"
            MetadataDiffItemResultOld.DIFFERENT_TEXT -> "unterschiedlicher Text"
        }
    },
    noDataLoadedTitle = "Keine Daten geladen",
    numberDifferent_ = { "$it unterschiedlich" },
    oneValueIsNull = "Ein Wert ist null",
    onlyInLeft = "Nur links",
    onlyConceptDifferences = "Konzeptunterschiede",
    onlyInRight = "Nur rechts",
    overallComparison = "Gesamt",
    publisher = "Herausgeber",
    showAll = "Alle",
    showDifferent = "Unterschiedliche",
    showIdentical = "Identische",
    showLeftGraphButton = "Linken Graphen zeigen",
    showRightGraphButton = "Rechten Graphen zeigen",
    toggleDarkTheme = "Helles/Dunkles Thema",
    title = "Titel",
    leftFileOpenFilename_ = { file -> "Linke Datei geöffnet: ${file.absolutePath}" },
    rightFileOpenFilename_ = { file -> "Rechte Datei geöffnet: ${file.absolutePath}" }
)

class EnglishStrings : LocalizedStrings(
    bothListsAreEmpty = "Both lists are empty",
    bothValuesAreNull = "Both values are null",
    canonicalUrl = "Canonical URL",
    changeLanguage = "Change Language",
    conceptDiff = "Concept Diff",
    contact = "Contact",
    comparison = "Comparison",
    conceptDiffResults_ = {
        when (it) {
            ConceptDiffItem.ConceptDiffResultEnum.DIFFERENT -> "Different"
            ConceptDiffItem.ConceptDiffResultEnum.IDENTICAL -> "Identical"
        }
    },
    date = "Date",
    description = "Description",
    differentValue = "Different value",
    diffGraph = "Difference Graph",
    displayAndInWhich_ = { display, inWhich ->
        val where = when (inWhich) {
            DiffGraphElementKind.LEFT -> "only left"
            DiffGraphElementKind.RIGHT -> "only right"
            DiffGraphElementKind.BOTH -> "in both"
        }
        "'$display' ($where)"
    },
    experimental = "Experimental?",
    identical = "Identical",
    identifiers = "Identifiers",
    jurisdiction = "Jurisdiction",
    loadLeftFile = "Load left file",
    loadRightFile = "Load right file",
    leftValue = "Left value",
    rightValue = "Right value",
    metadataDiff = "Metadata Diff",
    metadataDiffResults_ = {
        when (it) {
            MetadataDiff.MetadataComparisonResult.IDENTICAL -> "identical"
            MetadataDiff.MetadataComparisonResult.DIFFERENT -> "different"
        }
    },
    metadataDiffResultsOld_ = {
        when (it) {
            MetadataDiffItemResultOld.BOTH_EMPTY -> "both empty"
            MetadataDiffItemResultOld.DIFFERENT -> "different"
            MetadataDiffItemResultOld.IDENTICAL -> "identical"
            MetadataDiffItemResultOld.DIFFERENT_COUNT -> "different count"
            MetadataDiffItemResultOld.BOTH_NULL -> "both null"
            MetadataDiffItemResultOld.DIFFERENT_TEXT -> "different text"
        }
    },
    noDataLoadedTitle = "No data loaded",
    numberDifferent_ = { "$it different" },
    oneValueIsNull = "One value is null",
    onlyInLeft = "Only left",
    onlyConceptDifferences = "Concept differences",
    onlyInRight = "Only right",
    overallComparison = "Overall",
    publisher = "Publisher",
    showAll = "All",
    showDifferent = "Different",
    showIdentical = "Identical",
    showLeftGraphButton = "Show left graph",
    showRightGraphButton = "Show right graph",
    toggleDarkTheme = "Toggle dark theme",
    title = "Title",
    leftFileOpenFilename_ = { file -> "Left file open: ${file.absolutePath}" },
    rightFileOpenFilename_ = { file -> "Right file open: ${file.absolutePath}" },
)

val defaultStrings = getStrings()

fun getStrings(locale: SupportedLocale = SupportedLocale.getDefaultLocale()): LocalizedStrings =
    when (locale) {
        SupportedLocale.DE -> GermanStrings()
        SupportedLocale.EN -> EnglishStrings()
    }
