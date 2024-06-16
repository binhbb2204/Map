
package matrix;

import model.INode;

// Dijkstra and A* are one but A* has heuristic so just reuse the algo from AStar but return 0 on the heuristic method
public class DijkstraCostEvaluator extends AStarCostEvaluator{
    @Override
    public int evaluateHeuristic(INode node, INode start, INode end) {
        return 0;
    }
}
