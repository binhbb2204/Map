
/*
package matrix;

import model.INode;

// Dijkstra and A* are one but A* has heuristic so just reuse the algo from AStar but return 0 on the heuristic method
public class DijkstraCostEvaluator extends AStarCostEvaluator{
    @Override
    public int evaluateHeuristic(INode node, INode start, INode end) {
        return 0;
    }
}
*/
// O((V+E) log V)

package matrix;

import model.INode;
import model.IEdge;

import java.util.*;

public class DijkstraCostEvaluator extends AStarCostEvaluator {
    @Override
    public int evaluateHeuristic(INode node, INode start, INode end) {
        return 0;
    }

    public Map<INode, Integer> dijkstra(INode start, INode end) {
        // Map to store the shortest distance from start to each node
        Map<INode, Integer> distances = new HashMap<>();
        // Map to store the previous node in the optimal path
        Map<INode, INode> previousNodes = new HashMap<>();
        // Priority queue to select the next node with the smallest distance
        PriorityQueue<INode> priorityQueue = new PriorityQueue<>(Comparator.comparingInt(distances::get));

        // Initialize distances and priority queue
        for (INode node : getAllNodes()) {
            distances.put(node, Integer.MAX_VALUE);
        }
        distances.put(start, 0);
        priorityQueue.add(start);

        while (!priorityQueue.isEmpty()) {
            INode current = priorityQueue.poll();

            if (current.equals(end)) {
                break; // Reached the destination node
            }

            for (IEdge edge : current.getEdges()) {
                INode neighbor = edge.getOpposite(current);
                int newDist = distances.get(current) + edge.getWeight();

                if (newDist < distances.get(neighbor)) {
                    distances.put(neighbor, newDist);
                    previousNodes.put(neighbor, current);
                    priorityQueue.remove(neighbor); // Remove and re-add the neighbor to update its priority
                    priorityQueue.add(neighbor);
                }
            }
        }

        // Construct the shortest path from start to end if needed
        List<INode> path = new ArrayList<>();
        for (INode at = end; at != null; at = previousNodes.get(at)) {
            path.add(at);
        }
        Collections.reverse(path);

        return distances;
    }

    // This method should return all nodes in the graph
    private Set<INode> getAllNodes() {
        // Implement this method based on your graph structure
        return new HashSet<>();
    }
}


// O(V^2)
/* 
package matrix;

import model.INode;
import model.IEdge;

import java.util.*;

public class DijkstraCostEvaluator extends AStarCostEvaluator {
    @Override
    public int evaluateHeuristic(INode node, INode start, INode end) {
        return 0;
    }

    public Map<INode, Integer> dijkstra(INode start, INode end) {
        Map<INode, Integer> distances = new HashMap<>();
        Map<INode, INode> previousNodes = new HashMap<>();
        Set<INode> unvisited = new HashSet<>();

        // Initialize distances and unvisited set
        for (INode node : getAllNodes()) {
            distances.put(node, Integer.MAX_VALUE);
            unvisited.add(node);
        }
        distances.put(start, 0);

        while (!unvisited.isEmpty()) {
            // Find the unvisited node with the smallest distance
            INode current = null;
            int smallestDistance = Integer.MAX_VALUE;
            for (INode node : unvisited) {
                int dist = distances.get(node);
                if (dist < smallestDistance) {
                    smallestDistance = dist;
                    current = node;
                }
            }

            if (current == null) {
                break; // All remaining nodes are inaccessible from the start node
            }

            unvisited.remove(current);

            if (current.equals(end)) {
                break; // Reached the destination node
            }

            for (IEdge edge : current.getEdges()) {
                INode neighbor = edge.getOpposite(current);
                if (unvisited.contains(neighbor)) {
                    int newDist = distances.get(current) + edge.getWeight();
                    if (newDist < distances.get(neighbor)) {
                        distances.put(neighbor, newDist);
                        previousNodes.put(neighbor, current);
                    }
                }
            }
        }

        // Construct the shortest path from start to end if needed
        List<INode> path = new ArrayList<>();
        for (INode at = end; at != null; at = previousNodes.get(at)) {
            path.add(at);
        }
        Collections.reverse(path);

        return distances;
    }

    // This method should return all nodes in the graph
    private Set<INode> getAllNodes() {
        // Implement this method based on your graph structure
        return new HashSet<>();
    }
}

*/

