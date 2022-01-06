package terminodiff.ui.util

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ToggleableChip(
    name: String,
    modifier: Modifier = Modifier,
    text: String,
    isSelected: Boolean = false,
    onSelectionChanged: (String) -> Unit = {},
) {
    val chipColor = MaterialTheme.colorScheme.primary.copy(alpha = if (isSelected) 1f else 0.5f)
    Surface(
        modifier = modifier.padding(4.dp),
        color = chipColor,
        tonalElevation = 4.dp,
        shape = RoundedCornerShape(4.dp)
    ) {
        Row(modifier = Modifier.toggleable(value = isSelected, onValueChange = { onSelectionChanged(name) })) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = contentColorFor(chipColor),
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}

@Composable
fun ToggleableChipGroup(
    specs: List<ToggleableChipSpec>,
    chipModifier: Modifier = Modifier,
    selectedItem: String?,
    onSelectionChanged: (String) -> Unit,
    filterCounts: Map<String, Int>
) {
    Column(modifier = Modifier.padding(2.dp)) {
        LazyRow {
            items(specs) { spec ->
                ToggleableChip(
                    name = spec.name,
                    text = "${spec.text} (${filterCounts[spec.name]})",
                    modifier = chipModifier,
                    isSelected = spec.name == selectedItem,
                    onSelectionChanged = { name ->
                        onSelectionChanged(name)
                    }
                )
            }
        }
    }
}

data class ToggleableChipSpec(
    val name: String,
    val text: String,
) {
    companion object
}