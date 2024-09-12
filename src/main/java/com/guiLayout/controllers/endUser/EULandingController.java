package com.guiLayout.controllers.endUser;

import com.oopModel.NurseObject;
import com.oopModel.PatientObject;
import com.oopModel.account.EndUserAccount;
import com.oopModel.storage.DataStructures;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.HPos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.util.HashMap;

public class EULandingController {
    @FXML
    private Button logoutButton;
    @FXML
    private Button routeButton;
    @FXML
    private Label currentAccount;
    @FXML
    private GridPane shiftGrid;
    @FXML
    AnchorPane anchorPane;

    private Stage stage;
    private Scene loginScene;
    private Scene euRouteScene;
    private boolean onSceneOpen;

    private DataStructures dataStructures;
    private NurseObject nurseObject;
    private EndUserAccount accountLoggedIn;

    public void initialize() {
        onSceneOpen = true;
    }

    @FXML protected void setUpScene() {
        if (onSceneOpen) {
            stage = (Stage)  logoutButton.getScene().getWindow();
            dataStructures = (DataStructures) stage.getUserData();
            accountLoggedIn = dataStructures.getAccountLoggedIn();
            nurseObject = dataStructures.getNurseObjIdDict().get(accountLoggedIn.getAccountID());
            displayShifts();
            onSceneOpen = false;
        }
        currentAccount.setText(accountLoggedIn.getUsername());

    }

    public void displayShifts() {
        HashMap<String, PatientObject> patientObjIdDict = dataStructures.getPatientObjIdDict();
        String[][] orderedShifts = nurseObject.getOrderedShifts();
        for (int dayIndex=0; dayIndex<5; dayIndex++) {
            for (int slotIndex=0; slotIndex<24; slotIndex++) {
                String slotData = "";
                if (patientObjIdDict.containsKey(orderedShifts[dayIndex][slotIndex])) {
                    PatientObject currentPatient = patientObjIdDict.get(orderedShifts[dayIndex][slotIndex]);
                    slotData = currentPatient.getName();
                }
                Label slotLabel = new Label(slotData);
                shiftGrid.add(slotLabel, dayIndex+1, slotIndex+1);
                GridPane.setHalignment(slotLabel, HPos.CENTER);
            }
        }
    }

    //The methods below are borrowed code from https://stackoverflow.com/questions/12804664/how-to-swap-screens-in-a-javafx-application-in-the-controller-class
    public void setEURouteScene(Scene scene) {euRouteScene = scene;}
    @FXML protected void openEURouteScene() {
        stage.setScene(euRouteScene);
    }

    public void setLoginScene(Scene scene) {loginScene = scene;}
    @FXML protected void openLoginScene() {stage.setScene(loginScene);}
}
