package terminodiff.terminodiff.engine.metadata


import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import org.hl7.fhir.r4.model.CodeSystem
import terminodiff.i18n.LocalizedStrings

typealias ResultPair = Pair<MetadataComparisonResult, (LocalizedStrings.() -> String)?>
typealias StringResultPair = Pair<MetadataComparisonResult, String?>

class MetadataDiff(left: CodeSystem, right: CodeSystem, localizedStrings: LocalizedStrings) {

    private val comparisonDefinitions by derivedStateOf { generateComparisonDefinitions(localizedStrings) }

    val comparisons by derivedStateOf { runComparisons(left, right, localizedStrings, comparisonDefinitions) }

    private fun runComparisons(
        left: CodeSystem,
        right: CodeSystem,
        localizedStrings: LocalizedStrings,
        comparisonDefinitions: List<MetadataDiffItem>,
    ): List<MetadataComparison> = comparisonDefinitions.map {
        val (result, explanation) = it.compare(left, right)
        MetadataComparison(it, result, explanation, localizedStrings)
    }

    private fun generateComparisonDefinitions(localizedStrings: LocalizedStrings) = listOf(
        StringComparisonItem({ id }, true, localizedStrings) { it.id },
        StringComparisonItem({ canonicalUrl }, false, localizedStrings) { it.url },
        IdentifierDiffItem(localizedStrings),
        StringComparisonItem({ version }, true, localizedStrings) { it.version },
        StringComparisonItem({ name }, false, localizedStrings) { it.name },
        StringComparisonItem({ title }, false, localizedStrings) { it.title },
        // it would be great if Enum items could be refactored to their own class, but HAPI FHIR enums don't have a
        // common supertype, so string comparison it is!
        StringComparisonItem({ status }, false, localizedStrings) { it.status?.display },
        BooleanComparisonItem({ experimental }, false, localizedStrings) { it.experimental },
        StringComparisonItem({ date }, true, localizedStrings) { it.date?.toString() },
        StringComparisonItem({ publisher }, false, localizedStrings) { it.publisher?.toString() },
        ContactComparisonItem(localizedStrings),
        StringComparisonItem({ description }, false, localizedStrings) { it.description },
        UsageContextComparisonItem(localizedStrings),
        CodeableConceptComparisonItem({jurisdiction}, localizedStrings, false) {it.jurisdictionFirstRep},
        StringComparisonItem({ purpose }, false, localizedStrings) { it.purpose },
        StringComparisonItem({ copyright }, false, localizedStrings) { it.copyright },
        BooleanComparisonItem({ caseSensitive }, false, localizedStrings) { it.caseSensitive },
        StringComparisonItem({ valueSet }, false, localizedStrings) { it.valueSet },
        StringComparisonItem({ hierarchyMeaning }, false, localizedStrings) { it.hierarchyMeaning?.display },
        BooleanComparisonItem({ compositional }, false, localizedStrings) { it.compositional },
        BooleanComparisonItem({ versionNeeded }, false, localizedStrings) { it.versionNeeded },
        StringComparisonItem({ content }, false, localizedStrings) { it.content?.display },
        NumericComparisonItem({ count }, false, localizedStrings) { it.count },
        StringComparisonItem({ supplements }, false, localizedStrings) { it.supplements }
    )

    data class MetadataComparison(
        val diffItem: MetadataDiffItem,
        val result: MetadataComparisonResult,
        val explanation: (LocalizedStrings.() -> String)? = null,
        private val localizedStrings: LocalizedStrings,
    )

}