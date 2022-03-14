package terminodiff.terminodiff.ui.util

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import terminodiff.i18n.LocalizedStrings
import terminodiff.ui.MouseOverPopup
import java.net.MalformedURLException
import java.net.URL
import java.util.*

fun isError(validationResult: EditTextSpec.ValidationResult?) = when (validationResult) {
    EditTextSpec.ValidationResult.INVALID -> true
    else -> false
}

fun iconForValidationResult(
    validationResult: EditTextSpec.ValidationResult?,
    localizedStrings: LocalizedStrings,
): Pair<ImageVector, String>? = when (validationResult) {
    EditTextSpec.ValidationResult.INVALID -> Icons.Default.Error to localizedStrings.invalid
    EditTextSpec.ValidationResult.WARN -> Icons.Default.Warning to localizedStrings.notRecommended
    else -> null
}

@Composable
fun <T> EditText(
    modifier: Modifier = Modifier,
    data: T,
    spec: EditTextSpec<T>,
    backgroundColor: Color = colorScheme.secondaryContainer,
    foregroundColor: Color = colorScheme.contentColorFor(backgroundColor),
    localizedStrings: LocalizedStrings,
    weight: Float = 0.8f,
) {
    val valueState = spec.valueState.invoke(data)
    val validation = spec.validation?.invoke(valueState.value ?: "")
    LabeledTextField(modifier = modifier.fillMaxWidth(weight),
        value = valueState.value ?: "",
        onValueChange = { newValue ->
            if (valueState is MutableState) valueState.value = newValue
        },
        labelText = spec.title?.invoke(localizedStrings),
        singleLine = spec.singleLine,
        readOnly = spec.readOnly,
        isError = isError(validation),
        backgroundColor = backgroundColor,
        foregroundColor = foregroundColor
    ) {
        iconForValidationResult(validation, localizedStrings = localizedStrings)?.let { (icon, desc) ->
            Icon(imageVector = icon,
                contentDescription = desc,
                tint = if (validation == EditTextSpec.ValidationResult.INVALID) colorScheme.error else colorScheme.onTertiaryContainer)
        }
    }
}

@Composable
fun <T> EditTextGroup(group: EditTextGroupSpec<T>, localizedStrings: LocalizedStrings, backgroundColor: Color, data: T) {
    Card(Modifier.fillMaxWidth(0.9f).padding(4.dp),
        backgroundColor = colorScheme.secondaryContainer,
        elevation = 8.dp) {
        Column(modifier = Modifier.padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.Top),
            horizontalAlignment = Alignment.CenterHorizontally) {
            Text(group.title.invoke(localizedStrings),
                style = MaterialTheme.typography.titleSmall,
                color = colorScheme.onTertiaryContainer)
            group.specs.forEach { spec ->
                EditText(spec = spec, localizedStrings = localizedStrings, data = data, backgroundColor = backgroundColor)
            }
        }
    }
}

data class EditTextGroupSpec<T>(
    val title: LocalizedStrings.() -> String,
    val specs: List<EditTextSpec<T>>,
)

data class EditTextSpec<T>(
    val title: (LocalizedStrings.() -> String)?,
    val valueState: T.() -> State<String?>,
    val singleLine: Boolean = true,
    val readOnly: Boolean = false,
    val validation: ((String) -> ValidationResult)? = {
        when (it.isNotBlank()) {
            true -> ValidationResult.VALID
            else -> ValidationResult.INVALID
        }
    },
) {
    enum class ValidationResult {
        VALID, INVALID, WARN
    }
}

fun String.isUrl(): Boolean = try {
    URL(this).let { true }
} catch (e: MalformedURLException) {
    false
}


@Composable
fun LabeledTextField(
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    labelText: String?,
    singleLine: Boolean = true,
    readOnly: Boolean = false,
    isError: Boolean = false,
    backgroundColor: Color = colorScheme.secondaryContainer,
    foregroundColor: Color = colorScheme.contentColorFor(backgroundColor),
    trailingIcon: @Composable () -> Unit,
) = TextField(value = value,
    onValueChange = onValueChange,
    modifier = modifier,
    singleLine = singleLine,
    isError = isError,
    readOnly = readOnly,
    label = {
        labelText?.let { Text(text = it, color = foregroundColor.copy(0.75f)) }
    },
    trailingIcon = trailingIcon,
    colors = TextFieldDefaults.textFieldColors(backgroundColor = backgroundColor,
        textColor = foregroundColor,
        focusedIndicatorColor = foregroundColor.copy(0.75f)))

