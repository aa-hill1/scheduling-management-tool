package com.oopModel.storage;

import com.oopModel.algs.OptAlg;
import com.oopModel.algs.ScheduleAlg;
import com.google.gson.Gson;
import com.oopModel.NurseObject;
import com.oopModel.PatientObject;
import com.oopModel.account.AdminAccount;
import com.oopModel.account.EndUserAccount;
import com.oopModel.matrix.AdjMatrix;
import com.oopModel.matrix.Location;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;

public class DataStructures {
    private boolean adminUser = false;
    private EndUserAccount accountLoggedIn;

    private HashMap<String, EndUserAccount> allAccountsUNameDict;

    private HashMap<String, EndUserAccount> euAccountsUNameDict;
    private HashMap<String, AdminAccount> adminAccountsNameDict;

    private HashMap<String, NurseObject> nurseObjIdDict;
    private HashMap<String, NurseObject> nurseObjNameDict;

    private HashMap<String, PatientObject> patientObjIdDict;
    private HashMap<String, PatientObject> patientObjNameDict;

    private HashMap<String, Location> locationIdDict;
    private HashMap<String, Location> locationNameDict;

    private ArrayList<NurseObject> nurseArrayList;
    private ArrayList<PatientObject> patientArrayList;
    private ArrayList<Location> locationArrayList;

    private HashMap<String, Integer> workPatternJsonKeyDict;
    private HashMap<Integer, String> workPatternIdKeyDict;

    private DBaseConnection dBaseConnection;

    private Gson gson = new Gson();

    public DataStructures() {
        dBaseConnection = new DBaseConnection();
        allAccountsUNameDict = new HashMap<>();
        assembleAccDict("Both");
    }

    //-----------------------------------------SETUP-----------------------------------------

    public void initialiseStructures() {
        if (adminUser) {
            adminAccountsNameDict = new HashMap<>();
            euAccountsUNameDict = new HashMap<>();
            assembleAccDict("EU");
            assembleAccDict("Admin");

        }

        assembleWorkPatternDict();
        assembleNurseArrayList();
        assembleLocationArrayList();
        assemblePatientArrayList();

        assembleBaseObjectDicts();
    }

    //This method uses the attribute dBaseConnection to connect to the database, then queries it with one or more SELECT queries to gather
    //account data, which can then be instantiated into objects and stored in dictionaries.
    public void assembleAccDict(String whichAcc) {
        //Based on the value of whichAcc ("Both", "Admin" or "EU"), the method either retrieves data for and instantiates instances
        //of EndUserAccount, AdminAccount or both classes, then adds the objects created to a specific dictionary.
        try {
            Statement statement = dBaseConnection.connectToDBase();
            if (whichAcc.equals("Admin")||whichAcc.equals("Both")) {
                ResultSet adminData = statement.executeQuery("SELECT id_admin, salt, passwordhash, username, name FROM adminaccount");
                while (adminData.next()) {
                    //While the ResultSet adminData, made up of data from the database still contains records to iterate through, the program
                    //gets and formats the values of each field so that they can be used to instantiate instances of AdminAccount.
                    byte[] salt = gson.fromJson(adminData.getString("salt"), byte[].class);
                    AdminAccount account = new AdminAccount(adminData.getString("id_admin"), adminData.getString("username"),
                            adminData.getString("passwordhash"), salt, adminData.getString("name"));

                    if (whichAcc.equals("Both")) {
                        allAccountsUNameDict.put(account.getUsername(), account);
                    } else {
                        adminAccountsNameDict.put(account.getName(), account);
                    }
                }
            }
            if (whichAcc.equals("EU")||whichAcc.equals("Both")) {
                ResultSet euData = statement.executeQuery("SELECT nurse_id, salt, passwordhash, username FROM nurse_account");
                while (euData.next()) {
                    //While the ResultSet euData, made up of data from the database still contains records to iterate through, the program
                    //gets and formats the values of each field so that they can be used to instantiate instances of EndUserAccount.
                    byte[] salt = gson.fromJson(euData.getString("salt"), byte[].class);
                    EndUserAccount account = new EndUserAccount(euData.getString("nurse_id"),
                            euData.getString("username"), euData.getString("passwordhash"), salt);

                    if (whichAcc.equals("Both")) {
                        allAccountsUNameDict.put(account.getUsername(), account);
                    } else {
                        euAccountsUNameDict.put(account.getUsername(), account);
                    }
                }
            }
        } catch (SQLException e) {e.printStackTrace();}
    }

