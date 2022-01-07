package terminodiff.java.ui;

import org.jgrapht.Graph;
import org.jungrapht.visualization.VisualizationViewer;
import org.jungrapht.visualization.control.DefaultLensGraphMouse;
import org.jungrapht.visualization.decorators.EdgeShape;
import org.jungrapht.visualization.layout.algorithms.EiglspergerLayoutAlgorithm;
import org.jungrapht.visualization.renderers.Renderer;
import terminodiff.engine.graph.DiffEdge;
import terminodiff.engine.graph.DiffNode;
import terminodiff.engine.graph.FhirConceptEdge;
import terminodiff.i18n.LocalizedStrings;

import java.awt.*;
import java.util.function.Function;

public class GraphViewer {

    private static <V, E> void configureViewerLayoutAlgorithm(VisualizationViewer<V, E> viewer) {
        EiglspergerLayoutAlgorithm<V, E> layoutAlgorithm = EiglspergerLayoutAlgorithm.<V, E>edgeAwareBuilder()
                .threaded(true)
                .postStraighten(true)
                .build();
        layoutAlgorithm.setVertexBoundsFunction(viewer.getRenderContext().getVertexBoundsFunction());
        viewer.getVisualizationModel().setLayoutAlgorithm(layoutAlgorithm);
    }

    private static <V, E> void configureTheme(VisualizationViewer<V, E> viewer, boolean isDarkTheme, boolean configureVertexColor) {
        if (isDarkTheme) {
            viewer.getComponent().setBackground(Color.DARK_GRAY);
            if (configureVertexColor) {
                viewer.getRenderContext().setVertexFillPaintFunction(c -> Color.WHITE);
            }
            viewer.setForeground(Color.WHITE);
        } else {
            if (configureVertexColor) {
                viewer.getRenderContext().setVertexFillPaintFunction(c -> Color.LIGHT_GRAY); // default red does not look good
            }
        }
    }

    public static VisualizationViewer<DiffNode, DiffEdge> configureDiffGraphViewer(
            Graph<DiffNode, DiffEdge> graph,
            boolean isDarkTheme,
            LocalizedStrings localizedStrings
    ) {

        VisualizationViewer<DiffNode, DiffEdge> viewer = VisualizationViewer.builder(graph)
                .layoutSize(new Dimension(1000, 1000))
                .viewSize(new Dimension(1000, 1000))
                .build();

        final DefaultLensGraphMouse<DiffNode, DiffEdge> graphMouse = new DefaultLensGraphMouse<>();
        viewer.setGraphMouse(graphMouse);
        viewer.setVertexToolTipFunction(n -> n.getTooltip(localizedStrings));
        viewer.setEdgeToolTipFunction(DiffEdge::getTooltip);
        viewer.getRenderContext().setEdgeShapeFunction((g, e) -> EdgeShape.CUBIC_CURVE);
        viewer.getRenderContext().setVertexLabelFunction(DiffNode::getCode);
        viewer.getRenderContext().setVertexLabelPosition(Renderer.VertexLabel.Position.SW);
        viewer.getRenderContext().setEdgeLabelFunction(DiffEdge::getPropertyCode);
        viewer.getRenderContext().setVertexFillPaintFunction(DiffNode::getColor);
        viewer.getRenderContext().setArrowFillPaintFunction(DiffEdge::getColor);
        viewer.getRenderContext().setEdgeDrawPaintFunction(DiffEdge::getColor);
        viewer.getRenderContext().setEdgeWidth(2.0f);
        viewer.getRenderContext().setVertexLabelDrawPaintFunction(c -> {
            if (isDarkTheme) return Color.WHITE;
            else return Color.BLACK;
        });
        //viewer.getRenderContext().setVertexShapeFunction(SugiyamaLayoutViewer::diffGraphVertexShape);
        configureViewerLayoutAlgorithm(viewer);
        configureTheme(viewer, isDarkTheme, false);
        return viewer;

    }

//    public static Shape diffGraphVertexShape(DiffNode diffNode) {
//        return new Rectangle()
//    }

    public static VisualizationViewer<String, FhirConceptEdge> configureCodeSystemViewer(Graph<String, FhirConceptEdge> graph,
                                                                                         boolean isDarkTheme,
                                                                                         Function<String, String> getDisplay) {

        VisualizationViewer<String, FhirConceptEdge> viewer = VisualizationViewer.builder(graph)
                .layoutSize(new Dimension(1000, 1000))
                .viewSize(new Dimension(1000, 1000))
                .build();
        viewer.setVertexToolTipFunction(getDisplay); // retrieves the display for the tooltip, in O(log n) time
        viewer.setEdgeToolTipFunction(FhirConceptEdge::getLabel);
        viewer.getRenderContext().setEdgeLabelFunction(FhirConceptEdge::getPropertyCode);
        viewer.getRenderContext().setVertexLabelFunction(String::toString); // uses the code as the label
        viewer.getRenderContext().setVertexLabelPosition(Renderer.VertexLabel.Position.CNTR);
        viewer.getRenderContext().setArrowFillPaintFunction(FhirConceptEdge::getColor); // looks up the registered edge colors to be consistent for both graphs
        viewer.getRenderContext().setEdgeDrawPaintFunction(FhirConceptEdge::getColor);
        viewer.getRenderContext().setEdgeWidth(2.0f);

        configureViewerLayoutAlgorithm(viewer);
        configureTheme(viewer, isDarkTheme, true);
        return viewer;
    }
}
