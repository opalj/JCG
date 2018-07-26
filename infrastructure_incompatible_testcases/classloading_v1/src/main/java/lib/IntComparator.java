package lib;

import java.util.Comparator;

/**
 * @author Michael Reif
 */
public class IntComparator implements Comparator<Integer> {

    public int compare(Integer o1, Integer o2) {
        return o1.compareTo(o2);
    }
}
