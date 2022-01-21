package terminodiff.ui.util

import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.LocalContentColor
import androidx.compose.material.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow

/**
 * wrap the Text composable in a SelectionContainer, providing most of the options of Text
 */
@Composable
fun SelectableText(
    text: String?,
    fontStyle: FontStyle? = if (text == null) FontStyle.Italic else FontStyle.Normal,
    fontWeight: FontWeight? = null,
    modifier: Modifier = Modifier,
    color: Color = LocalContentColor.current,
    style: TextStyle = LocalTextStyle.current,
    textAlign: TextAlign? = null,
    overflow: TextOverflow = TextOverflow.Clip,
) {
    SelectionContainer {
        Text(
            text = text ?: "null",
            modifier = modifier,
            fontStyle = fontStyle,
            color = color,
            fontWeight = fontWeight,
            style = style,
            textAlign = textAlign,
            overflow = overflow,
        )
    }
}


@Composable
fun textForValue(
    value: Any?,
    limit: Int = 3,
) = SelectableText(text = when (value) {
    is List<*> -> value.joinToString(limit = limit)
    else -> value?.toString()
}, color = androidx.compose.material3.LocalContentColor.current)