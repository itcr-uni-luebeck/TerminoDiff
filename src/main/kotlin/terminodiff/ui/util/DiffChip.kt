package terminodiff.ui.util

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import terminodiff.engine.concepts.ConceptDiffItem
import terminodiff.engine.concepts.ConceptDiffResult
import terminodiff.engine.concepts.KeyedListDiffResultKind
import terminodiff.i18n.LocalizedStrings
import terminodiff.terminodiff.engine.metadata.MetadataComparison
import terminodiff.terminodiff.engine.metadata.MetadataComparisonResult
import terminodiff.ui.AppIconResource
import terminodiff.ui.theme.DiffColors

@Composable
fun DiffChip(
    modifier: Modifier = Modifier,
    text: String,
    backgroundColor: Color,
    textColor: Color,
    textStyle: TextStyle = MaterialTheme.typography.bodyMedium,
    icon: ImageVector? = null,
    fontStyle: FontStyle = FontStyle.Normal
) {
    Surface(
        modifier = modifier.padding(4.dp),
        color = backgroundColor,
        tonalElevation = 4.dp,
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            Modifier.fillMaxHeight().padding(8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Icon(
                    modifier = Modifier.padding(start = 4.dp, end = 4.dp),
                    imageVector = icon,
                    contentDescription = null,
                    tint = textColor
                )
            }
            Text(
                text = text,
                style = textStyle,
                color = textColor,
                fontStyle = fontStyle
            )
        }
    }
}

@Composable
fun DiffChip(
    modifier: Modifier = Modifier,
    text: String,
    colorPair: Pair<Color, Color>,
    textStyle: TextStyle = MaterialTheme.typography.bodyMedium,
    icon: ImageVector? = null,
    fontStyle: FontStyle = FontStyle.Normal
) = DiffChip(
    modifier = modifier,
    text = text,
    backgroundColor = colorPair.first,
    textColor = colorPair.second,
    textStyle = textStyle,
    icon = icon,
    fontStyle = fontStyle
)

fun colorPairForConceptDiffResult(
    comparisonResult: ConceptDiffResult, diffColors: DiffColors
): Pair<Color, Color> = when (comparisonResult.result) {
    ConceptDiffItem.ConceptDiffResultEnum.IDENTICAL -> diffColors.greenPair
    ConceptDiffItem.ConceptDiffResultEnum.DIFFERENT -> diffColors.yellowPair
}

fun colorPairForDiffResult(
    comparisonResult: MetadataComparison, diffColors: DiffColors
): Pair<Color, Color> = when (comparisonResult.result) {
    MetadataComparisonResult.DIFFERENT -> if (comparisonResult.diffItem.expectDifferences) diffColors.yellowPair else diffColors.redPair
    else -> diffColors.greenPair
}

@Composable
fun chipForDiffResult(
    localizedStrings: LocalizedStrings,
    diffColors: DiffColors,
    result: KeyedListDiffResultKind,
) {
    val colorPair: Pair<Color, Color>
    val chipText: String
    var chipIcon: ImageVector? = null
    when (result) {
        KeyedListDiffResultKind.IDENTICAL -> {
            colorPair = diffColors.greenPair
            chipText = localizedStrings.identical
        }
        KeyedListDiffResultKind.KEY_ONLY_IN_LEFT -> {
            colorPair = diffColors.redPair
            chipText = localizedStrings.onlyInLeft
            chipIcon = AppIconResource.loadXmlImageVector(AppIconResource.icLoadLeftFile)
        }
        KeyedListDiffResultKind.KEY_ONLY_IN_RIGHT -> {
            colorPair = diffColors.redPair
            chipText = localizedStrings.onlyInRight
            chipIcon = AppIconResource.loadXmlImageVector(AppIconResource.icLoadRightFile)
        }
        else -> {
            colorPair = diffColors.yellowPair
            chipText = localizedStrings.differentValue
        }
    }
    DiffChip(text = chipText, colorPair = colorPair, icon = chipIcon)
}