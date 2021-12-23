package terminodiff.java.ui;

import kotlin.jvm.functions.Function1;
import org.jgrapht.Graph;
import org.jungrapht.visualization.VisualizationViewer;
import org.jungrapht.visualization.layout.algorithms.EiglspergerLayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.SugiyamaLayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.sugiyama.Layering;
import org.jungrapht.visualization.layout.model.Circle;
import org.jungrapht.visualization.renderers.Renderer;
import terminodiff.engine.graph.FhirConceptDetails;
import terminodiff.engine.graph.FhirConceptEdge;

import java.awt.*;
import java.util.function.Function;

public class SugiyamaLayoutViewer {
    public static VisualizationViewer<String, FhirConceptEdge> configureViewer(Graph<String, FhirConceptEdge> graph, Function<String, String> getDisplay) {

        VisualizationViewer<String, FhirConceptEdge> viewer = VisualizationViewer.builder(graph)
                .layoutSize(new Dimension(1000, 1000))
                .viewSize(new Dimension(1000, 1000))
                .build();
        viewer.setVertexToolTipFunction(getDisplay); // retrieves the display for the tooltip, in O(log n) time
        viewer.getRenderContext().setEdgeLabelFunction(FhirConceptEdge::getPropertyCode);
        viewer.getRenderContext().setVertexLabelFunction(String::toString); // uses the code as the label
        viewer.getRenderContext().setVertexLabelPosition(Renderer.VertexLabel.Position.CNTR);
        viewer.getRenderContext().setArrowFillPaintFunction(FhirConceptEdge::getColor); // looks up the registered edge colors to be consistent for both graphs
        viewer.getRenderContext().setEdgeDrawPaintFunction(FhirConceptEdge::getColor);
        viewer.getRenderContext().setVertexFillPaintFunction(c -> Color.LIGHT_GRAY); // default red does not look good
        viewer.getRenderContext().setEdgeWidth(2.0f);

        EiglspergerLayoutAlgorithm<String, FhirConceptEdge> layoutAlgorithm = EiglspergerLayoutAlgorithm.<String, FhirConceptEdge>edgeAwareBuilder()
                .threaded(true)
                .postStraighten(true)
                .build();
        layoutAlgorithm.setVertexBoundsFunction(viewer.getRenderContext().getVertexBoundsFunction());
        viewer.getVisualizationModel().setLayoutAlgorithm(layoutAlgorithm);
        return viewer;
    }
}
