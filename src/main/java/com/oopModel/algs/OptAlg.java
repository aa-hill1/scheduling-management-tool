package com.oopModel.algs;


import com.oopModel.NurseObject;
import com.oopModel.PatientObject;
import com.oopModel.comparator.LocationComparator;
import com.oopModel.comparator.PatientComparator;
import com.oopModel.matrix.AdjMatrix;
import com.oopModel.matrix.Location;

import java.util.ArrayList;
import java.util.HashMap;

public class OptAlg {
    private HashMap<String, PatientObject> patientObjIdDict;
    private HashMap<String, Location> locationIdDict;

    private LocationComparator locationComparator;
    private AdjMatrix adjMatrix;

    private ArrayList<NurseObject> nurseArrayList;

    private ArrayList<PatientObject> shiftInfo;

    private Location initialNode;

    public OptAlg(HashMap<String, PatientObject> patientObjIdDict, AdjMatrix adjMatrix,
                  ArrayList<NurseObject> nurseArrayList, Location initialNode,
                  HashMap<String, Location> locationIdDict) {
        this.patientObjIdDict = patientObjIdDict;
        this.locationIdDict = locationIdDict;

        this.nurseArrayList = nurseArrayList;

        this.adjMatrix = adjMatrix;
        this.initialNode = initialNode;

        locationComparator = new LocationComparator();
    }

    //This method cycles through every shift of all nurses,
    //calling createOptimisedRouteAndShift() for each
    public void cycleNursesAndShifts() {
        for (NurseObject nurse : nurseArrayList) {
            boolean[] workPattern = nurse.getWorkPattern();
            for (int shiftIndex=0; shiftIndex< workPattern.length; shiftIndex++) {
                if (workPattern[shiftIndex]) {
                    createOptimisedRouteAndShift(nurse, shiftIndex);
                }
            }
        }
    }
    //The method below calls assembleShiftInfo(), gathering the IDs of every patient on a
    //specific shift, then calls assembleRoute().
    public void createOptimisedRouteAndShift(NurseObject nurse, int day) {
        shiftInfo = assembleShiftInfo(nurse, day);
        resetNodesForNextShift();
        String route = assembleRoute();
        nurse.setRoute(route, day);
        nurse.setOrderedShift(assembleOrderedShift(route, assembleShiftInfo(nurse, day)), day);
    }
    //This procedure resets the distances and routes
    //of each location in adjMatrix, as well as the
    //initialNode, so that assembleRoute() can
    //be run again.
    public void resetNodesForNextShift() {
        adjMatrix.resetAllNodeDistancesRoutes();
        initialNode = adjMatrix.getInitialNode();
    }
    //This function creates and returns an ArrayList of all of
    //the patients on the current route being optimised,
    //stored in order of overall priority level descending.
    public ArrayList<PatientObject> assembleShiftInfo(NurseObject nurse, int day) {
        String[] shift = nurse.getShift(day);
        ArrayList<PatientObject> sortedPatients = new ArrayList<>();
        int timeIndex = 0;
        while (timeIndex < shift.length) {
            String timeSlotContents = shift[timeIndex];
            if (timeSlotContents != null) {
                if (!timeSlotContents.equals("BREAK")) {
                    PatientObject patientToAdd = patientObjIdDict.get(timeSlotContents);
                    if (!sortedPatients.contains(patientToAdd)) {
                        sortedPatients.add(patientToAdd);
                    }
                    timeIndex += (patientToAdd.isExtendedVisit()) ? 5 : 4;
                } else {
                    timeIndex++;
                }
            } else {
                timeIndex = shift.length;
            }

        }
        sortedPatients.sort(new PatientComparator());
        return sortedPatients;
    }

    //This function assembles the optimised route between patient
    //locations, calling dijkstras() each time to find the most
    //efficient route to the next node.
    public String assembleRoute() {
        ArrayList<Location> targetNodes = assembleTargetNodesArray();
        Location finalNode = null;
        while (!targetNodes.isEmpty()) {
            targetNodes = dijkstras(targetNodes);

            if (targetNodes.size() == 1) {
                finalNode = targetNodes.get(0);
            }
        }
        return (finalNode != null) ? finalNode.getRoute() : null;
    }
    //This node assembles and returns an ArrayList of
    //the locations of the patients to visit for the
    //current shift.
    public ArrayList<Location> assembleTargetNodesArray() {
        ArrayList<Location> targetNodes = new ArrayList<>();
        for (PatientObject patient : shiftInfo) {
            targetNodes.add(patient.getLocation());
        }
        return targetNodes;
    }
    //This function performs Dijkstra's algorithm,
    //finding the shortest route between an inital node
    //and the others on the graph.
    public ArrayList<Location> dijkstras(ArrayList<Location> targetNodes) {
        ArrayList<Location> nodesByDistance = assembleNodesByDistance();

        ArrayList<String> visitedNodeIDs = new ArrayList<>();
        boolean reset = false;

        while (!nodesByDistance.isEmpty() && !reset && !shiftInfo.isEmpty()) {
            Location currentNode = nodesByDistance.remove(0);
            visitedNodeIDs.add(currentNode.getLocationID());

            updateAdjDistancesAndRoutes(currentNode, visitedNodeIDs);
            nodesByDistance.sort(locationComparator);
            //Each time a node is added to visited, and all of its
            //adjacencies are updated. The method checkIfNodeIsTarget()
            //is called to check whether the node is the location of a
            //patient to visit. If it is, the node is removed from
            //targetNodes, which is returned so that the dijkstra's
            // is reset and can be run again from a new initialNode.
            reset = checkIfNodeIsTarget(currentNode, targetNodes);
            if (reset) {
                targetNodes.remove(currentNode);
            }
        }
        return targetNodes;
    }

