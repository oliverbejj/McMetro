import java.util.*;
import java.lang.Math.*;

/**
 * Important: Documentation generated using ChatGPT-o1
 * A class that simulates the McMetro transportation system, allowing computations
 * such as maximum passenger flow between buildings and an optimal metro track layout
 * based on track "goodness".
 */
public class McMetro {
    /**
     * The array of available tracks.
     */
    protected Track[] tracks;

    /**
     * Maps building IDs to corresponding Building objects for quick lookups.
     */
    protected HashMap<BuildingID, Building> buildingTable = new HashMap<>();

    /**
     * Maps track IDs to corresponding Track objects for quick lookups.
     */
    private HashMap<TrackID, Track> trackTable = new HashMap<>();

    /**
     * Constructs the McMetro system with given arrays of tracks and buildings.
     * Initializes internal data structures for building and track lookups.
     *
     * @param tracks    the array of tracks
     * @param buildings the array of buildings
     */
    McMetro(Track[] tracks, Building[] buildings) {
        this.tracks = tracks;

        for (Track t : tracks) {
            trackTable.putIfAbsent(t.id(), t);
        }

        // Populate buildings table
        for (Building building : buildings) {
            buildingTable.putIfAbsent(building.id(), building);
        }
    }

    /**
     * Backtracks from the target building to the source using a parent map,
     * constructing the path in reverse.
     *
     * @param w      the building ID to start backtracking from
     * @param parent map of child->parent relationships (used to reconstruct the path)
     * @return the path (in correct order from source to w)
     */
    private List<BuildingID> backTrack(BuildingID w, HashMap<BuildingID, BuildingID> parent){
        List<BuildingID> path = new ArrayList<>();
        BuildingID u = w;

        while(u!=null){
            path.add(u);
            u = parent.get(u);
        }

        Collections.reverse(path);
        return path;
    }

    /**
     * Uses a BFS approach to find any path from start to end in the residual network.
     *
     * @param start     the start building ID
     * @param end       the end building ID
     * @param residuals the residual capacity graph
     * @return a list of building IDs representing the path from start to end, or null if no path
     */
    private List<BuildingID> findPath(BuildingID start, BuildingID end, HashMap<BuildingID, HashMap<BuildingID, Integer>> residuals){
        HashMap<BuildingID,BuildingID> parent = new HashMap<>();
        Queue<BuildingID> q = new LinkedList<>();
        HashMap<BuildingID,Boolean> visited = new HashMap<>();

        q.offer(start);
        while(!q.isEmpty()){
            BuildingID u = q.poll();
            visited.put(u,true);

            for(BuildingID w : residuals.get(u).keySet()){
                if(residuals.get(u).get(w)>0){
                    if(!visited.containsKey(w)){
                        parent.put(w,u);
                        if(w.equals(end)) {
                            return backTrack(w,parent);
                        }
                        visited.put(w,true);
                        q.offer(w);
                    }
                }
            }
        }
        return null;
    }

    /**
     * Augments the flow along a given path by finding the minimum residual capacity
     * and adjusting the forward and backward edges accordingly.
     *
     * @param P         the path along which flow will be augmented
     * @param residuals the residual capacity graph
     * @return the value of flow added (the bottleneck capacity along the path)
     */
    private int augment(List<BuildingID> P, HashMap<BuildingID, HashMap<BuildingID, Integer>> residuals){
        int min = Integer.MAX_VALUE;
        for(int i = 0; i < P.size() - 1; i++){
            int r = residuals.get(P.get(i)).get(P.get(i+1));
            min = Math.min(min, r);
        }

        for(int i = 0; i < P.size() - 1; i++){
            int r = residuals.get(P.get(i)).get(P.get(i+1));
            residuals.get(P.get(i)).put(P.get(i+1), r - min);

            int r2 = residuals.get(P.get(i+1)).get(P.get(i));
            residuals.get(P.get(i+1)).put(P.get(i), r2 + min);
        }
        return min;
    }

