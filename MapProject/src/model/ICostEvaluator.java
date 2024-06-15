
package model;

public interface ICostEvaluator {
    int evaluateWeight(IEdge edge);

    int evaluateHeuristic(INode node, INode start, INode end);

    int evaluateCost(INode candidate, IEdge edge, INode start, INode end);
}