    public void assembleBaseObjectDicts() {
        nurseObjIdDict = new HashMap<>();
        nurseObjNameDict = new HashMap<>();
        for (NurseObject nurseObject : nurseArrayList) {
            if (adminUser) {
                nurseObjNameDict.put(nurseObject.getName(), nurseObject);
            }
            nurseObjIdDict.put(nurseObject.getNurseID(), nurseObject);
        }
        patientObjIdDict = new HashMap<>();
        patientObjNameDict = new HashMap<>();
        for (PatientObject patientObject : patientArrayList) {
            if (adminUser) {
                patientObjNameDict.put(patientObject.getName(), patientObject);
            }
            patientObjIdDict.put(patientObject.getPatientID(), patientObject);
        }
        locationIdDict = new HashMap<>();
        locationNameDict = new HashMap<>();
        for (Location location : locationArrayList) {
            if (adminUser) {
                locationNameDict.put(location.getName(), location);
            }
            locationIdDict.put(location.getLocationID(), location);
        }
    }

    public void assembleWorkPatternDict() {
        workPatternJsonKeyDict = new HashMap<>();
        workPatternIdKeyDict = new HashMap<>();
        try {
            Statement statement = dBaseConnection.connectToDBase();
            ResultSet workPatternData = statement.executeQuery("SELECT idworkpattern, day_one, day_two, day_three, day_four, day_five FROM workpattern");
            while (workPatternData.next()) {
                int workPatternID = workPatternData.getInt("idworkpattern");
                boolean[] workPattern = {workPatternData.getBoolean("day_one"), workPatternData.getBoolean("day_two"),
                        workPatternData.getBoolean("day_three"), workPatternData.getBoolean("day_four"), workPatternData.getBoolean("day_five")};
                workPatternJsonKeyDict.put(gson.toJson(workPattern), workPatternID);
                workPatternIdKeyDict.put(workPatternID, gson.toJson(workPattern));
            }

        } catch (SQLException e) {e.printStackTrace();}
    }

    //This method queries the nurse table in the database, in order to retrieve nurse data to
    //format and instantiate into instances of NurseObject which are then appended to nurseArrayList.
    public void assembleNurseArrayList() {
        nurseArrayList = new ArrayList<>();
        try {
            Statement statement = dBaseConnection.connectToDBase();

            ResultSet nurseData = statement.executeQuery("SELECT idnurse, name, workpattern_id, routes, orderedshifts FROM nurse");
            while (nurseData.next()) {
                //While the ResultSet nurseData still contains records to iterate through, each field formatted and stored as a variable,
                //each of are then used as parameters to instantiate new instance of NurseObject.
                String nurseID = nurseData.getString("idnurse");
                String name = nurseData.getString("name");
                int workPatternID = nurseData.getInt("workpattern_id");
                boolean[] workPattern = gson.fromJson(workPatternIdKeyDict.get(workPatternID), boolean[].class);
                String[][] orderedShifts = getGson().fromJson(nurseData.getString("orderedshifts"), String[][].class);
                String[] routes = getGson().fromJson(nurseData.getString("routes"), String[].class);

                NurseObject nurse = new NurseObject(nurseID, name, workPattern, orderedShifts, routes);
                nurseArrayList.add(nurse);
            }
        } catch (SQLException e) {e.printStackTrace();}
    }
    public void assembleLocationArrayList() {
        locationArrayList = new ArrayList<>();
        try {
            Statement statement = dBaseConnection.connectToDBase();
            ResultSet locationData = statement.executeQuery("SELECT idlocation, name, adjacencies, adjweights FROM location");
            while (locationData.next()) {
                String[] adjacencies = getGson().fromJson(locationData.getString("adjacencies"), String[].class);
                int[] adjWeights = getGson().fromJson(locationData.getString("adjweights"), int[].class);

                Location location = new Location(locationData.getString("idlocation"), locationData.getString("name"), adjacencies, adjWeights);
                locationArrayList.add(location);
            }

        } catch (SQLException e) {e.printStackTrace();}
    }
    public void assemblePatientArrayList() {
        patientArrayList = new ArrayList<>();
        try {
            Statement statement = dBaseConnection.connectToDBase();
            ResultSet patientData = statement.executeQuery("SELECT idpatient, name, info, plevel, extendedvisit, address_line_one, location_id FROM patient");
            while (patientData.next()) {
                Location patientLocation = findLocationFromArrayList(patientData.getString("location_id"));
                PatientObject patient = new PatientObject(patientData.getString("idpatient"), patientData.getString("name"),
                        patientData.getString("info"), patientData.getInt("plevel"),
                        patientData.getBoolean("extendedvisit"),patientData.getString("address_line_one"), patientLocation);
                patientArrayList.add(patient);
            }
        } catch (SQLException e) {e.printStackTrace();}
    }
    //-----------------------------------------SETUP-----------------------------------------

