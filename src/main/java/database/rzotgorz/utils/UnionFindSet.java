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
        return key.equals(father.get(key)) ? key : father.replace(key, getRoot(father.get(key)));
    }

    public T addEdge(T u, T v) {
        T fu = getRoot(u);
        T fv = getRoot(v);
        father.replace(fu, fv);
        return fv;
    }
}
