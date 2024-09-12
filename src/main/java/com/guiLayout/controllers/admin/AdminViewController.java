package com.guiLayout.controllers.admin;

import com.oopModel.NurseObject;
import com.oopModel.storage.DataStructures;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.HPos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.HashMap;

import static com.algorithms.ViewRouteMethods.*;

public class AdminViewController {
    @FXML
    private Button backButton;
    @FXML
    private Button logoutButton;
    @FXML
    private ComboBox<String> nurseComboBox;
    @FXML
    private Label currentAccount;
    @FXML
    private AnchorPane anchorPane;
    @FXML
    private ComboBox<String> routeComboBox;
    @FXML
    private Label errorLabel;
    @FXML
    private GridPane routeGrid;

    private Stage stage;
    private Scene loginScene;
    private Scene adminLandingScene;
    private ArrayList<ArrayList<Label>> routeLabels;

    private boolean onSceneOpen;
    private DataStructures dataStructures;
    private HashMap<String, NurseObject> nurseObjNameDict;

    public void initialize() {
        onSceneOpen = true;
    }

    @FXML protected void setUpScene() {
        if (onSceneOpen) {
            stage = (Stage) anchorPane.getScene().getWindow();
            dataStructures = (DataStructures) stage.getUserData();
            nurseObjNameDict = dataStructures.getNurseObjNameDict();
            setUpComboBoxes(dataStructures.getNurseArrayList());
            setUpGrid();
            onSceneOpen = false;
        }
        currentAccount.setText(dataStructures.getAccountLoggedIn().getUsername());
    }
    public void setUpGrid() {
        routeLabels = new ArrayList<>();
        for (int rowIndex=0; rowIndex<4; rowIndex++) {
            routeLabels.add(new ArrayList<>());
            for (int columnIndex=0; columnIndex<6; columnIndex++) {
                routeLabels.get(rowIndex).add(new Label());
                routeGrid.add(routeLabels.get(rowIndex).get(columnIndex), columnIndex+1, rowIndex);
                GridPane.setHalignment(routeLabels.get(rowIndex).get(columnIndex), HPos.CENTER);
            }
        }
    }
    public void clearGrid() {
        for (ArrayList<Label> labelRow : routeLabels) {
            for (Label label : labelRow) {
                label.setText("");
            }
        }
    }

    public void setUpComboBoxes(ArrayList<NurseObject> nurseArrayList) {
        routeComboBox.getItems().addAll("Monday", "Tuesday", "Wednesday", "Thursday", "Friday");
        for (NurseObject nurse : nurseArrayList) {
            nurseComboBox.getItems().add(nurse.getName());
        }
    }

    @FXML protected void viewRouteDay() {
        errorLabel.setText("");
        clearGrid();
        NurseObject nurseToView = nurseObjNameDict.get(nurseComboBox.getValue());
        int dayToView = findDayIndex(routeComboBox.getValue());
        String routeToView = nurseToView.getRoute(dayToView);
        if (routeToView == null || routeToView.equals("BREAK")) {
            errorLabel.setText("No shift and route scheduled for "+routeComboBox.getValue());
        } else {
            fillGridPane(routeToView, nurseToView.getOrderedShift(dayToView));
        }
    }

    public void fillGridPane(String route, String[] orderedShift) {
        String[][] routeLabelContents = assembleRouteLabelContents(route, orderedShift, dataStructures.getPatientObjIdDict(), dataStructures.getLocationIdDict());
        for (int rowIndex=0; rowIndex<4; rowIndex++) {
            for (int columnIndex=0; columnIndex<routeLabelContents[rowIndex].length; columnIndex++) {
                String textToAdd = routeLabelContents[rowIndex][columnIndex];
                routeLabels.get(rowIndex).get(columnIndex).setText(textToAdd);
            }
        }
    }



    //The methods below are borrowed code from https://stackoverflow.com/questions/12804664/how-to-swap-screens-in-a-javafx-application-in-the-controller-class
    public void setLoginScene(Scene scene) {loginScene=scene;}
    @FXML protected void openLoginScene() {stage.setScene(loginScene);}

    public void setAdminLandingScene(Scene scene) {adminLandingScene=scene;}
    @FXML protected void openAdminLandingScene() {stage.setScene(adminLandingScene);}
}
