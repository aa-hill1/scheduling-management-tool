package com.guiLayout.controllers.admin;

import com.oopModel.NurseObject;
import com.oopModel.PatientObject;
import com.oopModel.account.AdminAccount;
import com.oopModel.account.EndUserAccount;
import com.oopModel.matrix.Location;
import com.oopModel.storage.DataStructures;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;

import static com.algorithms.FormattingMethods.*;

public class AdminEditController {
    @FXML
    private Label currentAccount;
    @FXML
    private Button backButton;
    @FXML
    private Button logoutButton;
    @FXML
    private AnchorPane anchorPane;
    @FXML
    private Button saveAllButton;


    @FXML
    private GridPane dataGrid;
    @FXML
    private ComboBox<String> tableComboBox;
    @FXML
    private TextField recordSearchTextField;
    @FXML
    private Button searchRecordsButton;
    @FXML
    private Label statusLabel;

    private ArrayList<Label> labels;
    private ArrayList<TextField> textFields;

    private Stage stage;
    private Scene loginScene;
    private Scene adminLandingScene;
    private boolean onSceneOpen;

    private DataStructures dataStructures;

    private String tableToEdit;
    private String currentRecordName;
    private int fieldNum;

//---------------SCENE-SETUP/SWITCHING---------------
    public void initialize() {
        onSceneOpen = true;
    }
    @FXML protected void setUpScene() {
        if (onSceneOpen) {
            stage = (Stage) anchorPane.getScene().getWindow();
            dataStructures = (DataStructures) stage.getUserData();


            tableComboBox.getItems().addAll("Nurse", "Patient", "Admin", "Location");
            tableToEdit = "";
            fieldNum = 0;
            setUpGrid();

            onSceneOpen = false;
        }
        currentAccount.setText(dataStructures.getAccountLoggedIn().getUsername());
    }

    //The next four methods below are borrowed code from https://stackoverflow.com/questions/12804664/how-to-swap-screens-in-a-javafx-application-in-the-controller-class
    public void setLoginScene(Scene scene) {loginScene = scene;}
    @FXML protected void openLoginScene() {
        stage.setScene(loginScene);
    }

    public void setAdminLandingScene(Scene scene) {adminLandingScene = scene;}
    @FXML protected void openAdminLandingScene() {
        stage.setScene(adminLandingScene);
    }
//---------------SCENE-SETUP/SWITCHING---------------


//-----------------------GRIDPANE-FORMATTING-----------------------
    public void setUpGrid() {
        labels = new ArrayList<>();
        textFields = new ArrayList<>();
        int labelCounter = 0;
        int fieldCounter = 0;
        for (int index = 0; index < 12; index++) {
            if (index%2==0) {
                labels.add(new Label());
                dataGrid.add(labels.get(labelCounter), 1, index);
                labelCounter++;
                if (index==0) {
                    Button updateButton = initialiseButton("Update");
                    dataGrid.add(updateButton, 0, index);
                } else if (index==2) {
                    Button insertButton = initialiseButton("Insert");
                    dataGrid.add(insertButton, 0, index);
                }
            } else {
                textFields.add(new TextField());
                dataGrid.add(textFields.get(fieldCounter), 1, index);
                fieldCounter++;
                if (index==1) {
                    Button deleteButton = initialiseButton("Delete");
                    dataGrid.add(deleteButton, 0, index);
                }
            }
        }
    }
    public Button initialiseButton(String buttonAction) {
        Button button = new Button();
        button.setText(buttonAction);
        button.setOnAction(actionEvent -> {
            forceUpdateShiftsRoutes();
            statusLabel.setText(null);
            try {
                switch (tableToEdit) {
                    case "Location":
                        switch (buttonAction) {
                            case "Update":
                                validateLocation(false);
                                break;
                            case "Insert":
                                validateLocation(true);
                                break;
                            default:
                                deleteLocation();
                        }
                        break;
                    case "Admin":
                        switch (buttonAction) {
                            case "Update":
                                validateAdmin(false);
                                break;
                            case "Insert":
                                validateAdmin(true);
                                break;
                            default:
                                deleteAdmin();
                        }
                        break;
                    case "Nurse":
                        switch (buttonAction) {
                            case "Update":
                                validateNurse(false);
                                break;
                            case "Insert":
                                validateNurse(true);
                                break;
                            default:
                                deleteNurse();
                        }
                        break;
                    case "Patient":
                        switch (buttonAction) {
                            case "Update":
                                validatePatient(false);
                                break;
                            case "Insert":
                                validatePatient(true);
                                break;
                            default:
                                deletePatient();
                        }
                        break;
                    default:
                        statusLabel.setText("Error - cannot perform action when no table selected");
                }
            } catch (NoSuchAlgorithmException e) {e.printStackTrace();}
        });
        return button;
    }

