
package model;

public class Edge implements IEdge {
    private INode nodeA;
    private INode nodeB;
    private int weight;

    @Override
    public INode getNodeA() {
        return this.nodeA;
    }

    @Override
    public INode getNodeB() {
        return this.nodeB;
    }

    @Override
    public INode getOpposite(INode node) {
        if(node == nodeA) return this.nodeB;
        if(node == nodeB) return this.nodeA;
        return null;
    }

    @Override
    public int getWeight() {
        return this.weight;
    }
    public void setWeight(int weight) {
		this.weight = weight;
	}
    
    public void setNodeA(Node nodeA){
        this.nodeA = nodeA;
    }

    public void setNodeB(Node nodeB){
        this.nodeB = nodeB;
    }
}
