package com.oopModel;


import com.oopModel.matrix.Location;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class PatientObject implements IDInterface {
    private final String patientID;
    private String name;
    private String information;
    private int pLevel;
    private boolean extendedVisit;
    private int overallPLevel;
    private String addressLineOne;
    private Location location;

    public PatientObject(String name, String information, int pLevel, boolean extendedVisit, String addressLineOne, Location location) throws NoSuchAlgorithmException {
        setUpObject(name, information, pLevel, extendedVisit, addressLineOne, location);
        this.patientID = createID();
    }
    public PatientObject(String patientID, String name, String information, int pLevel, boolean extendedVisit, String addressLineOne, Location location) {
        this.patientID = patientID;
        setUpObject(name, information, pLevel, extendedVisit, addressLineOne, location);
    }
    public void setUpObject(String name, String information, int pLevel, boolean extendedVisit, String addressLineOne, Location location) {
        this.name = name;
        this.information = information;
        this.pLevel = pLevel;
        this.extendedVisit = extendedVisit;
        this.addressLineOne = addressLineOne;
        this.location = location;
        this.overallPLevel = calcOverallPLevel();
    }

    public String getPatientID() {return patientID;}
    public String getName() {return name;}
    public void setName(String name) {this.name = name;}
    public String getInformation() {return information;}
    public void setInformation(String information) {this.information = information;}
    public int getpLevel() {return pLevel;}
    public void setpLevel(int pLevel) {this.pLevel = pLevel; calcOverallPLevel();}
    public boolean isExtendedVisit() {return extendedVisit;}
    public void setExtendedVisit(boolean extendedVisit) {this.extendedVisit = extendedVisit; calcOverallPLevel();}
    public int getOverallPLevel() {return overallPLevel;}
    public String getAddressLineOne() {return addressLineOne;}
    public void setAddressLineOne(String addressLineOne) {this.addressLineOne = addressLineOne;}
    public Location getLocation() {return location;}
    public void setLocation(Location location) {this.location = location;}

    public int calcOverallPLevel() {
        int overallP = 0;
        switch (pLevel) {
            case 1:
                overallP = 5;
                break;
            case 2:
                overallP = 3;
                break;
            case 3:
                overallP = 1;
                break;
        }
        if (extendedVisit) {overallP++;}
        return overallP;
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