    @FXML protected void onTableToEditUpdate() {
        tableToEdit = tableComboBox.getValue();
        recordSearchTextField.clear();
        clearCurrentRecordData();
        updateLabelNames();
    }
    public void updateLabelNames() {
        String[] labelNames = {""};
        refreshLabels(labelNames, true);

        switch (tableToEdit) {
            case "Location":
                fieldNum = 2;
                labelNames = new String[]{"Name", "Adjacent locations and distances (in format: \"Name: distance(km), Name: distance, ...\")"};
                break;
            case "Admin":
                fieldNum = 3;
                labelNames = new String[]{"Name", "Set new password", "Confirm new password"};
                break;
            case "Nurse":
                fieldNum = 4;
                labelNames = new String[]{"Name", "Days off (in format: \"Mon, Wed, Thu, ...\")", "Set new password", "Confirm new password"};
                break;
            default:
                fieldNum = 6;
                labelNames = new String[]{"Name", "Information", "Priority level (1-3)", "Requires extended visit (Y/N)", "Address line one", "Location name"};
        }
        refreshLabels(labelNames, false);
    }
    public void refreshLabels(String[] labelNames, boolean clear) {
        for (int index = 0; index<fieldNum; index++) {
            String textToSet = (clear) ? "" : labelNames[index];
            labels.get(index).setText(textToSet);
        }
    }
//-----------------------GRIDPANE-FORMATTING-----------------------

//-----------------------SEARCH+DISPLAY-RECORD-----------------------
    @FXML protected void onRecordSearch() {
        statusLabel.setText("");
        clearCurrentRecordData();
        if (tableToEdit == null) {
            statusLabel.setText("Error - table not selected");
        } else {
            String nameToFind = recordSearchTextField.getText();
            if (nameToFind.equals("")) {
                statusLabel.setText("Error - search field empty");
            } else if (!checkRecordExists(nameToFind, false)) {
                statusLabel.setText("Error - record with name: \""+nameToFind+"\" does not exist in "+tableToEdit+" data");
                recordSearchTextField.clear();
            } else {
                currentRecordName = nameToFind;
                displayRecordData();
            }
        }
    }
    public void clearCurrentRecordData() {
        for (int index=0; index<fieldNum; index++) {
            textFields.get(index).clear();
        }
        currentRecordName = "";
    }
    public boolean checkRecordExists(String name, boolean checkForInsert) {
        boolean alreadyExists = false;
        switch (tableToEdit) {
            case "Location":
                alreadyExists = dataStructures.getLocationNameDict().containsKey(name);
                break;
            case "Admin":
                alreadyExists = dataStructures.getAdminAccountsNameDict().containsKey(name);
                break;
            case "Nurse":
                alreadyExists = dataStructures.getNurseObjNameDict().containsKey(name);
                break;
            default:
                alreadyExists = dataStructures.getPatientObjNameDict().containsKey(name);
        }
        if (alreadyExists && checkForInsert) {
            statusLabel.setText("Error - cannot insert record as it already exists, search for and update instead");
        }
        return alreadyExists;
    }
    public void displayRecordData() {
        switch (tableToEdit) {
            case ("Location"):
                setLocationFields();
                break;
            case ("Admin"):
                setAdminFields();
                break;
            case ("Nurse"):
                setNurseFields();
                break;
            default:
                setPatientFields();
            }
    }