@Composable
fun LabeledTextField(
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    labelText: String?,
    singleLine: Boolean = true,
    readOnly: Boolean = false,
    isError: Boolean = false,
    backgroundColor: Color = colorScheme.secondaryContainer,
    foregroundColor: Color = colorScheme.contentColorFor(backgroundColor),
    trailingIconVector: ImageVector? = null,
    trailingIconDescription: String? = null,
    trailingIconTint: Color = colorScheme.onSecondaryContainer,
    onTrailingIconClick: (() -> Unit)? = null,
) {
    LabeledTextField(modifier = modifier,
        value = value,
        onValueChange = onValueChange,
        labelText = labelText,
        singleLine = singleLine,
        readOnly = readOnly,
        isError = isError,
        backgroundColor = backgroundColor,
        foregroundColor = foregroundColor
    ) {
        trailingIconVector?.let { imageVector ->
            if (trailingIconDescription == null) throw IllegalArgumentException("a content description has to be specified if a trailing icon is provided")
            MouseOverPopup(text = trailingIconDescription) {
                when (onTrailingIconClick) {
                    null -> Icon(imageVector = imageVector,
                        contentDescription = trailingIconDescription,
                        tint = trailingIconTint)
                    else -> IconButton(onClick = onTrailingIconClick) {
                        Icon(imageVector = imageVector,
                            contentDescription = trailingIconDescription,
                            tint = trailingIconTint)
                    }
                }
            }
        }
    }
}

@Composable
fun <T> Dropdown(
    elements: List<T>,
    selectedElement: T?,
    elementDisplay: (T) -> String,
    textFieldDisplay: (T) -> String = elementDisplay,
    fontStyle: (T) -> FontStyle = { FontStyle.Normal },
    dropdownColor: Color,
    onSelect: (T) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center) {
        Box(Modifier.fillMaxWidth(0.8f)) {
            LabeledTextField(value = selectedElement?.let(textFieldDisplay) ?: "",
                onValueChange = { },
                readOnly = true,
                labelText = null,
                backgroundColor = dropdownColor
            ) {
                IconButton({
                    expanded = !expanded
                }) {
                    val icon = when (expanded) {
                        true -> Icons.Filled.ArrowDropUp
                        else -> Icons.Filled.ArrowDropDown
                    }
                    Icon(icon, null)
                }
            }
            DropdownMenu(expanded = expanded,
                modifier = Modifier.background(dropdownColor),
                onDismissRequest = { expanded = false }) {
                elements.forEach { element ->
                    DropdownMenuItem(onClick = {
                        onSelect(element)
                        expanded = false
                    }) {
                        Text(text = elementDisplay(element),
                            fontStyle = fontStyle(element))
                    }
                }
            }
        }
    }
}

@Composable
fun AutocompleteEditText(
    autocompleteSuggestions: SortedMap<String, String>,
    value: String?,
    limitSuggestions: Int = 5,
    backgroundColor: Color = colorScheme.secondaryContainer,
    foregroundColor: Color = colorScheme.contentColorFor(backgroundColor),
    filterSuggestions: (String?, String) -> Boolean = { input, suggestion -> suggestion.startsWith(input ?: "") },
    validateInput: ((String) -> EditTextSpec.ValidationResult)? = null,
    localizedStrings: LocalizedStrings,
    onValueChange: (String) -> Unit,
) {
    var hasFocus by remember { mutableStateOf(false) }
    val currentSuggestions by derivedStateOf {
        autocompleteSuggestions.filterKeys { suggestion ->
            filterSuggestions(value, suggestion)
        }.entries.take(limitSuggestions)
    }
    Box(Modifier.fillMaxWidth()) {
        val validation = validateInput?.invoke(value ?: "")
        LabeledTextField(modifier = Modifier.onFocusChanged {
            hasFocus = it.isFocused
        },
            value = value ?: "",
            onValueChange = {
                onValueChange(it)
            },
            labelText = null,
            isError = isError(validation),
            backgroundColor = backgroundColor,
            foregroundColor = foregroundColor) {
            iconForValidationResult(validation, localizedStrings = localizedStrings)?.let { (icon, desc) ->
                Icon(imageVector = icon,
                    contentDescription = desc,
                    tint = if (validation == EditTextSpec.ValidationResult.INVALID) colorScheme.error else colorScheme.onTertiaryContainer)
            }
        }
        DropdownMenu(expanded = when {
            !hasFocus -> false
            currentSuggestions.isEmpty() -> false
            currentSuggestions.size == 1 && currentSuggestions[0].key == value -> false // the value is entered into the text field verbatim
            else -> true
        }, modifier = Modifier.background(colorScheme.secondaryContainer), onDismissRequest = {
            hasFocus = false
        }, focusable = false) {
            currentSuggestions.forEach { entry ->
                DropdownMenuItem(onClick = {
                    onValueChange(entry.key)
                    hasFocus = false
                }) {
                    Text(
                        text = entry.value,
                        color = colorScheme.onSecondaryContainer,
                    )
                }
            }
        }
    }
}