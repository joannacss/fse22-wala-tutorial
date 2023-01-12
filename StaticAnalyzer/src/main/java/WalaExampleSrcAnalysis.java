import com.ibm.wala.cast.ir.ssa.AstIRFactory;
import com.ibm.wala.cast.java.ipa.callgraph.JavaSourceAnalysisScope;
import com.ibm.wala.cast.java.translator.jdt.ecj.ECJClassLoaderFactory;
import com.ibm.wala.classLoader.ClassLoaderFactory;
import com.ibm.wala.classLoader.JarFileModule;
import com.ibm.wala.classLoader.SourceFileModule;
import com.ibm.wala.ipa.callgraph.*;
import com.ibm.wala.ipa.callgraph.impl.Util;
import com.ibm.wala.ipa.callgraph.propagation.SSAPropagationCallGraphBuilder;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.ipa.cha.ClassHierarchyFactory;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.ssa.SSAOptions;
import com.ibm.wala.ssa.SymbolTable;
import com.ibm.wala.util.CancelException;
import com.ibm.wala.util.config.FileOfClasses;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.jar.JarFile;

public class WalaExampleSrcAnalysis {

    public static JavaSourceAnalysisScope createScope(File sourceFile) throws IOException {
        JavaSourceAnalysisScope scope = new JavaSourceAnalysisScope();
        // adds primordial (built-in) classes
        scope.addToScope(scope.getPrimordialLoader(), new JarFileModule(new JarFile(WalaExample.JAVA_RUNTIME_8, false)));
        // adds application classes (source code)
        scope.addToScope(scope.getSourceLoader(), new SourceFileModule(sourceFile, sourceFile.getName(), null));
        // sets exclusion file
        scope.setExclusions(new FileOfClasses(new FileInputStream(WalaExample.EXCLUSION_FILE)));
        return scope;
    }


    public static CallGraph buildNCfaCallGraph(JavaSourceAnalysisScope scope, IClassHierarchy classHierarchy, int n) throws CallGraphBuilderCancelException {
        AnalysisOptions options = new AnalysisOptions();
        options.setAnalysisScope(scope);

        SSAOptions ssaOptions = new SSAOptions();
        ssaOptions.setDefaultValues(SymbolTable::getDefaultValue);

        options.setSSAOptions(ssaOptions);
        Iterable<Entrypoint> entrypoints = Util.makeMainEntrypoints(scope.getSourceLoader(), classHierarchy);
        options.setEntrypoints(entrypoints);
        AnalysisCache analysisCache = new AnalysisCacheImpl(AstIRFactory.makeDefaultFactory(), options.getSSAOptions());
        SSAPropagationCallGraphBuilder builder = Util.makeNCFABuilder(n, options, analysisCache, classHierarchy, scope);

        return builder.makeCallGraph(options, null);
    }


    public static void main(String[] args) throws IOException, CancelException, ClassHierarchyException {

        File project = new File("./src/main/resources/Example1.java");
//        System.out.println(new File(".").getAbsolutePath());
//        ECJJavaSourceAnalysisEngine engine = new ECJJavaSourceAnalysisEngine();
//        engine.addSystemModule(new JarFileModule(new JarFile(JAVA_RUNTIME, false)));
//        engine.setExclusionsFile(EXCLUSION_FILE);
//        engine.addSourceModule(new SourceFileModule(project, project.getName(), null));
//        engine.buildAnalysisScope();
//        IClassHierarchy cha = engine.buildClassHierarchy();
//        System.out.println("Classes: " + cha.getNumberOfClasses());
//        System.out.println(cha.getScope().toString());


        // creates class hierarchy
        JavaSourceAnalysisScope scope = createScope(project);
        ClassLoaderFactory factory = new ECJClassLoaderFactory(scope.getExclusions());
        IClassHierarchy classHierarchy = ClassHierarchyFactory.make(scope, factory);

        System.out.println("Classes: " + classHierarchy.getNumberOfClasses());
        System.out.println(classHierarchy.getScope().toString());


    }
}