    /**
     * Calculates the maximum number of passengers that can be transported from
     * a start building to an end building, constrained by building occupancy and
     * track capacities.
     *
     * @param start the start building ID
     * @param end   the end building ID
     * @return the maximum flow of passengers possible between these buildings
     */
    int maxPassengers(BuildingID start, BuildingID end) {
        // TODO: your implementation here

        HashMap<BuildingID, HashMap<BuildingID, Integer>> residuals = new HashMap<>();

        // Initialize residual graph
        for (Building b1 : this.buildingTable.values()) {
            residuals.putIfAbsent(b1.id(), new HashMap<>());
        }

        // Fill with track capacities (forward edges) and create back edges with 0 capacity
        for (Track t : this.tracks) {
            BuildingID b1 = t.startBuildingId();
            BuildingID b2 = t.endBuildingId();
            residuals.get(b1).put(b2, t.capacity());
            residuals.get(b2).putIfAbsent(b1, 0);
        }

        int sum = 0;
        while(true){
            List<BuildingID> P = findPath(start, end, residuals);
            if(P == null){
                break;
            }
            sum += augment(P, residuals);
        }

        // The flow is also constrained by the minimum of the start and end building occupants
        int m1 = Math.min(this.buildingTable.get(start).occupants(), this.buildingTable.get(end).occupants());
        return Math.min(sum, m1);
    }

    /**
     * Calculates a "goodness" score for a track based on the minimum building occupant
     * count of its endpoints, the track capacity, and its cost.
     *
     * @param t the track for which goodness is computed
     * @return the goodness value of the track
     */
    private int trackGoodness(Track t){
        BuildingID b1 = t.startBuildingId();
        BuildingID b2 = t.endBuildingId();
        int m1 = Math.min(this.buildingTable.get(b1).occupants(), this.buildingTable.get(b2).occupants());
        int goodness = Math.min(m1, t.capacity()) / t.cost();
        return goodness;
    }

    /**
     * Constructs a metro network (spanning tree) that connects all buildings
     * while maximizing the total "goodness" of chosen tracks.
     * <p>
     * This uses a variation of Kruskal's algorithm, where the edge selection
     * criterion is based on the computed goodness score rather than a traditional
     * weight or cost.
     *
     * @return an array of TrackIDs representing the chosen metro system
     */
    TrackID[] bestMetroSystem() {
        // TODO: your implementation here
        NaiveDisjointSet<BuildingID> disj = new NaiveDisjointSet<>();
        for(BuildingID b : this.buildingTable.keySet()){
            disj.add(b);
        }
        ArrayList<TrackID> A = new ArrayList<>();

        // Priority queue to pick tracks in order of descending goodness
        PriorityQueue<Track> pq = new PriorityQueue<>(
                (t1, t2) -> Integer.compare(trackGoodness(t2), trackGoodness(t1))
        );
        pq.addAll(Arrays.asList(tracks));

        int n = this.buildingTable.size();
        while(A.size() < n - 1){
            Track t = pq.poll();
            BuildingID b1 = t.startBuildingId();
            BuildingID b2 = t.endBuildingId();
            if (!disj.find(b1).equals(disj.find(b2))){
                disj.union(b1,b2);
                A.add(t.id());
            }
        }
        return A.toArray(new TrackID[0]);
    }

    /**
     * Determines the number of ticket checkers needed given a schedule.
     * Each checker can handle one event interval, and an interval is chosen
     * such that it does not overlap with previously covered intervals.
     *
     * @param schedule a 2D array where each element is [startTime, endTime]
     * @return the number of non-overlapping intervals that can be chosen
     */
    static int hireTicketCheckers(int[][] schedule) {
        // TODO: your implementation here
        Arrays.sort(schedule, (a, b) -> Integer.compare(a[1], b[1]));
        int c = 0;
        int endB = Integer.MIN_VALUE;
        for(int[] interval : schedule){
            int start = interval[0];
            int end = interval[1];
            if (endB <= start){
                c++;
                endB = end;
            }
        }
        return c;
    }
}
