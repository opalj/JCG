/*
 * BSD 2-Clause License:
 * Copyright (c) 2009 - 2016
 * Software Technology Group
 * Department of Computer Science
 * Technische Universität Darmstadt
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  - Redistributions of source code must retain the above copyright notice,
 *     this list of conditions and the following disclaimer.
 *  - Redistributions in binary form must reproduce the above copyright notice,
 *     this list of conditions and the following disclaimer in the documentation
 *     and/or other materials provided with the distribution.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 *  AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 *  IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 *  ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 *  LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 *  CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 *  SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 *  INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 *  CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 *  ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 *  POSSIBILITY OF SUCH DAMAGE.
 */

package expressions;

import annotations.callgraph.CallSite;
import annotations.callgraph.InvokedConstructor;
import annotations.callgraph.ResolvedMethod;
import annotations.documentation.CGNote;

import static annotations.documentation.CGCategory.*;

/**
 *
 * Simple map implementation which maintains a linked list of key-value pairs.
 *
 * <--
 *
 *
 *
 *
 * SPACE LEFT INTENTIONALLY FREE TO HANDLE FUTURE ADAPTIONS
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 * <--
 * @author Michael Eichberg
 * @author Michael Reif
 */
public class Map<K, V> {

    public static final String linkedEntryRecieverType = "expressions/Map$LinkedEntry";


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

    @CGNote(value = POLYMORPHIC_CALL, description = "an anonymous class is created; the methods of this class become potential call targets.")
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

    @InvokedConstructor(receiverType = linkedEntryRecieverType, parameterTypes = {Object.class, Object.class}, line = 115)
    @CallSite(name = "getNextEntry", resolvedMethods = {@ResolvedMethod(receiverType = linkedEntryRecieverType)}, line = 126)
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
