package paint;

import org.jxmapviewer.viewer.GeoPosition;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Graph {
    private final Map<GeoPosition, List<Edge>> adjacencyList;

    public Graph() {
        adjacencyList = new HashMap<>();
    }

    public void addEdge(GeoPosition source, GeoPosition destination, double weight) {
        adjacencyList.putIfAbsent(source, new LinkedList<>());
        adjacencyList.putIfAbsent(destination, new LinkedList<>());
        adjacencyList.get(source).add(new Edge(destination, weight));
    }

    public List<Edge> getEdges(GeoPosition node) {
        return adjacencyList.getOrDefault(node, new LinkedList<>());
    }

    // Nested class to represent the edges in the graph
    public static class Edge {
        private final GeoPosition destination;
        private final double weight;

        public Edge(GeoPosition destination, double weight) {
            this.destination = destination;
            this.weight = weight;
        }

        public GeoPosition getDestination() {
            return destination;
        }

        public double getWeight() {
            return weight;
        }
    }
}
