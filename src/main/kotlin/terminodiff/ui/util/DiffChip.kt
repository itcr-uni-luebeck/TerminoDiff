package terminodiff.ui.util

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import terminodiff.engine.concepts.ConceptDiffItem
import terminodiff.engine.concepts.ConceptDiffResult
import terminodiff.engine.metadata.MetadataDiff
import terminodiff.ui.theme.DiffColors

@Preview
@Composable
fun DiffChip(
    modifier: Modifier = Modifier,
    text: String,
    backgroundColor: Color,
    textColor: Color
) {
    Surface(
        modifier = modifier.padding(4.dp),
        color = backgroundColor,
        tonalElevation = 4.dp,
        shape = RoundedCornerShape(8.dp)
    ) {
        Row {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = textColor,
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}

fun colorPairForConceptDiffResult(
    comparisonResult: ConceptDiffResult,
    diffColors: DiffColors
): Pair<Color, Color> = when (comparisonResult.result) {
    ConceptDiffItem.ConceptDiffResultEnum.IDENTICAL -> diffColors.greenPair
    ConceptDiffItem.ConceptDiffResultEnum.DIFFERENT -> diffColors.redPair
}

fun colorPairForDiffResult(
    comparisonResult: MetadataDiff.MetadataComparisonResult,
    diffColors: DiffColors
): Pair<Color, Color> = when (comparisonResult.result) {
    MetadataDiff.MetadataDiffItemResult.DIFFERENT_TEXT, MetadataDiff.MetadataDiffItemResult.DIFFERENT, MetadataDiff.MetadataDiffItemResult.DIFFERENT_COUNT -> if (comparisonResult.expected) diffColors.yellowPair else diffColors.redPair
    else -> diffColors.greenPair
}