package cl;

import lib.annotations.callgraph.IndirectCall;
import lib.annotations.callgraph.IndirectCalls;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Comparator;

/**
 * The main method of this class triggers a test case where where two classes are loaded in an
 * incompatible version using different class loaders. After those different versioned classes are
 * loaded, methods are called on the classes which must be resolved to different targets.
 *
 * @author Michael Reif
 */
public class Demo {

    //private static final String DIR = System.getProperty("user.dir") + "/resources/";
    private static final String DIR = "/Users/mreif/Programming/git/jcg/infrastructure_incompatible_testcases/CL3/src/resources/";
    private static URL CLv1;
    private static URL CLv2;
    private static final String CLS_NAME = "lib.IntComparator";

    static {
        try {
            CLv1 = new URL("file://" + DIR + "classloading-version-1.jar");
            CLv2 = new URL("file://" + DIR + "classloading-version-2.jar");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    @IndirectCalls({
        @IndirectCall(name = "compare", line = 50, resolvedTargets = "Llib/IntComparator;"),
        @IndirectCall(name = "compare", line = 51, resolvedTargets = "Llib/IntCompare;")
    })
    public static void main(String[] args)
            throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        URLClassLoader oldVersionLoader = new URLClassLoader(new URL[] {CLv1},
                Thread.currentThread().getContextClassLoader());
        URLClassLoader newVersionLoader= new URLClassLoader(new URL[] {CLv2},
                Thread.currentThread().getContextClassLoader());

        System.out.println(CLv1.toExternalForm());

        Class<?> oldClass = oldVersionLoader.loadClass(CLS_NAME);
        Class<?> newClass = newVersionLoader.loadClass(CLS_NAME);

        Comparator<Integer> oldComparator = (Comparator<Integer>) oldClass.newInstance();
        Comparator<Integer> newComparator = (Comparator<Integer>) newClass.newInstance();

        oldComparator.compare(0,1);
        newComparator.compare(1,2);
    }
}