    //This method assembles and returns an ArrayList of
    //all of the nodes in the graph, sorted by distance from
    //the initialNode ascending. It acts in place of a priority
    //queue in the Dijkstra's algorithm.
    public ArrayList<Location> assembleNodesByDistance() {
        ArrayList<Location> nodesByDistance = new ArrayList<>();
        for (Location node : adjMatrix.getNodes()) {
            if (!node.equals(initialNode)) {
                node.setDistance(999);
            }
            nodesByDistance.add(node);
        }
        nodesByDistance.sort(locationComparator);
        return nodesByDistance;
    }
    //This function updates the distances of the current nodes adjacent nodes
    //if the route to them, from the initial node and via the current node,
    //is a shorter distance than the route which they already store.
    public void updateAdjDistancesAndRoutes(Location currentNode, ArrayList<String> visitedNodeIDs) {
        for (String adjNodeID : currentNode.getAdjacencies())  {
            if (!visitedNodeIDs.contains(adjNodeID)) {
                Location adjNode = locationIdDict.get(adjNodeID);
                int distance = currentNode.getDistance() + adjMatrix.getEdgeWeight(currentNode, adjNode);
                if (distance < adjNode.getDistance()) {
                    adjNode.setDistance(distance);
                    adjNode.setRoute(currentNode.getRoute() + adjNodeID);
                }
            }
        }
    }

    //This function checks whether the current node being processed is
    //the location of a patient of the highest priority level left to optimise.
    //If it is, the method whenTargetNodeFound() is called.
    public boolean checkIfNodeIsTarget(Location node, ArrayList<Location> targetNodes) {
        boolean targetLocation = false;
        if (targetNodes.contains(node)) {

            PatientObject currentPatient = shiftInfo.get(0);
            int targetPLvl = currentPatient.getOverallPLevel();

            int patientIndex = 0;
            boolean targetFoundWithinLoop = false;
            while (patientIndex < shiftInfo.size() - 1 &&
                    currentPatient.getOverallPLevel() == targetPLvl && !targetLocation) {
                if (currentPatient.getLocation().equals(node)) {
                    targetLocation = true;
                    whenTargetNodeFound(node, patientIndex);
                    targetFoundWithinLoop = true;
                } else {
                    patientIndex++;
                    currentPatient = shiftInfo.get(patientIndex);
                }
            }
            if (!targetFoundWithinLoop && shiftInfo.size() == 1 &&
                    currentPatient.getLocation().equals(node)) {
                targetLocation = true;
                whenTargetNodeFound(node, patientIndex);
            }
        }
        return targetLocation;
    }

    //This procedure adds break to the route of the current node to
    //signify that an appointment has been reached, then sets the current
    //node as initialNode and resets its distance (as well as removing the
    //patient who's appointment has now been optimised from shiftInfo). This
    //therefore enables the Dijkstra's algorithm to be run again to find the
    //next target node, using the current node as the initial node.
    public void whenTargetNodeFound(Location targetNode, int patientIndex) {
        if (targetNode.equals(initialNode)) {
            targetNode.setRoute(targetNode.getRoute() + targetNode.getLocationID());
        }
        targetNode.addBreakToRoute();
        initialNode = targetNode;
        initialNode.setDistance(0);
        shiftInfo.remove(patientIndex);
    }

    //This function assembles and returns the orderedShift for the newly generated route,
    //created using it.
    public String[] assembleOrderedShift(String route, ArrayList<PatientObject> patientsToVisit) {
        String[] orderedShift = new String[24];
        int orderedShiftIndex = 0;
        for (int routeIndex=0; routeIndex<route.length(); routeIndex+=10) {
            if (route.startsWith("BREAK", routeIndex)) {
                Location visitLocation = locationIdDict.get(route.substring(routeIndex-10, routeIndex));

                PatientObject patientVisited = findPatientFromLocation(visitLocation, patientsToVisit);
                patientsToVisit.remove(patientVisited);

                orderedShift = addToOrderedShift(orderedShiftIndex, patientVisited, orderedShift);
                orderedShiftIndex += (patientVisited.isExtendedVisit()) ? 5 : 4;
                routeIndex-=5;
            }
        }
        return orderedShift;
    }

    //This function finds and returns the patient to visit on the
    //current shift from the location sent to the function.
    public PatientObject findPatientFromLocation(Location location, ArrayList<PatientObject> patientsToVisit) {
        PatientObject patient = null;
        int patientIndex = 0;
        boolean found = false;
        while(patientIndex<patientsToVisit.size() && !found) {
            patient = patientsToVisit.get(patientIndex);
            if (location.equals(patient.getLocation())) {
                found = true;
            } else {
                patientIndex++;
            }
        }
        return patient;
    }

    //This function updates the current orderedShift by adding the next patient's
    //appointment to it, followed by "BREAK".
    public String[] addToOrderedShift(int orderedShiftIndex, PatientObject patientToAdd, String[] orderedShift) {
        int slotsToTakeUp = (patientToAdd.isExtendedVisit()) ? 4 : 3;
        for (int count=0; count<slotsToTakeUp; count++) {
            orderedShift[orderedShiftIndex] = patientToAdd.getPatientID();
            orderedShiftIndex++;
        }
        orderedShift[orderedShiftIndex] = "BREAK";
        return orderedShift;
    }

}
