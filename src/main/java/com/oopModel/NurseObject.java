package com.oopModel;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class NurseObject implements IDInterface{
    private String nurseID;
    private String name;
    private boolean[] workPattern;
    private String[][] shifts;
    private int shiftsFilled;
    private String[] routes;
    private String[][] orderedShifts;

    public NurseObject(String name, boolean[] workPattern) throws NoSuchAlgorithmException {
        this.name = name;
        this.workPattern = workPattern;
        nurseID = createID();

        assembleBaseShiftsRoutes();
    }
    public NurseObject(String nurseID, String name, boolean[] workPattern,  String [][] orderedShifts, String[] routes) {
        this.nurseID = nurseID;
        this.name = name;
        this.workPattern = workPattern;
        this.orderedShifts = orderedShifts;
        this.routes = routes;
    }
    public NurseObject(String nurseID, String name, boolean[] workPattern) {
        this.nurseID = nurseID;
        this.name = name;
        this.workPattern = workPattern;
        assembleBaseShiftsRoutes();
    }


    public String getNurseID() {
        return nurseID;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public boolean[] getWorkPattern() {
        return workPattern;
    }
    public void setWorkPattern(boolean[] workPattern) {
        this.workPattern = workPattern;
    }

    public String[] getShift(int day) {
        return shifts[day];
    }
    public void setShift(String [] shift, int day) {
        this.shifts[day] = shift;
    }

    public int getShiftsFilled() {
        return shiftsFilled;
    }
    public void setShiftsFilled(int shiftsFilled) {
        this.shiftsFilled = shiftsFilled;
    }

    public String[] getRoutes() {
        return routes;
    }
    public String getRoute(int index) {
        return routes[index];
    }
    public void setRoute(String route, int day) {
        this.routes[day] = route;
    }

    public String[][] getOrderedShifts() {
        return orderedShifts;
    }
    public String[] getOrderedShift(int day) {
        return orderedShifts[day];
    }
    public void setOrderedShift(String[] orderedShift, int day) {
        orderedShifts[day] = orderedShift;
    }

    public void assembleBaseShiftsRoutes() {
        shifts = new String[5][24];
        routes = new String[5];
        orderedShifts = new String[5][24];
        int count = 0;
        for (int dayIndex=0; dayIndex < workPattern.length; dayIndex++) {
            if (!workPattern[dayIndex]) {
                count++;
                routes[dayIndex] = "BREAK";
                for (int slotIndex = 0; slotIndex<24; slotIndex++) {
                    shifts[dayIndex][slotIndex] = "BREAK";
                    orderedShifts[dayIndex][slotIndex] = "BREAK";
                }
            }
        }
        this.shiftsFilled = count;
    }

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
