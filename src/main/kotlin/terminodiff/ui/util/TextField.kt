package terminodiff.terminodiff.ui.util

import androidx.compose.material.IconButton
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import terminodiff.ui.MouseOverPopup


@Composable
fun LabeledTextField(
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    labelText: String,
    singleLine: Boolean = true,
    readOnly: Boolean = false,
    isError: Boolean = false,
    trailingIconVector: ImageVector? = null,
    trailingIconDescription: String? = null,
    trailingIconTint: Color = MaterialTheme.colorScheme.onSecondaryContainer,
    onTrailingIconClick: (() -> Unit)? = null,
) = TextField(value = value,
    onValueChange = onValueChange,
    modifier = modifier,
    singleLine = singleLine,
    isError = isError,
    readOnly = readOnly,
    label = {
        Text(text = labelText, color = MaterialTheme.colorScheme.onSecondaryContainer.copy(0.75f))
    },
    trailingIcon = {
        trailingIconVector?.let { imageVector ->
            if (trailingIconDescription == null) throw IllegalArgumentException("a content description has to be specified if a trailing icon is provided")
            MouseOverPopup(
                text = trailingIconDescription
            ) {
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
    },
    colors = TextFieldDefaults.textFieldColors(backgroundColor = MaterialTheme.colorScheme.secondaryContainer,
        textColor = MaterialTheme.colorScheme.onSecondaryContainer,
        focusedIndicatorColor = MaterialTheme.colorScheme.onSecondaryContainer.copy(0.75f)))

