package paint;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import waypoint.MyWaypoint;

public class Graph {
    public final Map<MyWaypoint, Map<MyWaypoint, Double>> adjacencyList;
    private Map<MyWaypoint, MyWaypoint> predecessors; // Store predecessors for path reconstruction

    public Graph() {
        adjacencyList = new HashMap<>();
        predecessors = new HashMap<>();
    }

    public void addVertex(MyWaypoint waypoint) {
        adjacencyList.putIfAbsent(waypoint, new HashMap<>());
    }

    public void addEdge(MyWaypoint source, MyWaypoint destination, double weight) {
        if (!adjacencyList.containsKey(source)) {
            addVertex(source);
        }
        if (!adjacencyList.containsKey(destination)) {
            addVertex(destination);
        }
        adjacencyList.get(source).put(destination, weight);
        adjacencyList.get(destination).put(source, weight); // Assuming undirected graph
    }

    public Set<MyWaypoint> getVertices() {
        return adjacencyList.keySet();
    }

    public Map<MyWaypoint, Double> getNeighbors(MyWaypoint vertex) {
        return adjacencyList.get(vertex);
    }

    public void setPredecessors(Map<MyWaypoint, MyWaypoint> predecessors) {
        this.predecessors = predecessors;
    }

    public MyWaypoint getPredecessor(MyWaypoint waypoint) {
        return predecessors.get(waypoint);
    }

    public void clear() {
        adjacencyList.clear();
        predecessors.clear();
    }

    public void removeVertex(MyWaypoint waypoint) {
        adjacencyList.values().forEach(e -> e.remove(waypoint));
        adjacencyList.remove(waypoint);
    }
}
