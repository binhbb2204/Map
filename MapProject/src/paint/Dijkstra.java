package paint;

import org.jxmapviewer.viewer.GeoPosition;

import java.util.*;

public class Dijkstra {
    public static List<GeoPosition> computeShortestPath(Graph graph, GeoPosition source, GeoPosition destination) {
        Set<GeoPosition> settledNodes = new HashSet<>();
        Set<GeoPosition> unsettledNodes = new HashSet<>();
        Map<GeoPosition, GeoPosition> predecessors = new HashMap<>();
        Map<GeoPosition, Double> distance = new HashMap<>();

        distance.put(source, 0.0);
        unsettledNodes.add(source);

        while (unsettledNodes.size() != 0) {
            GeoPosition currentNode = getLowestDistanceNode(unsettledNodes, distance);
            unsettledNodes.remove(currentNode);
            for (Graph.Edge edge : graph.getEdges(currentNode)) {
                GeoPosition adjacentNode = edge.getDestination();
                double edgeWeight = edge.getWeight();
                if (!settledNodes.contains(adjacentNode)) {
                    calculateMinimumDistance(adjacentNode, edgeWeight, currentNode, distance, predecessors);
                    unsettledNodes.add(adjacentNode);
                }
            }
            settledNodes.add(currentNode);
        }

        return getPath(destination, predecessors);
    }

    private static List<GeoPosition> getPath(GeoPosition destination, Map<GeoPosition, GeoPosition> predecessors) {
        List<GeoPosition> path = new LinkedList<>();
        GeoPosition step = destination;
        // Check if a path exists
        if (predecessors.get(step) == null) {
            return path; // empty list means no path exists
        }
        path.add(step);
        while (predecessors.get(step) != null) {
            step = predecessors.get(step);
            path.add(step);
        }
        // Put it into the correct order
        Collections.reverse(path);
        return path;
    }

    private static GeoPosition getLowestDistanceNode(Set<GeoPosition> unsettledNodes, Map<GeoPosition, Double> distance) {
        GeoPosition lowestDistanceNode = null;
        double lowestDistance = Double.MAX_VALUE;
        for (GeoPosition node : unsettledNodes) {
            double nodeDistance = distance.get(node);
            if (nodeDistance < lowestDistance) {
                lowestDistance = nodeDistance;
                lowestDistanceNode = node;
            }
        }
        return lowestDistanceNode;
    }

    private static void calculateMinimumDistance(GeoPosition evaluationNode, double edgeWeigh, GeoPosition sourceNode, Map<GeoPosition, Double> distance, Map<GeoPosition, GeoPosition> predecessors) {
        Double sourceDistance = distance.get(sourceNode);
        if (sourceDistance + edgeWeigh < distance.getOrDefault(evaluationNode, Double.MAX_VALUE)) {
            distance.put(evaluationNode, sourceDistance + edgeWeigh);
            predecessors.put(evaluationNode, sourceNode);
        }
    }
}