    public void setLocationFields() {
        Location currentRecord = dataStructures.getLocationNameDict().get(currentRecordName);
        textFields.get(0).setText(currentRecordName);
        textFields.get(1).setText(formatAdjAndWeightsToView(currentRecord.getAdjacencies(), currentRecord.getAdjWeights(), dataStructures.getLocationIdDict()));
    }
    public void setAdminFields() {
        textFields.get(0).setText(currentRecordName);
    }
    public void setNurseFields() {
        NurseObject currentRecord = dataStructures.getNurseObjNameDict().get(currentRecordName);
        textFields.get(0).setText(currentRecordName);
        textFields.get(1).setText(formatWorkPatternToView(currentRecord.getWorkPattern()));
    }
    public void setPatientFields() {
        PatientObject currentRecord = dataStructures.getPatientObjNameDict().get(currentRecordName);
        textFields.get(0).setText(currentRecordName);
        textFields.get(1).setText(currentRecord.getInformation());
        textFields.get(2).setText(Integer.toString(currentRecord.getpLevel()));
        textFields.get(3).setText(formatExtendedVisitToView(currentRecord.isExtendedVisit()));
        textFields.get(4).setText(currentRecord.getAddressLineOne());
        textFields.get(5).setText(currentRecord.getLocation().getName());
    }
//-----------------------SEARCH+DISPLAY-RECORD-----------------------


    //This method is used to check whether data has
    //been selected, so that it can be deleted.
    public boolean checkRecordSelected() {
        boolean recordSelected = true;
        if (currentRecordName.equals("")) {
            recordSelected = false;
            statusLabel.setText("Error - cannot delete, no data selected");
        }
        return recordSelected;
    }
    //This method is used to validate input by checking
    //if the input string to too many characters long.
    public boolean isEnteredStringTooLong(String userInput, String type) {
        int maxLength;
        switch (type) {
            case "Name":
                maxLength = 45;
                break;
            case "Password":
                maxLength = 20;
                break;
            case "Info":
                maxLength = 200;
                break;
            default:
                maxLength = 100;
        }
        return (userInput.length() > maxLength);
    }

    //This method validates user input, formatting it into data which
    //can be used to instantiate in instance of Location. Then, it calls
    //saveLocation() to either update or insert the location.
    public void validateLocation(boolean insertRecord) throws NoSuchAlgorithmException {
        String name = textFields.get(0).getText();
        String adjAndWeights = textFields.get(1).getText();

        if (name.equals("") || adjAndWeights.equals("")) {
            statusLabel.setText("Error - field(s) left blank");
        } else if (isEnteredStringTooLong(name, "Name")) {
            statusLabel.setText("Error - name entered cannot be more than 45 characters long");
        } else {
            String[] adjacencies = formatUserInputToLocationAdj(adjAndWeights, dataStructures.getLocationNameDict());
            int[] adjWeights = formatUserInputToLocationWeights(adjAndWeights);

            if (adjWeights[0]==-1) {
                statusLabel.setText("Error - adjacent locations and weights entered incorrectly");
            } else if (adjacencies[0].startsWith("Invalid")) {
                statusLabel.setText("Error - adjacent location: \"" + adjacencies[0].substring(7) + "\" does not exist");
            } else if (insertRecord && checkRecordExists(name, true)) {
                    statusLabel.setText("Error - cannot insert record as one with same name already exists");
            } else {
                Location locationToSave = new Location(name, adjacencies, adjWeights);
                saveLocation(locationToSave, insertRecord);
                clearCurrentRecordData();
            }
        }
    }


