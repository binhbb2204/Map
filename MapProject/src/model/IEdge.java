
package model;

public interface IEdge {
    INode getNodeA(); //Return node A
    INode getNodeB(); //return node B
    INode getOpposite(INode node); //given either node A or B, return other of 2 nodes
    int getWeight(); //return weight of the edge

}
