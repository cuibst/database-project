package database.rzotgorz.utils;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class UnionFindSet<T extends Comparable<T>> {
    private final Map<T, T> father;

    public UnionFindSet(Collection<T> keys) {
        father = new HashMap<>();
        keys.forEach(key -> father.put(key, key));
    }

    public T getRoot(T key) {
        if(!father.containsKey(key))
            throw new RuntimeException("Unknown key!");
        if(key.equals(father.get(key)))
            return key;
        T newkey = getRoot(father.get(key));
        father.put(key, newkey);
        return newkey;
    }

    public T addEdge(T u, T v) {
        T fu = getRoot(u);
        T fv = getRoot(v);
        father.replace(fu, fv);
        return fv;
    }
}
