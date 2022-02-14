package terminodiff.i18n

import terminodiff.engine.concepts.ConceptDiffItem
import terminodiff.engine.concepts.KeyedListDiffResult
import terminodiff.engine.concepts.KeyedListDiffResultKind
import terminodiff.engine.graph.DiffGraphElementKind
import terminodiff.engine.resources.DiffDataContainer.*
import terminodiff.terminodiff.engine.metadata.MetadataComparisonResult
import terminodiff.terminodiff.engine.resources.InputResource

/**
 * we pass around an instance of LocalizedStrings, since we want every composable
 * to recompose when the language changes.
 */
abstract class LocalizedStrings(
    val anUnknownErrorOccurred: String,
    val boolean_: (Boolean?) -> String,
    val bothValuesAreNull: String,
    val calculateDiff: String,
    val canonicalUrl: String,
    val caseSensitive: String = "Case-Sensitive?",
    val changeLanguage: String,
    val clearSearch: String,
    val clickForDetails: String,
    val closeAccept: String,
    val closeReject: String,
    val comparison: String,
    val compositional: String,
    val code: String = "Code",
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
    val fhirTerminologyServer: String,
    val fileFromPath_: (String) -> String,
    val fileFromUrl_: (String) -> String,
    val fileSystem: String,
    val hierarchyMeaning: String,
    val id: String = "ID",
    val identical: String,
    val identifiers: String,
    val invalid: String,
    val jurisdiction: String,
    val keyedListResult_: (List<KeyedListDiffResult<*, *>>) -> String,
    val loadLeft: String,
    val loadRight: String,
    val loadFromFile: String,
    val loadedResources: String,
    val leftValue: String,
    val language: String,
    val rightValue: String,
    val metadataDiff: String,
    val metadataDiffResults_: (MetadataComparisonResult) -> String,
    val metaVersion: String,
    val name: String = "Name",
    val noDataLoaded: String,
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
    val openResources: String,
    val pending: String,
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
    val search: String,
    val select: String,
    val side_: (Side) -> String,
    val showAll: String,
    val showDifferent: String,
    val showIdentical: String,
    val showLeftGraphButton: String,
    val showRightGraphButton: String,
    val supplements: String,
    val status: String = "Status",
    val system: String = "System",
    val toggleDarkTheme: String,
    val text: String = "Text",
    val title: String,
    val terminoDiff: String = "TerminoDiff",
    val uniLuebeck: String,
    val use: String,
    val useContext: String,
    val valid: String,
    val value: String,
    val valueSet: String = "ValueSet",
    val version: String = "Version",
    val versionNeeded: String,
    val vRead: String = "VRead",
    val vReadFor_: (InputResource) -> String,
    val vReadExplanationEnabled_: (Boolean) -> String,
    val vreadFromUrlAndMetaVersion_: (String, String) -> String,
)

enum class SupportedLocale {
    EN, DE;

    companion object {
        val defaultLocale = EN
    }
}

