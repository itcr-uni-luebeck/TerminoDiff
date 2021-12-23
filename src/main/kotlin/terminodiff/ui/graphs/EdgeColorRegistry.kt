package terminodiff.ui.graphs

import net.mahdilamb.colormap.Colormap
import net.mahdilamb.colormap.Colormaps
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.awt.Color

private val logger: Logger = LoggerFactory.getLogger(EdgeColorRegistry::class.java)

class EdgeColorRegistry {
    companion object {

        private val colorMap = Colormaps.Qualitative.Tab10()
        private val propertyColorMap = mutableMapOf<String, Color>()

        fun getColor(property: String): Color = when (val color = propertyColorMap[property]) {
            null -> {
                val colorKey = (propertyColorMap.size + 1) * 0.1f - 0.01f // scale to [~0.1, ~1.0] (as long as there are less
                // than 10 properties with a code target, which should generally hold.
                // especially since child and concept relationships are both resolved to parent.
                val newColor = colorMap.get(colorKey)
                propertyColorMap[property] = newColor
                logger.info("generated color $newColor for code $property")
                newColor
            }
            else -> {
                logger.debug("retrieved color $color for code $property")
                color
            }

        }
    }
}