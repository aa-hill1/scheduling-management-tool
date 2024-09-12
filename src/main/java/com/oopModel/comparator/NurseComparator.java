package com.oopModel.comparator;

import com.oopModel.NurseObject;

import java.util.Comparator;

public class NurseComparator implements Comparator<NurseObject> {

    @Override
    public int compare(NurseObject nurseOne, NurseObject nurseTwo) {
        int nOneShiftsFilled = nurseOne.getShiftsFilled();
        int nTwoShiftsFilled = nurseTwo.getShiftsFilled();
        if (nOneShiftsFilled > nTwoShiftsFilled) {
            return 1;
        } else if (nOneShiftsFilled < nTwoShiftsFilled) {
            return -1;
        } else {
            return 0;
        }
    }
}