class GermanStrings : LocalizedStrings(
    anUnknownErrorOccurred = "Ein unbekannter Fehler ist aufgetrefen",
    boolean_ = {
        when (it) {
            null -> "null"
            true -> "WAHR"
            false -> "FALSCH"
        }
    },
    bothValuesAreNull = "Beide Werte sind null",
    calculateDiff = "Diff berechnen",
    canonicalUrl = "Kanonische URL",
    clearSearch = "Suche zurücksetzen",
    closeAccept = "Akzeptieren",
    closeReject = "Verwerfen",
    changeLanguage = "Sprache wechseln",
    clickForDetails = "Für Details klicken",
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
    designations = "Designationen",
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
    fhirTerminologyServer = "FHIR-Terminologieserver",
    fileFromPath_ = { "Datei von: $it" },
    fileFromUrl_ = { "FHIR-Server von: $it" },
    fileSystem = "Dateisystem",
    hierarchyMeaning = "Hierachie-Bedeutung",
    identical = "Identisch",
    identifiers = "IDs",
    invalid = "Ungültig",
    jurisdiction = "Jurisdiktion",
    keyedListResult_ = { results ->
        results.map { it.result }.groupingBy { it }.eachCount().let { eachCount ->
            listOfNotNull(
                if (KeyedListDiffResultKind.IDENTICAL in eachCount.keys) "${eachCount[KeyedListDiffResultKind.IDENTICAL]} identisch" else null,
                if (KeyedListDiffResultKind.VALUE_DIFFERENT in eachCount.keys) "${eachCount[KeyedListDiffResultKind.VALUE_DIFFERENT]} unterschiedlich" else null,
                if (KeyedListDiffResultKind.KEY_ONLY_IN_LEFT in eachCount.keys) "${eachCount[KeyedListDiffResultKind.KEY_ONLY_IN_LEFT]} nur links" else null,
                if (KeyedListDiffResultKind.KEY_ONLY_IN_RIGHT in eachCount.keys) "${eachCount[KeyedListDiffResultKind.KEY_ONLY_IN_RIGHT]} nur rechts" else null,
            )
        }.joinToString()
    },
    loadLeft = "Links laden",
    loadRight = "Rechts laden",
    loadFromFile = "Vom Dateisystem laden",
    loadedResources = "Geladene Ressourcen",
    leftValue = "Linker Wert",
    language = "Sprache",
    rightValue = "Rechter Wert",
    metadataDiff = "Metadaten-Diff",
    metadataDiffResults_ = {
        when (it) {
            MetadataComparisonResult.IDENTICAL -> "Identisch"
            MetadataComparisonResult.DIFFERENT -> "Unterschiedlich"
        }
    },
    metaVersion = "Meta-Version",
    noDataLoaded = "Keine Daten geladen",
    oneValueIsNull = "Ein Wert ist null",
    onlyInLeft = "Nur links",
    onlyConceptDifferences = "Konzeptunterschiede",
    onlyInRight = "Nur rechts",
    openResources = "Ressourcen öffnen",
    overallComparison = "Gesamt",
    pending = "Ausstehend...",
    publisher = "Herausgeber",
    purpose = "Zweck",
    property = "Eigenschaft",
    properties = "Eigenschaften",
    propertiesDesignations = "Eigenschaften / Designationen",
    propertiesDesignationsCount = { p, d -> "$p E / $d D" },
    propertiesDesignationsCountDelta = { p, d ->
        when {
            p.second == 0 && d.second != 0 -> "${p.first} E / ${d.first} Δ${d.second} D"
            p.second != 0 && d.second == 0 -> "${p.first} Δ${p.second} E / ${d.first} D"
            else -> "${p.first} Δ${p.second} E / ${d.first} Δ${d.second} D"
        }
    },
    propertyDesignationForCode_ = { code -> "Eigenschaften und Designationen für Konzept '$code'" },
    propertyType = "Typ",
    reload = "Neu laden",
    search = "Suchen",
    select = "Auswahl",
    side_ = {
        when (it) {
            Side.RIGHT -> "Rechts"
            Side.LEFT -> "Links"
        }
    },
    showAll = "Alle",
    showDifferent = "Unterschiedliche",
    showIdentical = "Identische",
    showLeftGraphButton = "Linken Graphen zeigen",
    showRightGraphButton = "Rechten Graphen zeigen",
    supplements = "Ergänzt",
    toggleDarkTheme = "Helles/Dunkles Thema",
    title = "Titel",
    uniLuebeck = "Universität zu Lübeck",
    use = "Zweck",
    useContext = "Nutzungskontext",
    valid = "Gültig",
    value = "Wert",
    versionNeeded = "Version erforderlich?",
    vReadFor_ = {
        "VRead für ${it.downloadableCodeSystem!!.canonicalUrl}"
    },
    vReadExplanationEnabled_ = {
        when (it) {
            true -> "Vergleiche Versionen der Ressource mit der \$history-Operation."
            else -> "Es gibt nur eine Ressourcen-Version der gewählten Ressource."
        }
    },
    vreadFromUrlAndMetaVersion_ = { url, meta ->
        "VRead von $url (Meta-Version: $meta)"
    }
)

