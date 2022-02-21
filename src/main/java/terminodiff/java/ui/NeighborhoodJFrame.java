package terminodiff.java.ui;

import org.jgrapht.Graph;
import org.jungrapht.visualization.VisualizationViewer;
import org.jungrapht.visualization.decorators.EdgeShape;
import org.jungrapht.visualization.renderers.Renderer.VertexLabel.Position;
import terminodiff.i18n.LocalizedStrings;
import terminodiff.terminodiff.engine.graph.CombinedEdge;
import terminodiff.terminodiff.engine.graph.CombinedVertex;

import javax.swing.*;
import java.awt.*;

public class NeighborhoodJFrame extends GraphJFrame<CombinedVertex, CombinedEdge> {
    final private JButton removeLayerButton = new JButton();
    final private JButton addLayerButton = new JButton();
    final private JLabel layerText = new JLabel();
    private int currentLayer = 1;

    public void setCurrentLayer(int newLayer) {
        currentLayer = newLayer;
        layerText.setText(String.valueOf(newLayer));
    }

    public NeighborhoodJFrame(Graph<CombinedVertex, CombinedEdge> graph, Boolean isDarkTheme, LocalizedStrings localizedStrings, String frameTitle) {
        super(graph, isDarkTheme, localizedStrings, frameTitle);
        removeLayerButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        removeLayerButton.setText(localizedStrings.getRemoveLayer());
        addLayerButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        addLayerButton.setText(localizedStrings.getAddLayer());
        layerText.setText(String.valueOf(currentLayer));
        layerText.setAlignmentX(Component.CENTER_ALIGNMENT);
        addBottomControls();
    }

    public void addClickListener(NeighborhoodChangeLayer neighborhoodChangeLayer) {
        removeLayerButton.addActionListener(e -> setCurrentLayer(neighborhoodChangeLayer.changeLayer(-1)));
        addLayerButton.addActionListener(e -> setCurrentLayer(neighborhoodChangeLayer.changeLayer(1)));
    }

    @SuppressWarnings("DuplicatedCode")
    @Override
    protected void configureMainViewer(VisualizationViewer<CombinedVertex, CombinedEdge> mainViewer) {
        mainViewer.setVertexToolTipFunction(n -> n.getTooltip(localizedStrings));
        mainViewer.setEdgeToolTipFunction(CombinedEdge::getTooltip);
        mainViewer.getRenderContext().setVertexLabelFunction(CombinedVertex::getCode);
        mainViewer.getRenderContext().setVertexLabelPosition(Position.W);
        mainViewer.getRenderContext().setEdgeLabelFunction(CombinedEdge::getProperty);
        mainVisualizationViewer.getRenderContext().setEdgeWidth(2.0f);
        mainVisualizationViewer.getRenderContext().setEdgeArrowLength(5);
    }

    @Override
    protected void configureBothViewers(VisualizationViewer<CombinedVertex, CombinedEdge> viewer) {
        viewer.getRenderContext().setVertexLabelDrawPaintFunction(CombinedVertex::getColor);
        viewer.getRenderContext().setEdgeShapeFunction((g, e) -> EdgeShape.CUBIC_CURVE);
        viewer.getRenderContext().setVertexFillPaintFunction(CombinedVertex::getColor);
        viewer.getRenderContext().setArrowFillPaintFunction(CombinedEdge::getColor);
        viewer.getRenderContext().setEdgeDrawPaintFunction(CombinedEdge::getColor);
    }

    private void addBottomControls() {
        JPanel bottomPanel = new JPanel(new GridLayout(1, 3));
        JComponent removeLayer = ControlHelpers.getContainer(Box.createHorizontalBox(), ControlHelpers.getCenteredContainer(localizedStrings.getRemoveLayer(), removeLayerButton));
        JComponent labelLayer = ControlHelpers.getContainer(Box.createHorizontalBox(), ControlHelpers.getCenteredContainer(localizedStrings.getLayers(), layerText));
        JComponent addLayer = ControlHelpers.getContainer(Box.createHorizontalBox(), ControlHelpers.getCenteredContainer(localizedStrings.getAddLayer(), addLayerButton));
        bottomPanel.add(removeLayer);
        bottomPanel.add(labelLayer);
        bottomPanel.add(addLayer);
        container.add(bottomPanel, BorderLayout.SOUTH);
    }
}