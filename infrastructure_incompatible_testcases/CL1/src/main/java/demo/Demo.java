package demo;

import lib.annotations.callgraph.IndirectCall;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Comparator;

/**
 * This class executes the code that is supposed to trigger the test case.
 *
 * @author Michael Reif
 */
public class Demo {

    private static final String DIR = System.getProperty("user.dir") + "/resources/";
    private static URL CLv1;

    static {
        try {
            CLv1 = new URL("file://" + DIR + "classloading_v1.jar");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    private static final String CLS_NAME = "lib.IntComparator";


    @IndirectCall(name = "compare", line = 38, resolvedTargets = "Llib/IntComparator;")
    public static void main(String[] args)
            throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        URLClassLoader cl = URLClassLoader.newInstance(new URL[]{CLv1}, ClassLoader.getSystemClassLoader());
        Class<?> cls = cl.loadClass(CLS_NAME);
        Comparator<Integer> comparator = (Comparator<Integer>) cls.newInstance();
        Integer one = Integer.valueOf(1);
        comparator.compare(one, one);
    }
}
