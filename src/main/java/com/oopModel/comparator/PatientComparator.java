package com.oopModel.comparator;

import com.oopModel.PatientObject;

import java.util.Comparator;

public class PatientComparator implements Comparator<PatientObject> {

    @Override
    public int compare(PatientObject patientOne, PatientObject patientTwo) {
        if (patientOne.getOverallPLevel() < patientTwo.getOverallPLevel()) {
            return 1;
        } else if (patientOne.getOverallPLevel() > patientTwo.getOverallPLevel()) {
            return -1;
        } else {
            return 0;
        }
    }
}
