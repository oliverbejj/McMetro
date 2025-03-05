/**
 * Important: Documentation generated using ChatGPT-o1
 * A naive implementation of the Disjoint Set (Union-Find) data structure.
 * <p>
 * This class supports the core operations:
 * <ul>
 *   <li>{@code add} - to add a new element as its own set</li>
 *   <li>{@code find} - to find the representative (root) of an element's set</li>
 *   <li>{@code union} - to merge two sets</li>
 * </ul>
 * <p>
 * Note: The union operation here is implemented by size, and the find
 * operation employs path compression.
 *
 * @param <T> the type of the elements stored in the disjoint sets
 */
public class NaiveDisjointSet<T> {
    /**
     * Maps each element to its parent in the tree structure.
     * If an element is its own parent, it is the root of that set.
     */
    HashMap<T, T> parentMap = new HashMap<>();

    /**
     * Maps each root element to the size (number of elements) of its set.
     */
    private HashMap<T, Integer> sizeMap = new HashMap<>();

    /**
     * Adds a new element to the disjoint set.
     * The element starts off in its own set (it is its own parent).
     *
     * @param element the element to add
     */
    void add(T element) {
        sizeMap.put(element, 1);
        parentMap.put(element, element);
    }

    // TODO: Implement path compression
    /**
     * Finds the representative (root) of the set that the element belongs to.
     * Path compression is used to flatten the structure for efficiency.
     *
     * @param a the element whose set representative is to be found
     * @return the root element that represents the set
     */
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
    /**
     * Merges the sets of two elements (if they are not already in the same set).
     * Uses the size of the sets to decide how to attach one tree to another.
     *
     * @param a an element in the first set
     * @param b an element in the second set
     */
    void union(T a, T b) {
        T rootA = find(a);
        T rootB = find(b);
        Integer sizeA = sizeMap.get(rootA);
        Integer sizeB = sizeMap.get(rootB);

        if(sizeA > sizeB){
            parentMap.put(rootB, rootA);
            sizeMap.remove(rootA);
            sizeMap.put(rootA, sizeA + sizeB);
        }
        else {
            parentMap.put(rootA, rootB);
            sizeMap.remove(rootA);
            sizeMap.put(rootB, sizeA + sizeB);
        }
    }
}
