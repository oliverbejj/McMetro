import java.util.*;

public class NaiveDisjointSet<T> {
    HashMap<T, T> parentMap = new HashMap<>();

    private HashMap<T, Integer> sizeMap = new HashMap<>();

    void add(T element) {
        sizeMap.put(element, 1);
        parentMap.put(element, element);
    }

    // TODO: Implement path compression
    T find(T a) {
        T node = parentMap.get(a);
        if (node.equals(a)) {
            return node;
        } else {
            parentMap.put(a, find(parentMap.get(a)));
            return parentMap.get(a);
        }
    }

    // TODO: Implement union by size or union by rank
    void union(T a, T b) {
        T rootA = find(a);
        T rootB = find(b);
        Integer sizeA = sizeMap.get(rootA);
        Integer sizeB = sizeMap.get(rootB);



        if(sizeA>sizeB){
            parentMap.put(rootB, rootA);
            sizeMap.remove(rootA);
            sizeMap.put(rootA, sizeA+sizeB);

        }
        else {
            parentMap.put(rootA, rootB);
            sizeMap.remove(rootA);
            sizeMap.put(rootB, sizeA+sizeB);
        }
    }


}
