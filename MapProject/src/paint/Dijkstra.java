package paint;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

import waypoint.MyWaypoint;

public class Dijkstra {
    public static Map<MyWaypoint, Double> shortestPath(Graph graph, MyWaypoint source) {
        Map<MyWaypoint, Double> distances = new HashMap<>();
        Map<MyWaypoint, MyWaypoint> previous = new HashMap<>();
        Set<MyWaypoint> visited = new HashSet<>();
        PriorityQueue<MyWaypoint> queue = new PriorityQueue<>((w1, w2) -> Double.compare(distances.getOrDefault(w1, Double.MAX_VALUE), distances.getOrDefault(w2, Double.MAX_VALUE)));

        for (MyWaypoint vertex : graph.getVertices()) {
            if (vertex.equals(source)) {
                distances.put(vertex, 0.0);
            } else {
                distances.put(vertex, Double.MAX_VALUE);
            }
            queue.add(vertex);
        }

        while (!queue.isEmpty()) {
            MyWaypoint current = queue.poll();
            visited.add(current);

            for (Map.Entry<MyWaypoint, Double> neighborEntry : graph.getNeighbors(current).entrySet()) {
                MyWaypoint neighbor = neighborEntry.getKey();
                double weight = neighborEntry.getValue();

                if (!visited.contains(neighbor)) {
                    double newDistance = distances.get(current) + weight;
                    if (newDistance < distances.get(neighbor)) {
                        distances.put(neighbor, newDistance);
                        previous.put(neighbor, current);
                        queue.add(neighbor);
                    }
                }
            }
        }

        return distances;
    }
}
