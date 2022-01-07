package terminodiff.java.ui;

import org.jgrapht.Graph;
import org.jungrapht.visualization.VisualizationViewer;
import org.jungrapht.visualization.renderers.Renderer;
import terminodiff.engine.graph.FhirConceptEdge;
import terminodiff.i18n.LocalizedStrings;

import java.util.function.Function;

public class CodeSystemGraphJFrame extends GraphJFrame<String, FhirConceptEdge> {

    private final Function<String, String> getDisplay;

    public CodeSystemGraphJFrame(Graph<String, FhirConceptEdge> graph,
                                 Boolean isDarkTheme,
                                 LocalizedStrings localizedStrings,
                                 Function<String, String> getDisplay
    ) {
        super(graph, isDarkTheme, localizedStrings);
        this.getDisplay = getDisplay;
    }

    @Override
    protected void configureMainViewer(VisualizationViewer<String, FhirConceptEdge> mainViewer) {
        mainViewer.setVertexToolTipFunction(getDisplay); // retrieves the display for the tooltip, in O(log n) time
        mainViewer.setEdgeToolTipFunction(FhirConceptEdge::getLabel);
        mainViewer.getRenderContext().setEdgeLabelFunction(FhirConceptEdge::getPropertyCode);
        mainViewer.getRenderContext().setVertexLabelFunction(String::toString); // uses the code as the label
        mainViewer.getRenderContext().setVertexLabelPosition(Renderer.VertexLabel.Position.CNTR);
    }

    @Override
    protected void configureBothViewers(VisualizationViewer<String, FhirConceptEdge> viewer) {
        viewer.getRenderContext().setArrowFillPaintFunction(FhirConceptEdge::getColor); // looks up the registered edge colors to be consistent for both graphs
        viewer.getRenderContext().setEdgeDrawPaintFunction(FhirConceptEdge::getColor);
    }
}