    //-----------------------------------------GETTERS-----------------------------------------
    public EndUserAccount getAccountLoggedIn() {
        return accountLoggedIn;
    }

    public HashMap<String, EndUserAccount> getAllAccountsUNameDict() {
        return allAccountsUNameDict;
    }
    public HashMap<String, EndUserAccount> getEuAccountsUNameDict() {
        return euAccountsUNameDict;
    }
    public HashMap<String, AdminAccount> getAdminAccountsNameDict() {
        return adminAccountsNameDict;
    }

    public HashMap<String, NurseObject> getNurseObjNameDict() {
        return nurseObjNameDict;
    }
    public HashMap<String, PatientObject> getPatientObjNameDict() {
        return patientObjNameDict;
    }
    public HashMap<String, Location> getLocationNameDict() {
        return locationNameDict;
    }

    public HashMap<String, NurseObject> getNurseObjIdDict() {
        return nurseObjIdDict;
    }
    public HashMap<String, PatientObject> getPatientObjIdDict() {
        return patientObjIdDict;
    }
    public HashMap<String, Location> getLocationIdDict() {
        return locationIdDict;
    }

    public HashMap<String, Integer> getWorkPatternJsonKeyDict() {
        return workPatternJsonKeyDict;
    }

    public ArrayList<NurseObject> getNurseArrayList() {
        return nurseArrayList;
    }
    public ArrayList<PatientObject> getPatientArrayList() {
        return patientArrayList;
    }

    public Gson getGson() {
        return gson;
    }
    //-----------------------------------------GETTERS-----------------------------------------

    //-----------------------------------------SETTERS-----------------------------------------
    public void setAccountLoggedIn(EndUserAccount accountLoggedIn) {
        this.accountLoggedIn = accountLoggedIn;
        if (accountLoggedIn instanceof AdminAccount) {
            adminUser = true;
        }
    }

    public void replaceInPatientStructures(String originalName, PatientObject updated) {
        PatientObject original = patientObjNameDict.get(originalName);
        removeFromPatientStructures(original);
        addToPatientStructures(updated);
    }
    public void replaceInNurseStructures(String originalName, NurseObject updatedNurse) {
        NurseObject original = nurseObjNameDict.get(originalName);
        removeFromNurseStructuresOnly(original);
        addToNurseStructuresOnly(updatedNurse);
    }
    public void replaceInAccountStructures(String originalUName, EndUserAccount updatedAcc, boolean adminAcc) {
        EndUserAccount originalAcc = allAccountsUNameDict.get(originalUName);
        removeFromAccountStructures(originalAcc, adminAcc);
        addToAccountStructures(updatedAcc, adminAcc);
    }

    //Example of method called when an admin user updates the attributes of a location.
    public void replaceInLocationStructures(String originalName, Location updated) {
        Location original = locationNameDict.get(originalName);
        removeFromLocationStructures(original);
        addToLocationStructures(updated);
    }

    public void addToPatientStructures(PatientObject item) {
        patientObjNameDict.put(item.getName(), item);
        patientObjIdDict.put(item.getPatientID(), item);
        patientArrayList.add(item);
    }

    //Example of method called when an admin user inserts a new nurse into the system, which also calls
    // addToAccountStructures to insert the new, corresponding End-User account.
    public void addToNurseStructures(NurseObject nurseToAdd, EndUserAccount accToAdd) {
        addToNurseStructuresOnly(nurseToAdd);
        addToAccountStructures(accToAdd, false);
    }
    public void addToNurseStructuresOnly(NurseObject nurseToAdd) {
        nurseObjNameDict.put(nurseToAdd.getName(), nurseToAdd);
        nurseObjIdDict.put(nurseToAdd.getNurseID(), nurseToAdd);
        nurseArrayList.add(nurseToAdd);
    }
    //Method called either when an admin user adds a new Admin account,
    //or, as a result of an admin user creating a new nurse.
    public void addToAccountStructures(EndUserAccount item, boolean adminAcc) {
        allAccountsUNameDict.put(item.getUsername(), item);
        if (adminAcc) {
            adminAccountsNameDict.put(((AdminAccount) item).getName(), (AdminAccount) item);
        } else {
            euAccountsUNameDict.put(item.getUsername(), item);
        }

    }
    public void addToLocationStructures(Location item) {
        locationNameDict.put(item.getName(), item);
        locationIdDict.put(item.getLocationID(), item);
        locationArrayList.add(item);
    }

