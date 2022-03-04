package terminodiff.i18n

import terminodiff.engine.concepts.ConceptDiffItem
import terminodiff.engine.concepts.KeyedListDiffResult
import terminodiff.engine.concepts.KeyedListDiffResultKind
import terminodiff.engine.resources.DiffDataContainer.*
import terminodiff.terminodiff.engine.graph.GraphSide
import terminodiff.terminodiff.engine.metadata.MetadataComparisonResult
import terminodiff.terminodiff.engine.resources.InputResource

/**
 * we pass around an instance of LocalizedStrings, since we want every composable
 * to recompose when the language changes.
 */
abstract class LocalizedStrings(
    val acceptAll: String,
    val actions: String,
    val addLayer: String,
    val addTarget: String,
    val anUnknownErrorOccurred: String,
    val areYouSure: String,
    val automatic: String,
    val automappedCount_: (Int) -> String,
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
    val code: String = "Code",
    val comments: String,
    val comparison: String,
    val compositional: String,
    val conceptDiff: String,
    val conceptDiffResults_: (ConceptDiffItem.ConceptDiffResultEnum) -> String,
    val conceptMap: String = "ConceptMap",
    val concepts_: (Int) -> String,
    val contact: String,
    val content: String = "Content",
    val copyright: String = "Copyright",
    val count: String,
    val date: String,
    val definition: String = "Definition",
    val description: String,
    val designation: String = "Designation",
    val designations: String,
    val diffGraph: String,
    val differentValue: String,
    val display: String = "Display",
    val displayAndInWhich_: (String?, GraphSide) -> String,
    val elements_: (Int) -> String,
    val equivalence: String,
    val experimental: String,
    val fhirTerminologyServer: String,
    val fileFromPath_: (String) -> String,
    val fileFromUrl_: (String) -> String,
    val fileSystem: String,
    val filtered: String,
    val graph: String = "Graph",
    val graphFor_: (String) -> String = { c -> "Graph ($c)"},
    val group: String,
    val hierarchyMeaning: String,
    val id: String = "ID",
    val identical: String,
    val identifiers: String,
    val invalid: String,
    val jurisdiction: String,
    val keyedListResult_: (List<KeyedListDiffResult<*, *>>) -> String,
    val language: String,
    val layers: String,
    val leftValue: String,
    val loadFromFile: String,
    val loadLeft: String,
    val loadRight: String,
    val loadedResources: String,
    val mappableCount_: (Int) -> String,
    val metaVersion: String,
    val metadata: String,
    val metadataDiff: String,
    val metadataDiffResults_: (MetadataComparisonResult) -> String,
    val name: String = "Name",
    val no: String,
    val noDataLoaded: String,
    val notRecommended: String,
    val numberItems_: (Int) -> String = {
        when (it) {
            1 -> "1 item"
            else -> "$it items"
        }
    },
    val ok: String = "OK",
    val oneValueIsNull: String,
    val onlyConceptDifferences: String,
    val onlyInLeft: String,
    val onlyInRight: String,
    val openResources: String,
    val overallComparison: String,
    val pending: String,
    val properties: String,
    val propertiesDesignations: String,
    val propertiesDesignationsCount: (Int, Int) -> String,
    val propertiesDesignationsCountDelta: (Pair<Int, Int>, Pair<Int, Int>) -> String,
    val property: String,
    val propertyDesignationForCode_: (String) -> String,
    val propertyType: String,
    val publisher: String,
    val purpose: String,
    val reallyAcceptAll: String,
    val reload: String,
    val removeLayer: String,
    val rightValue: String,
    val search: String,
    val select: String,
    val showAll: String,
    val showDifferent: String,
    val showIdentical: String,
    val showLeftGraphButton: String,
    val showRightGraphButton: String,
    val side_: (Side) -> String,
    val sourceUri: String,
    val sourceValueSet: String,
    val sourceVersion: String,
    val status: String = "Status",
    val supplements: String,
    val system: String = "System",
    val target: String,
    val targetUri: String,
    val targetValueSet: String,
    val targetVersion: String,
    val terminoDiff: String = "TerminoDiff",
    val text: String = "Text",
    val title: String,
    val toggleDarkTheme: String,
    val uniLuebeck: String,
    val use: String,
    val useContext: String,
    val vread: String = "VRead",
    val vreadExplanationEnabled_: (Boolean) -> String,
    val vReadFor_: (InputResource) -> String,
    val valid: String,
    val validAcceptedCount_: (Int) -> String,
    val value: String,
    val valueSet: String = "ValueSet",
    val version: String = "Version",
    val versionNeeded: String,
    val vreadFromUrlAndMetaVersion_: (String, String) -> String,
    val yes: String,
)

