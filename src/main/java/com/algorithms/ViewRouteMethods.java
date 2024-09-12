package com.algorithms;

import com.oopModel.NurseObject;
import com.oopModel.PatientObject;
import com.oopModel.matrix.Location;
import javafx.geometry.HPos;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;

public class ViewRouteMethods {

    public static String[][] assembleRouteLabelContents(String route, String[] orderedShift, HashMap<String, PatientObject> patientObjIdDict,
                                                        HashMap<String, Location> locationIdDict) {
        ArrayList<PatientObject> patientsToVisit = getPatients(orderedShift, patientObjIdDict);
        ArrayList<String> patientAddressDetails = getPatientAddressDetails(patientsToVisit);
        ArrayList<String> visitLegDetails = getLocationsForEachLeg(route, locationIdDict);
        ArrayList<LocalTime> visitTimes = getTimesForVisits(orderedShift);

        String[][] routelabelsContent = new String[4][patientsToVisit.size()];
        for (int rowIndex=0; rowIndex<4; rowIndex++) {
            for (int arrayListIndex=0; arrayListIndex< patientsToVisit.size(); arrayListIndex++) {
                String textToAdd = "";
                switch (rowIndex) {
                    case 0:
                        textToAdd = String.valueOf(visitTimes.get(arrayListIndex));
                        break;
                    case 1:
                        PatientObject currentPatient = patientsToVisit.get(arrayListIndex);
                        textToAdd = currentPatient.getName() + "\n" + currentPatient.getInformation();
                        break;
                    case 2:
                        textToAdd = patientAddressDetails.get(arrayListIndex);
                        break;
                    default:
                        textToAdd = visitLegDetails.get(arrayListIndex);
                }
                routelabelsContent[rowIndex][arrayListIndex] = textToAdd;
            }
        }
        return routelabelsContent;
    }

    public static ArrayList<PatientObject> getPatients(String[] orderedShift, HashMap<String, PatientObject> patientIdDict) {
        ArrayList<PatientObject> patientsToVisit = new ArrayList<>();
        boolean lastIndexWasBreak = true;
        for (int index = 0; index< orderedShift.length; index++) {
            String currentTimeSlot = orderedShift[index];
            if (lastIndexWasBreak && currentTimeSlot != null) {
                lastIndexWasBreak = false;
                patientsToVisit.add(patientIdDict.get(currentTimeSlot));
            } else if (currentTimeSlot != null && currentTimeSlot.equals("BREAK")) {
                lastIndexWasBreak = true;
            }
        }
        return patientsToVisit;
    }
    public static ArrayList<String> getPatientAddressDetails(ArrayList<PatientObject> patientsToVisit) {
        ArrayList<String> patientLocationDetails = new ArrayList<>();
        for (PatientObject currentPatient : patientsToVisit) {
            String patientAddressDetails = currentPatient.getAddressLineOne()+", "+currentPatient.getLocation().getName();
            patientLocationDetails.add(patientAddressDetails);
        }
        return patientLocationDetails;
    }

    public static ArrayList<String> getLocationsForEachLeg(String route, HashMap<String, Location> locationIdDict) {
        ArrayList<String> locationsToGoVia = new ArrayList<>();
        String locationsForLeg = "";
        boolean firstLocForLeg = true;
        for (int index=0; index<route.length(); index+=10) {
            if (route.startsWith("BREAK", index)) {
                index -= 5;
                locationsToGoVia.add(locationsForLeg);
                locationsForLeg = "";
                firstLocForLeg = true;
            } else {
                String nextLocationId = route.substring(index, index+10);
                if (firstLocForLeg) {
                    locationsForLeg += locationIdDict.get(nextLocationId).getName();
                    firstLocForLeg = false;
                } else {
                    locationsForLeg += ",\n" + locationIdDict.get(nextLocationId).getName();
                }
            }
        }
        return locationsToGoVia;
    }
    public static ArrayList<LocalTime> getTimesForVisits(String[] orderedShift) {
        ArrayList<LocalTime> visitTimes = new ArrayList<>();
        LocalTime baseTime = LocalTime.of(7, 0);
        boolean lastIndexWasBreak = true;
        for (int index=0; index<orderedShift.length; index++) {
            if (lastIndexWasBreak) {
                lastIndexWasBreak = false;
                visitTimes.add(baseTime.plusMinutes((index+1)* 30L));
            } else if (orderedShift[index] != null && orderedShift[index].equals("BREAK")) {
                lastIndexWasBreak = true;
            }
        }
        return visitTimes;
    }

    public static int findDayIndex(String day) {
        int dayIndex;
        switch (day) {
            case "Monday":
                dayIndex = 0;
                break;
            case "Tuesday":
                dayIndex = 1;
                break;
            case "Wednesday":
                dayIndex = 2;
                break;
            case "Thursday":
                dayIndex = 3;
                break;
            default:
                dayIndex = 4;
        }
        return dayIndex;
    }
}
