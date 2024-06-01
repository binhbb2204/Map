package paint;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

import waypoint.MyWaypoint;

public class Dijkstra {
    public static class ShortestPathResult {
        private final Map<MyWaypoint, Double> distances;
        private final Map<MyWaypoint, MyWaypoint> predecessors;

        public ShortestPathResult(Map<MyWaypoint, Double> distances, Map<MyWaypoint, MyWaypoint> predecessors) {
            this.distances = distances;
            this.predecessors = predecessors;
        }

        public Map<MyWaypoint, Double> getDistances() {
            return distances;
        }

        public Map<MyWaypoint, MyWaypoint> getPredecessors() {
            return predecessors;
        }
    }

    public static ShortestPathResult shortestPath(Graph graph, MyWaypoint source) {
        Map<MyWaypoint, Double> distances = new HashMap<>();
        Map<MyWaypoint, MyWaypoint> predecessors = new HashMap<>();
        Set<MyWaypoint> visited = new HashSet<>();
        PriorityQueue<MyWaypoint> queue = new PriorityQueue<>(
            (w1, w2) -> Double.compare(distances.getOrDefault(w1, Double.MAX_VALUE), distances.getOrDefault(w2, Double.MAX_VALUE))
        );

        // Initialize distances and queue
        for (MyWaypoint vertex : graph.getVertices()) {
            distances.put(vertex, Double.MAX_VALUE);
            queue.add(vertex);
        }
        distances.put(source, 0.0);

        // Main loop of Dijkstra's algorithm
        while (!queue.isEmpty()) {
            MyWaypoint current = queue.poll();
            visited.add(current);

            // Examine and relax all adjacent vertices
            for (Map.Entry<MyWaypoint, Double> neighborEntry : graph.getNeighbors(current).entrySet()) {
                MyWaypoint neighbor = neighborEntry.getKey();
                if (visited.contains(neighbor)) {
                    continue;
                }

                double edgeWeight = neighborEntry.getValue();
                double newDistance = distances.get(current) + edgeWeight;

                if (newDistance < distances.get(neighbor)) {
                    distances.put(neighbor, newDistance);
                    predecessors.put(neighbor, current);

                    // Update the priority queue
                    queue.remove(neighbor);
                    queue.add(neighbor);
                }
            }
        }

        return new ShortestPathResult(distances, predecessors);
    }
}
