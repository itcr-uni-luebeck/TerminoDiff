package terminodiff.ui.panes.conceptmap

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea
import org.fife.ui.rsyntaxtextarea.SyntaxConstants
import org.fife.ui.rsyntaxtextarea.Theme
import org.fife.ui.rtextarea.RTextScrollPane
import java.awt.BorderLayout
import javax.swing.JFrame
import javax.swing.JPanel

fun showJsonViewer(jsonText: String, isDarkTheme: Boolean) {
    JsonROTextEditor(jsonText = jsonText, isDarkTheme = isDarkTheme).isVisible = true
}

class JsonROTextEditor(val jsonText: String, val isDarkTheme: Boolean) : JFrame() {
    init {
        val cp = JPanel(BorderLayout()).apply {
            val textArea = RSyntaxTextArea(40, 80).apply {
                syntaxEditingStyle = SyntaxConstants.SYNTAX_STYLE_JSON
                isCodeFoldingEnabled = true
                antiAliasingEnabled = true
                isEditable = false
            }
            applyTheme(isDarkTheme, textArea)
            val sp = RTextScrollPane(textArea).apply {
                textArea.text = jsonText
            }
            add(sp)
        }
        contentPane = cp
        title = "FHIR JSON"
        defaultCloseOperation = DISPOSE_ON_CLOSE
        pack()
        setLocationRelativeTo(null)
    }

    private fun applyTheme(darkTheme: Boolean, textArea: RSyntaxTextArea) {
        val filename = "/org/fife/ui/rsyntaxtextarea/themes/${if(darkTheme) "dark.xml" else "default.xml"}"
        val theme = Theme.load(javaClass.getResourceAsStream(filename))
        theme.apply(textArea)
    }
}