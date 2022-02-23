package terminodiff.terminodiff.ui.panes.conceptmap

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.Card
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.tree.ParseTreeWalker
import terminodiff.antlr.json.JSONLexer
import terminodiff.antlr.json.JSONParser
import terminodiff.terminodiff.ui.panes.conceptmap.json.JsonResourceListener

@Composable
fun JSONDisplay(
    jsonText: String,
    scrollState: ScrollableState,
) {
    Card(modifier = Modifier.fillMaxWidth().padding(8.dp).background(colorScheme.surfaceVariant)) {
        Column(modifier = Modifier.padding(8.dp),//.background(colorScheme.surfaceVariant),
            horizontalAlignment = Alignment.CenterHorizontally) {
            JsonText(
                jsonText,
                contentColor = colorScheme.onSurfaceVariant,
                highlightColor = colorScheme.primary,
                literalColor = colorScheme.secondary,
                scrollState = scrollState,
            )
        }

    }
}

@Composable
private fun JsonText(
    jsonString: String,
    contentColor: Color,
    highlightColor: Color,
    literalColor: Color,
    scrollState: ScrollableState,
) {
    val charStream by remember { mutableStateOf(CharStreams.fromString(jsonString)) }
    val jsonTree: JSONParser.JsonContext by derivedStateOf {
        JSONLexer(charStream).let { lexer ->
            CommonTokenStream(lexer).let { tokens ->
                JSONParser(tokens).json()
            }
        }
    }
    val annotatedString by derivedStateOf {
        buildAnnotatedString {
            JsonResourceListener(this,
                normalColor = contentColor,
                highlightColor = highlightColor,
                literalColor = literalColor).let { listener ->
                ParseTreeWalker.DEFAULT.walk(listener, jsonTree)
            }
        }
    }
    SelectionContainer(Modifier.scrollable(scrollState, orientation = Orientation.Vertical)) {
        Text(text = annotatedString, fontFamily = FontFamily.Monospace)
    }

}