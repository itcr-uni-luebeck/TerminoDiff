package terminodiff.i18n

import terminodiff.engine.concepts.ConceptDiffItem
import terminodiff.engine.graph.DiffGraphElementKind
import terminodiff.terminodiff.engine.metadata.MetadataComparisonResult
import java.io.File

/**
 * we pass around an instance of LocalizedStrings, since we want every composable
 * to recompose when the language changes.
 */
abstract class LocalizedStrings(
    val boolean_: (Boolean?) -> String,
    val bothListsAreEmpty: String,
    val bothValuesAreNull: String,
    val canonicalUrl: String,
    val caseSensitive: String = "Case-Sensitive?",
    val changeLanguage: String,
    val code: String = "Code",
    val comparison: String,
    val compositional: String,
    val conceptDiff: String,
    val conceptDiffResults_: (ConceptDiffItem.ConceptDiffResultEnum) -> String,
    val contact: String,
    val content: String = "Content",
    val count: String,
    val copyright: String = "Copyright",
    val date: String,
    val description: String,
    val definition: String = "Definition",
    val designation: String = "Designation",
    val designations: String,
    val differentValue: String,
    val diffGraph: String,
    val display: String = "Display",
    val displayAndInWhich_: (String?, DiffGraphElementKind) -> String,
    val experimental: String,
    val hierarchyMeaning: String,
    val id: String = "ID",
    val identical: String,
    val identifiers: String,
    val jurisdiction: String,
    val loadLeftFile: String,
    val loadRightFile: String,
    val leftValue: String,
    val language: String,
    val keyIsDifferent_: (String) -> String,
    val rightValue: String,
    val metadataDiff: String,
    val metadataDiffResults_: (MetadataComparisonResult) -> String,
    val name: String = "Name",
    val noDataLoadedTitle: String,
    val numberDifferentReason_: (Int, List<String?>) -> String,
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
    val purpose: String,
    val property: String,
    val properties: String,
    val propertiesDesignations: String,
    val propertiesDesignationsCount: (Int, Int) -> String,
    val propertiesDesignationsCountDelta: (Pair<Int, Int>, Pair<Int, Int>) -> String,
    val propertyDesignationForCode_: (String) -> String,
    val propertyType: String,
    val reload: String,
    val showAll: String,
    val showDifferent: String,
    val showIdentical: String,
    val showLeftGraphButton: String,
    val showRightGraphButton: String,
    val supplements: String,
    val status: String = "Status",
    val toggleDarkTheme: String,
    val textDifferent: String,
    val textDifferentAndAnotherReason_: (String) -> String,
    val title: String,
    val terminoDiff: String = "TerminoDiff",
    val uniLuebeck: String,
    val useContext: String,
    val value: String,
    val valueSet: String = "ValueSet",
    val version: String = "Version",
    val versionNeeded: String,
    val leftFileOpenFilename_: (File) -> String,
    val rightFileOpenFilename_: (File) -> String,
)

enum class SupportedLocale {
    EN, DE;

    companion object {
        fun getDefaultLocale() = EN
    }
}

class GermanStrings : LocalizedStrings(boolean_ = {
    when (it) {
        null -> "null"
        true -> "WAHR"
        false -> "FALSCH"
    }
},
    bothListsAreEmpty = "Beide Listen sind leer",
    bothValuesAreNull = "Beide Werte sind null",
    canonicalUrl = "Kanonische URL",
    changeLanguage = "Sprache wechseln",
    comparison = "Vergleich",
    compositional = "Kompositionell?",
    conceptDiff = "Konzept-Diff",
    conceptDiffResults_ = {
        when (it) {
            ConceptDiffItem.ConceptDiffResultEnum.DIFFERENT -> "Unterschiedlich"
            ConceptDiffItem.ConceptDiffResultEnum.IDENTICAL -> "Identisch"
        }
    },
    contact = "Kontakt",
    count = "Anzahl",
    date = "Datum",
    description = "Beschreibung",
    designations = "Bezeichnungen",
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
    hierarchyMeaning = "Hierachie-Bedeutung",
    identical = "Identisch",
    identifiers = "IDs",
    jurisdiction = "Jurisdiktion",
    loadLeftFile = "Linke Datei laden",
    loadRightFile = "Rechte Datei laden",
    leftValue = "Linker Wert",
    language = "Sprache",
    keyIsDifferent_ = { "Schlüssel '$it' ist unterschiedlich" },
    rightValue = "Rechter Wert",
    metadataDiff = "Metadaten-Diff",
    metadataDiffResults_ = {
        when (it) {
            MetadataComparisonResult.IDENTICAL -> "Identisch"
            MetadataComparisonResult.DIFFERENT -> "Unterschiedlich"
        }
    },
    noDataLoadedTitle = "Keine Daten geladen",
    numberDifferentReason_ = { count, differences ->
        val reason = differences.filterNotNull().joinToString(separator = "; ", limit = 3)
        "$count unterschiedlich: $reason"
    },
    oneValueIsNull = "Ein Wert ist null",
    onlyInLeft = "Nur links",
    onlyConceptDifferences = "Konzeptunterschiede",
    onlyInRight = "Nur rechts",
    overallComparison = "Gesamt",
    publisher = "Herausgeber",
    purpose = "Zweck",
    property = "Eigenschaft",
    properties = "Eigenschaften",
    propertiesDesignations = "Eigenschaften / Bezeichnungen",
    propertiesDesignationsCount = { p, d -> "$p E / $d B" },
    propertiesDesignationsCountDelta = { p, d ->
        when {
            p.second == 0 && d.second != 0 -> "${p.first} E / ${d.first} Δ${d.second} B"
            p.second != 0 && d.second == 0 -> "${p.first} Δ${p.second} E / ${d.first} B"
            else -> "${p.first} Δ${p.second} E / ${d.first} Δ${d.second} B"
        }
    },
    propertyDesignationForCode_ = { code -> "Eigenschaften und Bezeichnungen für Konzept '$code'" },
    propertyType = "Typ",
    reload = "Neu laden",
    showAll = "Alle",
    showDifferent = "Unterschiedliche",
    showIdentical = "Identische",
    showLeftGraphButton = "Linken Graphen zeigen",
    showRightGraphButton = "Rechten Graphen zeigen",
    supplements = "Ergänzt",
    toggleDarkTheme = "Helles/Dunkles Thema",
    textDifferent = "Text ist unterschiedlich",
    textDifferentAndAnotherReason_ = { otherReason ->
        "Text ist unterschiedlich, und $otherReason"
    },
    title = "Titel",
    uniLuebeck = "Universität zu Lübeck",
    useContext = "Nutzungskontext",
    value = "Wert",
    versionNeeded = "Version erforderlich?",
    leftFileOpenFilename_ = { file -> "Linke Datei geöffnet: ${file.absolutePath}" },
    rightFileOpenFilename_ = { file -> "Rechte Datei geöffnet: ${file.absolutePath}" })