    //This procedure either inserts or updates a location, as well
    // as updating that location's adjacencies.
    public void saveLocation(Location locationToSave, boolean insertRecord) {
        if (insertRecord) {
            //If the location is to be inserted, it adds the location
            //to the location structures in data structures, and inserts
            //the location into the database.
            dataStructures.addToLocationStructures(locationToSave);
            String[] values = formatLocationAttributesToSave(locationToSave, true);
            boolean[] typeIsString = {true, true, true, true};
            dataStructures.insertInDb("location", values, typeIsString);
        } else {
            //If the location is to be updated, it is passed to the method
            //prepAndUpdateLocation().
            prepAndUpdateLocation(currentRecordName, locationToSave);
        }
        updateAdjLocations(locationToSave, false);
        forceUpdateShiftsRoutes();
    }
    //This method updates the location in DataStructures, then updates the
    //corresponding location record in the database.
    public void prepAndUpdateLocation(String originalName, Location locationToUpdate) {
        dataStructures.replaceInLocationStructures(originalName, locationToUpdate);

        String[] columns = {"name", "adjacencies", "adjweights"};
        String[] data = formatLocationAttributesToSave(locationToUpdate, false);
        boolean[] typeIsString = {true, true, true};
        dataStructures.updateInDb("location", columns, data, typeIsString,
                locationToUpdate.getLocationID());
    }

    //This method iterates through each of the adjacencies of the location
    //calling modifyAdj() to update each one.
    public void updateAdjLocations(Location newLocation, boolean delete) {
        String newLocationId = newLocation.getLocationID();
        String[] newAdj = newLocation.getAdjacencies();
        int[] newWeights = newLocation.getAdjWeights();

        for (int index=0; index<newAdj.length; index++) {
            String adjID = newAdj[index];
            modifyAdj(adjID, newLocationId, newWeights[index], delete);
        }
    }
    //This procedure gets the location's adjacency, and either adds the new adjacency
    //between them to it, changes the weight of the existing adjacency or removes it.
    //It then calls prepAndUpdateLocation() to save the changes to the adjacent node
    //to the database.
    public void modifyAdj(String adjID, String newLocationID, int newWeight, boolean delete) {
        Location adjLocation = dataStructures.getLocationIdDict().get(adjID);
        if (delete || newWeight == 0) {
            adjLocation.removeAdjacency(newLocationID);
        } else if (!adjLocation.checkAdjExists(newLocationID)) {
            adjLocation.addAdjacency(newLocationID, newWeight);
        } else if (adjLocation.getWeight(newLocationID) != newWeight) {
            adjLocation.changeWeight(newLocationID, newWeight);
        }
        prepAndUpdateLocation(adjLocation.getName(), adjLocation);
    }

    public void deleteLocation() {
        if (checkRecordSelected()) {
            Location locationToDelete = dataStructures.getLocationNameDict().get(currentRecordName);
            if (checkLocationNotInUse(locationToDelete)) {
                statusLabel.setText("Error - cannot delete location as is in use");
            } else {
                dataStructures.removeFromLocationStructures(locationToDelete);
                dataStructures.deleteInDb("location", "idlocation", locationToDelete.getLocationID());
                updateAdjLocations(locationToDelete, true);
                clearCurrentRecordData();
                forceUpdateShiftsRoutes();
            }
        }
    }
    public boolean checkLocationNotInUse(Location location) {
        ArrayList<PatientObject> patientArrayList = dataStructures.getPatientArrayList();
        boolean locationInUse = false;
        int index = 0;
        while (index<patientArrayList.size() && !locationInUse) {
            PatientObject currentPatient = patientArrayList.get(index);
            if (currentPatient.getLocation().equals(location)) {
                locationInUse = true;
            } else {
                index++;
            }
        }
        return locationInUse;
    }

    public String[] formatLocationAttributesToSave(Location location, boolean attributesToInsert) {
        String[] values = new String[3];
        if (attributesToInsert) {
            values = new String[]{location.getLocationID(), location.getName(),
                    dataStructures.getGson().toJson(location.getAdjacencies()),
                    dataStructures.getGson().toJson(location.getAdjWeights())};
        } else {
            values[0] = location.getName();
            values[1] = dataStructures.getGson().toJson(location.getAdjacencies());
            values[2] = dataStructures.getGson().toJson(location.getAdjWeights());
        }
        return values;
    }