class EnglishStrings : LocalizedStrings(
    anUnknownErrorOccurred = "An unknown error occured.",
    boolean_ = {
        when (it) {
            null -> "null"
            true -> "TRUE"
            false -> "FALSE"
        }
    },
    bothValuesAreNull = "Both values are null",
    calculateDiff = "Calculate diff",
    canonicalUrl = "Canonical URL",
    changeLanguage = "Change Language",
    clearSearch = "Clear search",
    clickForDetails = "Click for details",
    closeAccept = "Accept",
    closeReject = "Reject",
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
    fhirTerminologyServer = "FHIR Terminology Server",
    fileFromPath_ = { "File from: $it" },
    fileFromUrl_ = { "FHIR Server from: $it" },
    fileSystem = "Filesystem",
    hierarchyMeaning = "Hierarchy Meaning",
    identical = "Identical",
    identifiers = "Identifiers",
    invalid = "Ungültig",
    jurisdiction = "Jurisdiction",
    keyedListResult_ = { results ->
        results.map { it.result }.groupingBy { it }.eachCount().let { eachCount ->
            listOfNotNull(
                if (KeyedListDiffResultKind.IDENTICAL in eachCount.keys) "${eachCount[KeyedListDiffResultKind.IDENTICAL]} identical" else null,
                if (KeyedListDiffResultKind.VALUE_DIFFERENT in eachCount.keys) "${eachCount[KeyedListDiffResultKind.VALUE_DIFFERENT]} different" else null,
                if (KeyedListDiffResultKind.KEY_ONLY_IN_LEFT in eachCount.keys) "${eachCount[KeyedListDiffResultKind.KEY_ONLY_IN_LEFT]} only left" else null,
                if (KeyedListDiffResultKind.KEY_ONLY_IN_RIGHT in eachCount.keys) "${eachCount[KeyedListDiffResultKind.KEY_ONLY_IN_RIGHT]} only right" else null,
            )
        }.joinToString()
    },
    loadLeft = "Load left",
    loadRight = "Load right",
    loadFromFile = "Load from file",
    loadedResources = "Loaded resources",
    leftValue = "Left value",
    language = "Language",
    rightValue = "Right value",
    metadataDiff = "Metadata Diff",
    metadataDiffResults_ = {
        when (it) {
            MetadataComparisonResult.IDENTICAL -> "Identical"
            MetadataComparisonResult.DIFFERENT -> "Different"
        }
    },
    metaVersion = "Meta Version",
    noDataLoaded = "No data loaded",
    oneValueIsNull = "One value is null",
    onlyInLeft = "Only left",
    onlyConceptDifferences = "Concept differences",
    onlyInRight = "Only right",
    openResources = "Open Resources",
    overallComparison = "Overall",
    pending = "Pending...",
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
    search = "Search",
    select = "Select",
    side_ = {
        when (it) {
            Side.RIGHT -> "Right"
            Side.LEFT -> "Left"
        }
    },
    showAll = "All",
    showDifferent = "Different",
    showIdentical = "Identical",
    showLeftGraphButton = "Show left graph",
    showRightGraphButton = "Show right graph",
    supplements = "Supplements",
    toggleDarkTheme = "Toggle dark theme",
    title = "Title",
    uniLuebeck = "University of Luebeck",
    use = "Use",
    useContext = "Use context",
    valid = "Valid",
    value = "Value",
    versionNeeded = "Version needed?",
    vReadFor_ = {
        "VRead for ${it.downloadableCodeSystem!!.canonicalUrl}"
    },
    vReadExplanationEnabled_ = {
        when (it) {
            true -> "Compare versions of the resource using the \$history operation."
            else -> "There is only one resource version of the selected resource."
        }
    },
    vreadFromUrlAndMetaVersion_ = { url, meta ->
        "VRead from $url (Meta version: $meta)"
    })

fun getStrings(locale: SupportedLocale = SupportedLocale.defaultLocale): LocalizedStrings = when (locale) {
    SupportedLocale.DE -> GermanStrings()
    SupportedLocale.EN -> EnglishStrings()
}
