package com.oopModel.algs;

import com.oopModel.NurseObject;
import com.oopModel.PatientObject;
import com.oopModel.comparator.NurseComparator;
import com.oopModel.comparator.PatientComparator;

import java.util.ArrayList;
import java.util.PriorityQueue;

public class ScheduleAlg {
    private PriorityQueue<PatientObject> patientPQ;
    private PriorityQueue<NurseObject> nursePQ;

    //This constructor calls the method assemblePQs, which transfers the details of all
    //existing nurses and patients into two priority queues, one for each class.
    public ScheduleAlg(ArrayList<PatientObject> patientArrayList, ArrayList<NurseObject> nurseArrayList) {
        assemblePQs(patientArrayList, nurseArrayList);
    }
    //The priority queues in this method are sorted using instances of PatientComparator and NurseComparator.
    //This results in patients being stored in the order of overall priority level descending, and nurses being
    //stored in the order of number of shifts available descending.
    public void assemblePQs(ArrayList<PatientObject> patientArrayList, ArrayList<NurseObject> nurseArrayList) {
        patientPQ = new PriorityQueue<>(new PatientComparator());
        nursePQ = new PriorityQueue<>(new NurseComparator());
        patientPQ.addAll(patientArrayList);
        nursePQ.addAll(nurseArrayList);
    }

    //This method assigns patients to shifts and shifts to nurses
    //until either all patients have been assigned, or there
    //are no more shifts available to fill.
    public void schedule() {
        while (!(nursePQ.isEmpty()||patientPQ.isEmpty())) {
            String[] newShift = assembleNewShift();

            NurseObject nurseToAssignTo = nursePQ.remove();
            int dayToFill = findShiftToFill(nurseToAssignTo);

            fillShiftAndReset(nurseToAssignTo, newShift, dayToFill);
        }
    }

    //This method assembles and returns a new shift.
    public String[] assembleNewShift() {
        String[] newShift = new String[24];
        int length = newShift.length;
        int index = 0;
        while (index<length-1) {
            PatientObject patientToAdd = patientPQ.remove();
            int visitLen = (patientToAdd.isExtendedVisit()) ? 4 : 3;
            //This section of the method determines whether a patient's
            //appointment can be fitted into a shift, and may find a
            //replacement of possibly lower priority if only a shorter
            //appointment will fit by calling findReplacement().
            boolean addToShift = true;
            if ((index + visitLen) == length && visitLen == 4) {
                patientToAdd = findReplacement(patientToAdd, visitLen);
                if (!patientToAdd.isExtendedVisit()) {
                    visitLen = 3;
                } else {
                    addToShift = false;
                }
            } else if ((index + visitLen) >= length) {
                addToShift = false;
            }
            //This section of the method adds an appointment to the shift
            //if there is capacity.
            if (addToShift) {
                for (int count = 0; count <= visitLen; count++) {
                    newShift[index] = patientToAdd.getPatientID();
                    index++;
                }
                if (index < length) {
                    newShift[index] = "BREAK";
                    index++;
                }
            } else {
                index = length;
            }
        }
        return newShift;
    }

    //This method finds and returns a replacement patient to add to the shift if the
    //current patient selected will not fit (as longer appointment needed), whereas
    // another patient with a shorter visit time could.
    public PatientObject findReplacement(PatientObject patientToAdd, int visitLen) {
        ArrayList<PatientObject> extendedPatients = new ArrayList<>();
        while (visitLen == 4 && !patientPQ.isEmpty()) {
            if (!patientToAdd.isExtendedVisit()) {
                visitLen = 3;
            } else {
                extendedPatients.add(patientToAdd);
                patientToAdd = patientPQ.remove();
            }
        }
        while (!extendedPatients.isEmpty()){
            patientPQ.add(extendedPatients.remove(0));
        }
        return patientToAdd;
    }

    //This method finds and returns the index value of a nurse's
    //first available shift to fill.
    public int findShiftToFill(NurseObject nurseToAssignTo) {
        boolean[] workPattern = nurseToAssignTo.getWorkPattern();
        int shiftsFilled = nurseToAssignTo.getShiftsFilled();
        int dayToFill = -1;


        int count = getNumFalses(workPattern);
        int index = 0;
        while (index < workPattern.length && dayToFill == -1) {
            boolean aDay = workPattern[index];
            if (aDay) {
                count++;
                if (count > shiftsFilled) {
                    dayToFill = index;
                }
            }
            index++;
        }
        return dayToFill;
    }

    //In order to help find the first shift available to fill,
    //this method counts and returns the number of days on
    //which the nurse does not work.
    public int getNumFalses(boolean[] workPattern) {
        int falseCounter = 0;
        for (boolean day : workPattern) {
            if (!day) {falseCounter++;}
        }
        return falseCounter;
    }

    //This method assigns the shift newShift to nurseToAssignTo on
    //the dayToFill, then determines whether nurseToAssignTo can be
    //returned to nursePQ based on whether it has any shifts left
    //to fill.
    public void fillShiftAndReset(NurseObject nurseToAssignTo, String[] newShift, int dayToFill) {
        nurseToAssignTo.setShift(newShift, dayToFill);
        int shiftsFilled = nurseToAssignTo.getShiftsFilled() + 1;
        nurseToAssignTo.setShiftsFilled(shiftsFilled);
        if (shiftsFilled < 5) {
            nursePQ.add(nurseToAssignTo);
        }
    }


}
