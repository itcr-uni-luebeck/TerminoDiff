package terminodiff.java.ui;

import org.jgrapht.Graph;
import org.jungrapht.visualization.VisualizationViewer;
import org.jungrapht.visualization.renderers.Renderer;
import terminodiff.engine.graph.FhirConceptEdge;
import terminodiff.i18n.LocalizedStrings;
  import terminodiff.ui.graphs.Registry;

import javax.swing.*;
import java.awt.*;
import java.util.function.Function;

public class CodeSystemGraphJFrame extends GraphJFrame<String, FhirConceptEdge> {

    private final Function<String, String> getDisplay;

    public CodeSystemGraphJFrame(Graph<String, FhirConceptEdge> graph,
                                 Boolean isDarkTheme,
                                 LocalizedStrings localizedStrings,
                                 String frameTitle,
                                 Function<String, String> getDisplay
    ) {
        super(graph, isDarkTheme, localizedStrings, frameTitle);
        addButtomControls();
        this.getDisplay = getDisplay;
    }

    @Override
    protected void configureMainViewer(VisualizationViewer<String, FhirConceptEdge> mainViewer) {
        mainViewer.setVertexToolTipFunction(getDisplay); // retrieves the display for the tooltip, in O(log n) time
        mainViewer.setEdgeToolTipFunction(FhirConceptEdge::getLabel);
        mainViewer.getRenderContext().setEdgeLabelFunction(FhirConceptEdge::getPropertyCode);
        mainViewer.getRenderContext().setVertexLabelFunction(String::toString); // uses the code as the label
        mainViewer.getRenderContext().setVertexLabelPosition(Renderer.VertexLabel.Position.W);
        mainViewer.getRenderContext().setVertexLabelDrawPaintFunction(vl -> {
            if (isDarkTheme) return Color.LIGHT_GRAY;
            else return Color.DARK_GRAY;
        });
    }

    @Override
    protected void configureBothViewers(VisualizationViewer<String, FhirConceptEdge> viewer) {
        viewer.getRenderContext().setArrowFillPaintFunction(FhirConceptEdge::getColor); // looks up the registered edge colors to be consistent for both graphs
        viewer.getRenderContext().setEdgeDrawPaintFunction(FhirConceptEdge::getColor);
        viewer.getRenderContext().setVertexFillPaintFunction(v -> {
            if (isDarkTheme) return Color.LIGHT_GRAY;
            else return Color.DARK_GRAY;
        });
    }

    private void addButtomControls() {
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottomPanel.add(getEdgeColorLegend(localizedStrings.getLegend()));
        container.add(bottomPanel, BorderLayout.SOUTH);
    }
}
