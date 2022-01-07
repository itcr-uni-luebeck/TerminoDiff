package terminodiff.java.ui;

import java.util.HashMap;
import java.util.Map;

import javolution.io.Struct;
import org.jungrapht.visualization.layout.algorithms.*;
import org.jungrapht.visualization.layout.algorithms.repulsion.BarnesHutFA2Repulsion;
import org.jungrapht.visualization.layout.algorithms.repulsion.BarnesHutFRRepulsion;
import org.jungrapht.visualization.layout.algorithms.repulsion.BarnesHutSpringRepulsion;
import org.jungrapht.visualization.layout.algorithms.repulsion.StandardFA2Repulsion;
import org.jungrapht.visualization.layout.algorithms.repulsion.StandardFRRepulsion;
import org.jungrapht.visualization.layout.algorithms.repulsion.StandardSpringRepulsion;
import org.jungrapht.visualization.layout.algorithms.sugiyama.Layering;

@SuppressWarnings("rawtypes")
public class LayoutHelperDirectedGraphs {

    @SuppressWarnings("rawtypes")
    public enum Layouts {
        KK("Kamada Kawai", KKLayoutAlgorithm.builder(), true),
        CIRCLE("Circle", CircleLayoutAlgorithm.builder().reduceEdgeCrossing(false).threaded(false)),
        REDUCE_XING_CIRCLE(
                "Reduce Xing Circle", CircleLayoutAlgorithm.builder().reduceEdgeCrossing(true)),
        SELF_ORGANIZING_MAP("Self Organizing Map", ISOMLayoutAlgorithm.builder()),
        FR(
                "Fruchterman Reingold (Not Optimized)",
                FRLayoutAlgorithm.builder().repulsionContractBuilder(StandardFRRepulsion.builder())),
        FR_BH_VISITOR(
                "Fruchterman Reingold (Barnes Hut Optimized)",
                FRLayoutAlgorithm.builder().repulsionContractBuilder(BarnesHutFRRepulsion.builder())),
        FA2(
                "ForceAtlas2 (Not Optimized)",
                ForceAtlas2LayoutAlgorithm.builder()
                        .repulsionContractBuilder(StandardFA2Repulsion.builder()),
                true),
        FA2_BH_VISITOR(
                "ForceAtlas2 (Barnes Hut Optimized)",
                ForceAtlas2LayoutAlgorithm.builder()
                        .repulsionContractBuilder(BarnesHutFA2Repulsion.builder().repulsionK(100))),
        SPRING(
                "Spring (Not Optimized)",
                SpringLayoutAlgorithm.builder()
                        .repulsionContractBuilder(StandardSpringRepulsion.builder())),
        SPRING_BH_VISITOR(
                "Spring (Barnes Hut Optimized)",
                SpringLayoutAlgorithm.builder()
                        .repulsionContractBuilder(BarnesHutSpringRepulsion.builder())),
        GEM("GEM", GEMLayoutAlgorithm.edgeAwareBuilder()),
        TREE("Tree", TreeLayoutAlgorithm.builder()),
        EDGE_AWARE_TREE("EdgeAwareTree", EdgeAwareTreeLayoutAlgorithm.edgeAwareBuilder()),
        MULTI_ROW_TREE("Multirow Tree", MultiRowTreeLayoutAlgorithm.builder()),
        TIDY_TREE("Tidy Tree", TidierTreeLayoutAlgorithm.edgeAwareBuilder()),
        TIDY_RADIAL_TREE("Tidy Radial Tree", TidierRadialTreeLayoutAlgorithm.edgeAwareBuilder()),
        BALLOON("Balloon", BalloonLayoutAlgorithm.builder()),
        RADIAL("Radial", RadialTreeLayoutAlgorithm.builder()),
        EIGLSPERGERTD(
                "Eiglsperger TopDown",
                EiglspergerLayoutAlgorithm.edgeAwareBuilder().layering(Layering.TOP_DOWN)),
        EIGLSPERGERLP(
                "Eiglsperger LongestPath",
                EiglspergerLayoutAlgorithm.edgeAwareBuilder().layering(Layering.LONGEST_PATH)),
        EIGLSPERGERNS(
                "Eiglsperger NetworkSimplex",
                EiglspergerLayoutAlgorithm.edgeAwareBuilder().layering(Layering.NETWORK_SIMPLEX)),
        EIGLSPERGERCG(
                "EiglspergerCoffmanGraham",
                EiglspergerLayoutAlgorithm.edgeAwareBuilder().layering(Layering.COFFMAN_GRAHAM)),
        SUGIYAMATD("Sugiyama TopDown", SugiyamaLayoutAlgorithm.builder().layering(Layering.TOP_DOWN)),
        SUGIYAMALP(
                "Sugiyama LongestPath", SugiyamaLayoutAlgorithm.builder().layering(Layering.LONGEST_PATH)),
        SUGIYAMANS(
                "Sugiyama NetworkSimplex",
                SugiyamaLayoutAlgorithm.builder().layering(Layering.NETWORK_SIMPLEX)),
        SUGIYAMACG(
                "Sugiyama CoffmanGraham",
                SugiyamaLayoutAlgorithm.builder().layering(Layering.COFFMAN_GRAHAM)),
        SUGIYAMA("Sugiyama", SugiyamaLayoutAlgorithm.builder()),
        MINCROSS("MinCross", HierarchicalMinCrossLayoutAlgorithm.builder());

        private final Boolean isSlow;

        Layouts(String name, LayoutAlgorithm.Builder layoutAlgorithmBuilder, Boolean isSlow) {
            this.name = name;
            this.layoutAlgorithmBuilder = layoutAlgorithmBuilder;
            this.isSlow = isSlow;
        }

        Layouts(String name, LayoutAlgorithm.Builder layoutAlgorithmBuilder) {
            this.name = name;
            this.layoutAlgorithmBuilder = layoutAlgorithmBuilder;
            this.isSlow = false;
        }

        private final String name;

        private final LayoutAlgorithm.Builder layoutAlgorithmBuilder;

        public LayoutAlgorithm.Builder getLayoutAlgorithmBuilder() {
            return layoutAlgorithmBuilder;
        }

        @Override
        public String toString() {
            if (isSlow) {
                return String.format("%s (slow!)", name);
            } else return name;
        }

        public LayoutAlgorithm getLayoutAlgorithm() {
            return layoutAlgorithmBuilder.build();
        }
    }

    public static Layouts[] getCombos() {
        return Layouts.values();
    }
}

