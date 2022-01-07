package terminodiff.java.ui;

import org.jgrapht.Graph;
import org.jungrapht.visualization.VisualizationViewer;
import org.jungrapht.visualization.decorators.EdgeShape;
import org.jungrapht.visualization.renderers.Renderer;
import terminodiff.engine.graph.DiffEdge;
import terminodiff.engine.graph.DiffNode;
import terminodiff.i18n.LocalizedStrings;

import java.awt.*;

public class DiffGraphJFrame extends GraphJFrame<DiffNode, DiffEdge> {

    public DiffGraphJFrame(Graph<DiffNode, DiffEdge> graph, Boolean isDarkTheme, LocalizedStrings localizedStrings) {
        super(graph, isDarkTheme, localizedStrings);
    }

    @Override
    protected void configureMainViewer(VisualizationViewer<DiffNode, DiffEdge> mainViewer) {
        mainViewer.setVertexToolTipFunction(n -> n.getTooltip(localizedStrings));
        mainViewer.setEdgeToolTipFunction(DiffEdge::getTooltip);
        mainViewer.getRenderContext().setVertexLabelFunction(DiffNode::getCode);
        mainViewer.getRenderContext().setVertexLabelPosition(Renderer.VertexLabel.Position.W);
        mainViewer.getRenderContext().setEdgeLabelFunction(DiffEdge::getPropertyCode);
        mainVisualizationViewer.getRenderContext().setEdgeWidth(2.0f);
        mainVisualizationViewer.getRenderContext().setEdgeArrowLength(5);
        mainVisualizationViewer.getRenderContext().setVertexLabelDrawPaintFunction(c -> {
            if (isDarkTheme) return Color.WHITE;
            else return Color.BLACK;
        });
    }

    @Override
    protected void configureBothViewers(VisualizationViewer<DiffNode, DiffEdge> viewer) {
        viewer.getRenderContext().setEdgeShapeFunction((g, e) -> EdgeShape.CUBIC_CURVE);
        viewer.getRenderContext().setVertexFillPaintFunction(DiffNode::getColor);
        viewer.getRenderContext().setArrowFillPaintFunction(DiffEdge::getColor);
        viewer.getRenderContext().setEdgeDrawPaintFunction(DiffEdge::getColor);
    }
}
