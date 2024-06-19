
package model;

import java.util.Collection;

public interface INode {
    Collection<IEdge> getEdges(); //return through which node is connect to that other nodes

    INode getPredecessor(); //return the predecessor of the node
    /*
     * to form possibly the best path (the best is relative, depending on how the algorithm
	 * evaluate the cost) between the origin and destination. The search goes node by node
	 * from the origin to the destination, for every two consecutive nodes, the leading node
	 * is the predecessor of the trailing node.
     */
    void setPredecessor(INode node); 

    public boolean isOpen(); // return true if the node is opened, else false

    public void setOpen(boolean open); //return if the node has been visited during the graph path search

    public boolean isVisited(); // indicates whether or not the node has been visited during the graph path search

    public void setVisited(boolean visited); // return true if the node has been selected to be part of the resulted path, else false

    public boolean isSelected(); // indicates whether or not the node has been selected to be part of the resulted path or not

    public void setSelected(boolean selected);

    int getHeuristic(); // set the heuristic value evaluated as the cost from the node to the destination (mostly A*)

    void setHeuristic(int heuristic); //return cost of the node

    int getCost();

    void setCost(int cost);


}
