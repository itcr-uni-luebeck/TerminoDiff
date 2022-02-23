package terminodiff.terminodiff.ui.panes.conceptmap.json

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import terminodiff.antlr.json.*

class JsonResourceListener(
    private val builder: AnnotatedString.Builder,
    private val normalColor: Color,
    private val highlightColor: Color,
    private val literalColor: Color,
    private val indentSpaces: Int = 4
) : JSONBaseListener() {

    private var currentIndent = 0
    private var skipNextIndent = false

    override fun enterObj(ctx: JSONParser.ObjContext?) {
        builder.appendDefaultStyle("{", newLine = true)
        changeIndent(true)
    }

    override fun enterArr(ctx: JSONParser.ArrContext?) {
        builder.appendDefaultStyle("[", newLine = true)
        changeIndent(true)
    }

    override fun exitObj(ctx: JSONParser.ObjContext?) {
        builder.appendDefaultStyle("", newLine = true)
        changeIndent(false)
        builder.appendDefaultStyle("}", willSkipNextIndent = true)
    }

    override fun exitArr(ctx: JSONParser.ArrContext?) {
        builder.appendDefaultStyle("", newLine = true)
        changeIndent(false)
        //builder.append("\n")
        builder.appendDefaultStyle("]", willSkipNextIndent = true)
    }

    override fun enterPropertyName(ctx: JSONParser.PropertyNameContext) {
        builder.appendWithStyle(
            color = highlightColor,
            text = ctx.text, fontWeight = FontWeight.Bold,
            willSkipNextIndent = true
        )
        builder.appendDefaultStyle(": ", willSkipNextIndent = true)
    }

    override fun enterLiteral(ctx: JSONParser.LiteralContext) = builder.appendWithStyle(
        color = literalColor,
        text = ctx.text,
        willSkipNextIndent = true
    )

    override fun enterSpecialliteral(ctx: JSONParser.SpecialliteralContext) = builder.appendWithStyle(
        color = literalColor,
        text = ctx.text, fontStyle = FontStyle.Italic,
        willSkipNextIndent = true
    )

    override fun enterComma(ctx: JSONParser.CommaContext?) =
        builder.appendDefaultStyle(",", newLine = true)

    private fun AnnotatedString.Builder.appendDefaultStyle(
        text: String,
        newLine: Boolean = false,
        willSkipNextIndent: Boolean = false,
    ) = this.appendWithStyle(
        normalColor,
        text,
        newLine = newLine,
        willSkipNextIndent = willSkipNextIndent
    )

    private fun AnnotatedString.Builder.appendWithStyle(
        color: Color,
        text: String,
        fontWeight: FontWeight = FontWeight.Normal,
        fontStyle: FontStyle = FontStyle.Normal,
        newLine: Boolean = false,
        willSkipNextIndent: Boolean = false
    ) = this.withStyle(
        style = SpanStyle(color = color, fontWeight = fontWeight, fontStyle = fontStyle)
    ) {
        if (skipNextIndent) {
            skipNextIndent = false
        } else {
            append(" ".repeat(currentIndent))
        }
        skipNextIndent = willSkipNextIndent
        append(text)
        if (newLine) append("\n")
    }

    private fun changeIndent(increase: Boolean) {
        currentIndent = maxOf(0, currentIndent + when (increase) {
            true -> indentSpaces
            else -> (-indentSpaces)
        })
    }
}