import java.util.*;
import java.lang.Math.*;

public class McMetro {
    protected Track[] tracks;
    protected HashMap<BuildingID, Building> buildingTable = new HashMap<>();

    private HashMap<TrackID, Track> trackTable = new HashMap<>();


    // You may initialize anything you need in the constructor
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





    private List<BuildingID> backTrack(BuildingID w, HashMap<BuildingID,BuildingID> parent){
        List<BuildingID> path = new ArrayList<>();

        BuildingID u = w;

        while(u!=null){
            path.add(u);

            u=parent.get(u);
        }

        Collections.reverse(path);
        return path;
    }

    private List<BuildingID> findPath(BuildingID start, BuildingID end , HashMap<BuildingID, HashMap<BuildingID, Integer>> residuals){


        HashMap<BuildingID,BuildingID> parent = new HashMap<>(); // tracks parents in order to backtrack
        Queue<BuildingID> q = new LinkedList<>(); //empty queue

        HashMap<BuildingID,Boolean> visited = new HashMap<>(); //tracks visits

        q.offer(start); //q.enqueue(v)
        while(!q.isEmpty()){
            BuildingID u = q.poll(); // u=q.dequeue
            visited.put(u,true);

            for(BuildingID w : residuals.get(u).keySet()){ // for w in u's adj list
                if(residuals.get(u).get(w)>0){
                    if(!visited.containsKey(w)){

                        parent.put(w,u);//add u as w's parent

                        if(w.equals(end)) { //visit(w)
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


    private int augment (List<BuildingID> P,  HashMap<BuildingID, HashMap<BuildingID, Integer>> residuals){
        int min = Integer.MAX_VALUE;
        for(int i =0; i<P.size()-1;i++){
            int r = residuals.get(P.get(i)).get(P.get(i+1));
            min=Math.min(min,r);
        }

        for(int i =0; i<P.size()-1;i++){

            //adjust front edge in residuals
            int r = residuals.get(P.get(i)).get(P.get(i+1));
            residuals.get(P.get(i)).put(P.get(i+1),r-min);

            //adjust back edge in residuals
            int r2 = residuals.get(P.get(i+1)).get(P.get(i));
            residuals.get(P.get(i+1)).put(P.get(i),r2+min);

        }
        return min;
    }





    // Maximum number of passengers that can be transported from start to end
    int maxPassengers(BuildingID start, BuildingID end) {
        // TODO: your implementation here

        //init residual
        HashMap<BuildingID, HashMap<BuildingID, Integer>> residuals = new HashMap<>(); //residual


        //add submaps
        for (Building b1 : this.buildingTable.values()) {
            residuals.putIfAbsent(b1.id(), new HashMap<>());
        }

        //fill with caps
        for (Track t : this.tracks) {
            BuildingID b1 = t.startBuildingId();
            BuildingID b2 = t.endBuildingId();

            residuals.get(b1).put(b2, t.capacity());

            //possible back edge?
            residuals.get(b2).putIfAbsent(b1, 0);
        }



        int sum =0;



        while(true){
            List <BuildingID> P = findPath(start, end, residuals);
            if(P==null){
                break;

            }
            sum += augment(P, residuals);

        }
        int m1 = Math.min(this.buildingTable.get(start).occupants(),this.buildingTable.get(end).occupants());
        return Math.min(sum,m1);

    }


    private int trackGoodness(Track t){
        BuildingID b1 = t.startBuildingId();
        BuildingID b2 = t.endBuildingId();
        int m1 = Math.min (this.buildingTable.get(b1).occupants(),this.buildingTable.get(b2).occupants());
        int goodness= Math.min(m1,t.capacity())/t.cost();
        return goodness;
    }




    // Returns a list of trackIDs that connect to every building maximizing total network capacity taking cost into account
    TrackID[] bestMetroSystem() {
        // TODO: your implementation here
        NaiveDisjointSet<BuildingID> disj = new NaiveDisjointSet<>();
        for(BuildingID b : this.buildingTable.keySet()){
            disj.add(b);
        }
        ArrayList<TrackID> A = new ArrayList<>();

        PriorityQueue<Track> pq = new PriorityQueue<>(
                (t1, t2) -> Integer.compare(trackGoodness(t2), trackGoodness(t1))
        );
        pq.addAll(Arrays.asList(tracks));

        int n=this.buildingTable.size();

        while(A.size()<n-1){
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




    // Return how many ticket checkers will be hired
    static int hireTicketCheckers(int[][] schedule) {
        // TODO: your implementation here
        Arrays.sort(schedule, (a,b) -> Integer.compare(a[1], b[1]));
        int c =0;
        int endB = Integer.MIN_VALUE;
        for(int[] interval : schedule){
            int start = interval[0];
            int end = interval [1];
            if (endB<=start){
                c++; //not the programming language (buh dum tiss); no one's probably going to read this, but if you do, have a good day :)
                endB=end;
            }
        }

        return c; //apparently someone stole it
    }
}
