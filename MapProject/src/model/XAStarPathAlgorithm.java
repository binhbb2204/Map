
package model;

import matrix.AStarCostEvaluator.EvaluatorDisabledException;

public class XAStarPathAlgorithm extends AStarPathAlgorithm {
    private volatile boolean searching;

    @Override
    public boolean searchPath(INode start, INode end) {
        searching = true;
        boolean value = false;
        try{
            value = super.searchPath(start, end);
        }
        catch(EvaluatorDisabledException e){

        }
        searching = false;
        return value;
    }

    public boolean isSearching(){
        return searching;
    }
}
