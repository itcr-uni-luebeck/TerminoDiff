package terminodiff.terminodiff.engine.metadata


import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import org.hl7.fhir.r4.model.*
import terminodiff.engine.concepts.KeyedListDiffResult
import terminodiff.i18n.LocalizedStrings

typealias ResultPair = Pair<MetadataComparisonResult, (LocalizedStrings.() -> String)?>

class MetadataDiff(left: CodeSystem, right: CodeSystem, localizedStrings: LocalizedStrings) {

    private val comparisonDefinitions by derivedStateOf { generateComparisonDefinitions(localizedStrings) }

    val comparisons by derivedStateOf { runComparisons(left, right, comparisonDefinitions) }

    private fun runComparisons(
        left: CodeSystem,
        right: CodeSystem,
        comparisonDefinitions: List<MetadataDiffItem>,
    ): List<MetadataComparison> = comparisonDefinitions.map { diffItem ->
        when (diffItem) {
            is MetadataKeyedListDiffItem<*, *> -> diffItem.getComparisonResult(left, right)
            else -> {
                val (result, explanation) = diffItem.compare(left, right)
                MetadataComparison(diffItem, result, explanation)
            }
        }

    }

    private fun generateComparisonDefinitions(localizedStrings: LocalizedStrings) =
        listOf(StringComparisonItem({ id }, true, localizedStrings) { it.id },
            StringComparisonItem({ canonicalUrl }, false, localizedStrings) { it.url },
            IdentifierListDiffItem(localizedStrings),
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
            CodeableConceptComparisonItem({ jurisdiction }, localizedStrings, false) { it.jurisdiction },
            StringComparisonItem({ purpose }, false, localizedStrings) { it.purpose },
            StringComparisonItem({ copyright }, false, localizedStrings) { it.copyright },
            BooleanComparisonItem({ caseSensitive }, false, localizedStrings) { it.caseSensitive },
            StringComparisonItem({ valueSet }, false, localizedStrings) { it.valueSet },
            StringComparisonItem({ hierarchyMeaning }, false, localizedStrings) { it.hierarchyMeaning?.display },
            BooleanComparisonItem({ compositional }, false, localizedStrings) { it.compositional },
            BooleanComparisonItem({ versionNeeded }, false, localizedStrings) { it.versionNeeded },
            StringComparisonItem({ content }, false, localizedStrings) { it.content?.display },
            NumericComparisonItem({ count }, false, localizedStrings) { it.count },
            StringComparisonItem({ supplements }, false, localizedStrings) { it.supplements })

}

open class MetadataComparison(
    val diffItem: MetadataDiffItem,
    val result: MetadataComparisonResult,
    val explanation: (LocalizedStrings.() -> String)? = null,
)

abstract class MetadataListComparison<Type, KeyType>(
    val listDiffItem: MetadataKeyedListDiffItem<Type, KeyType>,
    result: MetadataComparisonResult,
    explanation: (LocalizedStrings.() -> String)?,
    val detailedResult: List<KeyedListDiffResult<KeyType, String>>,
) : MetadataComparison(diffItem = listDiffItem as MetadataDiffItem, result = result, explanation = explanation)

class IdentifierListComparison(
    listDiffItem: IdentifierListDiffItem,
    result: MetadataComparisonResult,
    explanation: (LocalizedStrings.() -> String)?,
    detailedResult: List<KeyedListDiffResult<IdentifierKeyType, String>>,
) : MetadataListComparison<Identifier, IdentifierKeyType>(listDiffItem, result, explanation, detailedResult)

class ContactListComparison(
    listDiffItem: ContactComparisonItem,
    result: MetadataComparisonResult,
    explanation: (LocalizedStrings.() -> String)?,
    detailedResult: List<KeyedListDiffResult<String, String>>,
) : MetadataListComparison<ContactDetail, String>(listDiffItem, result, explanation, detailedResult)

class CodeableConceptComparison(
    listDiffItem: CodeableConceptComparisonItem,
    result: MetadataComparisonResult,
    explanation: (LocalizedStrings.() -> String)?,
    detailedResult: List<KeyedListDiffResult<String, String>>,
) : MetadataListComparison<CodeableConcept, String>(listDiffItem, result, explanation, detailedResult)

class UsageContextComparison(
    listDiffItem: UsageContextComparisonItem,
    result: MetadataComparisonResult,
    explanation: (LocalizedStrings.() -> String)?,
    detailedResult: List<KeyedListDiffResult<String, String>>,
) : MetadataListComparison<UsageContext, String>(listDiffItem, result, explanation, detailedResult)