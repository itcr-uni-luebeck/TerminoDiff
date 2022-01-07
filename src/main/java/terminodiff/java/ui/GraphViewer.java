package terminodiff.java.ui;

import org.apache.commons.lang3.builder.Diff;
import org.jgrapht.Graph;
import org.jungrapht.visualization.SatelliteVisualizationViewer;
import org.jungrapht.visualization.VisualizationModel;
import org.jungrapht.visualization.VisualizationViewer;
import org.jungrapht.visualization.control.DefaultGraphMouse;
import org.jungrapht.visualization.control.DefaultSatelliteGraphMouse;
import org.jungrapht.visualization.decorators.EdgeShape;
import org.jungrapht.visualization.layout.algorithms.EiglspergerLayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.LayoutAlgorithm;
import org.jungrapht.visualization.renderers.Renderer;
import terminodiff.engine.graph.DiffEdge;
import terminodiff.engine.graph.DiffNode;
import terminodiff.engine.graph.FhirConceptEdge;
import terminodiff.i18n.LocalizedStrings;

import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.function.Function;

public class GraphViewer {

    private static <V, E> EiglspergerLayoutAlgorithm<V, E> configureLayoutAlgorithm() {
        return EiglspergerLayoutAlgorithm.<V, E>edgeAwareBuilder()
                .threaded(true)
                .postStraighten(true)
                .build();
    }

    @SuppressWarnings("unchecked")
    private static <V, E> VisualizationModel<V, E> configureVisualizationModel(Graph<V, E> graph, LayoutAlgorithm<V> layoutAlgorithm, Dimension layoutSize) {
        return VisualizationModel.builder(graph)
                .layoutAlgorithm(layoutAlgorithm)
                .layoutSize(layoutSize)
                .build();
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
        Dimension preferredSizeMain = new Dimension(1000, 1000);
        Dimension preferredSizeSatellite = new Dimension(250, 250);
        Dimension layoutSize = new Dimension(1000, 1000);

        EiglspergerLayoutAlgorithm<DiffNode, DiffEdge> layoutAlgorithm = configureLayoutAlgorithm();

        VisualizationModel<DiffNode, DiffEdge> visualizationModel = configureVisualizationModel(graph, layoutAlgorithm, layoutSize);

        final DefaultGraphMouse<DiffNode, DiffEdge> graphMouse = new DefaultGraphMouse<>();

        final VisualizationViewer<DiffNode, DiffEdge> mainVisualizationViewer = VisualizationViewer.builder(visualizationModel)
                .graphMouse(graphMouse)
                .viewSize(preferredSizeMain)
                .build();

        final SatelliteVisualizationViewer<DiffNode, DiffEdge> satelliteVisualizationViewer = SatelliteVisualizationViewer.builder(mainVisualizationViewer)
                .viewSize(preferredSizeSatellite)
                .graphMouse(DefaultSatelliteGraphMouse.builder().build())
                .transparent(false)
                .build();

        layoutAlgorithm.setVertexBoundsFunction(mainVisualizationViewer.getRenderContext().getVertexBoundsFunction());
        layoutAlgorithm.runAfter();

        /*VisualizationViewer<DiffNode, DiffEdge> viewer = VisualizationViewer.builder(graph)
                .layoutSize(new Dimension(1000, 1000))
                .viewSize(new Dimension(1000, 1000))
                .build();*/

        /*final DefaultLensGraphMouse<DiffNode, DiffEdge> graphMouse = new DefaultLensGraphMouse<>();
        viewer.setGraphMouse(graphMouse);*/
        mainVisualizationViewer.setVertexToolTipFunction(n -> n.getTooltip(localizedStrings));
        mainVisualizationViewer.setEdgeToolTipFunction(DiffEdge::getTooltip);
        mainVisualizationViewer.getRenderContext().setEdgeShapeFunction((g, e) -> EdgeShape.CUBIC_CURVE);
        mainVisualizationViewer.getRenderContext().setVertexLabelFunction(DiffNode::getCode);
        mainVisualizationViewer.getRenderContext().setVertexLabelPosition(Renderer.VertexLabel.Position.W);
        mainVisualizationViewer.getRenderContext().setEdgeLabelFunction(DiffEdge::getPropertyCode);
        mainVisualizationViewer.getRenderContext().setVertexFillPaintFunction(DiffNode::getColor);
        mainVisualizationViewer.getRenderContext().setArrowFillPaintFunction(DiffEdge::getColor);
        mainVisualizationViewer.getRenderContext().setEdgeDrawPaintFunction(DiffEdge::getColor);

        satelliteVisualizationViewer.getRenderContext().setVertexFillPaintFunction(DiffNode::getColor);
        satelliteVisualizationViewer.getRenderContext().setArrowFillPaintFunction(DiffEdge::getColor);
        satelliteVisualizationViewer.getRenderContext().setEdgeDrawPaintFunction(DiffEdge::getColor);

        mainVisualizationViewer.getRenderContext().setEdgeWidth(2.0f);
        mainVisualizationViewer.getRenderContext().setEdgeArrowLength(5);
        mainVisualizationViewer.getRenderContext().setVertexLabelDrawPaintFunction(c -> {
            if (isDarkTheme) return Color.WHITE;
            else return Color.BLACK;
        });
        //viewer.getRenderContext().setVertexShapeFunction(SugiyamaLayoutViewer::diffGraphVertexShape);
        //configureViewerLayoutAlgorithm(viewer);
        configureTheme(mainVisualizationViewer, isDarkTheme, false);
        mainVisualizationViewer.scaleToLayout();
        satelliteVisualizationViewer.scaleToLayout();

        mainVisualizationViewer.getComponent().setLayout(null);
        mainVisualizationViewer.add(satelliteVisualizationViewer.getComponent());
        Dimension satelliteVisualizationViewerSize = satelliteVisualizationViewer.getSize();

        mainVisualizationViewer.getComponent()
                .addComponentListener(new ComponentAdapter() {
                    @Override
                    public void componentResized(ComponentEvent e) {
                        super.componentResized(e);
                        Component vv = e.getComponent();
                        Dimension vvd = vv.getSize();
                        Point p = new Point(vvd.width - satelliteVisualizationViewerSize.width, vvd.height - satelliteVisualizationViewerSize.height);
                        satelliteVisualizationViewer
                                .getComponent()
                                .setBounds(p.x, p.y, satelliteVisualizationViewerSize.width, satelliteVisualizationViewerSize.height);
                    }
                });

        return mainVisualizationViewer;
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

        EiglspergerLayoutAlgorithm<String, FhirConceptEdge> layoutAlgorithm = configureLayoutAlgorithm();
        layoutAlgorithm.setVertexBoundsFunction(viewer.getRenderContext().getVertexBoundsFunction());
        viewer.getVisualizationModel().setLayoutAlgorithm(layoutAlgorithm);
        configureTheme(viewer, isDarkTheme, true);
        return viewer;
    }
}