    public void validateAdmin(boolean insertRecord) throws NoSuchAlgorithmException {
        String name = textFields.get(0).getText();
        String pWord = textFields.get(1).getText();
        String pWordConfirm = textFields.get(2).getText();

        if (name.equals("") || (insertRecord && (pWord.equals("")||pWordConfirm.equals("")))) {
            statusLabel.setText("Error - field(s) left blank");
        } else if (isEnteredStringTooLong(name, "Name")) {
            statusLabel.setText("Error - name entered cannot be more than 45 characters long");
        } else if (!name.contains(" ")) {
            statusLabel.setText("Error - must enter name in format: \"FirstName Surname\"");
        } else if (!pWord.equals(pWordConfirm)) {
            statusLabel.setText("Error - passwords do not match");
        } else if (isEnteredStringTooLong(pWord, "Password")) {
            statusLabel.setText("Error - password entered cannot be more than 20 characters long");
        } else if (insertRecord && checkRecordExists(name, true)) {
            statusLabel.setText("Error - cannot insert record as one with same name already exists");
        } else {
            if (insertRecord) {
                AdminAccount newAccount = new AdminAccount(name, pWord);
                insertAdmin(newAccount);
            } else {
                AdminAccount accToUpdate = dataStructures.getAdminAccountsNameDict().get(currentRecordName);
                prepAndUpdateAdmin(accToUpdate, name, pWord);
            }
            clearCurrentRecordData();
        }
    }
    public void prepAndUpdateAdmin(AdminAccount accToUpdate, String  name, String pWord) throws NoSuchAlgorithmException {
        accToUpdate.setName(name);

        String[] columns = new String[2];
        String[] data = new String[2];
        boolean[] typeIsString = new boolean[2];
        if (!pWord.equals("")) {
            accToUpdate.setNewPassword(pWord);
            columns = new String[3];
            columns[2] = "passwordhash";
            data = new String[3];
            data[2] = accToUpdate.getPasswordHash();
            typeIsString = new boolean[3];
            typeIsString[2] = true;
        }
        columns[0] = "name";
        data[0] = name;
        typeIsString[0] = true;

        columns[1] = "username";
        data[1] = accToUpdate.getUsername();
        typeIsString[1] = true;

        dataStructures.replaceInAccountStructures(convertToUName(currentRecordName), accToUpdate, true);
        dataStructures.updateInDb("adminaccount", columns, data, typeIsString, accToUpdate.getAccountID());

        if (!accToUpdate.getUsername().equals(currentAccount.getText()) && currentRecordName.equals(dataStructures.getAccountLoggedIn().getUsername())) {
            currentAccount.setText(accToUpdate.getUsername());
        }
    }
    public void insertAdmin(AdminAccount accToSave) {
        dataStructures.addToAccountStructures(accToSave, true);
        String[] values = {accToSave.getAccountID(), dataStructures.getGson().toJson(accToSave.getSalt()),
                accToSave.getPasswordHash(), accToSave.getUsername(), accToSave.getName()};
        boolean[] typeIsString = {true, true, true, true, true};
        dataStructures.insertInDb("adminaccount", values, typeIsString);
    }

    public void deleteAdmin() {
        if (checkRecordSelected() && !checkIfAdminToDeleteLoggedIn()) {
            AdminAccount accToDelete = dataStructures.getAdminAccountsNameDict().get(currentRecordName);
            dataStructures.removeFromAccountStructures(accToDelete, true);
            dataStructures.deleteInDb("adminaccount", "id_admin", accToDelete.getAccountID());
            clearCurrentRecordData();
        }
    }
    public boolean checkIfAdminToDeleteLoggedIn() {
        boolean loggedIn = false;
        AdminAccount accountLoggedIn = (AdminAccount) dataStructures.getAccountLoggedIn();
        if (accountLoggedIn.getUsername().equals(convertToUName(currentRecordName))) {
            loggedIn = true;
            statusLabel.setText("Error - cannot delete your own account while logged in");
        }
        return loggedIn;
    }