///**
// * This is a simple example WALA application that uses WALA to build a call graph from a source file.
// *
// * @author Joanna C. S. Santos
// */
//public class WalaExampleSrcAnalysis {
//    private static String EXCLUSION_FILE = "./src/main/resources/Java60RegressionExclusions.txt";
//    private static String JAVA_RUNTIME = "./src/main/resources/jdk-17.0.1/rt.jar";
//
//    public static JavaSourceAnalysisScope createScope(File sourceFile) throws IOException {
//        JavaSourceAnalysisScope scope = new JavaSourceAnalysisScope();
//        // adds primordial (built-in) classes
//        scope.addToScope(scope.getPrimordialLoader(), new JarFile(JAVA_RUNTIME, false));
//        // adds application classes (source code)
//        scope.addToScope(scope.getSourceLoader(), new SourceFileModule(sourceFile, sourceFile.getName(), null));
//        // sets exclusion file
//        scope.setExclusions(new FileOfClasses(new FileInputStream(EXCLUSION_FILE)));
//        return scope;
//    }
//
//    public static CallGraph buildChaCallGraph(JavaSourceAnalysisScope scope, IClassHierarchy classHierarchy) throws CancelException {
//        CHACallGraph cg = new CHACallGraph(classHierarchy, true);
//        Iterable<Entrypoint> entrypoints = Util.makeMainEntrypoints(scope.getSourceLoader(), classHierarchy);
//        cg.init(entrypoints);
//        return cg;
//    }
//
//    public static CallGraph buildRtaCallGraph(AnalysisScope scope, IClassHierarchy classHierarchy) throws CallGraphBuilderCancelException {
//        AnalysisOptions options = new AnalysisOptions();
//        Iterable<Entrypoint> entrypoints = Util.makeMainEntrypoints(scope, classHierarchy);
//        options.setEntrypoints(entrypoints);
//        AnalysisCache analysisCache = new AnalysisCacheImpl();
//        CallGraphBuilder<InstanceKey> cgBuilder = Util.makeRTABuilder(options, analysisCache, classHierarchy, scope);
//        return cgBuilder.makeCallGraph(options, null);
//    }
//
//    public static CallGraph buildNCfaCallGraph(AnalysisScope scope, IClassHierarchy classHierarchy, int n) throws CallGraphBuilderCancelException {
//        AnalysisOptions options = new AnalysisOptions();
//        Iterable<Entrypoint> entrypoints = Util.makeMainEntrypoints(scope, classHierarchy);
//        options.setEntrypoints(entrypoints);
//        AnalysisCache analysisCache = new AnalysisCacheImpl();
//        SSAPropagationCallGraphBuilder builder = Util.makeNCFABuilder(n, options, analysisCache, classHierarchy, scope);
//
//        return builder.makeCallGraph(options, null);
//    }
//
//    public static void main(String[] args) throws Exception {
//        String exampleFile = "./src/main/resources/Example1.java";
//
//        // creates analysis scope
//        JavaSourceAnalysisScope scope = createScope(new File(exampleFile));
//
//        // creates class hierarchy
//        IClassHierarchy classHierarchy = ClassHierarchyFactory.make(scope);
//
//
//        // CHA Call Graph
//        CallGraph chaCallGraph = buildChaCallGraph(scope, classHierarchy);
//
//        // Converts to DOT format
//        DotUtil.dotify(chaCallGraph,
//                cgNode -> cgNode.getMethod().getSignature()
//                , "cha-cg-" + exampleFile.split("/")[4] + ".dot",
//                null,
//                null);
//
//
//        for (CGNode node : chaCallGraph.getEntrypointNodes()) {
//            System.out.println(node.getIR());
//        }
//
//        // RTA
//        CallGraph rtaCallGraph = buildRtaCallGraph(scope, classHierarchy);
//        Graph<CGNode> prunedRta = GraphSlicer.prune(rtaCallGraph, n -> n.getMethod().getDeclaringClass().getClassLoader().getReference().equals(ClassLoaderReference.Application));
//        DotUtil.dotify(prunedRta,
//                cgNode -> cgNode.getMethod().getSignature(),
//                "rta-cg-" + exampleFile.split("/")[4] + ".dot",
//                null,
//                null);
//
//
//        // n-CFA
//        CallGraph oneCFA = buildNCfaCallGraph(scope, classHierarchy, 1);
//        Graph<CGNode> prunedCFA = GraphSlicer.prune(oneCFA, n -> n.getMethod().getDeclaringClass().getClassLoader().getReference().equals(ClassLoaderReference.Application));
//        DotUtil.dotify(prunedCFA,
//                cgNode -> cgNode.getMethod().getSignature() + "@" + cgNode.getContext(),
//                "1-cfa-cg-" + exampleFile.split("/")[4] + ".dot",
//                null,
//                null);
//
//    }
//
//
//}
