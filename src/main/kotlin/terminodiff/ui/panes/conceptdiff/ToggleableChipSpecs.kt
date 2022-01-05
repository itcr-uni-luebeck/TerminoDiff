package terminodiff.ui.panes.conceptdiff

import terminodiff.ui.util.ToggleableChipSpec

/**
 * add a static field for the unique identification of this chip
 */
val ToggleableChipSpec.Companion.showAll get() = "show-all"
val ToggleableChipSpec.Companion.showDifferent get() = "show-different"
val ToggleableChipSpec.Companion.showIdentical get() = "show-identical"
val ToggleableChipSpec.Companion.onlyInLeft get() = "show-only-in-left"
val ToggleableChipSpec.Companion.onlyInRight get() = "show-only-in-right"
val ToggleableChipSpec.Companion.onlyConceptDifferences get() = "show-only-concept-differences"