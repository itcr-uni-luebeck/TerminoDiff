package terminodiff.ui.graphs

import net.mahdilamb.colormap.Colormaps
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import terminodiff.terminodiff.engine.graph.GraphSide
import java.awt.Color

private val logger: Logger = LoggerFactory.getLogger(ColorRegistry::class.java)

class ColorRegistry {
    companion object {

        private val tab10 = Colormaps.Qualitative.Tab10()
        private val edgeColorRegistry = mutableMapOf<String, Color>()
        private val vertexColorRegistry = mutableMapOf<String, Color>()
        private val sideColorRegistry = mutableMapOf<String, Color>()

        fun getDiffGraphColor(inWhich: GraphSide): Color {
            return tab10.get((inWhich.ordinal).toFloat() * 0.1f)
        }

        fun getColor(registry: Registry, property: String): Color {
            val map = when (registry) {
                Registry.EDGES -> edgeColorRegistry
                Registry.VERTICES -> vertexColorRegistry
                Registry.SIDES -> sideColorRegistry
            }
            return when (val color = map[property]) {
                null -> {
                    val colorKey = (map.size + 1) * 0.1f - 0.01f
                    // scale to [~0.1, ~1.0] (as long as there are less
                    // than 10 properties with a code target, which should generally hold.
                    // especially since child and concept relationships are both resolved to parent.
                    val newColor = tab10.get(colorKey)
                    map[property] = newColor
                    logger.info("generated color $newColor for code $property in $registry")
                    newColor
                }
                else -> {
                    logger.debug("retrieved color $color for code $property in $registry")
                    color
                }

            }
        }
    }
}

enum class Registry {
    EDGES,
    VERTICES,
    SIDES
}