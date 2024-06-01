package paint;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import waypoint.MyWaypoint;

public class Graph {
    public final Map<MyWaypoint, Map<MyWaypoint, Double>> adjacencyList;

    public Graph() {
        adjacencyList = new HashMap<>();
        
    }

    public void addVertex(MyWaypoint waypoint) {
        adjacencyList.putIfAbsent(waypoint, new HashMap<>());
    }

    public void addEdge(MyWaypoint source, MyWaypoint destination, double weight) {
        adjacencyList.get(source).put(destination, weight);
        adjacencyList.get(destination).put(source, weight); // Assuming undirected graph
    }

    public Set<MyWaypoint> getVertices() {
        return adjacencyList.keySet();
    }

    public Map<MyWaypoint, Double> getNeighbors(MyWaypoint vertex) {
        return adjacencyList.get(vertex);
    }

    public MyWaypoint getPredecessor(MyWaypoint current, Map<MyWaypoint, Double> distances) {
        double minDistance = Double.POSITIVE_INFINITY;
        MyWaypoint predecessor = null;

        // Iterate through the neighbors of the current vertex
        for (Map.Entry<MyWaypoint, Double> entry : adjacencyList.get(current).entrySet()) {
            MyWaypoint neighbor = entry.getKey();
            double distance = distances.getOrDefault(neighbor, Double.POSITIVE_INFINITY);

            // Update the predecessor if the distance to the neighbor is smaller
            if (distance < minDistance) {
                minDistance = distance;
                predecessor = neighbor;
            }
        }

        return predecessor;
    }
    public void clear() {
        adjacencyList.clear();
    }
    public void removeVertex(MyWaypoint waypoint) {
        // Remove the vertex and its associated edges from the adjacency list
        adjacencyList.values().forEach(e -> e.remove(waypoint));
        adjacencyList.remove(waypoint);
    }
}