class EnglishStrings : LocalizedStrings(
    boolean_ = {
        when (it) {
            null -> "null"
            true -> "TRUE"
            false -> "FALSE"
        }
    },
    bothListsAreEmpty = "Both lists are empty",
    bothValuesAreNull = "Both values are null",
    canonicalUrl = "Canonical URL",
    changeLanguage = "Change Language",
    comparison = "Comparison",
    compositional = "Compositional?",
    conceptDiff = "Concept Diff",
    conceptDiffResults_ = {
        when (it) {
            ConceptDiffItem.ConceptDiffResultEnum.DIFFERENT -> "Different"
            ConceptDiffItem.ConceptDiffResultEnum.IDENTICAL -> "Identical"
        }
    },
    contact = "Contact",
    count = "Count",
    date = "Date",
    description = "Description",
    designations = "Designations",
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
    hierarchyMeaning = "Hierarchy Meaning",
    identical = "Identical",
    identifiers = "Identifiers",
    jurisdiction = "Jurisdiction",
    loadLeftFile = "Load left file",
    loadRightFile = "Load right file",
    leftValue = "Left value",
    language = "Language",
    keyIsDifferent_ = { "Key '$it' is different" },
    rightValue = "Right value",
    metadataDiff = "Metadata Diff",
    metadataDiffResults_ = {
        when (it) {
            MetadataComparisonResult.IDENTICAL -> "Identical"
            MetadataComparisonResult.DIFFERENT -> "Different"
        }
    },
    noDataLoadedTitle = "No data loaded",
    numberDifferentReason_ = { count, differences ->
        "$count different: ${differences.joinToString(separator = "; ", limit = 3)}"
    },
    oneValueIsNull = "One value is null",
    onlyInLeft = "Only left",
    onlyConceptDifferences = "Concept differences",
    onlyInRight = "Only right",
    overallComparison = "Overall",
    publisher = "Publisher",
    purpose = "Purpose",
    property = "Property",
    properties = "Properties",
    propertiesDesignations = "Properties / Designations",
    propertiesDesignationsCount = { p, d -> "$p P / $d D" },
    propertiesDesignationsCountDelta = { p, d ->
        when {
            p.second == 0 && d.second != 0 -> "${p.first} P / ${d.first} Δ${d.second} D"
            p.second != 0 && d.second == 0 -> "${p.first} Δ${p.second} P / ${d.first} D"
            else -> "${p.first} Δ${p.second} P / ${d.first} Δ${d.second} D"
        }
    },
    propertyDesignationForCode_ = { code -> "Properties and designations for concept '$code'" },
    propertyType = "Type",
    reload = "Reload",
    showAll = "All",
    showDifferent = "Different",
    showIdentical = "Identical",
    showLeftGraphButton = "Show left graph",
    showRightGraphButton = "Show right graph",
    supplements = "Supplements",
    toggleDarkTheme = "Toggle dark theme",
    textDifferent = "Text is different",
    textDifferentAndAnotherReason_ = { otherReason ->
        "Text is different, and $otherReason"
    },
    title = "Title",
    uniLuebeck = "University of Luebeck",
    useContext = "Use context",
    value = "Value",
    versionNeeded = "Version needed?",
    leftFileOpenFilename_ = { file -> "Left file open: ${file.absolutePath}" },
    rightFileOpenFilename_ = { file -> "Right file open: ${file.absolutePath}" },
)

fun getStrings(locale: SupportedLocale = SupportedLocale.getDefaultLocale()): LocalizedStrings = when (locale) {
    SupportedLocale.DE -> GermanStrings()
    SupportedLocale.EN -> EnglishStrings()
}
