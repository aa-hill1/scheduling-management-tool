package com.algorithms;

import com.oopModel.matrix.Location;

import java.util.ArrayList;
import java.util.HashMap;

public class FormattingMethods {
    public static String formatExtendedVisitToSave(Boolean extendedVisit) {
        return (extendedVisit) ? "TRUE" : "FALSE";
    }
    public static String formatExtendedVisitToView(boolean extendedVisit) {
        return (extendedVisit) ? "Y" : "N";
    }
    public static Boolean formatUserInputToExtendedVisit(String visitString) {
        Boolean extendedVisit;
        switch (visitString) {
            case "Y":
                extendedVisit = true;
                break;
            case "N":
                extendedVisit = false;
                break;
            default:
                extendedVisit = null;
        }
        return extendedVisit;
    }

    public static String[] formatWorkPatternToSave(boolean[] workPattern) {
        String[] formattedWorkPattern = new String[5];
        for (int index=0; index<5; index++) {
            formattedWorkPattern[index] = (workPattern[index]) ? "TRUE" : "FALSE";
        }
        return formattedWorkPattern;
    }
    public static String formatWorkPatternToView(boolean[] workPattern) {
        String formattedString = "";
        for (int index = 0; index< workPattern.length; index++) {
            if (!workPattern[index]) {
                String dayToAdd;
                switch (index) {
                    case 1:
                        dayToAdd="Tue";
                        break;
                    case 2:
                        dayToAdd="Wed";
                        break;
                    case 3:
                        dayToAdd="Thu";
                        break;
                    case 4:
                        dayToAdd="Fri";
                        break;
                    default:
                        dayToAdd="Mon";
                }
                formattedString += dayToAdd+", ";
            }
        }
        formattedString = formattedString.substring(0, formattedString.length()-2);
        return formattedString;
    }
    public static boolean[] formatUserInputToWorkPattern(String formattedString) {
        boolean[] workPattern = {true, true, true, true, true};
        int index = 0;
        boolean invalid = false;
        if (!formattedString.equals("")) {
            while (index < formattedString.length()-3 && !invalid) {
                if (index != 0 && !formattedString.startsWith(", ", index)) {
                    invalid = true;
                } else {
                    index += (index != 0) ? 2 : 0;
                    String day = formattedString.substring(index, index+3);
                    switch (day) {
                        case "Mon":
                            workPattern[0] = false;
                            break;
                        case "Tue":
                            workPattern[1] = false;
                            break;
                        case "Wed":
                            workPattern[2] = false;
                            break;
                        case "Thu":
                            workPattern[3] = false;
                            break;
                        case "Fri":
                            workPattern[4] = false;
                            break;
                        default:
                            invalid = true;
                    }
                    index+=3;
                }
                if (invalid) {
                    workPattern = null;
                }
            }
        }
        return workPattern;
    }

    public static int formatUserInputToPLevel(String pString) {
        int pLevel;
        switch (pString) {
            case "1":
                pLevel = 1;
                break;
            case "2":
                pLevel = 2;
                break;
            case "3":
                pLevel = 3;
                break;
            default:
                pLevel = -1;
        }
        return pLevel;
    }

    public static String formatAdjAndWeightsToView(String[] adjacencies, int[] adjWeights, HashMap<String, Location> locationIdDict) {
        String formattedString = "";
        for (int index = 0; index<adjacencies.length; index++) {
            formattedString += locationIdDict.get(adjacencies[index]).getName() + ": " + adjWeights[index];
            if (index != adjacencies.length - 1) {
                formattedString += ", ";
            }
        }
        return formattedString;
    }
    public static Location formatUserInputToLocationName(String locationName, HashMap<String, Location> locationNameDict) {
        return locationNameDict.getOrDefault(locationName, null);
    }
    public static String[] formatUserInputToLocationAdj(String formattedString, HashMap<String, Location> locationNameDict) {
        ArrayList<String> locationList = new ArrayList<>();
        String[] locationArray = {"Invalid"};
        int index = 0;
        String currentName = "";
        boolean gettingNextName = true;
        boolean invalidLocation = false;
        while (index < formattedString.length() && !invalidLocation) {
            char currentChar = formattedString.charAt(index);

            if (currentChar == ',') {
                index += 2;
                gettingNextName = true;
                currentName = "";
                currentChar = formattedString.charAt(index);
            } else if (currentChar == ':') {
                gettingNextName = false;
                if (locationNameDict.containsKey(currentName)) {
                    locationList.add(locationNameDict.get(currentName).getLocationID());
                } else {
                    locationArray[0] += currentName;
                    invalidLocation = true;
                }
            }

            if (gettingNextName) {
                currentName += currentChar;
            }
            index++;
        }

        if (!invalidLocation && locationList.size()>0) {
            locationArray = new String[locationList.size()];
            for (int arrayListIndex = 0; arrayListIndex < locationList.size(); arrayListIndex++) {
                locationArray[arrayListIndex] = locationList.get(arrayListIndex);
            }
        }
        return locationArray;
    }
    public static int[] formatUserInputToLocationWeights(String formattedString) {
        ArrayList<Integer> weightsList = new ArrayList<>();
        int[] weightsArray = {-1};
        int index = -1;
        String currentWeight = "";
        boolean gettingNextWeight = false;
        boolean invalidWeight = false;
        while (index < formattedString.length()-1 && !invalidWeight) {
            index++;
            char currentChar = formattedString.charAt(index);

            if  (currentChar == ':') {
                index += 2;
                gettingNextWeight = true;
                currentWeight = "";
                currentChar = formattedString.charAt(index);
            }

            if (gettingNextWeight) {
                if (currentChar == ',') {
                    gettingNextWeight = false;
                    weightsList.add(Integer.parseInt(currentWeight));
                } else if (index == formattedString.length() - 1) {
                    weightsList.add(Integer.parseInt(currentWeight + currentChar));
                } else {
                    try {
                        Integer.parseInt(Character.toString(currentChar));
                        currentWeight += currentChar;
                    } catch (NumberFormatException e) {
                        invalidWeight = true;
                    }
                }
            }
        }
        if (!invalidWeight && weightsList.size()>0) {
            weightsArray = new int[weightsList.size()];
            for (int arrayIndex = 0; arrayIndex<weightsList.size(); arrayIndex++) {
                weightsArray[arrayIndex] = weightsList.get(arrayIndex);
            }
        }
        return weightsArray;
    }

    public static String convertToUName(String name) {
        int index = 0;
        String uName = "";
        while (index<name.length()) {
            if (name.charAt(index)!= ' ') {
                uName += name.charAt(index);
            }
            index++;
        }
        return uName;
    }
}