    public void validateNurse(boolean insertRecord) throws NoSuchAlgorithmException {
        String name = textFields.get(0).getText();
        boolean[] daysOff = formatUserInputToWorkPattern(textFields.get(1).getText());
        String pWord = textFields.get(2).getText();
        String pWordConfirm = textFields.get(3).getText();

        if (name.equals("") || textFields.get(1).getText().equals("") || insertRecord && (pWord.equals("") || pWordConfirm.equals(""))) {
            statusLabel.setText("Error - field(s) left blank");
        } else if (isEnteredStringTooLong(name, "Name")) {
            statusLabel.setText("Error - name entered cannot be more than 45 characters long");
        } else if (!pWord.equals(pWordConfirm)) {
            statusLabel.setText("Error - passwords do not match");
        } else if (isEnteredStringTooLong(pWord, "Password")) {
            statusLabel.setText("Error - password entered cannot be more than 20 characters long");
        } else if (daysOff==null) {
            statusLabel.setText("Error - days off entered incorrectly");
        } else if (!name.contains(" ")) {
            statusLabel.setText("Error - must enter name in format: \"FirstName Surname\"");
        } else {
            if (insertRecord) {
                if (checkRecordExists(name, true)) {
                    statusLabel.setText("Error - cannot insert record as one with same name already exists");
                } else {
                    NurseObject nurseToInsert = new NurseObject(name, daysOff);
                    EndUserAccount euAccToInsert = new EndUserAccount(name, pWord);
                    insertNurse(nurseToInsert, euAccToInsert);
                    forceUpdateShiftsRoutes();
                }
            } else {
                formatAndUpdateNurse(name, daysOff);
                formatAndUpdateEuAccount(name, pWord);
                forceUpdateShiftsRoutes();
            }
            clearCurrentRecordData();
        }
    }
    public void formatAndUpdateEuAccount(String name, String pWord) throws NoSuchAlgorithmException {
        EndUserAccount accToUpdate = dataStructures.getEuAccountsUNameDict().get(convertToUName(currentRecordName));
        accToUpdate.setUsername(name);
        String[] columns = new String[1];
        String[] data = new String[1];
        boolean[] typeIsString = {true};

        if (!pWord.equals("")) {
            accToUpdate.setNewPassword(pWord);
            columns = new String[]{"", "passwordhash"};
            data = new String[]{"", accToUpdate.getPasswordHash()};
            typeIsString = new boolean[]{true, true};
        }
        columns[0] = "username";
        data[0] = accToUpdate.getUsername();
        dataStructures.replaceInAccountStructures(convertToUName(currentRecordName), accToUpdate, false);
        dataStructures.updateInDb("nurse_account", columns, data, typeIsString, accToUpdate.getAccountID());
    }
    public void formatAndUpdateNurse(String name, boolean[] daysOff) {
        NurseObject nurseToUpdate = dataStructures.getNurseObjNameDict().get(currentRecordName);
        nurseToUpdate.setName(name);
        nurseToUpdate.setWorkPattern(daysOff);

        String[] columns = {"name", "workPattern_id"};
        String[] data = {name, Integer.toString(determineWorkPatternID(daysOff))};
        boolean[] typeIsString = {true, false};

        dataStructures.replaceInNurseStructures(currentRecordName, nurseToUpdate);
        dataStructures.updateInDb("nurse", columns, data, typeIsString, nurseToUpdate.getNurseID());
    }
    public void insertNurse(NurseObject nurseToInsert, EndUserAccount euAccToInsert) {
        dataStructures.addToNurseStructures(nurseToInsert, euAccToInsert);

        int workPatternID = determineWorkPatternID(nurseToInsert.getWorkPattern());

        String[] nurseValues = {nurseToInsert.getNurseID(), nurseToInsert.getName(), Integer.toString(workPatternID)};
        String[] nurseColumns = {"idnurse", "name", "workpattern_id"};
        boolean[] nurseTypeIsString = {true, true, false};
        dataStructures.insertInDbNotAllColumns("nurse", nurseColumns, nurseValues, nurseTypeIsString);

        String[] euAccValues = {euAccToInsert.getAccountID(), dataStructures.getGson().toJson(euAccToInsert.getSalt()),
                euAccToInsert.getPasswordHash(), euAccToInsert.getUsername()};
        boolean[] euTypeIsString = {true, true, true, true};
        dataStructures.insertInDb("nurse_account", euAccValues, euTypeIsString);
    }

