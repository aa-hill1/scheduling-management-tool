package com.guiLayout.controllers.endUser;

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

import static com.algorithms.ViewRouteMethods.*;

public class EURoutesController {
    @FXML
    private Label currentAccount;
    @FXML
    private Button backButton;
    @FXML
    private Button logoutButton;
    @FXML
    private GridPane routeGrid;
    @FXML
    private AnchorPane anchorPane;
    @FXML
    private ComboBox<String> dayComboBox;
    @FXML
    private Label statusLabel;

    private Stage stage;
    private Scene loginScene;
    private Scene euLandingScene;

    private DataStructures dataStructures;
    private boolean onSceneOpen;
    ArrayList<ArrayList<Label>> routeLabels;

    public void initialize() {
        onSceneOpen = true;
    }

    @FXML protected void setUpScene() {
        if (onSceneOpen) {
            stage = (Stage) anchorPane.getScene().getWindow();
            dataStructures = (DataStructures) stage.getUserData();
            dayComboBox.getItems().addAll("Monday", "Tuesday", "Wednesday", "Thursday", "Friday");
            setUpGrid();
            onSceneOpen = false;
        }
        currentAccount.setText(dataStructures.getAccountLoggedIn().getUsername());
    }
    @FXML protected void displayDay() {
        statusLabel.setText("");
        clearGrid();
        int day = findDayIndex(dayComboBox.getValue());
        NurseObject nurseToDisplay = dataStructures.getNurseObjIdDict().get(dataStructures.getAccountLoggedIn().getAccountID());
        String routeToDisplay = nurseToDisplay.getRoute(day);
        if (routeToDisplay == null || routeToDisplay.equals("BREAK")) {
            statusLabel.setText("No shift and route scheduled for "+dayComboBox.getValue());
        } else {
            fillGridPane(routeToDisplay, nurseToDisplay.getOrderedShift(day));
        }
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
    public void setEULandingScene(Scene scene) {euLandingScene=scene;}
    public void openEULandingScene(ActionEvent actionEvent) {stage.setScene(euLandingScene);}

    public void setLoginScene(Scene scene) {loginScene=scene;}
    public void openLoginScene(ActionEvent actionEvent) {stage.setScene(loginScene);}
}
