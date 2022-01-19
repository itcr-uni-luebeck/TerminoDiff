package terminodiff.engine.concepts

import org.hl7.fhir.r4.model.CodeSystem
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import terminodiff.engine.graph.FhirConceptDetails
import terminodiff.engine.graph.PropertyMap
import terminodiff.i18n.LocalizedStrings

typealias PropertyDiff = MutableList<KeyedListDiffResult<String, String>>

private val logger: Logger = LoggerFactory.getLogger("ConceptDiff")

data class ConceptDiff(
    val conceptComparison: List<ConceptDiffResult>,
    val propertyComparison: PropertyDiff,
) {
    fun toString(localizedStrings: LocalizedStrings): String {
        return "ConceptDiff(conceptComparison=[${conceptComparison.map { it.toString(localizedStrings) }}], " +
                "propertyComparison=[${propertyComparison.joinToString(",")}]"
    }

    companion object {

        private val diffItems = listOf(
            ConceptDiffItem({ display }, { display }),
            ConceptDiffItem({ definition }, { definition })
        )

        fun compareConcept(
            leftConcept: FhirConceptDetails,
            rightConcept: FhirConceptDetails,
            leftProperties: PropertyMap,
            rightProperties: PropertyMap,
        ): ConceptDiff {
            val conceptDiff = diffItems.map { di ->
                di.compare(leftConcept, rightConcept)
            }
            val leftProperty = leftConcept.property ?: listOf()
            val rightProperty = rightConcept.property ?: listOf()
            val propertyDiff: PropertyDiff = keyedListDiff(
                left = leftProperty,
                right = rightProperty,
                leftProperties = leftProperties,
                rightProperties = rightProperties,
                getKey = { propertyCode }
            ) { this.value }
            return ConceptDiff(conceptDiff, propertyDiff)
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

fun <T, K> keyedListDiff(
    left: List<T>,
    right: List<T>,
    getKey: T.() -> K,
    leftProperties: PropertyMap,
    rightProperties: PropertyMap,
    getStringKey: (K) -> String = { it.toString() },
    getStringValue: T.() -> String?,
): MutableList<KeyedListDiffResult<K, String>> {
    val diffResult = mutableListOf<KeyedListDiffResult<K, String>>()
    val leftKeys = left.map { it.getKey() }.toSet()
    val rightKeys = right.map { it.getKey() }.toSet()
    val onlyInLeft = leftKeys.filter { it !in rightKeys }.toSet()
    onlyInLeft.forEach {
        diffResult.add(
            KeyedListDiffResult(
                result = KeyedListDiffResult.KeyedListDiffResultKind.KEY_ONLY_IN_LEFT,
                key = it,
                propertyType = leftProperties[getStringKey(it)]!!
            )
        )
    }
    val onlyInRight = rightKeys.filter { it !in leftKeys }.toSet()
    onlyInRight.forEach {
        diffResult.add(
            KeyedListDiffResult(
                result = KeyedListDiffResult.KeyedListDiffResultKind.KEY_ONLY_IN_RIGHT,
                key = it,
                propertyType = rightProperties[getStringKey(it)]!!
            )
        )
    }
    val inBoth = leftKeys.plus(rightKeys).minus(onlyInLeft).minus(onlyInRight)
    diffResult.addAll(left.filter { it.getKey() in inBoth }.groupBy(getKey).mapNotNull { l ->
        val valueLeft = l.value.map(getStringValue)
        val matchingRight = right.filter { r -> r.getKey() == l.key }
        val valueRight = matchingRight.map(getStringValue)
        val result =
            when {
                valueLeft != valueRight -> KeyedListDiffResult.KeyedListDiffResultKind.VALUE_DIFFERENT
                else -> KeyedListDiffResult.KeyedListDiffResultKind.IDENTICAL
            }
        val stringKey = getStringKey(l.key)
        val propertyType = leftProperties[stringKey]
        when {
            propertyType == null -> {
                logger.warn("The property type for prop-code='$stringKey' is null, this is not supported")
                return@mapNotNull null
            }
            propertyType != rightProperties[stringKey] -> {
                logger.warn("The property type for prop-code='$stringKey' is different, this is not supported")
                return@mapNotNull null
            }
            else -> KeyedListDiffResult(
                result = result,
                key = l.key,
                propertyType = propertyType,
                leftValue = valueLeft,
                rightValue = valueRight
            )
        }
    })
    return diffResult
}

data class KeyedListDiffResult<K, V>(
    val result: KeyedListDiffResultKind,
    val key: K,
    val propertyType: CodeSystem.PropertyType,
    val leftValue: List<V?>? = null,
    val rightValue: List<V?>? = null,
) {
    enum class KeyedListDiffResultKind {
        KEY_ONLY_IN_LEFT,
        KEY_ONLY_IN_RIGHT,
        VALUE_DIFFERENT,
        IDENTICAL,
    }
}