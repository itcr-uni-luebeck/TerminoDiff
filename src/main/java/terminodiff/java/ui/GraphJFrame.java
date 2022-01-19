package terminodiff.java.ui;

import org.jgrapht.Graph;
import org.jungrapht.visualization.SatelliteVisualizationViewer;
import org.jungrapht.visualization.VisualizationModel;
import org.jungrapht.visualization.VisualizationViewer;
import org.jungrapht.visualization.control.DefaultGraphMouse;
import org.jungrapht.visualization.control.DefaultSatelliteGraphMouse;
import org.jungrapht.visualization.layout.algorithms.LayoutAlgorithm;
import terminodiff.i18n.LocalizedStrings;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.List;

/**
 * combined implementation from the samples in jungraph-visualization: SatelliteViewRefactoredMouseDemo and
 * EiglspergerWithGhidraGraphInputExp
 *
 * @param <V> the vertex class
 * @param <E> the edge class
 */
public abstract class GraphJFrame<V, E> extends JFrame {

    protected final DefaultGraphMouse<V, E> graphMouse = new DefaultGraphMouse<>();
    protected final JPanel container = new JPanel(new BorderLayout());
    protected final LocalizedStrings localizedStrings;
    protected boolean isDarkTheme;

    @SuppressWarnings("unchecked")
    protected LayoutAlgorithm<V> layoutAlgorithm = LayoutHelperDirectedGraphs.Layouts.EIGLSPERGERLP.getLayoutAlgorithm();

    protected LayoutHelperDirectedGraphs.Layouts[] combos = LayoutHelperDirectedGraphs.getCombos();
    protected final JComboBox<LayoutHelperDirectedGraphs.Layouts> layoutComboBox = new JComboBox<>(combos);
    protected VisualizationModel<V, E> visualizationModel;
    protected VisualizationViewer<V, E> mainVisualizationViewer;
    protected SatelliteVisualizationViewer<V, E> satelliteVisualizationViewer;

    protected abstract void configureMainViewer(VisualizationViewer<V, E> mainViewer);

    protected abstract void configureBothViewers(VisualizationViewer<V, E> viewer);

    protected void configureSatelliteViewer(VisualizationViewer<V, E> satelliteViewer) {
    }

    public GraphJFrame(Graph<V, E> graph, Boolean isDarkTheme, LocalizedStrings localizedStrings, String frameTitle) {
        this(new Dimension(1000, 1000), new Dimension(200, 200), new Dimension(1000, 100), graph, isDarkTheme, localizedStrings, frameTitle);
    }

    public GraphJFrame(Dimension preferredSizeMain, Dimension preferredSizeSatellite, Dimension layoutSize, Graph<V, E> graph, Boolean isDarkTheme, LocalizedStrings localizedStrings, String frameTitle) {
        this.isDarkTheme = isDarkTheme;
        this.localizedStrings = localizedStrings;
        visualizationModel = configureVisualizationModel(graph, layoutSize);
        visualizationModel.setLayoutAlgorithm(layoutAlgorithm);

        mainVisualizationViewer = VisualizationViewer.builder(visualizationModel).graphMouse(graphMouse).viewSize(preferredSizeMain).build();
        satelliteVisualizationViewer = SatelliteVisualizationViewer.builder(mainVisualizationViewer).viewSize(preferredSizeSatellite).graphMouse(DefaultSatelliteGraphMouse.builder().build()).transparent(false).build();

        configureViewers(mainVisualizationViewer, satelliteVisualizationViewer);

        configureTheme(mainVisualizationViewer, isDarkTheme);
        mainVisualizationViewer.scaleToLayout();
        satelliteVisualizationViewer.scaleToLayout();

        configureLayoutComboBox();

        mainVisualizationViewer.getComponent().setLayout(null);
        mainVisualizationViewer.add(satelliteVisualizationViewer.getComponent());
        Dimension satelliteVisualizationViewerSize = satelliteVisualizationViewer.getSize();
        configureResizeHandler(mainVisualizationViewer, satelliteVisualizationViewer, satelliteVisualizationViewerSize);

        JPanel controlPanel = new JPanel(new GridLayout(1, 1));
        JComponent top = ControlHelpers.getContainer(Box.createHorizontalBox(), ControlHelpers.getCenteredContainer("Layouts", layoutComboBox));
        controlPanel.add(top);

        container.add(controlPanel, BorderLayout.NORTH);
        container.add(mainVisualizationViewer.getComponent(), BorderLayout.CENTER);
        getContentPane().add(container);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setTitle(frameTitle);
        pack();
        setVisible(true);
    }

    private void configureViewers(VisualizationViewer<V, E> mainVisualizationViewer, SatelliteVisualizationViewer<V, E> satelliteVisualizationViewer) {
        List.of(mainVisualizationViewer, satelliteVisualizationViewer).forEach(this::configureBothViewers);
        configureMainViewer(mainVisualizationViewer);
        configureSatelliteViewer(satelliteVisualizationViewer);
    }

    @SuppressWarnings("unchecked")
    private void configureLayoutComboBox() {
        layoutComboBox.addActionListener(e -> SwingUtilities.invokeLater(() -> {
            LayoutHelperDirectedGraphs.Layouts layoutType = (LayoutHelperDirectedGraphs.Layouts) layoutComboBox.getSelectedItem();
            assert layoutType != null;
            LayoutAlgorithm<V> layoutAlgorithm = layoutType.getLayoutAlgorithm();

            visualizationModel.setLayoutAlgorithm(layoutAlgorithm);
        }));
        layoutComboBox.setSelectedItem(LayoutHelperDirectedGraphs.Layouts.EIGLSPERGERLP);
    }

    private void configureResizeHandler(VisualizationViewer<V, E> mainViewer, SatelliteVisualizationViewer<V, E> satelliteViewer, Dimension satViewerSize) {
        mainViewer.getComponent().addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                super.componentResized(e);
                Component vv = e.getComponent();
                Dimension vvd = vv.getSize();
                Point p = new Point(vvd.width - satViewerSize.width, vvd.height - satViewerSize.height);
                satelliteViewer.getComponent().setBounds(p.x, p.y, satViewerSize.width, satViewerSize.height);
            }
        });
    }

    private void configureTheme(VisualizationViewer<V, E> viewer, boolean isDarkTheme) {
        if (isDarkTheme) {
            viewer.getComponent().setBackground(Color.DARK_GRAY);
            viewer.getRenderContext().setVertexFillPaintFunction(c -> Color.WHITE);
            viewer.setForeground(Color.WHITE);
        }
    }

    @SuppressWarnings("unchecked")
    private VisualizationModel<V, E> configureVisualizationModel(Graph<V, E> graph, Dimension layoutSize) {
        return VisualizationModel.builder(graph).layoutAlgorithm(layoutAlgorithm).layoutSize(layoutSize).build();
    }

}
