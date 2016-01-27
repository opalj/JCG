package expressions;

/**
 * Created by eichberg on 26.01.16.
 */
public class Map<K, V> {

    private class LinkedEntry {

        final K key;
        V value;

        private LinkedEntry nextEntry;

        LinkedEntry(K key, V value) {
            this.key = key;
            this.value = value;
        }

        public LinkedEntry getNextEntry() {
            return nextEntry;
        }

        public void setNextEntry(LinkedEntry nextEntry) {
            this.nextEntry = nextEntry;
        }

        public String toString(){
            return key.toString() + " -> " + value.toString();
        }
    }

    LinkedEntry root;
    LinkedEntry last;

    public static final Map<?,?> EMPTY = new Map<Object,Object>(){

        @Override public void add(Object o, Object o2) {
            throw new UnsupportedOperationException();
        }

        @Override public Object get(Object name) {
            return null;
        }
    };

    public Map() {

    }


    public void add(K k, V v) {
        if (root == null) {
            root = new LinkedEntry(k, v);
            last = root;
        } else {
            boolean found = false;

            LinkedEntry cur = root;
            while (cur != null) {
                if (cur.key.equals(k)) {
                    cur.value = v;
                    found = true;
                }
                cur = cur.getNextEntry();
            }

            if (!found) {
                LinkedEntry newElement = new LinkedEntry(k, v);
                last.setNextEntry(newElement);
                last = newElement;
            }
        }
    }

    public String toString() {
        String output = "Map(";

        LinkedEntry cur = root;
        while (cur != null) {
            output += cur.toString();
            if(cur != last)
                output += ",";

            cur = cur.getNextEntry();
        }

        return output + ")";
    }

    public V get(K name) {

        LinkedEntry cur = root;
        while(cur != null){
            if(cur.key.equals(name))
                return cur.value;

            cur = cur.getNextEntry();
        }

        return null;
    }


}
