package expressions;

import annotations.CGNote;

/**
 * Created by eichberg on 27.01.16.
 */
public class Stack<V> {

    private V[] data = (V[]) new Object[100];
    private int entries = 0;

    public Stack() {}

    @CGNote(value ="[POT_CALLBACK]",
            description = "potential callback because an object type is passed to a native method;" +
                    "methods of this object could be called from native code (I.e. toString, clone etc.)")
    public void push(V v){
        if(data.length == entries) {
            V[]  newData = (V[]) new Object[entries*2+1];
            System.arraycopy(data,0,newData,0,data.length);
            data = newData;
        }
        data[entries] = v;
        entries += 1;
    }

    public int size() {
        return entries;
    }

    public V peek(){
        return data[entries-1];
    }

    public V pop(){
        V v = data[entries-1];
                entries -= 1;
                        return  v;
    }

    public boolean isEmpty(){ return entries == 0; }
}