enum class SupportedLocale {
    EN, DE;

    companion object {
        val defaultLocale = EN
    }
}

class GermanStrings : LocalizedStrings(
    acceptAll = "Alle akzeptieren",
    actions = "Aktionen",
    addLayer = "Ebene hinzufügen",
    addTarget = "Ziel hinzufügen",
    anUnknownErrorOccurred = "Ein unbekannter Fehler ist aufgetreten",
    areYouSure = "Bist Du sicher?",
    automatic = "Automatik",
    automappedCount_ = { "$it automatisch gemappt"},
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
    changeLanguage = "Sprache wechseln",
    clearSearch = "Suche zurücksetzen",
    clickForDetails = "Für Details klicken",
    closeAccept = "Akzeptieren",
    closeReject = "Verwerfen",
    comments = "Kommentare",
    comparison = "Vergleich",
    compositional = "Kompositionell?",
    conceptDiff = "Konzept-Diff",
    conceptDiffResults_ = {
        when (it) {
            ConceptDiffItem.ConceptDiffResultEnum.DIFFERENT -> "Unterschiedlich"
            ConceptDiffItem.ConceptDiffResultEnum.IDENTICAL -> "Identisch"
        }
    },
    concepts_ = {
        when (it) {
            1 -> "Konzept"
            else -> "Konzepte"
        }
    },
    contact = "Kontakt",
    count = "Anzahl",
    date = "Datum",
    description = "Beschreibung",
    designations = "Designationen",
    diffGraph = "Differenz-Graph",
    differentValue = "Unterschiedliche Werte",
    displayAndInWhich_ = { display, inWhich ->
        val where = when (inWhich) {
            GraphSide.LEFT -> "nur links"
            GraphSide.RIGHT -> "nur rechts"
            GraphSide.BOTH -> "in beiden"
        }
        "'$display' ($where)"
    },
    elements_ = {
        when (it) {
            1 -> "Elemente"
            else -> "Elemente"
        }
    },
    equivalence = "Äquivalenz",
    experimental = "Experimentell?",
    fhirTerminologyServer = "FHIR-Terminologieserver",
    fileFromPath_ = { "Datei von: $it" },
    fileFromUrl_ = { "FHIR-Server von: $it" },
    fileSystem = "Dateisystem",
    filtered = "gefiltert",
    group = "Gruppe",
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
    language = "Sprache",
    layers = "Ebenen",
    leftValue = "Linker Wert",
    loadFromFile = "Vom Dateisystem laden",
    loadLeft = "Links laden",
    loadRight = "Rechts laden",
    loadedResources = "Geladene Ressourcen",
    metadata = "Metadaten",
    metadataDiff = "Metadaten-Diff",
    rightValue = "Rechter Wert",
    mappableCount_ = { "$it abbildbar" },
    metadataDiffResults_ = {
        when (it) {
            MetadataComparisonResult.IDENTICAL -> "Identisch"
            MetadataComparisonResult.DIFFERENT -> "Unterschiedlich"
        }
    },
    metaVersion = "Meta-Version",
    no = "Nein",
    noDataLoaded = "Keine Daten geladen",
    notRecommended = "Nicht empfohlen",
    oneValueIsNull = "Ein Wert ist null",
    onlyConceptDifferences = "Konzeptunterschiede",
    onlyInLeft = "Nur links",
    onlyInRight = "Nur rechts",
    openResources = "Ressourcen öffnen",
    overallComparison = "Gesamt",
    pending = "Ausstehend...",
    properties = "Eigenschaften",
    propertiesDesignations = "Eigenschaften / Designationen",
    propertiesDesignationsCount = { p, d -> "$p E / $d D" },
    property = "Eigenschaft",
    publisher = "Herausgeber",
    propertiesDesignationsCountDelta = { p, d ->
        when {
            p.second == 0 && d.second != 0 -> "${p.first} E / ${d.first} Δ${d.second} D"
            p.second != 0 && d.second == 0 -> "${p.first} Δ${p.second} E / ${d.first} D"
            else -> "${p.first} Δ${p.second} E / ${d.first} Δ${d.second} D"
        }
    },
    propertyDesignationForCode_ = { code -> "Eigenschaften und Designationen für Konzept '$code'" },
    propertyType = "Typ",
    purpose = "Zweck",
    reallyAcceptAll = "Möchtest Du wirklich alle atomatisch gemappten Konzepte akzeptieren?\n" +
            "Dies kann nicht rückgängig gemacht werden.",
    reload = "Neu laden",
    removeLayer = "Ebene entfernen",
    search = "Suchen",
    select = "Auswahl",
    sourceUri = "Quell-URI",
    sourceValueSet = "Quell-ValueSet",
    sourceVersion = "Quell-Version",
    side_ = {
        when (it) {
            Side.RIGHT -> "Rechts"
            Side.LEFT -> "Links"
        }
    },
    showAll = "Alle",
    showDifferent = "Unterschiedliche",
    showIdentical = "Identische",
    showLeftGraphButton = "Linker Graph",
    showRightGraphButton = "Rechter Graph",
    supplements = "Ergänzt",
    target = "Ziel",
    targetUri = "Ziel-URI",
    targetValueSet = "Ziel-ValueSet",
    targetVersion = "Ziel-Version",
    title = "Titel",
    toggleDarkTheme = "Helles/Dunkles Thema",
    uniLuebeck = "Universität zu Lübeck",
    use = "Zweck",
    useContext = "Nutzungskontext",
    vReadFor_ = { "VRead für ${it.downloadableCodeSystem!!.canonicalUrl}" },
    valid = "Gültig",
    validAcceptedCount_ = { "$it gültig/akzeptiert"},
    value = "Wert",
    versionNeeded = "Version erforderlich?",
    vreadExplanationEnabled_ = {
        when (it) {
            true -> "Vergleiche Versionen der Ressource mit der \$history-Operation."
            else -> "Es gibt nur eine Ressourcen-Version der gewählten Ressource."
        }
    },
    vreadFromUrlAndMetaVersion_ = { url, meta ->
        "VRead von $url (Meta-Version: $meta)"
    },
    yes = "Ja"
)

