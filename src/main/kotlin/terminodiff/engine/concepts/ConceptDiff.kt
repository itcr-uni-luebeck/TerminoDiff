package terminodiff.engine.concepts

import org.hl7.fhir.r4.model.CodeSystem
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import terminodiff.engine.graph.FhirConceptDetails
import terminodiff.engine.graph.PropertyMap
import terminodiff.i18n.LocalizedStrings
import terminodiff.terminodiff.engine.metadata.formatCoding

typealias PropertyDiff = List<PropertyDiffResult>
typealias DesignationKey = Pair<String?, String?>
typealias DesignationDiff = List<KeyedListDiffResult<DesignationKey, String>>

private val logger: Logger = LoggerFactory.getLogger("ConceptDiff")

data class ConceptDiff(
    val conceptComparison: List<ConceptDiffResult>,
    val propertyComparison: PropertyDiff,
    val designationComparison: DesignationDiff,
) {
    fun toString(localizedStrings: LocalizedStrings): String {
        return "ConceptDiff(conceptComparison=[${conceptComparison.map { it.toString(localizedStrings) }}], " + "propertyComparison=[${
            propertyComparison.joinToString(",")
        }]"
    }

    companion object {

        private val diffItems =
            listOf(ConceptDiffItem({ display }, { display }), ConceptDiffItem({ definition }, { definition }))

        private fun getPropertyType(
            leftProperties: PropertyMap,
            rightProperties: PropertyMap,
            key: String,
            result: KeyedListDiffResultKind,
        ): CodeSystem.PropertyType? = when (result) {
            KeyedListDiffResultKind.KEY_ONLY_IN_LEFT -> leftProperties[key]!!
            KeyedListDiffResultKind.KEY_ONLY_IN_RIGHT -> rightProperties[key]!!
            else -> {
                val propertyType = leftProperties[key]
                when {
                    propertyType == null -> {
                        logger.warn("The property type for prop-code='$key' is null, this is not supported")
                        null
                    }
                    propertyType != rightProperties[key] -> {
                        logger.warn("The property type for prop-code='$key' is different, this is not supported")
                        null
                    }
                    else -> propertyType
                }
            }
        }

        fun compareConcept(
            leftConcept: FhirConceptDetails,
            rightConcept: FhirConceptDetails,
            leftProperties: PropertyMap,
            rightProperties: PropertyMap,
        ): ConceptDiff {
            val conceptDiff = diffItems.map { di ->
                di.compare(leftConcept, rightConcept)
            }
            val leftProperty = leftConcept.property
            val rightProperty = rightConcept.property
            val propertyDiff: PropertyDiff = KeyedListDiff(left = leftProperty,
                right = rightProperty,
                getKey = { it.propertyCode },
                getStringValue = { it.value }).executeDiff().mapNotNull { result ->
                getPropertyType(leftProperties, rightProperties, result.key, result.result)?.let { propertyType ->
                    PropertyDiffResult(result = result.result,
                        key = result.key,
                        leftValue = result.leftValue,
                        rightValue = result.rightValue,
                        propertyType = propertyType)
                }

            }
            val designationDiff = KeyedListDiff(left = leftConcept.designation,
                right = rightConcept.designation,
                getKey = { it.language to it.use?.let { coding -> formatCoding(coding) } },
                getStringValue = {
                    it.value
                }).executeDiff()
            return ConceptDiff(conceptDiff, propertyDiff, designationDiff)
        }
    }
}

data class ConceptDiffResult(
    val diffItem: ConceptDiffItem,
    val result: ConceptDiffItem.ConceptDiffResultEnum,
) {
    fun toString(localizedStrings: LocalizedStrings): String {
        return "ConceptDiffResult(diffItem=${diffItem.toString(localizedStrings)}, result=$result)"
    }
}

data class ConceptDiffItem(
    val label: LocalizedStrings.() -> String,
    private val instanceGetter: FhirConceptDetails.() -> String?,
) {
    fun compare(c1: FhirConceptDetails, c2: FhirConceptDetails): ConceptDiffResult {
        val left = instanceGetter.invoke(c1)
        val right = instanceGetter.invoke(c2)
        @Suppress("KotlinConstantConditions") val result = when {
            left == null && right == null -> ConceptDiffResultEnum.IDENTICAL
            (left != null && right == null) || (left == null && right != null) -> ConceptDiffResultEnum.DIFFERENT
            else -> if (left == right) ConceptDiffResultEnum.IDENTICAL else ConceptDiffResultEnum.DIFFERENT
        }
        return ConceptDiffResult(this, result)
    }

    fun toString(localizedStrings: LocalizedStrings): String {
        return "ConceptDiffItem(label='${localizedStrings.label()}')"
    }

    enum class ConceptDiffResultEnum {
        IDENTICAL, DIFFERENT
    }
}

class KeyedListDiff<ElementType, KeyType>(
    val left: List<ElementType>,
    val right: List<ElementType>,
    val getKey: (ElementType) -> KeyType,
    val getStringValue: (ElementType) -> String?,
) {
    fun executeDiff(): MutableList<KeyedListDiffResult<KeyType, String>> {
        val diffResult = mutableListOf<KeyedListDiffResult<KeyType, String>>()
        val leftKeys = left.map { getKey.invoke(it) }.toSet()
        val rightKeys = right.map { getKey.invoke(it) }.toSet()
        val onlyInLeft = leftKeys.filter { it !in rightKeys }.toSet()
        onlyInLeft.forEach { key ->
            diffResult.add(KeyedListDiffResult(result = KeyedListDiffResultKind.KEY_ONLY_IN_LEFT,
                key = key,
                leftValue = left.filter { et -> getKey.invoke(et) == key }.map(getStringValue)))
        }
        val onlyInRight = rightKeys.filter { it !in leftKeys }.toSet()
        onlyInRight.forEach { key ->
            diffResult.add(KeyedListDiffResult(result = KeyedListDiffResultKind.KEY_ONLY_IN_RIGHT,
                key = key,
                rightValue = right.filter { et -> getKey.invoke(et) == key }.map(getStringValue)))
        }
        val inBoth = leftKeys.plus(rightKeys).minus(onlyInLeft).minus(onlyInRight)
        diffResult.addAll(left.filter { getKey.invoke(it) in inBoth }.groupBy(getKey).mapNotNull { l ->
            val valueLeft = l.value.map(getStringValue)
            val matchingRight = right.filter { r -> getKey.invoke(r) == l.key }
            val valueRight = matchingRight.map(getStringValue)
            val result = when {
                valueLeft != valueRight -> KeyedListDiffResultKind.VALUE_DIFFERENT
                else -> KeyedListDiffResultKind.IDENTICAL
            }
            KeyedListDiffResult(result = result, key = l.key, leftValue = valueLeft, rightValue = valueRight)
        })
        return diffResult
    }
}

open class KeyedListDiffResult<K, V>(
    val result: KeyedListDiffResultKind,
    val key: K,
    val leftValue: List<V?>? = null,
    val rightValue: List<V?>? = null,
)

enum class KeyedListDiffResultKind {
    KEY_ONLY_IN_LEFT, KEY_ONLY_IN_RIGHT, VALUE_DIFFERENT, IDENTICAL,
}

class PropertyDiffResult(
    result: KeyedListDiffResultKind,
    key: String,
    leftValue: List<String?>?,
    rightValue: List<String?>?,
    val propertyType: CodeSystem.PropertyType,
) : KeyedListDiffResult<String, String>(result = result, key = key, leftValue = leftValue, rightValue = rightValue)