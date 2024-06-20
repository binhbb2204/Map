package model;

import java.util.*;

import matrix.MatrixNode;

public class DFSMazeAlgorithm {

    private Random random;
    private Stack<MatrixNode> stack;
    private XMatrix matrix;

    public DFSMazeAlgorithm() {
        this.random = new Random();
        this.stack = new Stack<>();
    }

    public void initializeMaze(XMatrix matrix) {
        this.matrix = matrix;
        for (int i = 0; i < matrix.getRow(); i++) {
            for (int j = 0; j < matrix.getColumn(); j++) {
                matrix.getValue(i, j).setEnabled(false);
            }
        }
        int startRow = random.nextInt(matrix.getRow());
        int startCol = random.nextInt(matrix.getColumn());
        MatrixNode startNode = matrix.getValue(startRow, startCol);
        startNode.setEnabled(true);
        stack.push(startNode);
    }

    public boolean generateMazeStep() {
        if (stack.isEmpty()) {
            return false;
        }
        MatrixNode current = stack.pop();
        List<MatrixNode> neighbors = getNeighbors(matrix, current);
        Collections.shuffle(neighbors, random);
        for (MatrixNode neighbor : neighbors) {
            if (!neighbor.isEnabled()) {
                knockDownWall(matrix, current, neighbor);
                neighbor.setEnabled(true);
                stack.push(current);
                stack.push(neighbor);
                break;
            }
        }
        return true;
    }

    private List<MatrixNode> getNeighbors(XMatrix matrix, MatrixNode node) {
        int row = node.getRow();
        int col = node.getCol();
        List<MatrixNode> neighbors = new ArrayList<>();

        if (row > 1) neighbors.add(matrix.getValue(row - 2, col)); // Up
        if (row < matrix.getRow() - 2) neighbors.add(matrix.getValue(row + 2, col)); // Down
        if (col > 1) neighbors.add(matrix.getValue(row, col - 2)); // Left
        if (col < matrix.getColumn() - 2) neighbors.add(matrix.getValue(row, col + 2)); // Right

        return neighbors;
    }

    private void knockDownWall(XMatrix matrix, MatrixNode node, MatrixNode neighbor) {
        int rowDiff = neighbor.getRow() - node.getRow();
        int colDiff = neighbor.getCol() - node.getCol();

        int wallRow = node.getRow() + rowDiff / 2;
        int wallCol = node.getCol() + colDiff / 2;

        matrix.getValue(wallRow, wallCol).setEnabled(true);
    }
}