    //This method deletes a nurse and its corresponding End-User account from DataStructures
    //through calling the removeFromNurseStructures method, and from the database
    //by calling the deleteInDb() method for both the nurse and nurse_account tables.
    public void deleteNurse() {
        if (checkRecordSelected()) {
            NurseObject nurseToDelete = dataStructures.getNurseObjNameDict().get(currentRecordName);
            EndUserAccount accToDelete = dataStructures.getAllAccountsUNameDict().get(convertToUName(currentRecordName));
            dataStructures.removeFromNurseStructures(nurseToDelete, accToDelete);

            boolean[] nurseWorkPattern = nurseToDelete.getWorkPattern();
            String nurseWPJson = dataStructures.getGson().toJson(nurseWorkPattern);
            int nurseWorkPatternID = dataStructures.getWorkPatternJsonKeyDict().get(nurseWPJson);
            dataStructures.deleteInDb("nurse_account", "nurse_id", accToDelete.getAccountID());
            dataStructures.deleteInDb("nurse", "idnurse", nurseToDelete.getNurseID());
            if (!checkIfWorkPatternInUseElsewhere(nurseWorkPatternID)) {
                dataStructures.deleteInDb("workpattern", "idworkpattern", Integer.toString(nurseWorkPatternID));
            }

            forceUpdateShiftsRoutes();
            clearCurrentRecordData();
        }
    }
    //When a nurse is deleted, this method is run to check whether it is safe to delete
    //the nurse's working pattern from the database, as nurses and work patterns have
    //a many-to-one relationship.
    public boolean checkIfWorkPatternInUseElsewhere(int workPatternID) {
        int workPatternCount = 0;
        try {
            Statement statement = dataStructures.getStatement();
            String query = "SELECT idnurse FROM nurse, workpattern WHERE nurse.workpattern_id " +
                    "= workpattern.idworkpattern AND workpattern.idworkpattern = " + workPatternID;
            ResultSet workPatternData = statement.executeQuery(query);
            //This code queries the database, checking the number of nurse records linked
            //to the specific work pattern.
            while (workPatternData.next() && workPatternCount < 2) {
                workPatternCount++;
            }
        } catch (SQLException e) {e.printStackTrace();}
        return (workPatternCount > 1);
    }

    public int determineWorkPatternID(boolean[] workPattern) {
        String workPatternJson = dataStructures.getGson().toJson(workPattern);
        boolean patternAlreadyStored = dataStructures.getWorkPatternJsonKeyDict().containsKey(workPatternJson);
        if (!patternAlreadyStored) {
            String[] workPatternValues = formatWorkPatternToSave(workPattern);
            String[] columns = {"day_one", "day_two", "day_three", "day_four", "day_five"};
            boolean[] typeIsString = {false, false, false, false, false};
            dataStructures.insertInDbNotAllColumns("workpattern", columns, workPatternValues, typeIsString);
            dataStructures.assembleWorkPatternDict();
        }
        return dataStructures.getWorkPatternJsonKeyDict().get(workPatternJson);
    }

