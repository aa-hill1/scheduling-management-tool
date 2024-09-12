package com.oopModel.matrix;
public class AdjMatrix {
    private int[][] matrix;
    private int numNodes = 0;
    private Location[] nodes;

    //This constructor initialises the matrix by calling constructMatrix(),
    //however, this does not happen if the instance is created
    //with no adjacencies (an empty array is initialised instead).
    public AdjMatrix(int numNodes, Location[] nodes) {
        this.numNodes = numNodes;
        this.nodes = nodes;
        if (numNodes!=0) {
            matrix = constructMatrix();
        } else {
            matrix = new int[numNodes][numNodes];
        }
    }
    //This method creates and returns matrix, using the locations
    //in nodes array to update edge weights throughout the adjacency
    //matrix, so that it represents the graph accurately.
    public int[][] constructMatrix() {
        int[][] newMatrix = new int[numNodes][numNodes];
        for (int rowIndex=0; rowIndex<numNodes; rowIndex++) {
            Location node = nodes[rowIndex];
            String nodeID = node.getLocationID();
            for (int columnIndex=0; columnIndex<numNodes; columnIndex++) {
                String otherNodeID = nodes[columnIndex].getLocationID();
                if (!(otherNodeID.equals(nodeID))) {
                    newMatrix[rowIndex][columnIndex] = node.getWeight(otherNodeID);
                } else {
                    newMatrix[rowIndex][columnIndex] = -1;
                }
            }
        }
        return newMatrix;
    }

    public int getNumNodes() {
        return numNodes;
    }
    public Location[] getNodes() {
        return nodes;
    }
    public Location getNode(int nodeIndex) {
        return nodes[nodeIndex];
    }
    public void setMatrix(int[][] matrix) {this.matrix = matrix;}

    //This procedure adds an edge between two nodes in the matrix by updating
    //the nodes' corresponding index values, and the locations themselves.
    public void addEdge(Location nodeFrom, Location nodeTo, int weight) {
        int indexNodeFrom = findNodeIndex(nodeFrom);
        int indexNodeTo = findNodeIndex(nodeTo);
        if (indexNodeFrom != -1 && indexNodeTo != -1) {
            nodes[indexNodeFrom].addAdjacency(nodeTo.getLocationID(), weight);
            nodes[indexNodeTo].addAdjacency(nodeFrom.getLocationID(), weight);
            matrix[indexNodeFrom][indexNodeTo] = weight;
            matrix[indexNodeTo][indexNodeFrom] = weight;
        }
    }
    //This procedure gets the edge between two nodes in the matrix, using
    //the nodes' corresponding index values.
    public int getEdgeWeight(Location nodeFrom, Location nodeTo) {
        int indexNodeFrom = findNodeIndex(nodeFrom);
        int indexNodeTo = findNodeIndex(nodeTo);
        return matrix[indexNodeFrom][indexNodeTo];
    }
    //This procedure removes an edge between two nodes in the matrix by updating
    //the nodes' corresponding index values, and the locations themselves.
    public void removeEdge(Location nodeFrom, Location nodeTo) {
        int indexNodeFrom = findNodeIndex(nodeFrom);
        int indexNodeTo = findNodeIndex(nodeTo);
        if (indexNodeFrom != -1 && indexNodeTo != -1) {
            nodes[indexNodeFrom].removeAdjacency(nodeTo.getLocationID());
            nodes[indexNodeTo].removeAdjacency(nodeFrom.getLocationID());
            matrix[indexNodeFrom][indexNodeTo] = -1;
            matrix[indexNodeTo][indexNodeFrom] = -1;
        }
    }
    //This method adds a node to the matrix and reconstructs it.
    public void addNode(Location newNode) {
        numNodes++;
        Location[] newNodes = new Location[numNodes];
        newNodes[numNodes-1] = newNode;
        for (int nodesIndex = 0; nodesIndex<nodes.length;nodesIndex++) {
            newNodes[nodesIndex] = nodes[nodesIndex];
        }
        nodes = newNodes;
        matrix = constructMatrix();
    }

    //This method finds and returns the index
    //of a specific node in nodes (and
    //therefore its index in matrix.
    public int findNodeIndex(Location nodeToFind) {
        int index = 0;
        int foundIndex = -1;
        while (index < numNodes) {
            if (nodes[index] == nodeToFind) {
                foundIndex = index;
                index = numNodes;
            } else {
                index++;
            }
        }
        return foundIndex;
    }
    //This method removes a node from the matrix and reconstructs it.
    public void removeNode(Location nodeToRemove) {
        String nodeToRemoveID = nodeToRemove.getLocationID();
        numNodes--;
        Location[] newNodes = new Location[numNodes];
        int nextIndexToFill = 0;
        for (Location currentNode : nodes) {
            if (!((currentNode.getLocationID()).equals(nodeToRemoveID))) {
                newNodes[nextIndexToFill] = currentNode;
                nextIndexToFill++;
            }
        }
        nodes = newNodes;
        matrix = constructMatrix();
    }
    //This method resets the distances and routes of each node
    //to their original values.
    public void resetAllNodeDistancesRoutes() {
        for (Location node : nodes) {
            node.setDistance((node.isInitialNode()) ? 0 : 999);
            node.setRoute((node.isInitialNode()) ? node.getLocationID() : "");
        }
    }
    //This method finds and returns the initial
    //Hospice noed within the graph.
    public Location getInitialNode() {
        Location initialNode = null;
        int index = 0;
        boolean found = false;
        while (index < nodes.length && !found) {
            Location aNode = nodes[index];
            if (aNode.getName().equals("Hospice")) {
                initialNode = aNode;
                found = true;
            } else {
                index++;
            }
        }
        return initialNode;
    }
}


