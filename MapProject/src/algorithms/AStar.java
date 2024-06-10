package algorithms;
import org.jxmapviewer.viewer.GeoPosition;

import java.util.*;
import paint.Graph;

public class AStar {
    public static List<GeoPosition> computeShortestPath(Graph graph, GeoPosition source, GeoPosition destination) {
        Set<GeoPosition> closedSet = new HashSet<>();
        Set<GeoPosition> openSet = new HashSet<>();
        openSet.add(source);

        Map<GeoPosition, GeoPosition> cameFrom = new HashMap<>();

        Map<GeoPosition, Double> gScore = new HashMap<>();
        gScore.put(source, 0.0);

        Map<GeoPosition, Double> fScore = new HashMap<>();
        fScore.put(source, Haversine.calculate(source, destination));

        PriorityQueue<GeoPosition> openQueue = new PriorityQueue<>(Comparator.comparingDouble(fScore::get));
        openQueue.add(source);

        while (!openQueue.isEmpty()) {
            GeoPosition current = openQueue.poll();

            if (current.equals(destination)) {
                return reconstructPath(cameFrom, current);
            }

            openSet.remove(current);
            closedSet.add(current);

            for (Graph.Edge edge : graph.getEdges(current)) {
                GeoPosition neighbor = edge.getDestination();
                if (closedSet.contains(neighbor)) {
                    continue;
                }

                double tentativeGScore = gScore.get(current) + edge.getWeight();

                if (!openSet.contains(neighbor)) {
                    openSet.add(neighbor);
                } else if (tentativeGScore >= gScore.getOrDefault(neighbor, Double.MAX_VALUE)) {
                    continue;
                }

                cameFrom.put(neighbor, current);
                gScore.put(neighbor, tentativeGScore);
                fScore.put(neighbor, tentativeGScore + Haversine.calculate(neighbor, destination));
                openQueue.add(neighbor);
            }
        }

        return new LinkedList<>(); // Return an empty list if there is no path
    }

    private static List<GeoPosition> reconstructPath(Map<GeoPosition, GeoPosition> cameFrom, GeoPosition current) {
        List<GeoPosition> totalPath = new LinkedList<>();
        totalPath.add(current);
        while (cameFrom.containsKey(current)) {
            current = cameFrom.get(current);
            totalPath.add(current);
        }
        Collections.reverse(totalPath);
        return totalPath;
    }
}