    public void validatePatient(boolean insertRecord) throws NoSuchAlgorithmException {
        String name = textFields.get(0).getText();
        String info = textFields.get(1).getText();
        int pLevel = formatUserInputToPLevel(textFields.get(2).getText());
        Boolean extendedVisit = formatUserInputToExtendedVisit(textFields.get(3).getText());
        String addressLineOne = textFields.get(4).getText();
        Location location = formatUserInputToLocationName(textFields.get(5).getText(), dataStructures.getLocationNameDict());

        if (name.equals("")||info.equals("")||addressLineOne.equals("")) {
            statusLabel.setText("Error - field(s) left blank");
        } else if (isEnteredStringTooLong(name, "Name")) {
            statusLabel.setText("Error - name entered cannot be more than 45 characters long");
        } else if (isEnteredStringTooLong(info, "Info")) {
            statusLabel.setText("Error - information entered cannot be more than 200 characters long");
        } else if (isEnteredStringTooLong(addressLineOne, "Address")) {
            statusLabel.setText("Error - address entered cannot be more than 100 characters long");
        } else if (pLevel==-1) {
            statusLabel.setText("Error - priority level formatted incorrectly, must enter 1, 2 or 3");
        } else if (extendedVisit==null) {
            statusLabel.setText("Error - extended visit formatted incorrectly, must enter \"Y\" or \"N\"");
        } else if (location==null) {
            statusLabel.setText("Error - location name entered does not exist");
        } else if (insertRecord && checkRecordExists(name, true)) {
            statusLabel.setText("Error - cannot insert record as one with same name already exists");
        } else {
            PatientObject patientToSave = new PatientObject(name, info, pLevel, extendedVisit, addressLineOne, location);
            savePatient(patientToSave, insertRecord);
            forceUpdateShiftsRoutes();
            clearCurrentRecordData();
        }
    }
    public void savePatient(PatientObject patientToSave, boolean insertRecord) {
        if (insertRecord) {
            insertPatient(patientToSave);
        } else {
            updatePatient(patientToSave);
        }
    }
    public void insertPatient(PatientObject patientToInsert) {
        dataStructures.addToPatientStructures(patientToInsert);
        String[] values = {patientToInsert.getPatientID(), patientToInsert.getName(), patientToInsert.getInformation(),
                Integer.toString(patientToInsert.getpLevel()), formatExtendedVisitToSave(patientToInsert.isExtendedVisit()),
                patientToInsert.getAddressLineOne(), patientToInsert.getLocation().getLocationID()};
        boolean[] typeIsString = {true, true, true, false, false, true, true};
        dataStructures.insertInDb("patient", values, typeIsString);
    }
    public void updatePatient(PatientObject updatedPatient) {
        String[] columns = {"name", "info", "plevel", "extendedvisit", "address_line_one", "location_id"};
        String[] data = {updatedPatient.getName(), updatedPatient.getInformation(), Integer.toString(updatedPatient.getpLevel()),
                formatExtendedVisitToSave(updatedPatient.isExtendedVisit()), updatedPatient.getAddressLineOne(), updatedPatient.getLocation().getLocationID()};
        boolean[] typeIsString = {true, true, false, false, true, true};
        dataStructures.updateInDb("patient", columns, data, typeIsString, updatedPatient.getPatientID());

        dataStructures.replaceInPatientStructures(currentRecordName, updatedPatient);
    }

    public void deletePatient() {
        if (checkRecordSelected()) {
            PatientObject patientToDelete = dataStructures.getPatientObjNameDict().get(currentRecordName);
            dataStructures.removeFromPatientStructures(patientToDelete);
            dataStructures.deleteInDb("patient", "idpatient", patientToDelete.getPatientID());
            forceUpdateShiftsRoutes();
            clearCurrentRecordData();
        }
    }

    public void forceUpdateShiftsRoutes() {
        backButton.setDisable(true);
        logoutButton.setDisable(true);
    }
    @FXML protected void updateAllShiftsRoutes() {
        if (!initialNodeExists()) {
            statusLabel.setText("Error - cannot set changes as no Hospice location exists");
        } else {
            dataStructures.calculateShiftsAndRoutes();
            dataStructures.saveShiftsAndRoutes();

            backButton.setDisable(false);
            logoutButton.setDisable(false);
        }
    }
    public boolean initialNodeExists() {
        HashMap<String, Location> locationNameDict = dataStructures.getLocationNameDict();
        return (locationNameDict.containsKey("Hospice"));
    }
}