    //Example method of how DataStructures maintains its state when an admin user deletes a patient.
    public void removeFromPatientStructures(PatientObject patientToDelete) {
        patientArrayList.remove(patientToDelete);
        patientObjIdDict.remove(patientToDelete.getPatientID());
        patientObjNameDict.remove(patientToDelete.getName());
    }
    public void removeFromNurseStructures(NurseObject nurseToDelete, EndUserAccount accToDelete) {
        removeFromNurseStructuresOnly(nurseToDelete);
        removeFromAccountStructures(accToDelete, false);
    }
    public void removeFromNurseStructuresOnly(NurseObject nurseToDelete) {
        nurseArrayList.remove(nurseToDelete);
        nurseObjIdDict.remove(nurseToDelete.getNurseID());
        nurseObjNameDict.remove(nurseToDelete.getName());
    }
    public void removeFromAccountStructures(EndUserAccount accToDelete, boolean adminAcc) {
        allAccountsUNameDict.remove(accToDelete.getUsername());
        if (adminAcc) {
            adminAccountsNameDict.remove(((AdminAccount) accToDelete).getName());
        } else {
            euAccountsUNameDict.remove(accToDelete.getUsername());
        }
    }
    public void removeFromLocationStructures(Location locationToDelete) {
        locationArrayList.remove(locationToDelete);
        locationNameDict.remove(locationToDelete.getName());
        locationIdDict.remove(locationToDelete.getLocationID());
    }
    //-----------------------------------------SETTERS-----------------------------------------

    public Statement getStatement() {
        return dBaseConnection.connectToDBase();
    }

    //When an admin user changes data, the data they have either updated, inserted or deleted is sent to these methods
    //each of which call the corresponding method in the attribute dbConnection.
    public void updateInDb(String table, String[] columns, String[] data, boolean[] typeIsString, String id) {
        dBaseConnection.updateRecord(table, columns, data, typeIsString, id);
    }
    public void insertInDb(String table, String[] values, boolean[] typeIsString) {
        dBaseConnection.insertRecord(table, values, typeIsString);
    }
    public void insertInDbNotAllColumns(String table, String[] columns, String[] values, boolean[] typeIsString) {
        dBaseConnection.insertNotAllColumns(table, columns, values, typeIsString);
    }
    public void deleteInDb(String table, String idFieldName, String id) {
        dBaseConnection.deleteRecord(table, idFieldName, id);
    }

    public Location findLocationFromArrayList(String locationID) {
        int locationIndex = 0;
        boolean found = false;
        while (locationIndex < locationArrayList.size() && !found) {
            if ((locationArrayList.get(locationIndex).getLocationID()).equals(locationID)) {
                found = true;
            } else {
                locationIndex++;
            }
        }
        return locationArrayList.get(locationIndex);
    }

    //This method resets all nurses routes and shifts through calling
    //resetAllNursesShiftsRoutes(), then instantiates instances of ScheduleAlg,
    //OptAlg and AdjMatrix and starts shift scheduling, then route optimisation.
    public void calculateShiftsAndRoutes() {
        resetAllNursesShiftsRoutes();
        ScheduleAlg schedulingAlg = new ScheduleAlg(patientArrayList, nurseArrayList);
        schedulingAlg.schedule();

        OptAlg optimisationAlg = new OptAlg(patientObjIdDict,
                setUpAdjMatrix(), nurseArrayList, locationNameDict.get("Hospice"), locationIdDict);
        optimisationAlg.cycleNursesAndShifts();
    }
    //In order to reset the shifts and routes of each nurse, this method iterates
    //through nurseArrayList, an attribute of DataStructures which contains all
    //existing nurses and calls the method assembleBaseShiftsRoutes() for each,
    //which resets their shifts and routes.
    public void resetAllNursesShiftsRoutes() {
        for (NurseObject nurseObject : nurseArrayList) {
            nurseObject.assembleBaseShiftsRoutes();
        }
    }
    //This method instantiates and returns an instance of AdjMatrix,
    //using all existing Locations as nodes.
    public AdjMatrix setUpAdjMatrix() {
        Location[] locations = new Location[locationArrayList.size()];
        for (int index=0; index<locationArrayList.size(); index++) {
            locations[index] = locationArrayList.get(index);
        }
        return new AdjMatrix(locations.length, locations);
    }

    //This method iterates through each nurse, updating the corresponding record for each
    //in the database with their newly calculated routes and orderedShifts, using dBaseConnection
    //to make updates to the database.
    public void saveShiftsAndRoutes() {
        for (NurseObject currentNurse : nurseArrayList) {
            String orderedShiftsJson = gson.toJson(currentNurse.getOrderedShifts());
            String routesJson = gson.toJson(currentNurse.getRoutes());
            String[] columns = {"orderedshifts", "routes"};
            String[] values = {orderedShiftsJson, routesJson};
            boolean[] typeIsString = {true, true};
            updateInDb("nurse", columns, values, typeIsString, currentNurse.getNurseID());
        }
    }
}