class EnglishStrings : LocalizedStrings(
    acceptAll = "Accept all",
    actions = "Actions",
    addLayer = "Add layer",
    addTarget = "Add target",
    anUnknownErrorOccurred = "An unknown error occured.",
    areYouSure = "Are you sure?",
    automatic = "Automatic",
    automappedCount_ = { "$it automatically mapped" },
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
    comments = "Comments",
    comparison = "Comparison",
    compositional = "Compositional?",
    conceptDiff = "Concept Diff",
    conceptDiffResults_ = {
        when (it) {
            ConceptDiffItem.ConceptDiffResultEnum.DIFFERENT -> "Different"
            ConceptDiffItem.ConceptDiffResultEnum.IDENTICAL -> "Identical"
        }
    },
    concepts_ = {
        when (it) {
            1 -> "concept"
            else -> "concepts"
        }
    },
    contact = "Contact",
    count = "Count",
    date = "Date",
    description = "Description",
    designations = "Designations",
    diffGraph = "Difference Graph",
    differentValue = "Different value",
    displayAndInWhich_ = { display, inWhich ->
        val where = when (inWhich) {
            GraphSide.LEFT -> "only left"
            GraphSide.RIGHT -> "only right"
            GraphSide.BOTH -> "in both"
        }
        "'$display' ($where)"
    },
    elements_ = {
        when (it) {
            1 -> "element"
            else -> "elements"
        }
    },
    equivalence = "Equivalence",
    experimental = "Experimental?",
    fhirTerminologyServer = "FHIR Terminology Server",
    fileFromPath_ = { "File from: $it" },
    fileFromUrl_ = { "FHIR Server from: $it" },
    fileSystem = "Filesystem",
    filtered = "filtered",
    group = "Group",
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
    language = "Language",
    layers = "Layers",
    leftValue = "Left value",
    loadFromFile = "Load from file",
    loadLeft = "Load left",
    loadRight = "Load right",
    loadedResources = "Loaded resources",
    mappableCount_ = { "$it mappable" },
    metadata = "Metadata",
    metadataDiff = "Metadata Diff",
    metadataDiffResults_ = {
        when (it) {
            MetadataComparisonResult.IDENTICAL -> "Identical"
            MetadataComparisonResult.DIFFERENT -> "Different"
        }
    },
    metaVersion = "Meta Version",
    no = "No",
    noDataLoaded = "No data loaded",
    notRecommended = "Not recommended",
    oneValueIsNull = "One value is null",
    onlyConceptDifferences = "Concept differences",
    onlyInLeft = "Only left",
    onlyInRight = "Only right",
    openResources = "Open Resources",
    overallComparison = "Overall",
    pending = "Pending...",
    properties = "Properties",
    propertiesDesignations = "Properties / Designations",
    propertiesDesignationsCount = { p, d -> "$p P / $d D" },
    property = "Property",
    publisher = "Publisher",
    purpose = "Purpose",
    propertiesDesignationsCountDelta = { p, d ->
        when {
            p.second == 0 && d.second != 0 -> "${p.first} P / ${d.first} Δ${d.second} D"
            p.second != 0 && d.second == 0 -> "${p.first} Δ${p.second} P / ${d.first} D"
            else -> "${p.first} Δ${p.second} P / ${d.first} Δ${d.second} D"
        }
    },
    propertyDesignationForCode_ = { code -> "Properties and designations for concept '$code'" },
    propertyType = "Type",
    reallyAcceptAll = "Do you really want to accept all auto-mapped concepts?\n" +
            "You can not undo this.",
    reload = "Reload",
    removeLayer = "Remove layers",
    rightValue = "Right value",
    search = "Search",
    select = "Select",
    sourceUri = "Source URI",
    sourceValueSet = "Source ValueSet",
    sourceVersion = "Source version",
    side_ = {
        when (it) {
            Side.RIGHT -> "Right"
            Side.LEFT -> "Left"
        }
    },
    showAll = "All",
    showDifferent = "Different",
    showIdentical = "Identical",
    showLeftGraphButton = "Left graph",
    showRightGraphButton = "Right graph",
    supplements = "Supplements",
    target = "Target",
    targetUri = "Target URI",
    targetValueSet = "Target ValueSet",
    targetVersion = "Target version",
    title = "Title",
    toggleDarkTheme = "Toggle dark theme",
    uniLuebeck = "University of Luebeck",
    use = "Use",
    useContext = "Use context",
    vReadFor_ = { "VRead for ${it.downloadableCodeSystem!!.canonicalUrl}" },
    valid = "Valid",
    validAcceptedCount_ = { "$it valid/accepted"},
    value = "Value",
    versionNeeded = "Version needed?",
    vreadExplanationEnabled_ = {
        when (it) {
            true -> "Compare versions of the resource using the \$history operation."
            else -> "There is only one resource version of the selected resource."
        }
    },
    vreadFromUrlAndMetaVersion_ = { url, meta ->
        "VRead from $url (Meta version: $meta)"
    },
    yes = "Yes"
)

fun getStrings(locale: SupportedLocale = SupportedLocale.defaultLocale): LocalizedStrings = when (locale) {
    SupportedLocale.DE -> GermanStrings()
    SupportedLocale.EN -> EnglishStrings()
}
