package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import matrix.MatrixNode;
import model.XMatrix;

public class DFSMazeAlgorithm {

    private Random random;

    public DFSMazeAlgorithm() {
        this.random = new Random();
    }

    public void generateMaze(XMatrix matrix, Random random) {
        this.random = random;
        initializeMaze(matrix);
        int startRow = random.nextInt(matrix.getRow());
        int startCol = random.nextInt(matrix.getColumn());
        MatrixNode startNode = matrix.getValue(startRow, startCol);
        startNode.setEnabled(true);
        dfs(matrix, startNode);
    }

    private void initializeMaze(XMatrix matrix) {
        for (int i = 0; i < matrix.getRow(); i++) {
            for (int j = 0; j < matrix.getColumn(); j++) {
                matrix.getValue(i, j).setEnabled(false);
            }
        }
    }

    private void dfs(XMatrix matrix, MatrixNode node) {
        List<MatrixNode> neighbors = getNeighbors(matrix, node);
        Collections.shuffle(neighbors, random);

        for (MatrixNode neighbor : neighbors) {
            if (!neighbor.isEnabled()) {
                // Knock down the wall between the current node and the neighbor
                knockDownWall(matrix, node, neighbor);
                neighbor.setEnabled(true);
                dfs(matrix, neighbor);
            }
        }
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
