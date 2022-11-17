import com.ibm.wala.ipa.callgraph.AnalysisCacheImpl;
import com.ibm.wala.ipa.callgraph.AnalysisOptions;
import com.ibm.wala.ipa.callgraph.AnalysisScope;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.impl.Util;
import com.ibm.wala.ipa.callgraph.propagation.SSAPropagationCallGraphBuilder;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.ipa.cha.ClassHierarchyFactory;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.ssa.DefaultIRFactory;
import com.ibm.wala.ssa.IRFactory;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.util.CancelException;
import com.ibm.wala.util.config.AnalysisScopeReader;
import com.ibm.wala.util.config.FileOfClasses;
import com.ibm.wala.viz.viewer.WalaViewer;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Locale;

public class LiveDemo {
    public static void main(String[] args) throws IOException, CancelException, ClassHierarchyException {
        AnalysisScope scope = AnalysisScope.createJavaAnalysisScope();
        AnalysisScopeReader.addClassPathToScope("./src/main/resources/Example1.jar",scope, ClassLoaderReference.Application);
        AnalysisScopeReader.addClassPathToScope("./src/main/resources/jdk-17.0.1/rt.jar",scope, ClassLoaderReference.Primordial);
        scope.setExclusions(new FileOfClasses(new FileInputStream("./src/main/resources/Java60RegressionExclusions.txt")));

        IClassHierarchy classHierarchy = ClassHierarchyFactory.make(scope);
        System.out.println(scope);
        AnalysisOptions options = new AnalysisOptions();
        options.setEntrypoints(Util.makeMainEntrypoints(scope, classHierarchy));
        SSAPropagationCallGraphBuilder builder = Util.makeNCFABuilder(1, options, new AnalysisCacheImpl(), classHierarchy, scope);
        CallGraph callGraph = builder.makeCallGraph(options);

        new WalaViewer(callGraph, builder.getPointerAnalysis());

        


    }
}
