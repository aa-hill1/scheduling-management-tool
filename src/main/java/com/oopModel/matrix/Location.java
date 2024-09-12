package com.oopModel.matrix;

import com.oopModel.IDInterface;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Location implements IDInterface {
    private final String locationID;
    private String name;
    private String[] adjacencies;
    private int[] adjWeights;
    private int numAdj;
    private int distance;
    private String route;
    private boolean initialNode;

    //This constructor is used to initialise instances of Location
    //from user input, it calls createID() to create a unique locationID.
    public Location(String name, String[] adjacencies,
                    int[] adjWeights) throws NoSuchAlgorithmException {
        setupLocation(name, adjacencies, adjWeights);
        this.locationID = createID();
    }
    //This constructor is used to initialise instances of Location
    //from data stored in the database.
    public Location(String locationID, String name,
                    String[] adjacencies, int[] adjWeights) {
        this.locationID = locationID;
        setupLocation(name, adjacencies, adjWeights);
    }
    //Both constructors call this method, which initialises
    //most of the classes attributes, as well as calling
    //determineInitialNode().
    public void setupLocation(String name, String[] adjacencies,
                              int[] adjWeights) {
        this.name = name;
        this.adjacencies = adjacencies;
        this.adjWeights = adjWeights;
        numAdj = adjacencies.length;
        determineIfInitialNode(name);
    }
    //This procedure determines whether an instance of
    //Location represents the Hospice. In which case,
    //it sets the distance attribute to 0 and the
    //route attribute to locationID in order to prepare
    //the Location for use during the optimisation algorithm.
    public void determineIfInitialNode(String name) {
        initialNode = (name.equalsIgnoreCase("Hospice"));
        distance = (initialNode) ? 0 : 999;
        route = (initialNode) ? locationID : "";
    }

    public String getLocationID() {
        return locationID;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
        determineIfInitialNode(name);
    }
    public int getDistance() {
        return distance;
    }
    public void setDistance(int distance) {
        this.distance = distance;
    }
    public String getRoute() {
        return route;
    }

    public void setRoute(String route) {
        this.route = route;
    }
    public boolean isInitialNode() {
        return initialNode;
    }
    public String[] getAdjacencies() {
        return adjacencies;
    }
    public int[] getAdjWeights() {
        return adjWeights;
    }

    //This method appends "BREAK" to the end of route
    //in order to signify that an appointment Location
    //has been reached.
    public void addBreakToRoute() {
        route += "BREAK";
    }

    //This method reconstructs the adjacencies and adjWeights
    //attributes, adding an extra adjacency and its weight.
    public void addAdjacency(String adjacency, int weight) {
        String[] newAdj = new String[numAdj+1];
        int[] newWeights = new int[numAdj+1];
        for (int index=0; index<numAdj; index++) {
            newAdj[index] = adjacencies[index];
            newWeights[index] = adjWeights[index];
        }
        newAdj[numAdj] = adjacency;
        newWeights[numAdj] = weight;

        numAdj++;
        adjacencies = newAdj;
        adjWeights = newWeights;
    }
    //This method reconstructs the adjacencies and adjWeights
    //attributes, removing a specific adjacency and its weight.
    public void removeAdjacency(String adjToRemove) {
        numAdj--;
        String[] newAdj = new String[numAdj];
        int[] newWeights = new int[numAdj];
        int oldAdjIndex = 0;
        int newAdjIndex = 0;
        while (oldAdjIndex<numAdj+1) {
            if (!adjacencies[oldAdjIndex].equals(adjToRemove)) {
                newAdj[newAdjIndex] = adjacencies[oldAdjIndex];
                newWeights[newAdjIndex] = adjWeights[oldAdjIndex];
                newAdjIndex++;
            }
            oldAdjIndex++;
        }
        adjacencies = newAdj;
        adjWeights = newWeights;
    }
    //This method checks for the existence of an adjacency,
    //returning false if it does not exist.
    public boolean checkAdjExists(String adjacency) {
        boolean adjExists = false;
        int adjIndex = 0;
        while (adjIndex<numAdj && !adjExists) {
            if (adjacencies[adjIndex].equals(adjacency)) {
                adjExists = true;
            }
            adjIndex++;
        }
        return adjExists;
    }

    //This method gets the edge weight between the
    //Location instance and a specific adjacency.
    public int getWeight(String adjacency) {
        int index = 0;
        int weightToReturn = 0;
        while (index<numAdj) {
            if (adjacencies[index].equals(adjacency)) {
                weightToReturn = adjWeights[index];
                index = numAdj;
            } else {
                index++;
            }
        }
        return weightToReturn;
    }

    //This method changes the edge weight between the
    //Location instance and a specific adjacency.
    public void changeWeight(String adjacency, int newWeight) {
        int index = 0;
        while (index<numAdj) {
            if (adjacencies[index].equals(adjacency)) {
                adjWeights[index] = newWeight;
                index = numAdj;
            } else {
                index++;
            }
        }
    }

    //This method, from IDInterface, is used to create a unique 10-character
    //locationID for each instance of the class.
    //This method is borrowed code from: https://www.javainterviewpoint.com/java-salted-password-hashing/
    @Override
    public String createID() throws NoSuchAlgorithmException {
        MessageDigest hashDigest = MessageDigest.getInstance("SHA-256");
        byte[] nameHash = hashDigest.digest(name.getBytes(StandardCharsets.UTF_8));
        String ID = null;
        for (int index=0; index<10; index++) {
            byte nameHashByte = nameHash[index];
            ID += Integer.toHexString(Byte.toUnsignedInt(nameHashByte));
        }
        return ID.substring(0, 10);
    }
}
