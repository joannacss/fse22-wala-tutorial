import com.ibm.wala.ipa.callgraph.*;
import com.ibm.wala.ipa.callgraph.cha.CHACallGraph;
import com.ibm.wala.ipa.callgraph.impl.Util;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.ipa.callgraph.propagation.SSAPropagationCallGraphBuilder;
import com.ibm.wala.ipa.cha.ClassHierarchyFactory;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.ipa.slicer.PDG;
import com.ibm.wala.ipa.slicer.SDG;
import com.ibm.wala.ipa.slicer.Slicer;
import com.ibm.wala.ipa.slicer.Statement;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.util.CancelException;
import com.ibm.wala.util.WalaException;
import com.ibm.wala.util.config.AnalysisScopeReader;
import com.ibm.wala.util.config.FileOfClasses;
import com.ibm.wala.util.graph.Graph;
import com.ibm.wala.util.graph.GraphSlicer;
import com.ibm.wala.viz.DotUtil;
import com.ibm.wala.viz.NodeDecorator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class WalaExample {
    private static String exFile = "./src/main/resources/Java60RegressionExclusions.txt";
    private static String jarFile = "./src/main/resources/Example3.jar";
    private static String runtimeClasses = "./src/main/resources/jdk-17.0.1/rt.jar";

    private static AnalysisScope createScope() throws IOException {


        AnalysisScope scope = AnalysisScope.createJavaAnalysisScope();
        AnalysisScopeReader.addClassPathToScope(runtimeClasses, scope, ClassLoaderReference.Primordial);
        AnalysisScopeReader.addClassPathToScope(jarFile, scope, ClassLoaderReference.Application);
        scope.setExclusions(new FileOfClasses(new FileInputStream(exFile)));
        return scope;
    }

    public static CallGraph buildChaCallGraph(AnalysisScope scope, IClassHierarchy classHierarchy) throws CancelException {
        CHACallGraph cg = new CHACallGraph(classHierarchy, true);
        Iterable<Entrypoint> entrypoints = Util.makeMainEntrypoints(scope, classHierarchy);
        cg.init(entrypoints);
        return cg;
    }

    public static CallGraph buildRtaCallGraph(AnalysisScope scope, IClassHierarchy classHierarchy) throws CallGraphBuilderCancelException {
        AnalysisOptions options = new AnalysisOptions();
        Iterable<Entrypoint> entrypoints = Util.makeMainEntrypoints(scope, classHierarchy);
        options.setEntrypoints(entrypoints);
        AnalysisCache analysisCache = new AnalysisCacheImpl();
        CallGraphBuilder<InstanceKey> cgBuilder = Util.makeRTABuilder(options, analysisCache, classHierarchy, scope);
        return cgBuilder.makeCallGraph(options, null);
    }

    public static CallGraph buildNCfaCallGraph(AnalysisScope scope, IClassHierarchy classHierarchy, int n) throws CallGraphBuilderCancelException, WalaException {
        AnalysisOptions options = new AnalysisOptions();
        Iterable<Entrypoint> entrypoints = Util.makeMainEntrypoints(scope, classHierarchy);
        options.setEntrypoints(entrypoints);
        AnalysisCache analysisCache = new AnalysisCacheImpl();
        SSAPropagationCallGraphBuilder cgBuilder = Util.makeNCFABuilder(n, options, analysisCache, classHierarchy, scope);

        CallGraph cg = cgBuilder.makeCallGraph(options, null);
        SDG<InstanceKey> sdg = new SDG<>(cg, cgBuilder.getPointerAnalysis(), Slicer.DataDependenceOptions.NO_BASE_NO_HEAP, Slicer.ControlDependenceOptions.NO_EXCEPTIONAL_EDGES);
        PDG<InstanceKey> pdg = sdg.getPDG(cg.getEntrypointNodes().iterator().next());
        DotUtil.dotify(GraphSlicer.prune(sdg, WalaExample::test),
                s -> s.toString(), "sdg-"+jarFile.split("/")[4]+".dot",
                null,
                null);
        return cg;
    }

    public static void main(String[] args) throws Exception {
        // creates analysis scope
        AnalysisScope scope = createScope();

        // creates class hierarchy
        IClassHierarchy classHierarchy = ClassHierarchyFactory.make(scope);


        // CHA Call Graph
        CallGraph chaCallGraph = buildChaCallGraph(scope, classHierarchy);

        // Converts to DOT format
        DotUtil.dotify(chaCallGraph,
                cgNode -> cgNode.getMethod().getSignature()
                , "cha-cg-"+jarFile.split("/")[4]+".dot",
                null,
                null);


        for(CGNode node: chaCallGraph.getEntrypointNodes()){
            System.out.println(node.getIR());

        }

        // RTA
        CallGraph rtaCallGraph = buildRtaCallGraph(scope, classHierarchy);
        Graph<CGNode> prunedRta = GraphSlicer.prune(rtaCallGraph, n -> n.getMethod().getDeclaringClass().getClassLoader().getReference().equals(ClassLoaderReference.Application));
        DotUtil.dotify(prunedRta,
                cgNode -> cgNode.getMethod().getSignature(),
                "rta-cg-"+jarFile.split("/")[4]+".dot",
                null,
                null);


        // n-CFA
        CallGraph oneCFA = buildNCfaCallGraph(scope, classHierarchy, 1);
        Graph<CGNode> prunedCFA = GraphSlicer.prune(oneCFA, n -> n.getMethod().getDeclaringClass().getClassLoader().getReference().equals(ClassLoaderReference.Application));
        DotUtil.dotify(prunedCFA,
                cgNode -> cgNode.getMethod().getSignature() + "@" + cgNode.getContext(),
                "1-cfa-cg-"+jarFile.split("/")[4]+".dot",
                null,
                null);

    }

    private static boolean test(Statement stmt) {
        return stmt.getNode().getMethod().getDeclaringClass().getClassLoader().getReference().equals(ClassLoaderReference.Application);
    }
}
